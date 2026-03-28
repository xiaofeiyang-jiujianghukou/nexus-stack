package com.nexus.stack.brain.manager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.stack.brain.job.cdc.rest.GmvSqlSubmitter;
import com.nexus.stack.brain.job.cdc.rest.OrderWideSqlSubmitter;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FlinkJobManager {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${flink.rest.url:http://172.17.0.1:38081}")
    private String flinkRestUrl;

    @Value("${flink.sql-gateway.url:http://172.17.0.1:38083}")
    private String sqlGatewayUrl;

    @Value("${flink.job.auto-start:true}")
    private boolean autoStart;

    // 存储正在运行的作业线程
    private final ConcurrentHashMap<String, Thread> runningJobThreads = new ConcurrentHashMap<>();

    // 作业配置 - 使用 Runnable
    private final List<JobConfig> jobs = List.of(
            new JobConfig("order-wide",
                    "insert-into_default_catalog.default_database.ck_wide_orders",
                    OrderWideSqlSubmitter::run),
            new JobConfig("gmv",
                    "insert-into_default_catalog.default_database.ads_gmv_1m",
                    GmvSqlSubmitter::run)
    );

    @PostConstruct
    public void init() {
        log.info("🚀 Flink Job Manager 初始化...");
        log.info("Flink REST API: {}", flinkRestUrl);
        log.info("Flink SQL Gateway: {}", sqlGatewayUrl);

        if (!autoStart) {
            log.info("自动启动作业已禁用");
            return;
        }

        Thread startupThread = new Thread(() -> {
            try {
                Thread.sleep(5000);

                if (waitForFlinkRestApi()) {
                    checkAndStartJobs();
                } else {
                    log.error("Flink REST API 不可用，跳过作业启动");
                }
            } catch (Exception e) {
                log.error("作业启动失败", e);
            }
        });
        startupThread.setDaemon(false);
        startupThread.start();
    }

    /**
     * 等待 Flink REST API 就绪
     */
    private boolean waitForFlinkRestApi() {
        int maxRetries = 30;
        log.info("等待 Flink REST API 就绪: {}", flinkRestUrl);

        for (int i = 0; i < maxRetries; i++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(flinkRestUrl + "/overview"))
                        .timeout(Duration.ofSeconds(5))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    log.info("✅ Flink REST API 就绪 (尝试 {} 次)", i + 1);
                    return true;
                }
            } catch (Exception e) {
                log.debug("Flink REST API 未就绪 (尝试 {}/{}): {}", i + 1, maxRetries, e.getMessage());
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        log.error("❌ Flink REST API 不可用: {}", flinkRestUrl);
        return false;
    }

    /**
     * 获取指定作业名称的所有实例（精确匹配作业名称）
     */
    private List<JobInstance> getAllJobInstances(String targetJobName) {
        List<JobInstance> instances = new ArrayList<>();

        try {
            // 1. 获取所有作业 ID
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(flinkRestUrl + "/jobs"))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("获取作业列表失败，状态码: {}", response.statusCode());
                return instances;
            }

            JsonNode root = mapper.readTree(response.body());

            if (root.has("jobs") && root.get("jobs").isArray()) {
                for (JsonNode jobSummary : root.get("jobs")) {
                    String jobId = jobSummary.get("id").asText();
                    String status = jobSummary.get("status").asText();

                    // 2. 通过 jobId 获取作业详情（包含完整名称）
                    String jobName = getJobNameById(jobId);

                    // 3. 精确匹配作业名称
                    if (jobName != null && jobName.equals(targetJobName)) {
                        instances.add(new JobInstance(jobId, status, jobName));
                        log.debug("找到匹配作业: ID={}, Name={}, Status={}", jobId, jobName, status);
                    }
                }
            }

            log.info("作业 '{}' 共有 {} 个实例", targetJobName, instances.size());
            for (JobInstance instance : instances) {
                log.info("  - ID: {}, 状态: {}", instance.getJobId(), instance.getStatus());
            }

        } catch (Exception e) {
            log.error("获取作业实例失败: {}", e.getMessage());
        }

        return instances;
    }

    /**
     * 通过作业 ID 获取作业名称
     */
    private String getJobNameById(String jobId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(flinkRestUrl + "/jobs/" + jobId))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = mapper.readTree(response.body());
                if (root.has("name")) {
                    return root.get("name").asText();
                }
            }
            return null;
        } catch (Exception e) {
            log.debug("获取作业名称失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 检查并处理作业状态（考虑多个同名实例）
     */
    private JobCheckResult checkAndHandleJobStatus(String jobName) {
        // 1. 获取所有同名作业实例
        List<JobInstance> instances = getAllJobInstances(jobName);

        if (instances.isEmpty()) {
            log.info("作业 '{}' 没有运行中的实例", jobName);
            return JobCheckResult.NOT_EXISTS;
        }

        // 2. 检查是否有正在运行的实例（RUNNING 或 CREATED）
        List<JobInstance> runningInstances = instances.stream()
                .filter(i -> "RUNNING".equals(i.getStatus()) || "CREATED".equals(i.getStatus()))
                .collect(Collectors.toList());

        if (!runningInstances.isEmpty()) {
            log.info("作业 '{}' 有 {} 个正在运行的实例，跳过启动", jobName, runningInstances.size());
            for (JobInstance instance : runningInstances) {
                log.info("  - 运行中实例: ID={}, 状态={}", instance.getJobId(), instance.getStatus());
            }
            return JobCheckResult.RUNNING;
        }

        // 3. 检查是否有重启中的实例（RESTARTING）
        List<JobInstance> restartingInstances = instances.stream()
                .filter(i -> "RESTARTING".equals(i.getStatus()))
                .collect(Collectors.toList());

        if (!restartingInstances.isEmpty()) {
            log.warn("作业 '{}' 有 {} 个 RESTARTING 状态的实例，准备取消...", jobName, restartingInstances.size());

            // 取消所有 RESTARTING 实例
            for (JobInstance instance : restartingInstances) {
                log.info("取消 RESTARTING 实例: ID={}", instance.getJobId());
                cancelJob(instance.getJobId());
            }

            return JobCheckResult.RESTARTING;
        }

        // 4. 其他状态（FINISHED, FAILED, CANCELED）
        log.info("作业 '{}' 所有实例都已终止（状态: {}），需要重新启动",
                jobName,
                instances.stream().map(JobInstance::getStatus).collect(Collectors.joining(", ")));
        return JobCheckResult.NOT_EXISTS;
    }

    /**
     * 取消作业
     */
    private void cancelJob(String jobId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(flinkRestUrl + "/jobs/" + jobId + "/cancel"))
                    .method("PATCH", HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 202) {
                log.info("✅ 作业取消请求已提交: {}", jobId);
            } else {
                log.warn("取消失败，状态码: {}", response.statusCode());
            }
        } catch (Exception e) {
            log.error("取消作业失败: {}", e.getMessage());
        }
    }

    /**
     * 启动一个作业 - 直接运行 Runnable
     */
    private void startJob(JobConfig job) {
        String jobName = job.getJobName();

        // 1. 检查本地线程
        Thread runningThread = runningJobThreads.get(jobName);
        if (runningThread != null && runningThread.isAlive()) {
            log.info("作业 '{}' 已在本地线程中运行，跳过启动", jobName);
            return;
        }

        // 2. 检查 Flink 集群中所有同名作业的状态
        JobCheckResult result = checkAndHandleJobStatus(jobName);

        switch (result) {
            case RUNNING:
                log.info("✅ 作业 '{}' 已在 Flink 集群运行中，跳过启动", jobName);
                return;
            case RESTARTING:
                log.info("作业 '{}' 的 RESTARTING 实例已取消，等待后重新提交...", jobName);
                // 等待取消完成
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                break;
            case NOT_EXISTS:
                log.info("作业 '{}' 未运行，准备启动...", jobName);
                break;
        }

        log.info("🚀 启动作业: {}", jobName);

        // 3. 在独立线程中运行 Runnable
        Thread jobThread = new Thread(() -> {
            try {
                log.info("作业 '{}' 开始执行...", jobName);
                job.getRunnable().run();
                log.info("作业 '{}' 执行完成", jobName);
            } catch (Exception e) {
                log.error("作业 '{}' 执行失败: {}", jobName, e.getMessage(), e);
            } finally {
                runningJobThreads.remove(jobName);
            }
        });

        jobThread.setName("flink-job-" + jobName);
        jobThread.setDaemon(false);
        runningJobThreads.put(jobName, jobThread);
        jobThread.start();

        log.info("✅ 作业 '{}' 已启动", jobName);
    }

    /**
     * 检查并启动所有未运行的作业
     */
    public void checkAndStartJobs() {
        log.info("🔍 开始检查作业状态...");

        for (JobConfig job : jobs) {
            try {
                startJob(job);
            } catch (Exception e) {
                log.error("启动作业 '{}' 失败: {}", job.getJobName(), e.getMessage(), e);
            }
        }
    }

    /**
     * 停止作业
     */
    public void stopJob(String jobName) {
        Thread jobThread = runningJobThreads.get(jobName);
        if (jobThread != null && jobThread.isAlive()) {
            log.info("停止作业: {}", jobName);
            jobThread.interrupt();
            try {
                jobThread.join(5000);
            } catch (InterruptedException e) {
                log.warn("等待作业停止超时: {}", jobName);
            }
            runningJobThreads.remove(jobName);
        } else {
            log.warn("作业 '{}' 未在运行", jobName);
        }
    }

    /**
     * 获取所有运行中的作业
     */
    public List<RunningJob> getRunningJobs() {
        List<RunningJob> runningJobs = new ArrayList<>();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(flinkRestUrl + "/jobs"))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 && response.body() != null) {
                JsonNode root = mapper.readTree(response.body());
                JsonNode jobs = root.get("jobs");

                if (jobs != null && jobs.isArray()) {
                    for (JsonNode job : jobs) {
                        runningJobs.add(new RunningJob(
                                job.get("id").asText(),
                                job.get("status").asText()
                        ));
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取运行中作业失败", e);
        }

        return runningJobs;
    }

    /**
     * 获取所有作业的状态
     */
    public List<JobStatus> getAllJobsStatus() {
        List<JobStatus> statuses = new ArrayList<>();

        for (JobConfig job : jobs) {
            boolean isRunning = isJobRunning(job.getJobName());
            statuses.add(new JobStatus(job.getJobName(), isRunning));
        }

        return statuses;
    }

    /**
     * 检查作业是否在运行（简化版）
     */
    public boolean isJobRunning(String jobName) {
        Thread runningThread = runningJobThreads.get(jobName);
        if (runningThread != null && runningThread.isAlive()) {
            return true;
        }

        List<JobInstance> instances = getAllJobInstances(jobName);
        return instances.stream()
                .anyMatch(i -> "RUNNING".equals(i.getStatus()) || "CREATED".equals(i.getStatus()));
    }

    // ========== 枚举和内部类 ==========

    private enum JobCheckResult {
        RUNNING,      // 有运行中的实例，跳过
        RESTARTING,   // 有重启中的实例，已取消
        NOT_EXISTS    // 没有实例，需要启动
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class JobInstance {
        private String jobId;
        private String status;
        private String jobName;  // 增加 jobName 用于调试
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class JobConfig {
        private String name;
        private String jobName;
        private Runnable runnable;

        public String getJobName() { return jobName; }
        public Runnable getRunnable() { return runnable; }
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class RunningJob {
        private String jobId;
        private String status;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class JobStatus {
        private String jobName;
        private boolean isRunning;
    }
}