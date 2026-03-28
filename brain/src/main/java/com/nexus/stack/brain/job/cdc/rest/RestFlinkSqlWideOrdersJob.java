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
//public class RestFlinkSqlWideOrdersJob {
//
//    @Autowired
//    private SqlScriptManager sqlManager; // 注入工具类
//    @Autowired
//    @Qualifier("chDataSource")
//    private DataSource clickhouseDataSource;
//
//    //@Override
//    public void run() {
//        // 核心：强制让 Flink 使用当前线程上下文加载器，否则它在反射加载 JDBC 驱动时会致盲
//        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
//        try (StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment()) {
//            StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
//
//            // 关键配置：JDK 21 环境下，显式设置状态后端和 Checkpoint
//            // Flink 2.2 对 Checkpoint 存储有更简洁的 API
//            env.enableCheckpointing(5000);
//
//            // 注册表
//            log.info("正在注册 mysql_orders...");
//            tableEnv.executeSql(sqlManager.load("flink/sql/cdc/mysql/mysql_orders.sql"));
//
//            log.info("正在注册 mysql_users...");
//            tableEnv.executeSql(sqlManager.load("flink/sql/cdc/mysql/mysql_users.sql"));
//
//            log.info("正在注册 mysql_members...");
//            tableEnv.executeSql(sqlManager.load("flink/sql/cdc/mysql/mysql_members.sql"));
//
//            log.info("正在注册 Sink ck_wide_orders...");
//            tableEnv.executeSql(sqlManager.load("flink/sql/sink/ck_wide_orders.sql"));
//
//            log.info("🔥 所有表注册完成，开始执行 INSERT...");
//
//            // 执行 INSERT 并捕获结果
//            TableResult result = tableEnv.executeSql(
//                    sqlManager.load("flink/sql/insert/mysql/insert_ck_wide_orders.sql")
//            );
//
//            log.info("✅ 订单宽表作业 已提交运行...");
//
//            // 延时5s触发 ClickHouse 去重
//            executeOptimizeFinalWithDelay("default.dwd_order_wide", 5);
//
//            // Flink 2.2 推荐使用这个方法等待作业完成，它能更好地处理取消信号
//            result.await();
//        } catch (Exception e) {
//            log.error("❌ Flink 作业启动失败！", e);
//        }
//
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