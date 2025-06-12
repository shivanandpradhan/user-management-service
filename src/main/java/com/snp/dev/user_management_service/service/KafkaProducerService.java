package com.snp.dev.user_management_service.service;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface KafkaProducerService {

    Mono<Void> send(String topic, String key, String message);

    Mono<Void> sendUserEvent(String userId, String eventType, String payload);

    Mono<Void> sendEmailEvent(String email, String templateName, Map<String, Object> variables);
}
