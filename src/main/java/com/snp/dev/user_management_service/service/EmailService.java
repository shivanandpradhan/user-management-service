package com.snp.dev.user_management_service.service;

import reactor.core.publisher.Mono;

public interface EmailService {

    Mono<Void> sendEmailVerification(String email, String token);
    Mono<Void> sendPasswordResetEmail(String email, String token);
    Mono<Void> sendWelcomeEmail(String email);
    Mono<Void> sendAccountLockedEmail(String email);
    Mono<Void> sendAccountUnlockedEmail(String email);
    Mono<Void> sendPasswordChangedEmail(String email);
    Mono<Void> sendMfaEnabledEmail(String email);
    Mono<Void> sendMfaDisabledEmail(String email);
}

