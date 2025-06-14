package com.snp.dev.user_management_service.controller;

import com.snp.dev.user_management_service.dto.*;
import com.snp.dev.user_management_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public Mono<ResponseEntity<ApiResponse<AuthResponse>>> signUp(@RequestBody SignUpRequest signUpRequest) {
        return authService.signUp(signUpRequest)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<ApiResponse<AuthResponse>>> login(@RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/verify-otp")
    public Mono<ResponseEntity<ApiResponse<AuthResponse>>> verifyOtp(@RequestBody VerifyOtpRequest verifyOtpRequest) {
        return authService.verifyOtp(verifyOtpRequest)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/forgot-password")
    public Mono<ResponseEntity<ApiResponse<Void>>> forgotPassword(@RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        return authService.forgotPassword(forgotPasswordRequest)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/reset-password")
    public Mono<ResponseEntity<ApiResponse<Void>>> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        return authService.resetPassword(resetPasswordRequest)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/change-password")
    public Mono<ResponseEntity<ApiResponse<Void>>> changePassword(
            @RequestBody ChangePasswordRequest changePasswordRequest,
            @RequestHeader("X-User-Id") String userId) {
        return authService.changePassword(changePasswordRequest, userId)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/refresh-token")
    public Mono<ResponseEntity<ApiResponse<TokenRefreshResponse>>> refreshToken(@RequestBody TokenRefreshRequest tokenRefreshRequest) {
        return authService.refreshToken(tokenRefreshRequest)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/mfa/setup")
    public Mono<ResponseEntity<ApiResponse<MfaSetupResponse>>> setupMfa(
            @RequestBody MfaSetupRequest mfaSetupRequest,
            @RequestHeader("X-User-Id") String userId) {
        return authService.setupMfa(mfaSetupRequest, userId)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/mfa/verify")
    public Mono<ResponseEntity<ApiResponse<AuthResponse>>> verifyMfa(
            @RequestBody MfaVerifyRequest mfaVerifyRequest,
            @RequestHeader("X-User-Id") String userId) {
        return authService.verifyMfa(mfaVerifyRequest, userId)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/mfa/disable")
    public Mono<ResponseEntity<ApiResponse<Void>>> disableMfa(@RequestHeader("X-User-Id") String userId) {
        return authService.disableMfa(userId)
                .map(ResponseEntity::ok);
    }
}