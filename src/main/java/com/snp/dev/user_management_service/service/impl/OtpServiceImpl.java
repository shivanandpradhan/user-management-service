//package com.snp.dev.user_management_service.service.impl;
//
//import com.snp.dev.user_management_service.service.OtpService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.core.ReactiveRedisTemplate;
//import org.springframework.stereotype.Service;
//import reactor.core.publisher.Mono;
//
//import java.time.Duration;
//import java.util.concurrent.ThreadLocalRandom;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class OtpServiceImpl implements OtpService {
//
//    private final ReactiveRedisTemplate<String, String> redisTemplate;
//    private static final Duration OTP_EXPIRY = Duration.ofMinutes(5);
//
//    @Override
//    public Mono<String> generateOtp(String key) {
//        String otp = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
//        return redisTemplate.opsForValue().set(key, otp, OTP_EXPIRY)
//                .thenReturn(otp);
//    }
//
//    @Override
//    public Mono<Boolean> validateOtp(String key, String otp) {
//        return redisTemplate.opsForValue().get(key)
//                .flatMap(storedOtp -> Mono.just(otp.equals(storedOtp)))
//                .defaultIfEmpty(false);
//    }
//
//    @Override
//    public Mono<Void> clearOtp(String key) {
//        return redisTemplate.delete(key).then();
//    }
//}
//
