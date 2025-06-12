package com.snp.dev.user_management_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private Meta meta;
    private T data;
    private List<ErrorDetail> errors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta {
        private Instant timestamp;
        private String version;
        private String path;
        private String method;
        private String requestId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDetail {
        private String code;
        private String message;
        private String field;
        private String detail;
    }

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .meta(Meta.builder().timestamp(Instant.now()).build())
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(List<ErrorDetail> errors) {
        return ApiResponse.<T>builder()
                .meta(Meta.builder().timestamp(Instant.now()).build())
                .errors(errors)
                .build();
    }

    public static <T> ApiResponse<T> error(List<ErrorDetail> errors, T data) {
        return new ApiResponse<>(null, data, errors);
    }

}

