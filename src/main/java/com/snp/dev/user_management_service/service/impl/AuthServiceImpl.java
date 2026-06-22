package com.snp.dev.user_management_service.service.impl;

import com.snp.dev.user_management_service.dto.ApiResponse;
import com.snp.dev.user_management_service.dto.request.*;
import com.snp.dev.user_management_service.dto.response.AuthResponse;
import com.snp.dev.user_management_service.dto.response.MfaSetupResponse;
import com.snp.dev.user_management_service.dto.response.TokenRefreshResponse;
import com.snp.dev.user_management_service.exception.*;
import com.snp.dev.user_management_service.model.User;
import com.snp.dev.user_management_service.model.UserMetadata;
import com.snp.dev.user_management_service.model.UserProfile;
import com.snp.dev.user_management_service.model.UserSecurity;
import com.snp.dev.user_management_service.repository.*;
import com.snp.dev.user_management_service.security.JwtTokenProvider;
import com.snp.dev.user_management_service.service.AuditService;
import com.snp.dev.user_management_service.service.AuthService;
import com.snp.dev.user_management_service.service.MfaService;
import com.snp.dev.user_management_service.service.OtpService;
import com.snp.dev.user_management_service.util.ExceptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserSecurityRepository userSecurityRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserMetadataRepository userMetadataRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final EmailServiceImpl emailService;
    private final OtpService otpService;
    private final MfaService mfaService;
    private final AuditService auditService;
//    private final KafkaProducerService kafkaProducerService;

    @Value("${app.audit-logs.enabled:false}")
    private boolean auditLogsEnabled;

    @Override
    public Mono<ApiResponse<AuthResponse>> signUp(SignUpRequest signUpRequest) {
        return Mono.zip(
                        userRepository.existsByUsername(signUpRequest.getUsername()),
                        userRepository.existsByEmail(signUpRequest.getEmail())
                )
                .flatMap(tuple -> {
                    boolean usernameExists = tuple.getT1();
                    boolean emailExists = tuple.getT2();

                    if (usernameExists) {
                        return Mono.error(new BadRequestException("Username is already taken"));
                    }

                    if (emailExists) {
                        return Mono.error(new BadRequestException("Email is already in use"));
                    }

                    return roleRepository.findByName("ROLE_USER")
                            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Default role not found")))
                            .flatMap(role -> {
                                User user = User.builder()
                                        .username(signUpRequest.getUsername())
                                        .email(signUpRequest.getEmail())
                                        .password(passwordEncoder.encode(signUpRequest.getPassword()))
                                        .enabled(true)
                                        .accountNonExpired(true)
                                        .accountNonLocked(true)
                                        .credentialsNonExpired(true)
                                        .roles(new HashSet<>(Collections.singletonList(role.getName())))
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build();

                                return userRepository.save(user)
                                        .flatMap(savedUser -> {
                                            UserSecurity userSecurity = UserSecurity.builder()
                                                    .userId(savedUser.getId())
                                                    .mfaEnabled(false)
                                                    .lastPasswordResetDate(LocalDateTime.now())
                                                    .failedLoginAttempts(0)
                                                    .passwordResetRequired(false)
                                                    .createdAt(LocalDateTime.now())
                                                    .updatedAt(LocalDateTime.now())
                                                    .build();

                                            UserProfile userProfile = UserProfile.builder()
                                                    .userId(savedUser.getId())
                                                    .createdAt(LocalDateTime.now())
                                                    .updatedAt(LocalDateTime.now())
                                                    .build();

                                            UserMetadata userMetadata = UserMetadata.builder()
                                                    .userId(savedUser.getId())
                                                    .createdAt(LocalDateTime.now())
                                                    .updatedAt(LocalDateTime.now())
                                                    .build();

                                            return Mono.zip(
                                                    userSecurityRepository.save(userSecurity),
                                                    userProfileRepository.save(userProfile),
                                                    userMetadataRepository.save(userMetadata)
                                            ).thenReturn(savedUser);
                                        });
                            })
                            .flatMap(savedUser -> {

                                Mono<String> accessTokenMono = tokenProvider.generateAccessToken(savedUser.getUsername(), savedUser.getRoles());
                                Mono<String> refreshTokenMono = tokenProvider.generateRefreshToken(savedUser.getUsername(), savedUser.getRoles());

                                Mono<Boolean> emailMono = emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getUsername())
                                        .thenReturn(true);

                                Mono<Boolean> auditMono = auditLogsEnabled ? auditService.logUserEvent(
                                        savedUser.getId(),
                                        "USER_SIGNUP",
                                        "New user registered with username: " + savedUser.getUsername()
                                ).thenReturn(true) : Mono.just(true);

                                return Mono.zip(accessTokenMono, emailMono, auditMono, refreshTokenMono)
                                        .flatMap(tuple1 -> {
                                            String token = tuple1.getT1();
                                            return Mono.just(ApiResponse.success(
                                                    AuthResponse.builder()
                                                            .accessToken(token)
                                                            .refreshToken(tuple1.getT4())
                                                            .mfaEnabled(false)
                                                            .otpLoginEnabled(false)
                                                            .userId(savedUser.getId())
                                                            .username(savedUser.getUsername())
                                                            .email(savedUser.getEmail())
                                                            .roles(savedUser.getRoles())
                                                            .build()
                                            ));
                                        });
                            });
                })
                .onErrorResume(ex -> {
                    log.error("Signup error: {}", ex.getMessage());
                    if (ExceptionUtil.isHandledException(ex)) {
                        // Let GlobalExceptionHandler handle it
                        return Mono.error(ex);
                    }
                    return Mono.error(new ApiErrorException(ApiResponse.error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("SIGNUP_ERROR", ex.getMessage(), null, null)
                    ))));
                });
    }

    @Override
    public Mono<ApiResponse<AuthResponse>> login(LoginRequest loginRequest) {
        return userRepository.findByUsernameOrEmail(loginRequest.getUsernameOrEmail(), loginRequest.getUsernameOrEmail())
                .switchIfEmpty(Mono.error(new BadRequestException("Invalid username or password")))
                .flatMap(user -> userSecurityRepository.findByUserId(user.getId())
                        .flatMap(userSecurity -> {
                            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                                return handleFailedLogin(user, userSecurity);
                            }

                            if (!user.isEnabled()) {
                                return Mono.error(new AccountDisabledException("Account is disabled"));
                            }

                            if (!user.isAccountNonLocked()) {
                                if (userSecurity.getAccountLockedUntil() != null &&
                                        userSecurity.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
                                    return Mono.error(new AccountLockedException("Account is locked until " +
                                            userSecurity.getAccountLockedUntil()));
                                } else {
                                    // Lock expired, unlock the account
                                    user.setAccountNonLocked(true);
                                    userSecurity.setAccountLockedUntil(null);
                                    userSecurity.setFailedLoginAttempts(0);
                                }
                            }

                            // Reset failed attempts on successful login
                            userSecurity.setFailedLoginAttempts(0);
                            userSecurity.setLastFailedLoginAttempt(null);
                            userSecurity.setLastLoginDate(LocalDateTime.now());

                            return userRepository.save(user)
                                    .then(userSecurityRepository.save(userSecurity))
                                    .then(generateAuthLoginResponse(user, userSecurity));
                        }))
                .onErrorResume(e -> {
                    log.error("Login error: {}", e.getMessage());
                    if (ExceptionUtil.isHandledException(e)) {
                        // Let GlobalExceptionHandler handle it
                        return Mono.error(e);
                    }
                    return Mono.error(new ApiErrorException(ApiResponse.error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("LOGIN_ERROR", e.getMessage(), null, null)
                    ))));
                });
    }

    private Mono<ApiResponse<AuthResponse>> generateAuthLoginResponse(User user, UserSecurity userSecurity) {
        if(userSecurity.isMfaEnabled()){
            return Mono.just(ApiResponse.success(
                    AuthResponse.builder()
                            .mfaEnabled(userSecurity.isMfaEnabled())
                            .userId(user.getId())
                            .build()
            ));
        } else if(userSecurity.isOtpLoginEnabled()){
            return otpService.generateOtpAndSendEmail(user, userSecurity.isOtpLoginEnabled())
                    .flatMap(otpSent -> {
                        if (otpSent) {
                            log.info("OTP successfully generated and sent to {}", user.getEmail());
                        } else {
                            log.warn("OTP generation failed for {}", user.getEmail());
                        }
                        return Mono.just(ApiResponse.success(
                                AuthResponse.builder()
                                        .otpLoginEnabled(userSecurity.isOtpLoginEnabled())
                                        .userId(user.getId())
                                        .build()
                                ));
                    });
        } else {
            // For non-MFA and otp enabled users, generate tokens immediately
            Mono<String> accessToken = tokenProvider.generateAccessToken(user.getUsername(), user.getRoles());
            Mono<String> refreshToken = tokenProvider.generateRefreshToken(user.getUsername(), user.getRoles());
            return Mono.zip(accessToken, refreshToken)
                    .map(tuple -> ApiResponse.success(
                            AuthResponse.builder()
                                    .accessToken(tuple.getT1())
                                    .refreshToken(tuple.getT2())
                                    .mfaEnabled(userSecurity.isMfaEnabled())
                                    .otpLoginEnabled(userSecurity.isMfaEnabled())
                                    .userId(user.getId())
                                    .username(user.getUsername())
                                    .email(user.getEmail())
                                    .roles(user.getRoles())
                                    .build()
                    ));
        }
    }

    private Mono<ApiResponse<AuthResponse>> handleFailedLogin(User user, UserSecurity userSecurity) {
        int failedAttempts = userSecurity.getFailedLoginAttempts() + 1;
        userSecurity.setFailedLoginAttempts(failedAttempts);
        userSecurity.setLastFailedLoginAttempt(LocalDateTime.now());

        // Lock account after 5 failed attempts
        if (failedAttempts >= 5) {
            user.setAccountNonLocked(false);
            userSecurity.setAccountLockedUntil(LocalDateTime.now().plusHours(1));

            // Send account locked email via Kafka
            Map<String, Object> emailVariables = new HashMap<>();
            emailVariables.put("username", user.getUsername());
            emailVariables.put("lockedUntil", userSecurity.getAccountLockedUntil());

            return userRepository.save(user)
                    .then(userSecurityRepository.save(userSecurity))
//                    .then(kafkaProducerService.sendEmailEvent(
//                            user.getEmail(),
//                            "account-locked",
//                            emailVariables
//                    ))
                    .then(Mono.error(new BadRequestException("Invalid username or password. Account locked for 1 hour")));
        }

        return userSecurityRepository.save(userSecurity)
                .then(Mono.error(new BadRequestException("Invalid username or password")));
    }

    @Override
    public Mono<ApiResponse<AuthResponse>> verifyOtp(VerifyOtpRequest verifyOtpRequest, String userId) {
        return  userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new BadRequestException("User not found")))
                .flatMap(user -> otpService.validateOtp(user.getEmail(), verifyOtpRequest.getOtp())
                        .flatMap(valid -> {
                            if (!valid) {
                                return Mono.error(new BadRequestException("Invalid OTP"));
                            }

                            return userSecurityRepository.findByUserId(user.getId())
                                    .flatMap(userSecurity -> {
                                        if (!userSecurity.isOtpLoginEnabled() && userSecurity.isMfaEnabled()) {
                                            return Mono.error(new BadRequestException("MFA is already enabled"));
                                        }
                                        Mono<String> accessToken = tokenProvider.generateAccessToken(user.getUsername(), user.getRoles());
                                        Mono<String> refreshToken = tokenProvider.generateRefreshToken(user.getUsername(), user.getRoles());
                                        return Mono.zip(accessToken, refreshToken)
                                                .map(tuple -> ApiResponse.success(
                                                        AuthResponse.builder()
                                                                .accessToken(tuple.getT1())
                                                                .refreshToken(tuple.getT2())
                                                                .mfaEnabled(false)
                                                                .userId(user.getId())
                                                                .username(user.getUsername())
                                                                .email(user.getEmail())
                                                                .build()
                                                        ));
                                    });
                        })
                .onErrorResume(e -> {
                    log.error("OTP verification error: {}", e.getMessage());
                    if (ExceptionUtil.isHandledException(e)) {
                        // Let GlobalExceptionHandler handle it
                        return Mono.error(e);
                    }
                    return Mono.error(new ApiErrorException(ApiResponse.error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("OTP_ERROR", e.getMessage(), null, null)
                    ))));
                }));
    }

    @Override
    public Mono<ApiResponse<Void>> forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        return userRepository.findByEmail(forgotPasswordRequest.getEmail())
                .switchIfEmpty(Mono.error(new BadRequestException("User not found with email: " + forgotPasswordRequest.getEmail())))
                .flatMap(user -> {
                    // Generate reset token
                    String resetToken = tokenProvider.generatePasswordResetToken(user.getEmail());

                    return userSecurityRepository.findByUserId(user.getId())
                            .switchIfEmpty(Mono.error(new ResourceNotFoundException("User security record not found")))
                            .flatMap(userSecurity -> {
                                userSecurity.setLastPasswordResetDate(LocalDateTime.now());

                                return userSecurityRepository.save(userSecurity).flatMap(savedUserSecurity -> {
                                    Mono<Boolean> emailMono = emailService.sendPasswordResetEmail(user.getEmail(), resetToken)
                                            .thenReturn(true);

                                    Mono<Boolean> auditMono = auditLogsEnabled ?
                                            auditService.logUserEvent(
                                                    user.getId(),
                                                    "USER_RESET_PASSWORD",
                                                    "Request made to reset password: " + user.getUsername()
                                            ).thenReturn(true)
                                            : Mono.just(true);

                                    return Mono.zip(auditMono, emailMono)
                                            .thenReturn(ApiResponse.<Void>success(null));
//                                            .thenReturn(new ApiResponse<Void>(true,
//                                                    "If an account exists with this email, a password reset link has been sent", null));
                                });
                            });
                })
                .onErrorResume(e -> {
                    log.error("Error in forgot password process: {}", e.getMessage());
                    if (ExceptionUtil.isHandledException(e)) {
                        // Let GlobalExceptionHandler handle it
                        return Mono.error(e);
                    }
                    return Mono.error(new ApiErrorException(
                            ApiResponse.error(Collections.singletonList(
                                    new ApiResponse.ErrorDetail("FORGOT_PASSWORD_ERROR", e.getMessage(), null, null)
                            ))
                    ));

//                    return Mono.just(new ApiResponse<Void>(false, "Error processing password reset request", null));
                });
    }

    @Override
    public Mono<ApiResponse<Void>> resetPassword(ResetPasswordRequest resetPasswordRequest) {
        // Validate token first
        return tokenProvider.validatePasswordResetToken(resetPasswordRequest.getToken())
                .flatMap(email -> userRepository.findByEmail(email)
                        .switchIfEmpty(Mono.error(new BadRequestException("User not found with email: " + email)))
                        .flatMap(user -> {
                            // Update password
                            String encodedPassword = passwordEncoder.encode(resetPasswordRequest.getNewPassword());
                            user.setPassword(encodedPassword);

                            // Update user security
                            return userSecurityRepository.findByUserId(user.getId())
                                    .switchIfEmpty(Mono.error(new ResourceNotFoundException("User security record not found")))
                                    .flatMap(userSecurity -> {
                                        userSecurity.setLastPasswordResetDate(LocalDateTime.now());
                                        userSecurity.setPasswordResetRequired(false);
                                        userSecurity.setFailedLoginAttempts(0);
                                        return userSecurityRepository.save(userSecurity);
                                    })
                                    .then(userRepository.save(user))
                                    .then(auditService.logUserEvent(
                                                    user.getId(),
                                                    "PASSWORD_RESET_SUCCESS",
                                                    "User reset his password")
                                    )
                                    .thenReturn(ApiResponse.<Void>success(null));
                        })
                        .onErrorResume(e -> {
                            log.error("Error in reset password process: {}", e.getMessage());
                            if (e instanceof TokenExpiredException) {
                                return Mono.error(new ApiErrorException(
                                        ApiResponse.error(Collections.singletonList(
                                                new ApiResponse.ErrorDetail("RESET_PASSWORD_ERROR", "Password reset token has expired", null, null)
                                        ))));
                            } else if (e instanceof InvalidTokenException) {
                                return Mono.error(new ApiErrorException(
                                        ApiResponse.error(Collections.singletonList(
                                                new ApiResponse.ErrorDetail("RESET_PASSWORD_ERROR", "Invalid password reset token", null, null)
                                        ))));
                            } else if (ExceptionUtil.isHandledException(e)){
                                return Mono.error(e);
                            }
                            return Mono.error(new ApiErrorException(
                                    ApiResponse.error(Collections.singletonList(
                                            new ApiResponse.ErrorDetail("RESET_PASSWORD_ERROR", "Error resetting password", null, null)
                                    ))));
                        }));
    }

    @Override
    public Mono<ApiResponse<Void>> changePassword(ChangePasswordRequest changePasswordRequest, String userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")))
                .flatMap(user -> userSecurityRepository.findByUserId(user.getId())
                        .flatMap(userSecurity -> {
                            if (!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), user.getPassword())) {
                                return Mono.error(new BadRequestException("Current password is incorrect"));
                            }

                            user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
                            userSecurity.setLastPasswordResetDate(LocalDateTime.now());

                            return userRepository.save(user)
                                    .then(userSecurityRepository.save(userSecurity))
                                    .then(auditService.logUserEvent(
                                            user.getId(),
                                            "PASSWORD_CHANGED",
                                            "User changed his password"
                                    ))
//                                    .then(kafkaProducerService.sendEmailEvent(
//                                            user.getEmail(),
//                                            "password-changed",
//                                            Collections.singletonMap("username", user.getUsername())
//                                    ))
                                    .thenReturn(ApiResponse.<Void>success(null));
                        }))
                .onErrorResume(e -> {
                    log.error("Change password error: {}", e.getMessage());
                    if (ExceptionUtil.isHandledException(e)) {
                        // Let GlobalExceptionHandler handle it
                        return Mono.error(e);
                    }
                    return Mono.error(new ApiErrorException(ApiResponse.error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("CHANGE_PASSWORD_ERROR", e.getMessage(), null, null)
                    ))));
                });
    }

    public Mono<ApiResponse<TokenRefreshResponse>> refreshToken(
            TokenRefreshRequest request) {

        String refreshToken = request.getRefreshToken();

        return tokenProvider.validateRefreshToken(refreshToken)
                .flatMap(valid -> {
                    if (!valid) {
                        return Mono.error(new BadCredentialsException("Invalid refresh token"));
                    }

                    // ✅ Extract username from refresh token
                    return tokenProvider.getUsernameFromToken(refreshToken)
                            .flatMap(username -> userRepository.findByUsername(username))
                            .switchIfEmpty(Mono.error(new BadCredentialsException("User not found")))
                            .flatMap(user -> {
                                // ✅ Generate new access token ONLY
                                return tokenProvider.generateAccessToken(user.getUsername(), user.getRoles())
                                        .map(newAccessToken -> {
                                            // ✅ Return new access token + same refresh token
                                            TokenRefreshResponse response = TokenRefreshResponse.builder()
                                                    .accessToken(newAccessToken)
                                                    .refreshToken(refreshToken)
                                                    .build();

                                            log.info("Token refreshed for user: {}", user.getUsername());
                                            return ApiResponse.success(response);
                                        });
                            });
                })
                .onErrorResume(e -> {
                    log.error("Refresh error: {}", e.getMessage());
                    if(ExceptionUtil.isHandledException(e)){
                        return Mono.error(e);
                    }
                    return Mono.just(ApiResponse.error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("REFRESH_ERROR", e.getMessage(), null, null)
                    )));
                });
    }

    @Override
    public Mono<ApiResponse<MfaSetupResponse>> setupMfa(MfaSetupRequest mfaSetupRequest, String userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")))
                .flatMap(user -> userSecurityRepository.findByUserId(user.getId())
                        .flatMap(userSecurity -> {
                            if (userSecurity.isMfaEnabled()) {
                                return Mono.error(new BadRequestException("MFA is already enabled"));
                            }

                            return mfaService.generateSecret()
                                    .flatMap(secret -> {
                                        userSecurity.setMfaSecret(secret);
                                        userSecurity.setMfaEnabled(true);
                                        userSecurity.setUpdatedAt(LocalDateTime.now());

                                        return userSecurityRepository.save(userSecurity)
                                                .then(mfaService.generateQrCode(secret, user.getUsername()))
                                                .map(qrCode -> ApiResponse.success(
                                                        MfaSetupResponse.builder()
                                                                .secret(secret)
                                                                .qrCode(qrCode)
                                                                .build()
                                                ));
                                    });
                        }))
                .onErrorResume(e -> {
                    log.error("MFA setup error: {}", e.getMessage());
                    if (ExceptionUtil.isHandledException(e)) {
                        // Let GlobalExceptionHandler handle it
                        return Mono.error(e);
                    }
                    return Mono.error(new ApiErrorException(ApiResponse.error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("MFA_SETUP_ERROR", e.getMessage(), null, null)
                    ))));
                });
    }

    @Override
    public Mono<ApiResponse<AuthResponse>> verifyMfa(MfaVerifyRequest mfaVerifyRequest, String userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")))
                .flatMap(user -> userSecurityRepository.findByUserId(user.getId())
                        .flatMap(userSecurity -> {
                            if (!userSecurity.isMfaEnabled() || userSecurity.getMfaSecret() == null) {
                                return Mono.error(new BadRequestException("MFA is not enabled for this user"));
                            }

                            return mfaService.verifyCode(userSecurity.getMfaSecret(), mfaVerifyRequest.getCode())
                                    .flatMap(valid -> {
                                        if (!valid) {
                                            return Mono.error(new BadRequestException("Invalid MFA code"));
                                        }

                                        return auditService.logUserEvent(
                                                user.getId(),
                                                "MFA_VERIFIED",
                                                "User verified MFA setup"
                                        ).then(
                                                Mono.zip(tokenProvider.generateAccessToken(user.getUsername(), user.getRoles()),
                                                tokenProvider.generateRefreshToken(user.getUsername(), user.getRoles()))
                                                .map(tokens -> ApiResponse.success(
                                                        AuthResponse.builder()
                                                                .accessToken(tokens.getT1())
                                                                .refreshToken(tokens.getT2())
                                                                .userId(user.getId())
                                                                .username(user.getUsername())
                                                                .email(user.getEmail())
                                                                .roles(user.getRoles())
                                                                .build()
                                                )));
                                    });
                        }))
                .onErrorResume(e -> {
                    log.error("MFA verification error: {}", e.getMessage());
                    if (ExceptionUtil.isHandledException(e)) {
                        // Let GlobalExceptionHandler handle it
                        return Mono.error(e);
                    }
                    return Mono.error(new ApiErrorException(ApiResponse.error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("MFA_VERIFY_ERROR", e.getMessage(), null, null)
                    ))));
                });
    }

    @Override
    public Mono<ApiResponse<Void>> disableMfa(String userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")))
                .flatMap(user -> userSecurityRepository.findByUserId(user.getId())
                        .flatMap(userSecurity -> {
                            if (!userSecurity.isMfaEnabled()) {
                                return Mono.error(new BadRequestException("MFA is not enabled for this user"));
                            }

                            userSecurity.setMfaEnabled(false);
                            userSecurity.setMfaSecret(null);
                            userSecurity.setUpdatedAt(LocalDateTime.now());

                            return userSecurityRepository.save(userSecurity)
                                    .then(auditService.logUserEvent(
                                            user.getId(),
                                            "MFA_DISABLED",
                                            "User disabled MFA"
                                    ))
//                                    .then(kafkaProducerService.sendEmailEvent(
//                                            user.getEmail(),
//                                            "mfa-disabled",
//                                            Collections.singletonMap("username", user.getUsername())
//                                    ))
                                    .thenReturn(ApiResponse.<Void>success(null));
                        }))
                .onErrorResume(e -> {
                    log.error("MFA disable error: {}", e.getMessage());
                    return Mono.error(new ApiErrorException(ApiResponse.error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("MFA_DISABLE_ERROR", e.getMessage(), null, null)
                    ))));
                });
    }
}