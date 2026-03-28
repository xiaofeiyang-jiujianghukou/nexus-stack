//package com.nexus.stack.brain.job.cdc.mysql.client;
//
//import com.nexus.stack.brain.loader.SqlScriptManager;
//import com.nexus.stack.brain.util.JsonUtils;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import javax.sql.DataSource;
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//
//@Slf4j
////@Component
//public class FlinkSqlGmvClientJob {
//
//    @Autowired
//    private SqlScriptManager sqlManager; // 注入工具类
//    @Autowired
//    @Qualifier("chDataSource")
//    private DataSource clickhouseDataSource;
//
//    @Value("${flink.sql-gateway.address}")
//    private String sqlGatewayAddress; // SQL Gateway REST 地址，例如 http://127.0.0.1:8083
//
//    public void run() {
//        try {
//            HttpClient client = HttpClient.newHttpClient();
//
//            // 1️⃣ 创建 Session
//            String sessionBody = """
//            {
//              "sessionName": "my-session",
//              "properties": {
//                "parallelism.default": "4"
//              }
//            }
//            """;
//
//            HttpRequest sessionRequest = HttpRequest.newBuilder()
//                    .uri(URI.create(sqlGatewayAddress + "/sessions"))
//                    .header("Content-Type", "application/json")
//                    .POST(HttpRequest.BodyPublishers.ofString(sessionBody))
//                    .build();
//
//            HttpResponse<String> sessionResponse = client.send(sessionRequest, HttpResponse.BodyHandlers.ofString());
//            log.info("Session Response Raw: {}", sessionResponse.body());
//            String sessionId = JsonUtils.getStringField("sessionHandle", sessionResponse.body());
//            log.info("✅ SQL Gateway Session 创建成功, sessionId={}", sessionId);
//
//            // 2️⃣ 注册表
//            executeStatement(client, sessionId, sqlManager.load("flink/sql/cdc/mysql/mysql_orders.sql"));
//            executeStatement(client, sessionId, sqlManager.load("flink/sql/sink/ads_gmv_1m.sql"));
//            log.info("✅ 表注册完成");
//
//            // 3️⃣ 提交 GMV INSERT SQL
//            String insertSql = sqlManager.load("flink/sql/insert/mysql/insert_gmv_1m.sql");
//            String statementId = executeStatement(client, sessionId, insertSql);
//            log.info("✅ SQL 已提交, statementId={}", statementId);
//            log.info("✅ GMV 统计 SQL 已提交, statementId={}", statementId);
//
//            // 4️⃣ 延迟执行 ClickHouse 去重
//            executeOptimizeFinalWithDelay("default.ads_gmv_1m", 5);
//
//            // 5️⃣ 可选：轮询作业状态
//            pollStatementStatus(client, sessionId, statementId);
//
//        } catch (Exception e) {
//            log.error("❌ Flink SQL Gateway 作业启动失败！", e);
//        }
//    }
//
//    private String executeStatement(HttpClient client, String sessionId, String sql) throws Exception {
//        String body = "{\"statement\":\"" + escapeJson(sql) + "\"}";
//        HttpRequest stmtRequest = HttpRequest.newBuilder()
//                .uri(URI.create(sqlGatewayAddress + "/sessions/" + sessionId + "/statements"))
//                .header("Content-Type", "application/json")
//                .POST(HttpRequest.BodyPublishers.ofString(body))
//                .build();
//
//        HttpResponse<String> stmtResp = client.send(stmtRequest, HttpResponse.BodyHandlers.ofString());
//        log.info(" SQL 已提交 Body: {}", stmtResp.body());
//        String statementId = JsonUtils.getStringField("operationHandle", stmtResp.body());
//        log.info("✅ SQL 已提交, statementId={}", statementId);
//        return statementId;
//    }
//
//    private void pollStatementStatus(HttpClient client, String sessionId, String statementId) throws Exception {
//        while (true) {
//            // 1. 先查状态
//            HttpRequest statusRequest = HttpRequest.newBuilder()
//                    .uri(URI.create(sqlGatewayAddress + "/sessions/" + sessionId + "/operations/" + statementId + "/status"))
//                    .GET()
//                    .build();
//
//            HttpResponse<String> statusResp = client.send(statusRequest, HttpResponse.BodyHandlers.ofString());
//            String status = JsonUtils.getStringField("status", statusResp.body());
//            log.info("SQL 状态: {}", status);
//
//            if ("ERROR".equalsIgnoreCase(status) || "FAILED".equalsIgnoreCase(status)) {
//                // 2. 如果报错，尝试获取错误堆栈 (关键步骤)
//                fetchErrorDetails(client, sessionId, statementId);
//                break;
//            }
//
//            if ("RUNNING".equalsIgnoreCase(status) || "FINISHED".equalsIgnoreCase(status)) {
//                log.info("✅ 任务已成功启动/运行");
//            }
//
//            TimeUnit.SECONDS.sleep(2);
//        }
//    }
//
//    private void fetchErrorDetails(HttpClient client, String sessionId, String statementId) throws Exception {
//        // 尝试请求结果接口，通常这里会包含具体的 Exception 信息
//        HttpRequest errorRequest = HttpRequest.newBuilder()
//                .uri(URI.create(sqlGatewayAddress + "/sessions/" + sessionId + "/operations/" + statementId + "/result/0"))
//                .GET()
//                .build();
//        HttpResponse<String> errorResp = client.send(errorRequest, HttpResponse.BodyHandlers.ofString());
//        log.error("❌ Flink 任务执行详情: {}", errorResp.body());
//    }
//
//    private void executeOptimizeFinalWithDelay(String tableName, long delaySeconds) {
//        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
//        scheduler.schedule(() -> {
//            try (Connection conn = clickhouseDataSource.getConnection();
//                 Statement stmt = conn.createStatement()) {
//                stmt.execute("OPTIMIZE TABLE " + tableName + " FINAL");
//                log.info("✅ ClickHouse 表 {} 已执行 OPTIMIZE FINAL", tableName);
//            } catch (SQLException e) {
//                log.error("❌ 执行 OPTIMIZE FINAL 失败！", e);
//            }
//        }, delaySeconds, TimeUnit.SECONDS);
//    }
//
//    private String escapeJson(String s) {
//        return s.replace("\"", "\\\"").replace("\n", " ");
//    }
//}