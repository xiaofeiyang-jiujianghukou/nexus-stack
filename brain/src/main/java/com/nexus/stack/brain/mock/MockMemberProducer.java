package com.nexus.stack.brain.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Slf4j
@Component
public class MockMemberProducer {

    @Resource
    private KafkaProducer<String, String> producer;

    private final ObjectMapper mapper = new ObjectMapper();
    private final Random random = new Random();

    // 定义用户池大小，必须与 OrderProducer 一致
    public static final int USER_POOL_SIZE = 1000;

    //@Scheduled(fixedRate = 2000) // 每2秒模拟一个用户的等级变化或新用户
    public void sendUser() {
        try {
            // 联动核心：生成 1-1000 之间的 ID
            int userId = random.nextInt(USER_POOL_SIZE) + 1;
            //String level = random.nextDouble() < 0.2 ? "VIP" : "NORMAL";
            boolean isMember = random.nextInt(10) < 3;

            Map<String, Object> user = new HashMap<>();
            user.put("userId", userId);
            user.put("isMember", isMember);
            user.put("ts", System.currentTimeMillis());

            String json = mapper.writeValueAsString(user);

            log.info("MockMemberProducer send: " + json);
            // 👉 发送时指定 Key 为 userId，确保 Kafka Compact 生效
            producer.send(new ProducerRecord<>("member-topic", String.valueOf(userId), json));

            log.info("👤 [会员维度更新] ID: {}, 是否会员: {}", userId, isMember);
        } catch (Exception e) {
            log.error("会员发送失败", e);
        }
    }
}
