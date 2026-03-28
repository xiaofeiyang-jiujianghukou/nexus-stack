package com.nexus.stack.brain.job.cdc.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jspecify.annotations.NonNull;

import java.util.Map;

public class GmvSqlSubmitter {

    private static final String SQL_GATEWAY_URL = "http://172.17.0.1:38083";
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 创建会话
     */
    public static String createSession() throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(SQL_GATEWAY_URL + "/v1/sessions");
            post.setHeader("Content-Type", "application/json");

            try (CloseableHttpResponse response = client.execute(post)) {
                String result = EntityUtils.toString(response.getEntity());
                JsonNode json = mapper.readTree(result);
                return json.get("sessionHandle").asText();
            }
        }
    }

    /**
     * 执行单条 SQL 语句
     */
    public static String executeSql(String sessionHandle, String sql, String sqlName) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost executeStmt = new HttpPost(
                    SQL_GATEWAY_URL + "/v1/sessions/" + sessionHandle + "/statements");
            executeStmt.setHeader("Content-Type", "application/json");

            // 转义 SQL 中的特殊字符
            String escapedSql = sql.replace("\"", "\\\"").replace("\n", "\\n");
            String requestBody = "{\"statement\": \"" + escapedSql + "\"}";
            executeStmt.setEntity(new StringEntity(requestBody));

            try (CloseableHttpResponse stmtResp = client.execute(executeStmt)) {
                String stmtResult = EntityUtils.toString(stmtResp.getEntity());
                JsonNode json = mapper.readTree(stmtResult);
                if (json.has("operationHandle")) {
                    return json.get("operationHandle").asText();
                } else if (json.has("errors")) {
                    throw new RuntimeException(sqlName + " SQL执行失败: " + stmtResult);
                }
                return stmtResult;
            }
        }
    }


    public static void main(String[] args) {
        run();
    }

    public static void run() {
        try {
            // 1. 创建会话（只创建一次）
            String sessionHandle = createSession();
            System.out.println("✅ 会话创建成功: " + sessionHandle);

            // 2. 设置流模式
            executeSql(sessionHandle, "SET 'execution.runtime-mode' = 'streaming'", "设置流模式");
            System.out.println("✅ 流模式设置完成");

            // 3. 设置 checkpoint
            executeSql(sessionHandle, "SET 'execution.checkpointing.interval' = '3s'", "设置 checkpoint");
            System.out.println("✅ Checkpoint 设置完成");

            // 在 main 方法中，设置 checkpoint 后添加
            executeSql(sessionHandle, "SET 'parallelism.default' = '1'", "设置 并行度");

            // 4. 创建 orders 表
            executeSql(sessionHandle, getOrderSql(), "创建 orders 表");
            System.out.println("✅ orders 表创建完成");

            // 7. 创建 sink 表
            executeSql(sessionHandle, getSinkSql(), "创建 sink 表");
            System.out.println("✅ sink 表创建完成");

            // 8. 提交作业
            String operationHandle = executeSql(sessionHandle, getInsertSql(), "创建 Gmv作业");
            System.out.println("✅ 作业提交成功: " + operationHandle);

            // 9. 保持程序运行，监控作业
            monitorJob(sessionHandle, operationHandle);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 监控作业状态
     */
    private static void monitorJob(String sessionHandle, String operationHandle) {
        // 这里可以添加轮询逻辑
        System.out.println("作业正在运行中...");
        // 实际监控逻辑可以参考之前的代码
    }

    private static @NonNull String getInsertSql() {
        return """
                INSERT INTO ads_gmv_1m
                SELECT
                    TUMBLE_START(row_time, INTERVAL '10' SECOND) AS stat_time,
                    ROUND(SUM(amount), 2)                        AS gmv
                FROM mysql_orders
                GROUP BY TUMBLE(row_time, INTERVAL '10' SECOND);""";
    }
    private static @NonNull String getSinkSql() {
        return """
                CREATE TABLE IF NOT EXISTS ads_gmv_1m (
                    stat_time   TIMESTAMP_LTZ(3),
                    gmv         DECIMAL(10, 2)
                ) WITH (
                    'connector' = 'clickhouse',
                    'url' = 'jdbc:clickhouse://172.17.0.1:38123/default',
                    'username' = 'default',
                    'password' = '123456',
                    'database-name' = 'default',
                    'table-name' = 'ads_gmv_1m',
                    'sink.batch-size' = '1000',           -- 每批次写入行数
                    'sink.flush-interval' = '2000',       -- 刷新间隔（毫秒）
                    'sink.max-retries' = '3'
                );""";
    }

    private static @NonNull String getOrderSql() {
        return """
                CREATE TABLE IF NOT EXISTS mysql_orders (
                      order_id    BIGINT,
                      user_id     BIGINT,
                      amount      DECIMAL(10, 2),
                      ts          BIGINT,
                      row_time AS TO_TIMESTAMP_LTZ(ts, 3),
                      WATERMARK FOR row_time AS row_time - INTERVAL '5' SECOND,
                      PRIMARY KEY (order_id) NOT ENFORCED
                  ) WITH (
                      'connector' = 'mysql-cdc',
                      'hostname' = '172.17.0.1',
                      'port' = '33306',
                      'username' = 'root',
                      'password' = '123456',
                      'database-name' = 'brain_db',
                      'table-name' = 'orders',
                      'server-time-zone' = 'UTC',
                      'scan.startup.mode' = 'initial',
                      'scan.incremental.snapshot.enabled' = 'true',
                        'server-id' = '5401-5410',
                        'connect.timeout' = '30s',
                        'connect.max-retries' = '5',
                        'debezium.snapshot.fetch.size' = '1000',
                        'debezium.snapshot.locking.mode' = 'none',
                        'debezium.heartbeat.interval.ms' = '5000'
                  );""";
    }

}
