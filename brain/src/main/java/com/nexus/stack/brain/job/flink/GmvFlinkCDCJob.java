package com.nexus.stack.brain.job.flink;

import com.nexus.stack.brain.loader.SqlScriptManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GmvFlinkCDCJob {

    @Autowired
    private SqlScriptManager sqlManager;

    public void run() throws Exception {
        // 1. 创建环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        // 强制关闭 upsert 物料化（防止和 ClickHouse 冲突）
        tableEnv.executeSql("SET 'table.exec.sink.upsert-materialize' = 'NONE';");

        log.info("🚀 GMV 统计任务启动...");

        // 2. 清理旧表
        tableEnv.executeSql("DROP TABLE IF EXISTS mysql_orders");
        tableEnv.executeSql("DROP TABLE IF EXISTS ads_gmv_1m");

        // 3. 注册表
        tableEnv.executeSql(sqlManager.load("flink/sql/cdc/mysql_orders.sql"));
        tableEnv.executeSql(sqlManager.load("flink/sql/sink/ads_gmv_1m.sql"));

        log.info("✅ 表注册完成，开始执行 GMV 统计...");

        // 4. 执行 INSERT（核心）
        TableResult result = tableEnv.executeSql(sqlManager.load("flink/sql/insert/gmv_1m.sql"));

        log.info("✅ GMV 统计任务已提交运行...");

        // 本地测试时保持运行
        result.await();
    }
}
