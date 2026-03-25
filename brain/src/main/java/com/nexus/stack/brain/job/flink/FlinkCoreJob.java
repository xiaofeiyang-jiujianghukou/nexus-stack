package com.nexus.stack.brain.job.flink;

import com.nexus.stack.brain.loader.SqlScriptManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FlinkCoreJob {

    @Autowired
    private SqlScriptManager sqlManager; // 注入工具类

    public void run() throws Exception {
        Configuration conf = new Configuration();

        // 强制关闭 upsert 物料化
        conf.setString("table.exec.sink.upsert-materialize", "NONE");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf);
        env.setParallelism(1);
        env.disableOperatorChaining();

        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        log.info("🚀 开始创建 Flink 本地环境，Web UI: http://localhost:8081");

        try {
            // 清理表
            tableEnv.executeSql("DROP TABLE IF EXISTS ck_wide_orders");
            tableEnv.executeSql("DROP TABLE IF EXISTS kafka_orders");   // 注意改成 kafka_orders
            tableEnv.executeSql("DROP TABLE IF EXISTS mysql_users");
            tableEnv.executeSql("DROP TABLE IF EXISTS mysql_members");
            log.info("✅ 已清理旧表");

            // 注册表
            log.info("正在注册 kafka_orders...");
            tableEnv.executeSql(sqlManager.load("flink/sql/cdc/kafka/kafka_orders.sql"));

            log.info("正在注册 mysql_users...");
            tableEnv.executeSql(sqlManager.load("flink/sql/cdc/mysql/mysql_users.sql"));

            log.info("正在注册 mysql_members...");
            tableEnv.executeSql(sqlManager.load("flink/sql/cdc/mysql/mysql_members.sql"));

            log.info("正在注册 Sink ck_wide_orders...");
            tableEnv.executeSql(sqlManager.load("flink/sql/sink/ck_wide_orders.sql"));

            log.info("🔥 所有表注册完成，开始执行 INSERT...");

            // 执行 INSERT 并捕获结果
            TableResult result = tableEnv.executeSql(
                    sqlManager.load("flink/sql/insert/kafka/insert_ck_wide_orders.sql")
            );

            log.info("✅ INSERT 语句已提交，作业开始运行...");

            // 让作业一直运行（本地测试强烈推荐加上）
            result.await();

        } catch (Exception e) {
            log.error("❌ Flink 作业启动失败！", e);
            throw e;
        }
    }
}