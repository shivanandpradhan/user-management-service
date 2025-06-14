package com.snp.dev.user_management_service.service.impl;

import com.snp.dev.user_management_service.exception.AccountDisabledException;
import com.snp.dev.user_management_service.exception.AccountLockedException;
import com.snp.dev.user_management_service.exception.BadRequestException;
import com.snp.dev.user_management_service.exception.ResourceNotFoundException;
import com.snp.dev.user_management_service.security.JwtTokenProvider;
import com.snp.dev.user_management_service.dto.*;
import com.snp.dev.user_management_service.model.*;
import com.snp.dev.user_management_service.repository.*;
import com.snp.dev.user_management_service.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;

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
//    private final EmailService emailService;
    private final OtpService otpService;
    private final MfaService mfaService;
    private final AuditService auditService;
//    private final KafkaProducerService kafkaProducerService;

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
                                // Send welcome email via Kafka
                                Map<String, Object> emailVariables = new HashMap<>();
                                emailVariables.put("username", savedUser.getUsername());
                                emailVariables.put("email", savedUser.getEmail());

//                                return kafkaProducerService.sendEmailEvent(
//                                        savedUser.getEmail(),
//                                        "welcome-email",
//                                        emailVariables
//                                ).thenReturn(savedUser);
                                return Mono.just(savedUser);
                            })
                            .flatMap(savedUser -> {
                                String token = tokenProvider.createToken(savedUser.getUsername(), savedUser.getRoles())
                                        .block(); // Note: In reactive, we should avoid block(), but for JWT creation it's often necessary

                                return auditService.logUserEvent(
                                        savedUser.getId(),
                                        "USER_SIGNUP",
                                        "New user registered with username: " + savedUser.getUsername()
                                ).thenReturn(ApiResponse.success(
                                        AuthResponse.builder()
                                                .accessToken(token)
                                                .refreshToken(UUID.randomUUID().toString()) // In real app, generate proper refresh token
                                                .mfaEnabled(false)
                                                .userId(savedUser.getId())
                                                .username(savedUser.getUsername())
                                                .email(savedUser.getEmail())
                                                .build()
                                ));
                            });
                })
                .onErrorResume(e -> {
                    log.error("Signup error: {}", e.getMessage());
                    return Mono.just(ApiResponse.error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("SIGNUP_ERROR", e.getMessage(), null, null)
                    )));
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

                            return userRepository.save(user)
                                    .then(userSecurityRepository.save(userSecurity))
                                    .then(otpService.generateOtp(user.getEmail()))
                                    .then(generateAuthResponse(user, userSecurity));
                        }))
                .onErrorResume(e -> {
                    log.error("Login error: {}", e.getMessage());
                    return Mono.just(ApiResponse.error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("LOGIN_ERROR", e.getMessage(), null, null)
                    )));
                });
    }

    private Mono<ApiResponse<AuthResponse>> generateAuthResponse(User user, UserSecurity userSecurity) {
        if (userSecurity.isMfaEnabled()) {
            // For MFA enabled users, don't return tokens yet
            return Mono.just(ApiResponse.success(
                    AuthResponse.builder()
                            .mfaEnabled(true)
                            .mfaType("TOTP") // or whatever MFA type you're using
                            .userId(user.getId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .build()
            ));
        } else {
            // For non-MFA users, generate tokens immediately
            return tokenProvider.createToken(user.getUsername(), user.getRoles())
                    .map(accessToken -> ApiResponse.success(
                            AuthResponse.builder()
                                    .accessToken(accessToken)
                                    .refreshToken(UUID.randomUUID().toString()) // In real app, generate proper refresh token
                                    .mfaEnabled(false)
                                    .userId(user.getId())
                                    .username(user.getUsername())
                                    .email(user.getEmail())
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
    public Mono<ApiResponse<AuthResponse>> verifyOtp(VerifyOtpRequest verifyOtpRequest) {
        return userRepository.findByUsernameOrEmail(verifyOtpRequest.getUsernameOrEmail(), verifyOtpRequest.getUsernameOrEmail())
                .switchIfEmpty(Mono.error(new BadRequestException("User not found")))
                .flatMap(user -> otpService.validateOtp(user.getEmail(), verifyOtpRequest.getOtp())
                        .flatMap(valid -> {
                            if (!valid) {
                                return Mono.error(new BadRequestException("Invalid OTP"));
                            }

                            return userSecurityRepository.findByUserId(user.getId())
                                    .flatMap(userSecurity -> {
                                        if (userSecurity.isMfaEnabled()) {
                                            return Mono.error(new BadRequestException("MFA is already enabled"));
                                        }
                                        return otpService.clearOtp(user.getEmail())
                                                .then(tokenProvider.createToken(user.getUsername(), user.getRoles())
                                                        .map(accessToken -> ApiResponse.success(
                                                                AuthResponse.builder()
                                                                        .accessToken(accessToken)
                                                                        .refreshToken(UUID.randomUUID().toString()) // In real app, generate proper refresh token
                                                                        .mfaEnabled(false)
                                                                        .userId(user.getId())
                                                                        .username(user.getUsername())
                                                                        .email(user.getEmail())
                                                                        .build()
                                                        )));
                                    });
                        })

//                        .then(otpService.clearOtp(verifyOtpRequest.getUsernameOrEmail())))
                .onErrorResume(e -> {
                    log.error("OTP verification error: {}", e.getMessage());
                    return Mono.just(ApiResponse.error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("OTP_ERROR", e.getMessage(), null, null)
                    )));
                }));
    }

    @Override
    public Mono<ApiResponse<Void>> forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        return userRepository.findByEmail(forgotPasswordRequest.getEmail())
                .switchIfEmpty(Mono.error(new BadRequestException("Email not found")))
                .flatMap(user -> {
                    String resetToken = UUID.randomUUID().toString();
                    // In a real app, you would save this token with an expiry date

                    // Send password reset email via Kafka
                    Map<String, Object> emailVariables = new HashMap<>();
                    emailVariables.put("username", user.getUsername());
                    emailVariables.put("resetToken", resetToken);
//
//                    return kafkaProducerService.sendEmailEvent(
//                            user.getEmail(),
//                            "password-reset",
//                            emailVariables
//                    ).thenReturn(ApiResponse.<Void>success(null));z
                    return Mono.just(ApiResponse.<Void>success(null));
                })
                .onErrorResume(e -> {
                    log.error("Forgot password error: {}", e.getMessage());
                    return Mono.just(ApiResponse.error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("FORGOT_PASSWORD_ERROR", e.getMessage(), null, null)
                    )));
                });
    }

    @Override
    public Mono<ApiResponse<Void>> resetPassword(ResetPasswordRequest resetPasswordRequest) {
        // In a real app, you would validate the token first
        return Mono.error(new UnsupportedOperationException("Reset password not implemented"));
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
                                            "User changed their password"
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
                    return Mono.just(ApiResponse.error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("CHANGE_PASSWORD_ERROR", e.getMessage(), null, null)
                    )));
                });
    }

    @Override
    public Mono<ApiResponse<TokenRefreshResponse>> refreshToken(TokenRefreshRequest tokenRefreshRequest) {
        return Mono.error(new UnsupportedOperationException("Token refresh not implemented"));
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
                    return Mono.just(ApiResponse.error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("MFA_SETUP_ERROR", e.getMessage(), null, null)
                    )));
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
                                        ).then(tokenProvider.createToken(user.getUsername(), user.getRoles())
                                                .map(accessToken -> ApiResponse.success(
                                                        AuthResponse.builder()
                                                                .accessToken(accessToken)
                                                                .refreshToken(UUID.randomUUID().toString())
                                                                .userId(user.getId())
                                                                .username(user.getUsername())
                                                                .email(user.getEmail())
                                                                .build()
                                                )));
                                    });
                        }))
                .onErrorResume(e -> {
                    log.error("MFA verification error: {}", e.getMessage());
                    return Mono.just(ApiResponse.error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("MFA_VERIFY_ERROR", e.getMessage(), null, null)
                    )));
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
                    return Mono.just(ApiResponse.error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("MFA_DISABLE_ERROR", e.getMessage(), null, null)
                    )));
                });
    }
}