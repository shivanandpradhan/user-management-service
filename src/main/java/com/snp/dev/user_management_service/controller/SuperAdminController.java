package com.snp.dev.user_management_service.controller;

import com.snp.dev.user_management_service.dto.ApiResponse;
import com.snp.dev.user_management_service.dto.PageResponse;
import com.snp.dev.user_management_service.dto.UserResponse;
import com.snp.dev.user_management_service.repository.RoleRepository;
import com.snp.dev.user_management_service.service.AuditService;
import com.snp.dev.user_management_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Collections;

@RestController
@RequestMapping("/api/super-admin/users")
@RequiredArgsConstructor
public class SuperAdminController {

    private final UserService userService;
    private final RoleRepository roleRepository;
    private final AuditService auditService;

    @Operation(summary = "List users", description = "Get paginated list of all users")
    @GetMapping
    public Mono<ResponseEntity<ApiResponse<PageResponse<UserResponse>>>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        return userService.listUsers(page, limit)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response)));
    }

    @PostMapping("/{userId}/promote-to-admin")
    public Mono<ResponseEntity<ApiResponse<String>>> promoteToAdmin(
            @PathVariable String userId,
            @RequestHeader("X-User-Id") String superAdminId) {
        return roleRepository.findByName("ROLE_ADMIN")
                .flatMap(role -> userService.getUserById(userId)
                        .flatMap(user -> {
                            user.getRoles().add(role.getName());
                            return userService.updateUser(user)
                                    .then(auditService.logAdminEvent(
                                            superAdminId,
                                            "USER_PROMOTED",
                                            "USER",
                                            userId,
                                            "User promoted to admin"
                                    ))
                                    .thenReturn(ResponseEntity.ok(ApiResponse.success("User promoted to admin")));
                        }))
                .switchIfEmpty(Mono.just(ResponseEntity.badRequest().body(ApiResponse.error(
                        Collections.singletonList(new ApiResponse.ErrorDetail("ROLE_NOT_FOUND", "Admin role not found", null, null))
                ))));
    }

    @PostMapping("/{userId}/demote-from-admin")
    public Mono<ResponseEntity<ApiResponse<String>>> demoteFromAdmin(
            @PathVariable String userId,
            @RequestHeader("X-User-Id") String superAdminId) {
        return roleRepository.findByName("ROLE_ADMIN")
                .flatMap(role -> userService.getUserById(userId)
                        .flatMap(user -> {
                            user.getRoles().remove(role.getName());
                            return userService.updateUser(user)
                                    .then(auditService.logAdminEvent(
                                            superAdminId,
                                            "USER_DEMOTED",
                                            "USER",
                                            userId,
                                            "User demoted from admin"
                                    ))
                                    .thenReturn(ResponseEntity.ok(ApiResponse.success("User demoted from admin")));
                        }))
                .switchIfEmpty(Mono.just(ResponseEntity.badRequest().body(ApiResponse.error(
                        Collections.singletonList(new ApiResponse.ErrorDetail("ROLE_NOT_FOUND", "Admin role not found", null, null))
                ))));
    }

    @PostMapping("/{userId}/promote-to-super-admin")
    public Mono<ResponseEntity<ApiResponse<String>>> promoteToSuperAdmin(
            @PathVariable String userId,
            @RequestHeader("X-User-Id") String superAdminId) {
        return roleRepository.findByName("ROLE_SUPER_ADMIN")
                .flatMap(role -> userService.getUserById(userId)
                        .flatMap(user -> {
                            user.getRoles().add(role.getName());
                            return userService.updateUser(user)
                                    .then(auditService.logAdminEvent(
                                            superAdminId,
                                            "USER_PROMOTED",
                                            "USER",
                                            userId,
                                            "User promoted to super admin"
                                    ))
                                    .thenReturn(ResponseEntity.ok(ApiResponse.success("User promoted to super admin")));
                        }))
                .switchIfEmpty(Mono.just(ResponseEntity.badRequest().body(ApiResponse.error(
                        Collections.singletonList(new ApiResponse.ErrorDetail("ROLE_NOT_FOUND", "Super admin role not found", null, null))
                ))));
    }

    @PostMapping("/{userId}/demote-from-super-admin")
    public Mono<ResponseEntity<ApiResponse<String>>> demoteFromSuperAdmin(
            @PathVariable String userId,
            @RequestHeader("X-User-Id") String superAdminId) {
        // Prevent demoting yourself
        if (userId.equals(superAdminId)) {
            return Mono.just(ResponseEntity.badRequest().body(ApiResponse.error(
                    Collections.singletonList(new ApiResponse.ErrorDetail("SELF_DEMOTION", "Cannot demote yourself", null, null))
            )));
        }

        return roleRepository.findByName("ROLE_SUPER_ADMIN")
                .flatMap(role -> userService.getUserById(userId)
                        .flatMap(user -> {
                            user.getRoles().remove(role.getName());
                            return userService.updateUser(user)
                                    .then(auditService.logAdminEvent(
                                            superAdminId,
                                            "USER_DEMOTED",
                                            "USER",
                                            userId,
                                            "User demoted from super admin"
                                    ))
                                    .thenReturn(ResponseEntity.ok(ApiResponse.success("User demoted from super admin")));
                        }))
                .switchIfEmpty(Mono.just(ResponseEntity.badRequest().body(ApiResponse.error(
                        Collections.singletonList(new ApiResponse.ErrorDetail("ROLE_NOT_FOUND", "Super admin role not found", null, null))
                ))));
    }
}

