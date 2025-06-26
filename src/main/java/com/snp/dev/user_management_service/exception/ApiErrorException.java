package com.snp.dev.user_management_service.exception;

import com.snp.dev.user_management_service.dto.ApiResponse;

public class ApiErrorException extends RuntimeException {
    private final ApiResponse<?> response;

    public ApiErrorException(ApiResponse<?> response) {
        super("API Error");
        this.response = response;
    }

    public ApiResponse<?> getResponse() {
        return response;
    }
}
