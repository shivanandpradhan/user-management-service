// config/DataLoader.java
package com.snp.dev.user_management_service.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snp.dev.user_management_service.model.*;
import com.snp.dev.user_management_service.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class DataLoader implements CommandLineRunner {

    private final ProjectRepository projectRepository;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserSecurityRepository userSecurityRepository;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final PasswordEncoder passwordEncoder;

    // ✅ Portfolio User Config
    @Value("${app.data.portfolio-user.enabled:true}")
    private boolean portfolioUserEnabled;

    @Value("${app.data.portfolio-user.id:}")
    private String portfolioUserId;

    @Value("${app.data.portfolio-user.username:portfolio-user}")
    private String portfolioUsername;

    @Value("${app.data.portfolio-user.email:portfolio@example.com}")
    private String portfolioEmail;

    @Value("${app.data.portfolio-user.password:Portfolio@123}")
    private String portfolioPassword;

    @Value("${app.data.portfolio-user.roles:ROLE_PORTFOLIO,ROLE_USER}")
    private List<String> portfolioRoles;

    @Value("${app.data.portfolio-user.enabled:true}")
    private boolean portfolioEnabled;

    @Value("${app.data.portfolio-user.account-non-expired:true}")
    private boolean portfolioAccountNonExpired;

    @Value("${app.data.portfolio-user.account-non-locked:true}")
    private boolean portfolioAccountNonLocked;

    @Value("${app.data.portfolio-user.credentials-non-expired:true}")
    private boolean portfolioCredentialsNonExpired;

    // ✅ Data Initialization Config
    @Value("${app.data.initialize.projects:true}")
    private boolean initializeProjects;

    @Value("${app.data.initialize.resume:true}")
    private boolean initializeResume;

    @Value("${app.data.initialize.force:false}")
    private boolean forceInitialize;

    @Value("${app.data.projects-file:data/projects.json}")
    private String projectsFilePath;

    @Value("${app.data.resume-file:data/resume.json}")
    private String resumeFilePath;

    @Override
    public void run(String... args) {
        log.info("🚀 Starting data initialization...");

        // ✅ 1. First, create the portfolio user
        if (portfolioUserEnabled) {
            createPortfolioUser().subscribe(
                    user -> log.info("✅ Portfolio user created/verified: {}", user.getUsername()),
                    error -> log.error("❌ Failed to create portfolio user: {}", error.getMessage())
            );
        } else {
            log.info("⏭️ Portfolio user creation disabled");
        }

        // ✅ 2. Then load projects (using the portfolio user ID)
        if (initializeProjects) {
            loadProjects().subscribe(
                    count -> log.info("✅ Projects loaded: {} records", count),
                    error -> log.error("❌ Failed to load projects: {}", error.getMessage())
            );
        } else {
            log.info("⏭️ Projects initialization disabled");
        }

        // ✅ 3. Then load resume (using the portfolio user ID)
        if (initializeResume) {
            loadResume().subscribe(
                    count -> log.info("✅ Resume loaded: {} record", count),
                    error -> log.error("❌ Failed to load resume: {}", error.getMessage())
            );
        } else {
            log.info("⏭️ Resume initialization disabled");
        }
    }

    // ==================== Create Portfolio User ====================

    private Mono<User> createPortfolioUser() {
        String userId = portfolioUserId != null && !portfolioUserId.isEmpty()
                ? portfolioUserId
                : java.util.UUID.randomUUID().toString();

        return userRepository.findById(userId)
                .flatMap(existingUser -> {
                    log.info("📁 Portfolio user already exists with ID: {}", userId);
                    return Mono.just(existingUser);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("👤 Creating portfolio user with ID: {}", userId);

                    User user = new User();
                    user.setId(userId);
                    user.setUsername(portfolioUsername);
                    user.setEmail(portfolioEmail);
                    user.setPassword(passwordEncoder.encode(portfolioPassword));
                    user.setEnabled(portfolioEnabled);
                    user.setAccountNonExpired(portfolioAccountNonExpired);
                    user.setAccountNonLocked(portfolioAccountNonLocked);
                    user.setCredentialsNonExpired(portfolioCredentialsNonExpired);
                    user.setRoles(new HashSet<>(portfolioRoles));
                    user.setCreatedAt(LocalDateTime.now());
                    user.setUpdatedAt(LocalDateTime.now());

                    return userRepository.save(user)
                            .flatMap(savedUser -> {
                                log.info("✅ Portfolio user saved: {}", savedUser.getUsername());

                                UserProfile userProfile = new UserProfile();
                                userProfile.setUserId(savedUser.getId());
                                userProfile.setFirstName(portfolioUsername);
                                userProfile.setLastName("");
                                userProfile.setCreatedAt(LocalDateTime.now());
                                userProfile.setUpdatedAt(LocalDateTime.now());

                                UserSecurity userSecurity = new UserSecurity();
                                userSecurity.setUserId(savedUser.getId());
                                userSecurity.setMfaEnabled(false);
                                userSecurity.setOtpLoginEnabled(false);
                                userSecurity.setFailedLoginAttempts(0);
                                userSecurity.setPasswordResetRequired(false);
                                userSecurity.setCreatedAt(LocalDateTime.now());
                                userSecurity.setUpdatedAt(LocalDateTime.now());

                                return Mono.zip(
                                        userProfileRepository.save(userProfile),
                                        userSecurityRepository.save(userSecurity)
                                ).thenReturn(savedUser);
                            });
                }));
    }

    // ==================== Get Portfolio User ID ====================

    private Mono<String> getPortfolioUserId() {
        String userId = portfolioUserId != null && !portfolioUserId.isEmpty()
                ? portfolioUserId
                : "portfolio-user-id";

        return userRepository.findById(userId)
                .map(User::getId)
                .switchIfEmpty(Mono.just(userId));
    }

    // ==================== Load Projects ====================

    private Mono<Long> loadProjects() {
        return getPortfolioUserId()
                .flatMap(portfolioUserId ->
                        projectRepository.count()
                                .flatMap(count -> {
                                    if (count > 0 && !forceInitialize) {
                                        log.info("📁 Projects already exist ({}), skipping initialization", count);
                                        return Mono.just(count);
                                    }

                                    if (forceInitialize) {
                                        log.info("🔄 Force initialization: clearing existing projects...");
                                        return projectRepository.deleteAll()
                                                .then(loadProjectsFromJson(portfolioUserId)
                                                        .flatMap(project -> projectRepository.save(project))  // ✅ Fix: save one by one
                                                        .count());
                                    }

                                    log.info("📂 Loading projects from JSON: {}", projectsFilePath);
                                    return loadProjectsFromJson(portfolioUserId)
                                            .flatMap(project -> projectRepository.save(project))  // ✅ Fix: save one by one
                                            .count();
                                })
                );
    }

    private Flux<Project> loadProjectsFromJson(String portfolioUserId) {
        try {
            Resource resource = resourceLoader.getResource("classpath:" + projectsFilePath);
            if (!resource.exists()) {
                log.error("❌ Projects file not found: {}", projectsFilePath);
                return Flux.empty();
            }

            InputStream inputStream = resource.getInputStream();
            Map<String, List<Project>> wrapper = objectMapper.readValue(
                    inputStream,
                    new TypeReference<Map<String, List<Project>>>() {}
            );

            List<Project> projects = wrapper.get("projects");

            projects.forEach(project -> {
                if (project.getUserId() == null || project.getUserId().isEmpty()) {
                    project.setUserId(portfolioUserId);
                }
                if (project.getCreatedAt() == null) {
                    project.setCreatedAt(LocalDateTime.now());
                }
                if (project.getUpdatedAt() == null) {
                    project.setUpdatedAt(LocalDateTime.now());
                }
                if (project.getViewCount() == 0) {
                    project.setViewCount(0);
                }
            });

            log.info("📄 Found {} projects in JSON file", projects.size());
            return Flux.fromIterable(projects);
        } catch (Exception e) {
            log.error("❌ Failed to load projects from JSON: {}", e.getMessage());
            return Flux.empty();
        }
    }

    // ==================== Load Resume ====================

    private Mono<Long> loadResume() {
        return getPortfolioUserId()
                .flatMap(portfolioUserId ->
                        resumeRepository.count()
                                .flatMap(count -> {
                                    if (count > 0 && !forceInitialize) {
                                        log.info("📁 Resume already exists, skipping initialization");
                                        return Mono.just(count);
                                    }

                                    if (forceInitialize) {
                                        log.info("🔄 Force initialization: clearing existing resume...");
                                        return resumeRepository.deleteAll()
                                                .then(loadResumeFromJson(portfolioUserId)
                                                        .flatMap(resume -> resumeRepository.save(resume))  // ✅ Fix: save correctly
                                                        .map(resume -> 1L)
                                                        .defaultIfEmpty(0L));
                                    }

                                    log.info("📂 Loading resume from JSON: {}", resumeFilePath);
                                    return loadResumeFromJson(portfolioUserId)
                                            .flatMap(resume -> resumeRepository.save(resume))  // ✅ Fix: save correctly
                                            .map(resume -> 1L)
                                            .defaultIfEmpty(0L);
                                })
                );
    }

    private Mono<Resume> loadResumeFromJson(String portfolioUserId) {
        try {
            Resource resource = resourceLoader.getResource("classpath:" + resumeFilePath);
            if (!resource.exists()) {
                log.error("❌ Resume file not found: {}", resumeFilePath);
                return Mono.empty();
            }

            InputStream inputStream = resource.getInputStream();
            Resume resume = objectMapper.readValue(inputStream, Resume.class);

            if (resume != null) {
                resume.setUserId(portfolioUserId);
                if (resume.getCreatedAt() == null) {
                    resume.setCreatedAt(LocalDateTime.now());
                }
                if (resume.getUpdatedAt() == null) {
                    resume.setUpdatedAt(LocalDateTime.now());
                }
                log.info("📄 Found resume for: {}",
                        resume.getPersonalInfo() != null ? resume.getPersonalInfo().getName() : "Unknown");
            }

            return Mono.justOrEmpty(resume);
        } catch (Exception e) {
            log.error("❌ Failed to load resume from JSON: {}", e.getMessage());
            return Mono.empty();
        }
    }
}