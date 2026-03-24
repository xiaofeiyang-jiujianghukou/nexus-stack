package com.nexus.stack.brain.job.gmv;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.Properties;

@Slf4j
public class GmvFlinkJob {

    public static void run() throws Exception {

        StreamExecutionEnvironment env =
                StreamExecutionEnvironment.getExecutionEnvironment();

        // 不要只用 getExecutionEnvironment()
        // 改为连接到你的 Docker 映射端口 28081
        /*StreamExecutionEnvironment env = StreamExecutionEnvironment
                .createRemoteEnvironment("localhost", 28081, "target/brain-1.0-SNAPSHOT.jar");*/

        env.setParallelism(1);

        ObjectMapper mapper = new ObjectMapper();

        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers("localhost:29092")
                .setTopics("order-topic")
                .setGroupId("gmv-group")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        // 选择 forBoundedOutOfOrderness 是为了给数据一点“迟到”的机会
        // 3 秒的等待能显著提升 GMV 统计的准确率，防止因为网络抖动导致订单被算进错误的窗口里
        WatermarkStrategy<String> watermarkStrategy = WatermarkStrategy.<String>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                .withTimestampAssigner((json, timestamp) -> {
                    // 从 JSON 中提取业务时间戳（假设字段叫 createTime）
                    try {
                        return mapper.readTree(json).get("createTime").asLong();
                    } catch (Exception e) {
                        return System.currentTimeMillis();
                    }
                });

        DataStream<Double> result = env
                .fromSource(source, watermarkStrategy, "kafka-source")
                .map(json -> {
                    try {
                        JsonNode node = mapper.readTree(json);
                        // 加上非空判断，防止脏数据导致 NullPointerException
                        return node.has("amount") ? node.get("amount").asDouble() : 0.0;
                    } catch (Exception e) {
                        // 打印错误日志，但不让程序崩溃
                        log.error("JSON Parse Error: " + json);
                        return 0.0; // 忽略脏数据
                    }
                })
                .windowAll(TumblingProcessingTimeWindows.of(Time.seconds(10))) // 👉 测试用10秒
                .sum(0);

        // 👉 Sink 必须在 execute 前
        result.addSink(new RichSinkFunction<Double>() {

            private Connection conn;

            @Override
            public void open(Configuration parameters) throws Exception {

                Properties props = new Properties();
                props.setProperty("user", "default");
                props.setProperty("password", "123456"); // 👈 必须与 YAML 中的设置一致
                conn = DriverManager.getConnection(
                        "jdbc:clickhouse://localhost:28123/default",
                        props
                );
            }

            @Override
            public void invoke(Double value, Context context) throws Exception {
                // 使用 try-with-resources 自动关闭 PreparedStatement
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO ads_gmv_1m VALUES (?, ?)"
                )) {
                    ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                    ps.setDouble(2, value);
                    ps.execute();
                }
            }

            @Override
            public void close() throws Exception {
                // 👉 核心修改：在任务结束时优雅关闭数据库连接
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                    log.info("ClickHouse Connection Closed.");
                }
            }
        });

        result.print();

        env.execute("GMV Job");
    }
}