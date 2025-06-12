package com.snp.dev.user_management_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private boolean mfaEnabled;
    private String mfaType;
    private String userId;
    private String username;
    private String email;
}
