//package com.snp.dev.user_management_service.service.impl;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.snp.dev.user_management_service.service.KafkaProducerService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
//import org.springframework.stereotype.Service;
//import reactor.core.publisher.Mono;
//
//import java.util.Map;
//
////@Service
//@RequiredArgsConstructor
//@Slf4j
//public class KafkaProducerServiceImpl implements KafkaProducerService {
//
//    private final ReactiveKafkaProducerTemplate<String, String> kafkaTemplate;
//    private final ObjectMapper objectMapper;
//
//    @Override
//    public Mono<Void> send(String topic, String key, String message) {
//        return kafkaTemplate.send(topic, key, message)
//                .doOnError(e -> log.error("Failed to send Kafka message", e))
//                .then();
//    }
//
//    @Override
//    public Mono<Void> sendUserEvent(String userId, String eventType, String payload) {
//        try {
//            String message = objectMapper.writeValueAsString(Map.of(
//                    "userId", userId,
//                    "eventType", eventType,
//                    "payload", payload,
//                    "timestamp", System.currentTimeMillis()
//            ));
//            return send("user-events", userId, message);
//        } catch (JsonProcessingException e) {
//            log.error("Failed to serialize user event", e);
//            return Mono.error(e);
//        }
//    }
//
//    @Override
//    public Mono<Void> sendEmailEvent(String email, String templateName, Map<String, Object> variables) {
//        try {
//            String message = objectMapper.writeValueAsString(Map.of(
//                    "email", email,
//                    "templateName", templateName,
//                    "variables", variables,
//                    "timestamp", System.currentTimeMillis()
//            ));
//            return send("email-events", email, message);
//        } catch (JsonProcessingException e) {
//            log.error("Failed to serialize email event", e);
//            return Mono.error(e);
//        }
//    }
//}
