//package com.nexus.stack.brain.mock;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.annotation.Resource;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.kafka.clients.producer.KafkaProducer;
//import org.apache.kafka.clients.producer.ProducerRecord;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Random;
//
//@Slf4j
//@Component
//public class MockOrderProducer {
//
//    @Resource
//    private KafkaProducer<String, String> producer;
//
//    private final ObjectMapper mapper = new ObjectMapper();
//    private final Random random = new Random();
//
//    //@Scheduled(fixedRate = 3000) // 每1秒生成一个订单
//    public void sendOrder() {
//        try {
//            // 联动核心：订单的 userId 必须从 1-1000 中随机选取
//            // 这样它才能命中 Flink 内存中通过 user-topic 回放进去的维度数据
//            int userId = random.nextInt(MockUserProducer.USER_POOL_SIZE) + 1;
//
//            Map<String, Object> order = new HashMap<>();
//            order.put("orderId", System.currentTimeMillis());
//            order.put("userId", userId);
//            order.put("amount", random.nextDouble() * 1000);
//            order.put("ts", System.currentTimeMillis());
//
//            String json = mapper.writeValueAsString(order);
//
//            producer.send(new ProducerRecord<>("order-topic", json));
//
//            log.info("💰 [订单生成] 用户ID: {}, 金额: {}", userId, String.format("%.2f", (double)order.get("amount")));
//        } catch (Exception e) {
//            log.error("订单发送失败", e);
//        }
//    }
//}
