package com.snp.dev.user_management_service.service.impl;

import com.snp.dev.user_management_service.service.OtpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class InMemoryOtpServiceImpl implements OtpService {

    private final Map<String, String> otpStore = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    @Override
    public Mono<String> generateOtp(String key) {
        // Generate OTP
        String otp = generateOtpCode();
        otpStore.put(key, otp);
        log.info("Generated OTP :- key - value :-'{}  {}'. OTP is stored in-memory.", key, otp);

        // Schedule OTP expiration
        executor.schedule(() -> {
            otpStore.remove(key);
            log.info("OTP for key '{}' has expired and has been removed from in-memory store.", key);
        }, 5, TimeUnit.MINUTES);

        log.debug("OTP expiration task scheduled for key '{}' after 5 minutes.", key);

        return Mono.just(otp);
    }

    @Override
    public Mono<Boolean> validateOtp(String key, String otp) {
        log.info("Validating OTP for key '{}'.", key);

        String storedOtp = otpStore.get(key);
        if (storedOtp == null) {
            log.warn("OTP validation failed for key '{}'. No OTP found for this key.", key);
            return Mono.just(false);
        }

        boolean isValid = otp.equals(storedOtp);

        if (isValid) {
            log.info("OTP validation successful for key '{}'.", key);
        } else {
            log.warn("OTP validation failed for key '{}'. Provided OTP '{}' does not match the stored OTP.", key, otp);
        }

        return Mono.just(isValid);
    }

    @Override
    public Mono<Void> clearOtp(String key) {
        log.info("Clearing OTP for key '{}'.", key);

        return Mono.fromRunnable(() -> {
            if (otpStore.remove(key) != null) {
                log.info("Successfully cleared OTP for key '{}'.", key);
            } else {
                log.warn("No OTP was found to clear for key '{}'.", key);
            }
        });
    }

    private String generateOtpCode() {
        // Generate a 6-digit random number
        String otp = String.valueOf((int) (Math.random() * 900000 + 100000));
        log.debug("Generated OTP: {}", otp);

        return otp;
    }
}