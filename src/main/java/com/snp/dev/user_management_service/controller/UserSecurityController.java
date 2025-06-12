//package com.snp.dev.user_management_service.controller;
//
//import com.snp.dev.user_management_service.dto.ApiResponse;
//import com.snp.dev.user_management_service.dto.UserSecurityDto;
//import com.snp.dev.user_management_service.model.UserSecurity;
//import com.snp.dev.user_management_service.repository.UserRepository;
//import com.snp.dev.user_management_service.repository.UserSecurityRepository;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import reactor.core.publisher.Mono;
//
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/api/users")
//public class UserSecurityController {
//
//    private final UserSecurityRepository userSecurityRepository;
//    private final UserRepository userRepository;
//
//    public UserSecurityController(UserSecurityRepository userSecurityRepository,
//                                  UserRepository userRepository) {
//        this.userSecurityRepository = userSecurityRepository;
//        this.userRepository = userRepository;
//    }
//
//    @GetMapping("/me/security")
//    public Mono<ResponseEntity<ApiResponse<UserSecurityDto>>> getUserSecurity(
//            @AuthenticationPrincipal String username) {
//        return userRepository.findByUsername(username)
//                .flatMap(user -> userSecurityRepository.findByUserId(user.getId()))
//                .map(security -> ResponseEntity.ok(ApiResponse.success(mapToDto(security))))
//                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
//    }
//
//    private UserSecurityDto mapToDto(UserSecurity security) {
//        return UserSecurityDto.builder()
//                .mfaEnabled(security.isMfaEnabled())
//                .lastPasswordResetDate(security.getLastPasswordResetDate().toString())
//                .failedLoginAttempts(security.getFailedLoginAttempts())
//                .accountLockedUntil(security.getAccountLockedUntil() != null ?
//                        security.getAccountLockedUntil().toString() : null)
////                .activeSessions(security.getActiveSessions().stream()
////                        .map(session -> new SessionDto(
////                                session.getId(),
////                                session.getDeviceInfo(),
////                                session.getIpAddress(),
////                                session.getLastAccessed().toString()
////                        ))
////                        .collect(Collectors.toList()))
//                .build();
//    }
//}
