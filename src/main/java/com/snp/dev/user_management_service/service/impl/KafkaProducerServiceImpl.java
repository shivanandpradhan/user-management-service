//package com.snp.dev.user_management_service.service.impl;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.snp.dev.user_management_service.service.KafkaProducerService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
////import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
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

package com.snp.dev.user_management_service.service.impl;

import com.snp.dev.user_management_service.dto.EmailMessage;
import com.snp.dev.user_management_service.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

import java.util.Map;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private final ReactiveKafkaProducerTemplate<String, EmailMessage> kafkaProducer;

    @Value("${app.kafka.topics.email}")
    private String emailTopic;

    @Override
    public Mono<SenderResult<Void>> sendEmailMessage(EmailMessage message) {
        String messageKey = UUID.randomUUID().toString();

        return kafkaProducer.send(emailTopic, messageKey, message)
                .doOnSuccess(result -> log.info("Sent email message to Kafka. Topic: {}, Key: {}", emailTopic, messageKey))
                .doOnError(e -> log.error("Failed to send email message to Kafka", e));
    }

    // Convenience methods for different email types
    @Override
    public Mono<SenderResult<Void>> sendWelcomeEmail(String recipient, String username) {
        EmailMessage message = EmailMessage.builder()
                .to(recipient)
                .subject("Welcome to Our Service")
                .template("welcome-email")
                .variables(Map.of(
                        "username", username,
                        "supportEmail", "support@example.com"
                ))
                .build();

        return sendEmailMessage(message);
    }

    @Override
    public Mono<SenderResult<Void>> sendPasswordResetEmail(String recipient, String resetToken) {
        EmailMessage message = EmailMessage.builder()
                .to(recipient)
                .subject("Password Reset Request")
                .template("password-reset")
                .variables(Map.of(
                        "resetLink", "https://yourapp.com/reset?token=" + resetToken,
                        "expiryHours", 24
                ))
                .build();

        return sendEmailMessage(message);
    }

    @Override
    public Mono<SenderResult<Void>> sendOtpEmail(String recipient, String otp) {
        EmailMessage message = EmailMessage.builder()
                .to(recipient)
                .subject("Your Verification Code")
                .template("otp-verification")
                .variables(Map.of(
                        "otp", otp,
                        "validMinutes", 5
                ))
                .build();

        return sendEmailMessage(message);
    }

//    @Override
//    public Mono<Void> send(String topic, String key, String message) {
//        return null;
//    }
//
//    @Override
//    public Mono<Void> sendUserEvent(String userId, String eventType, String payload) {
//        return null;
//    }
//
//    @Override
//    public Mono<Void> sendEmailEvent(String email, String templateName, Map<String, Object> variables) {
//        return null;
//    }
}
