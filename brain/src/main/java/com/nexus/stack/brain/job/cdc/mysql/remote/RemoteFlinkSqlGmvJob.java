//package com.nexus.stack.brain.job.cdc.mysql.remote;
//
//import com.nexus.stack.brain.loader.SqlScriptManager;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.flink.configuration.Configuration;
//import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
//import org.apache.flink.table.api.EnvironmentSettings;
//import org.apache.flink.table.api.TableEnvironment;
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
//public class RemoteFlinkSqlGmvJob {
//
//    @Autowired
//    private SqlScriptManager sqlManager; // 注入工具类
//    @Autowired
//    @Qualifier("chDataSource")
//    private DataSource clickhouseDataSource;
//
//    public void run() {
//        EnvironmentSettings settings = EnvironmentSettings.newInstance()
//                .inStreamingMode()
//                .build();
//
//        TableEnvironment tableEnv = TableEnvironment.create(settings);
//
//        tableEnv.getConfig().getConfiguration().setString("execution.target", "remote");
//        tableEnv.getConfig().getConfiguration().setString("parallelism.default", "1");
//        tableEnv.getConfig().getConfiguration().setString("execution.job-manager.address", "flink-jobmanager");
//        tableEnv.getConfig().getConfiguration().setInteger("execution.job-manager.port", 6123);
//
//        try {
//            // 2. 清理旧表
////            tableEnv.executeSql("DROP TABLE IF EXISTS mysql_orders");
////            tableEnv.executeSql("DROP TABLE IF EXISTS ads_gmv_1m");
//            log.info("✅ 已清理旧表");
//
//            // 3. 注册表
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
//
//            log.info("✅ GMV 统计任务 await...");
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
