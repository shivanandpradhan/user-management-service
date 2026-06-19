package com.snp.dev.user_management_service.service;

import com.snp.dev.user_management_service.dto.ApiResponse;
import com.snp.dev.user_management_service.dto.response.PageResponse;
import com.snp.dev.user_management_service.dto.UserProfileDto;
import com.snp.dev.user_management_service.dto.response.UserResponse;
import com.snp.dev.user_management_service.model.User;
import reactor.core.publisher.Mono;

public interface UserService {

    Mono<User> getUserById(String id);
    Mono<User> getUserByUsername(String username);
    Mono<User> getUserByEmail(String email);
    Mono<UserProfileDto> getUserProfile(String userId);
    Mono<UserProfileDto> updateUserProfile(UserProfileDto userProfileDto, String userId);
    Mono<Void> deleteUser(String userId);
    Mono<Void> lockUser(String userId, String adminId);
    Mono<Void> unlockUser(String userId, String adminId);
    Mono<Void> enableUser(String userId, String adminId);
    Mono<Void> disableUser(String userId, String adminId);
    Mono<User> updateUser(User user);
    Mono<PageResponse<UserResponse>> listUsers(int page, int limit);
    Mono<ApiResponse<Boolean>> canEditPortfolio(String userId, String userId1);
}