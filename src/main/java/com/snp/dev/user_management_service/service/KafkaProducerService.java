package com.snp.dev.user_management_service.service;

import com.snp.dev.user_management_service.dto.EmailMessage;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

import java.util.Map;

public interface KafkaProducerService {
    Mono<SenderResult<Void>> sendEmailMessage(EmailMessage message);

    // Convenience methods for different email types
    Mono<SenderResult<Void>> sendWelcomeEmail(String recipient, String username);

    Mono<SenderResult<Void>> sendPasswordResetEmail(String recipient, String resetToken);

    Mono<SenderResult<Void>> sendOtpEmail(String recipient, String otp);

//    Mono<Void> send(String topic, String key, String message);
//
//    Mono<Void> sendUserEvent(String userId, String eventType, String payload);
//
//    Mono<Void> sendEmailEvent(String email, String templateName, Map<String, Object> variables);
}