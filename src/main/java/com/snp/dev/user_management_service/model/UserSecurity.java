package com.snp.dev.user_management_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_security")
public class UserSecurity {

    @Id
    private String id;
    private String userId;
    private boolean mfaEnabled;
    private String mfaSecret;
    private LocalDateTime lastPasswordResetDate;
    private int failedLoginAttempts;
    private LocalDateTime lastFailedLoginAttempt;
    private boolean passwordResetRequired;
    private LocalDateTime accountLockedUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
