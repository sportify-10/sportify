//package com.sparta.sportify.config;
//
//import org.apache.kafka.common.serialization.StringSerializer;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.annotation.EnableKafka;
//import org.springframework.kafka.core.DefaultKafkaProducerFactory;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.kafka.core.ProducerFactory;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
//import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
//import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;
//
//@EnableKafka
//@Configuration
//public class KafkaProducerConfig {
//    @Bean
//    public ProducerFactory<String, String> producerFactory() {
//        Map<String, Object> properties = new HashMap<>();
//        properties.put(BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");  // Kafka 주소
//        properties.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//        properties.put(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//
//        return new DefaultKafkaProducerFactory<>(properties);
//    }
//
//    @Bean
//    public KafkaTemplate<String, String> kafkaTemplate() {
//        return new KafkaTemplate<>(producerFactory());
//    }
//}