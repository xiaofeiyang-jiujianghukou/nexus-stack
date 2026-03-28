//package com.nexus.stack.brain.service;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.nexus.stack.brain.pojo.Member;
//import com.nexus.stack.brain.pojo.User;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.kafka.clients.producer.KafkaProducer;
//import org.apache.kafka.clients.producer.ProducerRecord;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Service;
//
//import java.util.Properties;
//
//@Slf4j
//@Service
//public class DimensionSyncService {
//
//    @Autowired
//    private JdbcTemplate jdbcTemplate; // Spring 自动处理数据源连接
//
//    @Value("${kafka.bootstrap-servers}")
//    private String kafkaServers;
//
//    private final ObjectMapper mapper = new ObjectMapper();
//
//    public void syncUserDimensions() {
//        log.info("🔄 [DimensionSync] 正在从数据库回填用户维度到 Kafka...");
//
//        Properties props = new Properties();
//        props.put("bootstrap.servers", kafkaServers);
//        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//
//        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
//            // 使用 JdbcTemplate 简化 SQL 执行
//            jdbcTemplate.query("SELECT user_id, level, register_time FROM users", rs -> {
//                try {
//                    User user = new User();
//                    user.setUserId(rs.getLong("user_id"));
//                    user.setLevel(rs.getString("level"));
//                    user.setRegisterTime(rs.getTimestamp("register_time").getTime());
//                    user.setTs(System.currentTimeMillis());
//
//                    String json = mapper.writeValueAsString(user);
//                    // 必须带上 Key (userId)，触发 Kafka 的 Compact 策略
//                    ProducerRecord<String, String> record = new ProducerRecord<>(
//                            "user-topic",
//                            String.valueOf(user.getUserId()),
//                            json
//                    );
//                    producer.send(record);
//                } catch (Exception e) {
//                    log.error("❌ 同步单条用户数据失败: " + e.getMessage());
//                }
//            });
//            // 使用 JdbcTemplate 简化 SQL 执行
//            jdbcTemplate.query("SELECT user_id, is_member FROM members", rs -> {
//                try {
//                    Member user = new Member();
//                    user.setUserId(rs.getLong("user_id"));
//                    user.setMember(rs.getInt("is_member") == 1);
//                    user.setTs(System.currentTimeMillis());
//
//                    String json = mapper.writeValueAsString(user);
//                    log.info("DB会员 json: " + json);
//                    // 必须带上 Key (userId)，触发 Kafka 的 Compact 策略
//                    ProducerRecord<String, String> record = new ProducerRecord<>(
//                            "member-topic",
//                            String.valueOf(user.getUserId()),
//                            json
//                    );
//                    producer.send(record);
//                } catch (Exception e) {
//                    log.error("❌ 同步单条会员数据失败: " + e.getMessage());
//                }
//            });
//            producer.flush();
//            log.info("✅ [DimensionSync] 维度同步完成。");
//        } catch (Exception e) {
//            log.error("❌ [DimensionSync] Kafka 连接失败: " + e.getMessage());
//        }
//    }
//}
