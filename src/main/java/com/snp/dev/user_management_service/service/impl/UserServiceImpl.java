package com.snp.dev.user_management_service.service.impl;

import com.snp.dev.user_management_service.dto.ApiResponse;
import com.snp.dev.user_management_service.dto.response.PageResponse;
import com.snp.dev.user_management_service.dto.UserProfileDto;
import com.snp.dev.user_management_service.dto.response.UserResponse;
import com.snp.dev.user_management_service.exception.ForbiddenException;
import com.snp.dev.user_management_service.exception.ResourceNotFoundException;
import com.snp.dev.user_management_service.model.User;
import com.snp.dev.user_management_service.model.UserProfile;
import com.snp.dev.user_management_service.model.UserSecurity;
import com.snp.dev.user_management_service.repository.CustomUserRepository;
import com.snp.dev.user_management_service.repository.UserProfileRepository;
import com.snp.dev.user_management_service.repository.UserRepository;
import com.snp.dev.user_management_service.repository.UserSecurityRepository;
import com.snp.dev.user_management_service.service.AuditService;
import com.snp.dev.user_management_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserSecurityRepository userSecurityRepository;
    private final AuditService auditService;
    private final CustomUserRepository customUserRepository;
//    private final KafkaProducerService kafkaProducerService;

    @Override
    public Mono<User> getUserById(String id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")));
    }

    @Override
    public Mono<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")));
    }

    @Override
    public Mono<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")));
    }

    @Override
    public Mono<UserProfileDto> getUserProfile(String userId) {
        // Use Mono.zip to combine data from userRepository and userProfileRepository
        return Mono.zip(
                        userRepository.findById(userId)
                                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found"))),
                        userProfileRepository.findByUserId(userId)
                                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User profile not found"))),
                        userSecurityRepository.findByUserId(userId)
                                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User security not found")))
                )
                .map(tuple -> {
                    // Extract User and UserProfile from the tuple
                    User user = tuple.getT1();
                    UserProfile userProfile = tuple.getT2();
                    UserSecurity userSecurity = tuple.getT3();

                    // Map to UserProfileDto and set additional fields from User
                    UserProfileDto userProfileDto = mapToProfileDto(userProfile);
                    userProfileDto.setEnabled(user.isEnabled());
                    userProfileDto.setRoles(user.getRoles());
                    userProfileDto.setCreatedAt(user.getCreatedAt());
                    userProfileDto.setLastLogin(userSecurity.getLastLoginDate());
                    userProfileDto.setAccountLocked(!user.isAccountNonLocked());
                    return userProfileDto;
                });
    }

    @Override
    public Mono<UserProfileDto> updateUserProfile(UserProfileDto userProfileDto, String userId) {
        return userProfileRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User profile not found")))
                .flatMap(profile -> {
                    profile.setFirstName(userProfileDto.getFirstName());
                    profile.setLastName(userProfileDto.getLastName());
                    profile.setDateOfBirth(userProfileDto.getDateOfBirth());
                    profile.setPhoneNumber(userProfileDto.getPhoneNumber());
                    profile.setAddress(userProfileDto.getAddress());
                    profile.setCity(userProfileDto.getCity());
                    profile.setCountry(userProfileDto.getCountry());
                    profile.setPostalCode(userProfileDto.getPostalCode());
                    profile.setProfileImageUrl(userProfileDto.getProfileImageUrl());
                    profile.setBio(userProfileDto.getBio());
                    profile.setUpdatedAt(LocalDateTime.now());

                    return userProfileRepository.save(profile)
                            .flatMap(savedProfile -> {
                                // Log profile update event
                                return auditService.logUserEvent(
                                        userId,
                                        "PROFILE_UPDATED",
                                        "User updated their profile"
                                ).thenReturn(mapToProfileDto(savedProfile));
                            });
                });
    }

    @Override
    public Mono<Void> deleteUser(String userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")))
                .flatMap(user -> {
                    if (user.getRoles().contains("ROLE_SUPER_ADMIN")) {
                        return Mono.error(new ForbiddenException("Cannot delete super admin"));
                    }

                    return Mono.zip(
                            userRepository.delete(user),
                            userProfileRepository.deleteByUserId(userId),
                            userSecurityRepository.deleteByUserId(userId)
                    ).then();
                })
                .then(auditService.logUserEvent(
                        userId,
                        "ACCOUNT_DELETED",
                        "User deleted their account"
                ));
    }

    @Override
    public Mono<Void> lockUser(String userId, String adminId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")))
                .flatMap(user -> {
                    if (user.getRoles().contains("ROLE_SUPER_ADMIN")) {
                        return Mono.error(new ForbiddenException("Cannot lock super admin"));
                    }

                    user.setAccountNonLocked(false);
                    return userRepository.save(user)
                            .then(userSecurityRepository.findByUserId(userId))
                            .flatMap(userSecurity -> {
                                userSecurity.setAccountLockedUntil(LocalDateTime.now().plusDays(1));
                                return userSecurityRepository.save(userSecurity);
                            });
                })
                .then(auditService.logAdminEvent(
                        adminId,
                        "USER_LOCKED",
                        "USER",
                        userId,
                        "Admin locked user account"
                ));
//                .then(kafkaProducerService.sendUserEvent(
//                        userId,
//                        "ACCOUNT_LOCKED",
//                        "{\"lockedBy\":\"" + adminId + "\",\"reason\":\"Manual lock by admin\"}"
//                ));
    }

    @Override
    public Mono<Void> unlockUser(String userId, String adminId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")))
                .flatMap(user -> {
                    user.setAccountNonLocked(true);
                    return userRepository.save(user)
                            .then(userSecurityRepository.findByUserId(userId))
                            .flatMap(userSecurity -> {
                                userSecurity.setAccountLockedUntil(null);
                                userSecurity.setFailedLoginAttempts(0);
                                return userSecurityRepository.save(userSecurity);
                            });
                })
                .then(auditService.logAdminEvent(
                        adminId,
                        "USER_UNLOCKED",
                        "USER",
                        userId,
                        "Admin unlocked user account"
                ));
//                .then(kafkaProducerService.sendUserEvent(
//                        userId,
//                        "ACCOUNT_UNLOCKED",
//                        "{\"unlockedBy\":\"" + adminId + "\"}"
//                ));
    }

    @Override
    public Mono<Void> enableUser(String userId, String adminId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")))
                .flatMap(user -> {
                    user.setEnabled(true);
                    return userRepository.save(user);
                })
                .then(auditService.logAdminEvent(
                        adminId,
                        "USER_ENABLED",
                        "USER",
                        userId,
                        "Admin enabled user account"
                ));
//                .then(kafkaProducerService.sendUserEvent(
//                        userId,
//                        "ACCOUNT_ENABLED",
//                        "{\"enabledBy\":\"" + adminId + "\"}"
//                ));
    }

    @Override
    public Mono<Void> disableUser(String userId, String adminId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")))
                .flatMap(user -> {
                    if (user.getRoles().contains("ROLE_SUPER_ADMIN")) {
                        return Mono.error(new ForbiddenException("Cannot disable super admin"));
                    }

                    user.setEnabled(false);
                    return userRepository.save(user);
                })
                .then(auditService.logAdminEvent(
                        adminId,
                        "USER_DISABLED",
                        "USER",
                        userId,
                        "Admin disabled user account"
                ));
//                .then(kafkaProducerService.sendUserEvent(
//                        userId,
//                        "ACCOUNT_DISABLED",
//                        "{\"disabledBy\":\"" + adminId + "\"}"
//                ));
    }

    private UserProfileDto mapToProfileDto(UserProfile profile) {
        return UserProfileDto.builder()
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .dateOfBirth(profile.getDateOfBirth())
                .phoneNumber(profile.getPhoneNumber())
                .address(profile.getAddress())
                .city(profile.getCity())
                .country(profile.getCountry())
                .postalCode(profile.getPostalCode())
                .profileImageUrl(profile.getProfileImageUrl())
                .bio(profile.getBio())
                .build();
    }

    @Override
    public Mono<User> updateUser(User user) {
        return userRepository.save(user)
                .flatMap(savedUser -> {
                    return Mono.just(savedUser);
                });
    }

    @Override
    public Mono<PageResponse<UserResponse>> listUsers(int page, int limit) {
        // Create a Pageable instance for pagination
        Pageable pageable = PageRequest.of(page, limit);

        // Fetch users from the repository
//        return customUserRepository.findAllWithPagination(pageable)
        return userRepository.findAll()
        .collectList() // Collect the users into a list
                .zipWith(userRepository.count()) // Zip the list with the total count of users
                .map(tuple -> {
                    List<User> users = tuple.getT1(); // Extract user list
                    long totalItems = tuple.getT2(); // Extract total number of items

                    // Convert the entity list to a response DTO list
                    List<UserResponse> userResponses = users.stream()
                            .map(this::toUserResponse) // Assuming a mapping method is available
                            .toList();

                    // Build and return a PageResponse
                    return new PageResponse<>(
                            userResponses, page, limit, totalItems);
                });
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse
                .builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .roles(user.getRoles()).build();
    }

    @Override
    public Mono<ApiResponse<Boolean>> canEditPortfolio(String ownerId, String requesterId) {
        log.debug("Checking if user {} can edit portfolio of {}", requesterId, ownerId);

        if (ownerId == null || requesterId == null) {
            return Mono.just(ApiResponse.<Boolean>error(Collections.singletonList(
                    new ApiResponse.ErrorDetail("VALIDATION_ERROR", "Owner ID and Requester ID cannot be null", null, null)
            )));
        }

        if (ownerId.equals(requesterId)) {
            return Mono.just(ApiResponse.success(true));
        }

        return userRepository.findById(ownerId)
                .map(portfolioUser -> {
                    boolean canEdit = false;
                    if (portfolioUser.isEditAllowed()) {
                        Set<String> allowedEditors = portfolioUser.getAllowedEditors();
                        if (allowedEditors != null && allowedEditors.contains(requesterId)) {
                            canEdit = true;
                        }
                    }
                    return ApiResponse.<Boolean>success(canEdit);
                })
                .switchIfEmpty(Mono.defer(() ->
                        Mono.just(ApiResponse.<Boolean>error(Collections.singletonList(
                                new ApiResponse.ErrorDetail("NOT_FOUND", "Portfolio not found for user: " + ownerId, null, null)
                        )))
                ))
                .onErrorResume(e -> {
                    log.error("Error checking edit permission: {}", e.getMessage());
                    return Mono.just(ApiResponse.<Boolean>error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("PERMISSION_ERROR", "Failed to check permission: " + e.getMessage(), null, null)
                    )));
                });
    }

}
