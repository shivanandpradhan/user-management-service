package com.snp.dev.user_management_service.config;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MfaConfig {

    @Bean
    public SecretGenerator secretGenerator() {
        return new DefaultSecretGenerator();
    }

    @Bean
    public QrGenerator qrGenerator() {
        return new ZxingPngQrGenerator(); // Using ZxingQrGenerator implementation
    }

    @Bean
    public TimeProvider timeProvider() {
        // Use SystemTimeProvider as the default implementation
        return new SystemTimeProvider();
    }

    @Bean
    public CodeGenerator codeGenerator() {
        // Use DefaultCodeGenerator with SHA1 algorithm
        return new DefaultCodeGenerator(HashingAlgorithm.SHA1);
    }

    @Bean
    public CodeVerifier codeVerifier(CodeGenerator codeGenerator, TimeProvider timeProvider) {
        // Inject CodeGenerator and TimeProvider into DefaultCodeVerifier
        return new DefaultCodeVerifier(codeGenerator, timeProvider);
    }



}
