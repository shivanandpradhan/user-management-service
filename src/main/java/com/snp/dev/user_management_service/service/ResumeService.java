package com.snp.dev.user_management_service.service;

import com.snp.dev.user_management_service.dto.ApiResponse;
import com.snp.dev.user_management_service.dto.portfolio.ResumeDTO;
import reactor.core.publisher.Mono;

public interface ResumeService {

    // Public endpoint
    Mono<ApiResponse<ResumeDTO>> getPublicResume(String userId);

    // Protected endpoints - All include loggedInUserId for permission check
    Mono<ApiResponse<ResumeDTO>> getResume(String userId, String loggedInUserId);
    Mono<ApiResponse<ResumeDTO>> createOrUpdateResume(ResumeDTO resumeDTO, String userId);
    Mono<ApiResponse<ResumeDTO>> updateResume(ResumeDTO resumeDTO, String userId, String loggedInUserId);
    Mono<ApiResponse<Void>> deleteResume(String userId, String loggedInUserId);
}