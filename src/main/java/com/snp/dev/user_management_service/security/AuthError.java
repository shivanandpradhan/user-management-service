package com.snp.dev.user_management_service.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
class AuthError {
    private final String error;
    private final String message;
}
