package com.snp.dev.user_management_service.config;


import com.snp.dev.user_management_service.model.*;
import com.snp.dev.user_management_service.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initializeData(
            UserRepository userRepository,
            UserSecurityRepository userSecurityRepository,
            UserProfileRepository userProfileRepository,
            UserMetadataRepository userMetadataRepository,
            RoleRepository roleRepository,
            @Value("${app.super-admin.initial-email}") String superAdminEmail,
            @Value("${app.super-admin.initial-password}") String superAdminPassword,
            PasswordEncoder passwordEncoder) {

        return args -> {
            // Define roles
            Role userRole = Role.builder()
                    .name("ROLE_USER")
                    .description("Regular user role")
                    .systemRole(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            Role adminRole = Role.builder()
                    .name("ROLE_ADMIN")
                    .description("Admin role")
                    .systemRole(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            Role superAdminRole = Role.builder()
                    .name("ROLE_SUPER_ADMIN")
                    .description("Super admin role")
                    .systemRole(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            // Insert roles if they do not exist
            roleRepository.findByName("ROLE_USER")
                    .switchIfEmpty(roleRepository.save(userRole))
                    .subscribe();

            roleRepository.findByName("ROLE_ADMIN")
                    .switchIfEmpty(roleRepository.save(adminRole))
                    .subscribe();

            roleRepository.findByName("ROLE_SUPER_ADMIN")
                    .switchIfEmpty(roleRepository.save(superAdminRole))
                    .subscribe();

            // Insert super admin user if not exists
            userRepository.findByEmail(superAdminEmail)
                    .switchIfEmpty(Mono.defer(() -> {
                                User superAdmin = User.builder()
                                        .username("superadmin")
                                        .email(superAdminEmail)
                                        .password(passwordEncoder.encode(superAdminPassword))
                                        .enabled(true)
                                        .accountNonExpired(true)
                                        .accountNonLocked(true)
                                        .credentialsNonExpired(true)
                                        .roles(new HashSet<>(Collections.singletonList(superAdminRole.getName())))
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .createdBy("system")
                                        .updatedBy("system")
                                        .build();
                                return userRepository.save(superAdmin)
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
                    }))
                    .subscribe();

            log.info("Initial data initialized successfully");
        };
    }
}
