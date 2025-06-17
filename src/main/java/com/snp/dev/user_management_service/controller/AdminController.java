package com.snp.dev.user_management_service.controller;

import com.snp.dev.user_management_service.dto.ApiResponse;
import com.snp.dev.user_management_service.dto.PageResponse;
import com.snp.dev.user_management_service.dto.UserProfileDto;
import com.snp.dev.user_management_service.dto.UserResponse;
import com.snp.dev.user_management_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @Operation(summary = "List users", description = "Get paginated list of all users")
    @GetMapping
    public Mono<ResponseEntity<ApiResponse<PageResponse<UserResponse>>>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        return userService.listUsers(page, limit)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response)));
    }

    @GetMapping("/{userId}")
    public Mono<ResponseEntity<ApiResponse<UserProfileDto>>> getUserProfile(
            @PathVariable String userId,
            @RequestHeader("X-User-Id") String adminId) {
        return userService.getUserProfile(userId)
                .map(profile -> ApiResponse.success(profile))
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{userId}/lock")
    public Mono<ResponseEntity<ApiResponse<Void>>> lockUser(
            @PathVariable String userId,
            @RequestHeader("X-User-Id") String adminId) {
        return userService.lockUser(userId, adminId)
                .thenReturn(ResponseEntity.ok(ApiResponse.success(null)));
    }

    @PutMapping("/{userId}/unlock")
    public Mono<ResponseEntity<ApiResponse<Void>>> unlockUser(
            @PathVariable String userId,
            @RequestHeader("X-User-Id") String adminId) {
        return userService.unlockUser(userId, adminId)
                .thenReturn(ResponseEntity.ok(ApiResponse.success(null)));
    }

    @PutMapping("/{userId}/enable")
    public Mono<ResponseEntity<ApiResponse<Void>>> enableUser(
            @PathVariable String userId,
            @RequestHeader("X-User-Id") String adminId) {
        return userService.enableUser(userId, adminId)
                .thenReturn(ResponseEntity.ok(ApiResponse.success(null)));
    }

    @PutMapping("/{userId}/disable")
    public Mono<ResponseEntity<ApiResponse<Void>>> disableUser(
            @PathVariable String userId,
            @RequestHeader("X-User-Id") String adminId) {
        return userService.disableUser(userId, adminId)
                .thenReturn(ResponseEntity.ok(ApiResponse.success(null)));
    }
}