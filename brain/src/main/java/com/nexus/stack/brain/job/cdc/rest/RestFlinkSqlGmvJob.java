//package com.nexus.stack.brain.job.cdc.rest;
//
//import com.nexus.stack.brain.loader.SqlScriptManager;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
//import org.apache.flink.table.api.TableResult;
//import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.stereotype.Component;
//
//import javax.sql.DataSource;
//import java.sql.Connection;
//import java.sql.Statement;
//import java.time.Duration;
//
//@Slf4j
//@Component
//public class RestFlinkSqlGmvJob {
//
//    @Autowired
//    private SqlScriptManager sqlManager; // 注入工具类
//    @Autowired
//    @Qualifier("chDataSource")
//    private DataSource clickhouseDataSource;
//
//    public void run() {
//        // 核心：强制让 Flink 使用当前线程上下文加载器，否则它在反射加载 JDBC 驱动时会致盲
//        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
//        // 使用 JDK 21 的特性，环境创建非常轻量
//        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//        // 开启检查点是 CDC 的灵魂
//        env.getCheckpointConfig().setCheckpointTimeout(5000);
//        env.setParallelism(1);
//        //env.getCheckpointConfig().setCheckpointStorage("file:///app/checkpoints");
//
//        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
//
//        // 使用 Flink 2.2 推荐的语句集执行方式（如果是多步 INSERT）
//        // StatementSet statementSet = tableEnv.createStatementSet();
//
//        try {
//            tableEnv.executeSql(sqlManager.load("flink/sql/cdc/mysql/mysql_orders.sql"));
//            tableEnv.executeSql(sqlManager.load("flink/sql/sink/ads_gmv_1m.sql"));
//
//            log.info("✅ 表注册完成，开始执行 GMV 统计...");
//
//            // 4. 执行 INSERT（核心）
//            TableResult result = tableEnv.executeSql(sqlManager.load("flink/sql/insert/mysql/insert_gmv_1m.sql"));
//
//            log.info("✅ GMV 统计任务已提交运行...");
//
//            // 延时5s触发 ClickHouse 去重
//            executeOptimizeFinalWithDelay("default.ads_gmv_1m", 5);
//
//            // 让作业一直运行（本地测试强烈推荐加上）
//            result.await();
//        } catch (Exception e) {
//            log.error("Flink Job Runtime Error", e);
//        }
//    }
//
//    private void executeOptimizeFinalWithDelay(String tableName, long delaySeconds) {
//        Thread.ofVirtual().start(() -> {
//            try {
//                Thread.sleep(Duration.ofSeconds(delaySeconds));
//                try (Connection conn = clickhouseDataSource.getConnection();
//                     Statement stmt = conn.createStatement()) {
//                    // MySQL CDC 2.0+ 配合 ClickHouse 时，OPTIMIZE 其实开销很大
//                    // 建议确认 ClickHouse 表引擎是否为 ReplacingMergeTree
//                    stmt.execute("OPTIMIZE TABLE " + tableName + " FINAL");
//                    log.info("✅ ClickHouse 表 {} 已执行 OPTIMIZE FINAL", tableName);
//                }
//            } catch (Exception e) {
//                log.error("❌ 执行 OPTIMIZE FINAL 失败！", e);
//            }
//        });
//    }
//}
