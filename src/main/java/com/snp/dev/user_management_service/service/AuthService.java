package com.snp.dev.user_management_service.service;


import com.snp.dev.user_management_service.dto.*;
import com.snp.dev.user_management_service.dto.request.*;
import com.snp.dev.user_management_service.dto.response.AuthResponse;
import com.snp.dev.user_management_service.dto.response.MfaSetupResponse;
import com.snp.dev.user_management_service.dto.response.TokenRefreshResponse;
import reactor.core.publisher.Mono;

public interface AuthService {

    Mono<ApiResponse<AuthResponse>> signUp(SignUpRequest signUpRequest);
    Mono<ApiResponse<AuthResponse>> login(LoginRequest loginRequest);
    Mono<ApiResponse<AuthResponse>> verifyOtp(VerifyOtpRequest verifyOtpRequest, String userId);
    Mono<ApiResponse<Void>> forgotPassword(ForgotPasswordRequest forgotPasswordRequest);
    Mono<ApiResponse<Void>> resetPassword(ResetPasswordRequest resetPasswordRequest);
    Mono<ApiResponse<Void>> changePassword(ChangePasswordRequest changePasswordRequest, String userId);
    Mono<ApiResponse<TokenRefreshResponse>> refreshToken(TokenRefreshRequest tokenRefreshRequest);
    Mono<ApiResponse<MfaSetupResponse>> setupMfa(MfaSetupRequest mfaSetupRequest, String userId);
    Mono<ApiResponse<AuthResponse>> verifyMfa(MfaVerifyRequest mfaVerifyRequest, String userId);
    Mono<ApiResponse<Void>> disableMfa(String userId);
}