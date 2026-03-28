package com.nexus.stack.brain.job.cdc.rest;

import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.util.EntityUtils;
import org.jspecify.annotations.NonNull;

public class OrderWideSqlSubmitter {

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

            // 4. 创建 users 表
            executeSql(sessionHandle, getUserSql(), "创建 users 表");
            System.out.println("✅ users 表创建完成");

            // 5. 创建 orders 表（CDC）
            executeSql(sessionHandle, getOrderSql(), "创建 orders 表（CDC）");
            System.out.println("✅ orders 表创建完成");

            // 6. 创建 members 表
            executeSql(sessionHandle, getMemberSql(), "创建 members 表");
            System.out.println("✅ members 表创建完成");

            // 7. 创建 sink 表
            executeSql(sessionHandle, getSinkSql(), "创建 sink 表");
            System.out.println("✅ sink 表创建完成");

            // 8. 提交作业
            String operationHandle = executeSql(sessionHandle, getInsertSql(), "作业");
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

    /**
     * 测试创建会话
     */
    public static void testCreateSession() {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(SQL_GATEWAY_URL + "/v1/sessions");
            post.setHeader("Content-Type", "application/json");

            try (CloseableHttpResponse response = client.execute(post)) {
                String result = EntityUtils.toString(response.getEntity());
                System.out.println("创建会话响应: " + result);

                JsonNode json = mapper.readTree(result);
                if (json.has("sessionHandle")) {
                    String sessionHandle = json.get("sessionHandle").asText();
                    System.out.println("✅ 会话创建成功: " + sessionHandle);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试提交 SQL
     */
    public static void testSubmitSql2() {
        String userSql = getUserSql();
        String orderSql = getOrderSql();
        String memberSql = getMemberSql();
        String sinkSql = getSinkSql();
        String insertSql = getInsertSql();

        excuteSql("userSql", userSql);
        excuteSql("orderSql", orderSql);
        excuteSql("memberSql", memberSql);
        excuteSql("sinkSql", sinkSql);
        excuteSql("insertSql", insertSql);
    }

    private static void excuteSql(String sqlName, String sql) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            // 1. 创建会话
            HttpPost createSession = new HttpPost(SQL_GATEWAY_URL + "/v1/sessions");
            createSession.setHeader("Content-Type", "application/json");

            try (CloseableHttpResponse sessionResp = client.execute(createSession)) {
                String sessionResult = EntityUtils.toString(sessionResp.getEntity());
                JsonNode sessionJson = mapper.readTree(sessionResult);
                String sessionHandle = sessionJson.get("sessionHandle").asText();
                System.out.println("会话: " + sessionHandle);

                // 2. 执行 SQL
                HttpPost executeStmt = new HttpPost(
                        SQL_GATEWAY_URL + "/v1/sessions/" + sessionHandle + "/statements");
                executeStmt.setHeader("Content-Type", "application/json");

                // 转义 SQL 中的特殊字符
                String escapedSql = sql.replace("\"", "\\\"").replace("\n", "\\n");
                String requestBody = "{\"statement\": \"" + escapedSql + "\"}";
                executeStmt.setEntity(new StringEntity(requestBody));

                try (CloseableHttpResponse stmtResp = client.execute(executeStmt)) {
                    String stmtResult = EntityUtils.toString(stmtResp.getEntity());
                    System.out.println(sqlName + " SQL 执行结果: " + stmtResult);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static @NonNull String getInsertSql() {
        return """
                INSERT INTO ck_wide_orders
                SELECT
                    o.order_id                                   AS order_id,
                    o.user_id                                    AS user_id,
                    o.amount,
                    COALESCE(u.level, 'NORMAL')                  AS level,
                    COALESCE(m.is_member, 0)                     AS is_member,
                    o.ts                                         AS ts
                
                FROM mysql_orders o
                         LEFT JOIN mysql_users AS u
                                   ON o.user_id = u.user_id
                         LEFT JOIN mysql_members AS m
                                   ON o.user_id = m.user_id;""";
    }
    private static @NonNull String getSinkSql() {
        return """
                CREATE TABLE IF NOT EXISTS ck_wide_orders (
                    order_id  BIGINT,
                    user_id   BIGINT,
                    amount    DECIMAL(10, 2),
                    `level`   STRING,
                    is_member INT,
                    ts        BIGINT,
                    PRIMARY KEY (order_id) NOT ENFORCED
                ) WITH (
                    'connector' = 'clickhouse',
                    'url' = 'jdbc:clickhouse://172.17.0.1:38123',
                    'username' = 'default',
                    'password' = '123456',
                    'database-name' = 'default',
                    'table-name' = 'dwd_order_wide',
                    'sink.batch-size' = '1000',           -- 每批次写入行数
                    'sink.flush-interval' = '2000',       -- 刷新间隔（毫秒）
                    'sink.max-retries' = '3'
                );""";
    }
    private static @NonNull String getMemberSql() {
        return """
                CREATE TABLE IF NOT EXISTS mysql_members (
                    user_id     BIGINT,
                    is_member   INT,
                    PRIMARY KEY (user_id) NOT ENFORCED
                ) WITH (
                    'connector' = 'jdbc',
                    'url' = 'jdbc:mysql://172.17.0.1:33306/brain_db?useSSL=false&connectionTimeZone=UTC&allowPublicKeyRetrieval=true',
                    'username' = 'root',
                    'password' = '123456',
                    'driver' = 'com.mysql.cj.jdbc.Driver',
                    'table-name' = 'members',
                    'lookup.cache' = 'PARTIAL',
                    'lookup.partial-cache.max-rows' = '20000',
                    'lookup.partial-cache.expire-after-access' = '5s',
                    'lookup.max-retries' = '3'
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
                    'server-id' = '5501-5510',
                    'connect.timeout' = '30s',
                    'connect.max-retries' = '5',
                    'debezium.snapshot.fetch.size' = '1000',
                    'debezium.snapshot.locking.mode' = 'none',
                    'debezium.heartbeat.interval.ms' = '5000'
                  );""";
    }
    private static @NonNull String getUserSql() {
        return """
                CREATE TABLE IF NOT EXISTS mysql_users (
                    user_id        BIGINT,
                    level         STRING,
                    register_time  BIGINT,
                    PRIMARY KEY (user_id) NOT ENFORCED
                ) WITH (
                    'connector' = 'jdbc',
                    'url' = 'jdbc:mysql://172.17.0.1:33306/brain_db?useSSL=false&connectionTimeZone=UTC&allowPublicKeyRetrieval=true',
                    'username' = 'root',
                    'password' = '123456',
                    'driver' = 'com.mysql.cj.jdbc.Driver',
                    'table-name' = 'users',
                    'lookup.cache' = 'PARTIAL',
                    'lookup.partial-cache.max-rows' = '20000',
                    'lookup.partial-cache.expire-after-access' = '5s',
                    'lookup.max-retries' = '3'
                );""";
    }

}
