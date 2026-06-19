package com.snp.dev.user_management_service.service;

import com.snp.dev.user_management_service.dto.ApiResponse;
import com.snp.dev.user_management_service.dto.portfolio.ProjectDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ProjectService {
    Mono<ApiResponse<ProjectDTO>> createProject(ProjectDTO projectDTO, String userId);

    Mono<ApiResponse<ProjectDTO>> updateProject(String id, ProjectDTO projectDTO, String userId);

    Mono<ApiResponse<Void>> deleteProject(String id, String userId);

    Mono<ApiResponse<ProjectDTO>> getProjectById(String id);

    Mono<ApiResponse<List<ProjectDTO>>> getProjectsByUser(String userId);

    Mono<ApiResponse<List<ProjectDTO>>> getPublicProjects(String userId);

    Mono<ApiResponse<ProjectDTO>> getPublicProjectById(String id);

    Mono<ApiResponse<Long>> getProjectCount(String userId);

    Mono<ApiResponse<List<ProjectDTO>>> getAllPublicProjects();

    Mono<ApiResponse<List<ProjectDTO>>> getFeaturedProjects();

    Mono<ApiResponse<List<ProjectDTO>>> getProjectsByCategory(String category);
}
