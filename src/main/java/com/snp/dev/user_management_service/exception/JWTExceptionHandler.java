package com.snp.dev.user_management_service.exception;

import com.snp.dev.user_management_service.dto.ApiResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import java.util.Collections;

@RequiredArgsConstructor
@Slf4j
public class JWTExceptionHandler{

    public static Pair<HttpStatus, ApiResponse<Void>> handle(Throwable ex) {
        log.error("GlobalWebExceptionHandler caught: {}", ex.getClass().getSimpleName(), ex);
        ApiResponse<Void> response;
        HttpStatus status;

        // Handle JWT exceptions
        if (ex instanceof ExpiredJwtException) {
            log.error("JWT token expired: {}", ex.getMessage());
            status = HttpStatus.UNAUTHORIZED;
            response = ApiResponse.error(
                    Collections.singletonList(
                            new ApiResponse.ErrorDetail(
                                    "TOKEN_EXPIRED",
                                    "JWT token has expired. Please login again.",
                                    null,
                                    null
                            )
                    )
            );
        }
        else if (ex instanceof MalformedJwtException) {
            log.error("Malformed JWT token: {}", ex.getMessage());
            status = HttpStatus.UNAUTHORIZED;
            response = ApiResponse.error(
                    Collections.singletonList(
                            new ApiResponse.ErrorDetail(
                                    "MALFORMED_TOKEN",
                                    "Invalid JWT token format",
                                    null,
                                    null
                            )
                    )
            );
        }
        else if (ex instanceof SignatureException) {
            log.error("Invalid JWT signature: {}", ex.getMessage());
            status = HttpStatus.UNAUTHORIZED;
            response = ApiResponse.error(
                    Collections.singletonList(
                            new ApiResponse.ErrorDetail(
                                    "INVALID_SIGNATURE",
                                    "Invalid JWT signature",
                                    null,
                                    null
                            )
                    )
            );
        }
        else if (ex instanceof BadCredentialsException) {
            log.error("Bad credentials: {}", ex.getMessage());
            status = HttpStatus.UNAUTHORIZED;
            response = ApiResponse.error(
                    Collections.singletonList(
                            new ApiResponse.ErrorDetail(
                                    "INVALID_CREDENTIALS",
                                    ex.getMessage() != null ? ex.getMessage() : "Invalid credentials",
                                    null,
                                    null
                            )
                    )
            );
        }
        else if (ex instanceof AuthenticationException) {
            log.error("Authentication error: {}", ex.getMessage());
            status = HttpStatus.UNAUTHORIZED;
            response = ApiResponse.error(
                    Collections.singletonList(
                            new ApiResponse.ErrorDetail(
                                    "UNAUTHORIZED",
                                    ex.getMessage() != null ? ex.getMessage() : "Authentication failed",
                                    null,
                                    null
                            )
                    )
            );
        }
        else if (ex instanceof IllegalArgumentException) {
            log.error("Illegal argument: {}", ex.getMessage());
            status = HttpStatus.BAD_REQUEST;
            response = ApiResponse.error(
                    Collections.singletonList(
                            new ApiResponse.ErrorDetail(
                                    "BAD_REQUEST",
                                    ex.getMessage() != null ? ex.getMessage() : "Invalid request",
                                    null,
                                    null
                            )
                    )
            );
        }
        else {
            //  Fallback for unknown exceptions
            log.error("Unexpected error: {}", ex.getMessage(), ex);
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            response = ApiResponse.error(
                    Collections.singletonList(
                            new ApiResponse.ErrorDetail(
                                    "INTERNAL_ERROR",
                                    "An unexpected error occurred. Please try again later.",
                                    null,
                                    null
                            )
                    )
            );
        }
        return Pair.of(status, response);
    }
}