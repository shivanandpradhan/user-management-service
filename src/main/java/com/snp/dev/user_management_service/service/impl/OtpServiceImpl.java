package com.snp.dev.user_management_service.service.impl;

import com.snp.dev.user_management_service.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final Scheduler scheduler = Schedulers.boundedElastic();

    @Value("${app.otp.length}")
    private int otpLength;

    @Value("${app.otp.expiry-minutes}")
    private long expiryMinutes;

    @Override
    public Mono<String> generateOtp(String key) {
        String otp = generateSecureOtp();
        return redisTemplate.opsForValue()
                .set(key, otp, Duration.ofMinutes(expiryMinutes))
                .thenReturn(otp)
                .doOnSuccess(o -> log.info("Generated OTP for {}: {}", key, o))
                .publishOn(scheduler);
    }

    @Override
    public Mono<Boolean> validateOtp(String key, String otp) {
        return redisTemplate.opsForValue().get(key)
                .flatMap(storedOtp -> {
                    if (storedOtp != null && storedOtp.equals(otp)) {
                        return redisTemplate.delete(key)
                                .thenReturn(true)
                                .doOnSuccess(b -> log.info("Valid OTP for {}", key));
                    }
                    return Mono.just(false)
                            .doOnSuccess(b -> log.warn("Invalid OTP attempt for {}", key));
                })
                .defaultIfEmpty(false)
                .publishOn(scheduler);
    }

    @Override
    public Mono<Void> clearOtp(String key) {
        return null;
    }

    private String generateSecureOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(otpLength);
        for (int i = 0; i < otpLength; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
