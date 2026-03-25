package com.nexus.stack.brain.config;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class StreamExecutionEnvironmentConfig {

    @Bean
    public StreamExecutionEnvironment streamExecutionEnvironment() {
        Configuration configuration = new Configuration();
        // 在你的 FlinkCoreJob 中执行 INSERT 之前添加：

        // 1. 治本配置：即使 Sink 没主键，也强制处理 Upsert 逻辑
        configuration.setString("table.exec.sink.upsert-materialize", "NONE");

        // 2. 辅助配置：确保数据在内存中按主键进行 Shuffle，保证顺序正确
        configuration.setString("table.exec.sink.keyed-shuffle", "FORCE");

        // 3. 状态保留：维表数据在内存里存多久（根据你会员/用户更新的频率定，比如 24 小时）
        configuration.setString("table.exec.state.ttl", "24h");
        // 允许 Flink 在写入前在内存中先合并一部分回撤消息，减少发给 CK 的压力
        configuration.setString("table.exec.sink.upsert-materialize", "FORCE");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);
        env.setParallelism(1);

        env.disableOperatorChaining(); //
        return env;
    }
}
