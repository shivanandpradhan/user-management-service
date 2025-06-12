package com.snp.dev.user_management_service.service.impl;

import com.snp.dev.user_management_service.service.MfaService;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

@Service
@Slf4j
public class MfaServiceImpl implements MfaService {

    private final SecretGenerator secretGenerator;
    private final QrGenerator qrGenerator;
    private final CodeVerifier codeVerifier;

    // Constructor for dependency injection
    public MfaServiceImpl(SecretGenerator secretGenerator, QrGenerator qrGenerator, CodeVerifier codeVerifier) {
        this.secretGenerator = secretGenerator;
        this.qrGenerator = qrGenerator;
        this.codeVerifier = codeVerifier;
    }


    @Override
    public Mono<String> generateSecret() {
        return Mono.fromCallable(secretGenerator::generate);
    }

    @Override
    public Mono<String> generateQrCode(String secret, String username) {
        return Mono.fromCallable(() -> {
            QrData data = new QrData.Builder()
                    .label(username)
                    .secret(secret)
                    .issuer("User Management Service")
                    .algorithm(HashingAlgorithm.SHA1)
                    .digits(6)
                    .period(30)
                    .build();

            byte[] imageData = qrGenerator.generate(data);
            return getDataUriForImage(imageData, qrGenerator.getImageMimeType());
        }).onErrorResume(e -> {
            log.error("Error generating QR code", e);
            return Mono.error(new RuntimeException("Failed to generate QR code"));
        });
    }

    @Override
    public Mono<Boolean> verifyCode(String secret, String code) {
        return Mono.fromCallable(() -> codeVerifier.isValidCode(secret, code));
    }
}
