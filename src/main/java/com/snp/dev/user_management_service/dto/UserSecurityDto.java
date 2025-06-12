package com.snp.dev.user_management_service.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserSecurityDto {
    private boolean mfaEnabled;
    private String lastPasswordResetDate;
    private int failedLoginAttempts;
    private String accountLockedUntil;
    private List<SessionDto> activeSessions;
}
