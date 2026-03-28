//package com.nexus.stack.brain.config.kafka;
//
//import org.apache.kafka.clients.producer.KafkaProducer;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.Properties;
//
//@Configuration
//public class KafkaProducerConfig {
//
//    @Value("${kafka.bootstrap-servers}")
//    private String bootstrapServers;
//    @Bean
//    public KafkaProducer<String, String> kafkaProducer() {
//        Properties props = new Properties();
//        props.put("bootstrap.servers", bootstrapServers);
//        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//        return new KafkaProducer<>(props);
//    }
//}