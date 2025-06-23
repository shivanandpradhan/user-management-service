package com.snp.dev.user_management_service.service.impl;

import com.snp.dev.user_management_service.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final KafkaProducerServiceImpl kafkaProducerService;
    private final JavaMailSender mailSender;

    @Value("${app.kafka.email.enabled:false}")
    private boolean kafkaEmailEnabled;

    @Override
    public Mono<Void> sendWelcomeEmail(String email, String username) {
        return kafkaEmailEnabled ? kafkaProducerService.sendWelcomeEmail(email, username)
                .doOnSuccess(__ -> log.info("Welcome email queued for {}", email))
                .doOnError(e -> log.error("Failed to queue welcome email for {}", email, e))
                .then() : Mono.empty();
    }

    @Override
    public Mono<Void> sendPasswordResetEmail(String email, String resetToken) {
        return kafkaEmailEnabled ? kafkaProducerService.sendPasswordResetEmail(email, resetToken)
                .doOnSuccess(__ -> log.info("Password reset email queued for {}", email))
                .doOnError(e -> log.error("Failed to queue password reset email for {}", email, e))
                .then() : Mono.empty();
    }

    @Override
    public Mono<Void> sendOtpEmail(String email, String otp) {
        return kafkaEmailEnabled ? kafkaProducerService.sendOtpEmail(email, otp)
                .doOnSuccess(__ -> log.info("OTP email queued for {}", email))
                .doOnError(e -> log.error("Failed to queue OTP email for {}", email, e))
                .then() : Mono.empty();
    }

    @Override
    public Mono<Void> sendEmailVerification(String email, String token) {
        return sendDirectEmail(
                email,
                "Email Verification",
                "Hi Testing email",
                "email-verification"
        );
    }

    // Generic method for direct email sending
    private Mono<Void> sendDirectEmail(String to, String subject, String text, String templateName) {
        return Mono.fromCallable(() -> {
                    MimeMessage message = mailSender.createMimeMessage();
                    MimeMessageHelper helper = new MimeMessageHelper(message, true);
                    helper.setTo(to);
                    helper.setSubject(subject);
                    helper.setText(text, true);
                    return message;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(mailSender::send)
                .doOnSuccess(__ -> log.info("Email sent to {} with subject {}", to, subject))
                .doOnError(e -> log.error("Failed to send email to {}", to, e))
                .then();
    }

//    // Additional Kafka-based email methods
//    public Mono<Void> sendTemplatedEmail(String email, String templateName, Map<String, Object> variables) {
//        return kafkaProducerService.sendTemplatedEmail(email, templateName, variables)
//                .doOnSuccess(__ -> log.info("Templated email '{}' queued for {}", templateName, email))
//                .doOnError(e -> log.error("Failed to queue templated email '{}' for {}", templateName, email, e))
//                .then();
//    }
}