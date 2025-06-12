//package com.snp.dev.user_management_service.service.impl;
//
//import com.snp.dev.user_management_service.dto.UserProfileDto;
//import com.snp.dev.user_management_service.exception.ForbiddenException;
//import com.snp.dev.user_management_service.exception.ResourceNotFoundException;
//import com.snp.dev.user_management_service.model.User;
//import com.snp.dev.user_management_service.model.UserProfile;
//import com.snp.dev.user_management_service.repository.UserProfileRepository;
//import com.snp.dev.user_management_service.repository.UserRepository;
//import com.snp.dev.user_management_service.repository.UserSecurityRepository;
//import com.snp.dev.user_management_service.service.AuditService;
////import com.snp.dev.user_management_service.service.KafkaProducerService;
//import com.snp.dev.user_management_service.service.UserService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import reactor.core.publisher.Mono;
//
//import java.time.LocalDateTime;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class UserServiceImpl implements UserService {
//
//    private final UserRepository userRepository;
//    private final UserProfileRepository userProfileRepository;
//    private final UserSecurityRepository userSecurityRepository;
//    private final AuditService auditService;
////    private final KafkaProducerService kafkaProducerService;
//
//    @Override
//    public Mono<User> getUserById(String id) {
//        return userRepository.findById(id)
//                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")));
//    }
//
//    @Override
//    public Mono<User> getUserByUsername(String username) {
//        return userRepository.findByUsername(username)
//                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")));
//    }
//
//    @Override
//    public Mono<User> getUserByEmail(String email) {
//        return userRepository.findByEmail(email)
//                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")));
//    }
//
//    @Override
//    public Mono<UserProfileDto> getUserProfile(String userId) {
//        return userProfileRepository.findByUserId(userId)
//                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User profile not found")))
//                .map(this::mapToProfileDto);
//    }
//
//    @Override
//    public Mono<UserProfileDto> updateUserProfile(UserProfileDto userProfileDto, String userId) {
//        return userProfileRepository.findByUserId(userId)
//                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User profile not found")))
//                .flatMap(profile -> {
//                    profile.setFirstName(userProfileDto.getFirstName());
//                    profile.setLastName(userProfileDto.getLastName());
//                    profile.setDateOfBirth(userProfileDto.getDateOfBirth());
//                    profile.setPhoneNumber(userProfileDto.getPhoneNumber());
//                    profile.setAddress(userProfileDto.getAddress());
//                    profile.setCity(userProfileDto.getCity());
//                    profile.setCountry(userProfileDto.getCountry());
//                    profile.setPostalCode(userProfileDto.getPostalCode());
//                    profile.setProfileImageUrl(userProfileDto.getProfileImageUrl());
//                    profile.setBio(userProfileDto.getBio());
//                    profile.setUpdatedAt(LocalDateTime.now());
//
//                    return userProfileRepository.save(profile)
//                            .flatMap(savedProfile -> {
//                                // Log profile update event
//                                return auditService.logUserEvent(
//                                        userId,
//                                        "PROFILE_UPDATED",
//                                        "User updated their profile"
//                                ).thenReturn(mapToProfileDto(savedProfile));
//                            });
//                });
//    }
//
//    @Override
//    public Mono<Void> deleteUser(String userId) {
//        return userRepository.findById(userId)
//                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")))
//                .flatMap(user -> {
//                    if (user.getRoles().contains("ROLE_SUPER_ADMIN")) {
//                        return Mono.error(new ForbiddenException("Cannot delete super admin"));
//                    }
//
//                    return Mono.zip(
//                            userRepository.delete(user),
//                            userProfileRepository.deleteByUserId(userId),
//                            userSecurityRepository.deleteByUserId(userId)
//                    ).then();
//                })
//                .then(auditService.logUserEvent(
//                        userId,
//                        "ACCOUNT_DELETED",
//                        "User deleted their account"
//                ));
//    }
//
//    @Override
//    public Mono<Void> lockUser(String userId, String adminId) {
//        return userRepository.findById(userId)
//                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")))
//                .flatMap(user -> {
//                    if (user.getRoles().contains("ROLE_SUPER_ADMIN")) {
//                        return Mono.error(new ForbiddenException("Cannot lock super admin"));
//                    }
//
//                    user.setAccountNonLocked(false);
//                    return userRepository.save(user)
//                            .then(userSecurityRepository.findByUserId(userId))
//                            .flatMap(userSecurity -> {
//                                userSecurity.setAccountLockedUntil(LocalDateTime.now().plusDays(1));
//                                return userSecurityRepository.save(userSecurity);
//                            });
//                })
//                .then(auditService.logAdminEvent(
//                        adminId,
//                        "USER_LOCKED",
//                        "USER",
//                        userId,
//                        "Admin locked user account"
//                ));
////                .then(kafkaProducerService.sendUserEvent(
////                        userId,
////                        "ACCOUNT_LOCKED",
////                        "{\"lockedBy\":\"" + adminId + "\",\"reason\":\"Manual lock by admin\"}"
////                ));
//    }
//
//    @Override
//    public Mono<Void> unlockUser(String userId, String adminId) {
//        return userRepository.findById(userId)
//                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")))
//                .flatMap(user -> {
//                    user.setAccountNonLocked(true);
//                    return userRepository.save(user)
//                            .then(userSecurityRepository.findByUserId(userId))
//                            .flatMap(userSecurity -> {
//                                userSecurity.setAccountLockedUntil(null);
//                                userSecurity.setFailedLoginAttempts(0);
//                                return userSecurityRepository.save(userSecurity);
//                            });
//                })
//                .then(auditService.logAdminEvent(
//                        adminId,
//                        "USER_UNLOCKED",
//                        "USER",
//                        userId,
//                        "Admin unlocked user account"
//                ));
////                .then(kafkaProducerService.sendUserEvent(
////                        userId,
////                        "ACCOUNT_UNLOCKED",
////                        "{\"unlockedBy\":\"" + adminId + "\"}"
////                ));
//    }
//
//    @Override
//    public Mono<Void> enableUser(String userId, String adminId) {
//        return userRepository.findById(userId)
//                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")))
//                .flatMap(user -> {
//                    user.setEnabled(true);
//                    return userRepository.save(user);
//                })
//                .then(auditService.logAdminEvent(
//                        adminId,
//                        "USER_ENABLED",
//                        "USER",
//                        userId,
//                        "Admin enabled user account"
//                ));
////                .then(kafkaProducerService.sendUserEvent(
////                        userId,
////                        "ACCOUNT_ENABLED",
////                        "{\"enabledBy\":\"" + adminId + "\"}"
////                ));
//    }
//
//    @Override
//    public Mono<Void> disableUser(String userId, String adminId) {
//        return userRepository.findById(userId)
//                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")))
//                .flatMap(user -> {
//                    if (user.getRoles().contains("ROLE_SUPER_ADMIN")) {
//                        return Mono.error(new ForbiddenException("Cannot disable super admin"));
//                    }
//
//                    user.setEnabled(false);
//                    return userRepository.save(user);
//                })
//                .then(auditService.logAdminEvent(
//                        adminId,
//                        "USER_DISABLED",
//                        "USER",
//                        userId,
//                        "Admin disabled user account"
//                ));
////                .then(kafkaProducerService.sendUserEvent(
////                        userId,
////                        "ACCOUNT_DISABLED",
////                        "{\"disabledBy\":\"" + adminId + "\"}"
////                ));
//    }
//
//    private UserProfileDto mapToProfileDto(UserProfile profile) {
//        return UserProfileDto.builder()
//                .firstName(profile.getFirstName())
//                .lastName(profile.getLastName())
//                .dateOfBirth(profile.getDateOfBirth())
//                .phoneNumber(profile.getPhoneNumber())
//                .address(profile.getAddress())
//                .city(profile.getCity())
//                .country(profile.getCountry())
//                .postalCode(profile.getPostalCode())
//                .profileImageUrl(profile.getProfileImageUrl())
//                .bio(profile.getBio())
//                .build();
//    }
//
//    @Override
//    public Mono<User> updateUser(User user) {
//        return userRepository.save(user)
//                .flatMap(savedUser -> {
//                    return Mono.just(savedUser);
//                });
//    }
//}
