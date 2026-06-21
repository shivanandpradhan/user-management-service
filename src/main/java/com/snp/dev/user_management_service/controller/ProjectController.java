package com.snp.dev.user_management_service.controller;

import com.snp.dev.user_management_service.dto.ApiResponse;
import com.snp.dev.user_management_service.dto.portfolio.ProjectDTO;
import com.snp.dev.user_management_service.model.CustomUserDetails;
import com.snp.dev.user_management_service.service.ProjectService;
import com.snp.dev.user_management_service.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio/projects")
@RequiredArgsConstructor
@Slf4j
public class ProjectController {

    private final CommonUtil commonUtil;
    private final ProjectService projectService;

    // ==================== Public Endpoints ====================

    @GetMapping("/public")
    public Mono<ApiResponse<List<ProjectDTO>>> getPublicProjects(
            @RequestParam(required = false) String userId) {

        if (userId != null && !userId.isEmpty()) {
            log.info("GET /api/portfolio/projects/public?userId={}", userId);
            return projectService.getPublicProjects(userId);
        } else {
            log.info("GET /api/portfolio/projects/public - Fetching all projects");
            return projectService.getAllPublicProjects();
        }
    }

    @GetMapping("/public/category/{category}")
    public Mono<ApiResponse<List<ProjectDTO>>> getProjectsByCategory(
            @PathVariable String category) {
        log.info("GET /api/portfolio/projects/public/category/{}", category);
        return projectService.getProjectsByCategory(category);
    }

    @GetMapping("/public/featured")
    public Mono<ApiResponse<List<ProjectDTO>>> getFeaturedProjects() {
        log.info("GET /api/portfolio/projects/public/featured");
        return projectService.getFeaturedProjects();
    }

    @GetMapping("/public/{id}")
    public Mono<ApiResponse<ProjectDTO>> getPublicProjectById(@PathVariable String id) {
        log.info("GET /api/portfolio/projects/public/{}", id);
        return projectService.getPublicProjectById(id);
    }

    // ==================== Protected Endpoints ====================

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('PORTFOLIO')")
    public Mono<ApiResponse<List<ProjectDTO>>> getProjects(
            @PathVariable String userId) {
        log.info("GET /api/portfolio/projects/{} by user: {}", userId);
        return projectService.getProjectsByUser(userId);
    }

    @GetMapping("/{userId}/count")
    @PreAuthorize("hasRole('PORTFOLIO')")
    public Mono<ApiResponse<Long>> getProjectCount(@PathVariable String userId) {
        log.info("GET /api/portfolio/projects/{}/count", userId);
        return projectService.getProjectCount(userId);
    }

    @PostMapping
    @PreAuthorize("hasRole('PORTFOLIO')")
    public Mono<ApiResponse<ProjectDTO>> createProject(
            @RequestBody ProjectDTO projectDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = commonUtil.extractUserId(userDetails);
        log.info("POST /api/portfolio/projects by user: {}", userId);
        return projectService.createProject(projectDTO, userId);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PORTFOLIO')")
    public Mono<ApiResponse<ProjectDTO>> updateProject(
            @PathVariable String id,
            @RequestBody ProjectDTO projectDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = commonUtil.extractUserId(userDetails);
        log.info("PUT /api/portfolio/projects/{} by user: {}", id, userId);
        return projectService.updateProject(id, projectDTO, userId);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PORTFOLIO')")
    public Mono<ApiResponse<Void>> deleteProject(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = commonUtil.extractUserId(userDetails);
        log.info("DELETE /api/portfolio/projects/{} by user: {}", id, userId);
        return projectService.deleteProject(id, userId);
    }

    private String extractUserId(UserDetails userDetails) {
        if(userDetails instanceof CustomUserDetails){
            return ((CustomUserDetails) userDetails).getId();
        }
        return userDetails.getUsername();
    }
}