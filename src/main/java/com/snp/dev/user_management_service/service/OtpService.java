package com.snp.dev.user_management_service.service;

import reactor.core.publisher.Mono;

public interface OtpService {

    Mono<String> generateOtp(String key);

    Mono<Boolean> generateOtpAndSendEmail(String email);

    Mono<Boolean> validateOtp(String key, String otp);

    Mono<Void> clearOtp(String key);
}
