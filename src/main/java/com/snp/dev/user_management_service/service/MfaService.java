package com.snp.dev.user_management_service.service;

import reactor.core.publisher.Mono;

public interface MfaService {

    Mono<String> generateSecret();
    Mono<String> generateQrCode(String secret, String username);
    Mono<Boolean> verifyCode(String secret, String code);
}