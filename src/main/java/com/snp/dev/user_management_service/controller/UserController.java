package com.snp.dev.user_management_service.controller;

import com.snp.dev.user_management_service.dto.ApiResponse;
import com.snp.dev.user_management_service.dto.UserProfileDto;
import com.snp.dev.user_management_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public Mono<ResponseEntity<ApiResponse<UserProfileDto>>> getMyProfile(@RequestHeader("X-User-Id") String userId) {
        return userService.getUserProfile(userId)
                .map(profile -> ApiResponse.success(profile))
                .map(ResponseEntity::ok);
    }

    @PutMapping("/me")
    public Mono<ResponseEntity<ApiResponse<UserProfileDto>>> updateMyProfile(
            @RequestBody UserProfileDto userProfileDto,
            @RequestHeader("X-User-Id") String userId) {
        return userService.updateUserProfile(userProfileDto, userId)
                .map(profile -> ApiResponse.success(profile))
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/me")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteMyAccount(@RequestHeader("X-User-Id") String userId) {
        return userService.deleteUser(userId)
                .thenReturn(ResponseEntity.ok(ApiResponse.success(null)));
    }
}
