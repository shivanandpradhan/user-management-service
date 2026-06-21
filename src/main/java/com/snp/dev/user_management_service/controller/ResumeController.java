package com.snp.dev.user_management_service.controller;

import com.snp.dev.user_management_service.dto.ApiResponse;
import com.snp.dev.user_management_service.dto.portfolio.ResumeDTO;
import com.snp.dev.user_management_service.service.ResumeService;
import com.snp.dev.user_management_service.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/portfolio/resume")
@RequiredArgsConstructor
@Slf4j
public class ResumeController {

    private final ResumeService resumeService;
    private final CommonUtil commonUtil;

    // ==================== Public Endpoints ====================

    @GetMapping("/public/{userId}")
    public Mono<ApiResponse<ResumeDTO>> getPublicResume(@PathVariable String userId) {
        log.info("GET /api/portfolio/resume/public/{}", userId);
        return resumeService.getPublicResume(userId);
    }

    // ==================== Protected Endpoints ====================

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('PORTFOLIO')")
    public Mono<ApiResponse<ResumeDTO>> getResume(
            @PathVariable String userId,
            @AuthenticationPrincipal UserDetails userDetails) {

        String loggedInUserId = commonUtil.extractUserId(userDetails);
        log.info("GET /api/portfolio/resume/{} by user: {}", userId, loggedInUserId);
        return resumeService.getResume(userId, loggedInUserId);
    }

    @PostMapping
    @PreAuthorize("hasRole('PORTFOLIO')")
    public Mono<ApiResponse<ResumeDTO>> createOrUpdateResume(
            @RequestBody ResumeDTO resumeDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        String userId = commonUtil.extractUserId(userDetails);
        log.info("POST /api/portfolio/resume by user: {}", userId);
        return resumeService.createOrUpdateResume(resumeDTO, userId);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('PORTFOLIO')")
    public Mono<ApiResponse<ResumeDTO>> updateResume(
            @PathVariable String userId,
            @RequestBody ResumeDTO resumeDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        String loggedInUserId = commonUtil.extractUserId(userDetails);
        log.info("PUT /api/portfolio/resume/{} by user: {}", userId, loggedInUserId);
        return resumeService.updateResume(resumeDTO, userId, loggedInUserId);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('PORTFOLIO')")
    public Mono<ApiResponse<Void>> deleteResume(
            @PathVariable String userId,
            @AuthenticationPrincipal UserDetails userDetails) {

        String loggedInUserId = commonUtil.extractUserId(userDetails);
        log.info("DELETE /api/portfolio/resume/{} by user: {}", userId, loggedInUserId);
        return resumeService.deleteResume(userId, loggedInUserId);
    }
}