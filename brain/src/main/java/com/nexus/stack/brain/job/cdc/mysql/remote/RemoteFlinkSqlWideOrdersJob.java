//package com.nexus.stack.brain.job.cdc.mysql.remote;
//
//import com.nexus.stack.brain.loader.SqlScriptManager;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.flink.configuration.Configuration;
//import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
//import org.apache.flink.table.api.TableResult;
//import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.stereotype.Component;
//
//import javax.sql.DataSource;
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//
//@Slf4j
//@Component
//public class RemoteFlinkSqlWideOrdersJob {
//
//    @Autowired
//    private SqlScriptManager sqlManager; // 注入工具类
//    @Autowired
//    @Qualifier("chDataSource")
//    private DataSource clickhouseDataSource;
//
//    //@Override
//    public void run() {
//        Configuration conf = new Configuration();
//
//        // 强制关闭 upsert 物料化
//        conf.setString("rest.bind-port", "8081");
//        conf.setString("table.exec.sink.upsert-materialize", "NONE");
//        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf);
//        env.setParallelism(1);
//        env.disableOperatorChaining();
//
//        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
//        try {
//            // 清理表
////            tableEnv.executeSql("DROP TABLE IF EXISTS ck_wide_orders");
////            tableEnv.executeSql("DROP TABLE IF EXISTS mysql_orders");   // 注意改成 kafka_orders
////            tableEnv.executeSql("DROP TABLE IF EXISTS mysql_users");
////            tableEnv.executeSql("DROP TABLE IF EXISTS mysql_members");
////            log.info("✅ 已清理旧表");
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
//            // 让作业一直运行（本地测试强烈推荐加上）
//            result.await();
//
//            log.info("✅ 订单宽表作业 await...");
//
//        } catch (Exception e) {
//            log.error("❌ Flink 作业启动失败！", e);
//        }
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
//}