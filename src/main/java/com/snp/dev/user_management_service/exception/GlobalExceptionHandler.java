package com.snp.dev.user_management_service.exception;

import com.snp.dev.user_management_service.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleValidationExceptions(WebExchangeBindException ex) {
        List<ApiResponse.ErrorDetail> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ApiResponse.ErrorDetail(
                        "VALIDATION_ERROR",
                        error.getDefaultMessage(),
                        error.getField(),
                        error.getRejectedValue() != null ? error.getRejectedValue().toString() : null
                ))
                .collect(Collectors.toList());

        return Mono.just(ResponseEntity
                .badRequest()
                .body(ApiResponse.error(errors)));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return Mono.just(ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(Collections.singletonList(
                        new ApiResponse.ErrorDetail("NOT_FOUND", ex.getMessage(), null, null)
                ))));
    }

    @ExceptionHandler(BadRequestException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleBadRequestException(BadRequestException ex) {
        return Mono.just(ResponseEntity
                .badRequest()
                .body(ApiResponse.error(Collections.singletonList(
                        new ApiResponse.ErrorDetail("BAD_REQUEST", ex.getMessage(), null, null)
                ))));
    }

    @ExceptionHandler(ForbiddenException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleForbiddenException(ForbiddenException ex) {
        return Mono.just(ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(Collections.singletonList(
                        new ApiResponse.ErrorDetail("FORBIDDEN", ex.getMessage(), null, null)
                ))));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleUnauthorizedException(UnauthorizedException ex) {
        return Mono.just(ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(Collections.singletonList(
                        new ApiResponse.ErrorDetail("UNAUTHORIZED", ex.getMessage(), null, null)
                ))));
    }

    @ExceptionHandler(AccountLockedException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleAccountLockedException(AccountLockedException ex) {
        return Mono.just(ResponseEntity
                .status(HttpStatus.LOCKED)
                .body(ApiResponse.error(Collections.singletonList(
                        new ApiResponse.ErrorDetail("ACCOUNT_LOCKED", ex.getMessage(), null, null)
                ))));
    }

    @ExceptionHandler(AccountDisabledException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleAccountDisabledException(AccountDisabledException ex) {
        return Mono.just(ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(Collections.singletonList(
                        new ApiResponse.ErrorDetail("ACCOUNT_DISABLED", ex.getMessage(), null, null)
                ))));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleAccessDeniedException(AccessDeniedException ex) {
        return Mono.just(ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(Collections.singletonList(
                        new ApiResponse.ErrorDetail("ACCESS_DENIED", ex.getMessage(), null, null)
                ))));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleGenericException(Exception ex) {
        return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(Collections.singletonList(
                        new ApiResponse.ErrorDetail("INTERNAL_ERROR", "An unexpected error occurred", null, null)
                ))));
    }
}