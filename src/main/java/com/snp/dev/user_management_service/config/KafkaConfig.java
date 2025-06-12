//package com.snp.dev.user_management_service.config;
//
//import org.apache.kafka.clients.producer.ProducerConfig;
//import org.apache.kafka.common.serialization.StringSerializer;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.annotation.EnableKafka;
//import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
//import reactor.kafka.sender.SenderOptions;
//
//import java.util.HashMap;
//import java.util.Map;
//
////@Configuration
////@EnableKafka
//public class KafkaConfig {
//
//    @Value("${spring.kafka.bootstrap-servers}")
//    private String bootstrapServers;
//
//    @Bean
//    public ReactiveKafkaProducerTemplate<String, String> reactiveKafkaProducerTemplate() {
//        Map<String, Object> props = new HashMap<>();
//        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "com.snp.userservice.serialization.EmailMessageSerializer");
//        props.put(ProducerConfig.ACKS_CONFIG, "all");
//        props.put(ProducerConfig.RETRIES_CONFIG, 3);
//        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 30000);
//        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 15000);
//
//        // Create SenderOptions from props
//        SenderOptions<String, String> senderOptions = SenderOptions.create(props);
//
//        // Return ReactiveKafkaProducerTemplate
//        return new ReactiveKafkaProducerTemplate<>(senderOptions);
//    }
//}