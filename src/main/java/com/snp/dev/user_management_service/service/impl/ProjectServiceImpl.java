package com.snp.dev.user_management_service.service.impl;

import com.snp.dev.user_management_service.dto.ApiResponse;
import com.snp.dev.user_management_service.dto.portfolio.ProjectDTO;
import com.snp.dev.user_management_service.model.Project;
import com.snp.dev.user_management_service.repository.ProjectRepository;
import com.snp.dev.user_management_service.service.ProjectService;
import com.snp.dev.user_management_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserService portfolioUserService;

    @Override
    public Mono<ApiResponse<ProjectDTO>> createProject(ProjectDTO projectDTO, String userId) {
        log.info("Creating project for user: {}", userId);

        return portfolioUserService.canEditPortfolio(userId, userId)
                .flatMap(canEdit -> {
                    Boolean canEditData = canEdit.getData();
                    if (canEditData == null || !canEditData) {
                        return Mono.just(ApiResponse.<ProjectDTO>error(Collections.singletonList(
                                new ApiResponse.ErrorDetail("FORBIDDEN", "You don't have permission to create projects", null, null)
                        )));
                    }

                    Project project = Project.builder()
                            .userId(userId)
                            .title(projectDTO.getTitle())
                            .description(projectDTO.getDescription())
                            .longDescription(projectDTO.getLongDescription())
                            .githubUrl(projectDTO.getGithubUrl())
                            .deployedUri(projectDTO.getDeployedUri())
                            .tools(projectDTO.getTools())
                            .category(projectDTO.getCategory())
                            .featured(projectDTO.isFeatured())
                            .coverImageUrl(projectDTO.getCoverImageUrl())
                            .images(projectDTO.getImages())
                            .challenges(projectDTO.getChallenges())
                            .solutions(projectDTO.getSolutions())
                            .date(projectDTO.getDate())
                            .viewCount(0)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();

                    return projectRepository.save(project)
                            .map(saved -> ApiResponse.<ProjectDTO>success(mapToDTO(saved)))
                            .onErrorResume(e -> {
                                log.error("Error creating project: {}", e.getMessage());
                                return Mono.just(ApiResponse.<ProjectDTO>error(Collections.singletonList(
                                        new ApiResponse.ErrorDetail("CREATE_ERROR", "Failed to create project: " + e.getMessage(), null, null)
                                )));
                            });
                })
                .onErrorResume(e -> {
                    log.error("Error in createProject: {}", e.getMessage());
                    return Mono.just(ApiResponse.<ProjectDTO>error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("CREATE_ERROR", "Failed to create project: " + e.getMessage(), null, null)
                    )));
                });
    }

    @Override
    public Mono<ApiResponse<ProjectDTO>> updateProject(String id, ProjectDTO projectDTO, String userId) {
        log.info("Updating project: {} for user: {}", id, userId);

        return projectRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Project not found")))
                .flatMap(existingProject -> {
                    if (!existingProject.getUserId().equals(userId)) {
                        return portfolioUserService.canEditPortfolio(existingProject.getUserId(), userId)
                                .flatMap(canEdit -> {
                                    Boolean canEditData = canEdit.getData();
                                    if (canEditData == null || !canEditData) {
                                        return Mono.just(ApiResponse.<ProjectDTO>error(Collections.singletonList(
                                                new ApiResponse.ErrorDetail("FORBIDDEN", "You don't have permission to edit this project", null, null)
                                        )));
                                    }
                                    return proceedWithUpdate(existingProject, projectDTO);
                                });
                    }
                    return proceedWithUpdate(existingProject, projectDTO);
                })
                .onErrorResume(e -> {
                    log.error("Error updating project: {}", e.getMessage());
                    return Mono.just(ApiResponse.<ProjectDTO>error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("UPDATE_ERROR", "Failed to update project: " + e.getMessage(), null, null)
                    )));
                });
    }

    private Mono<ApiResponse<ProjectDTO>> proceedWithUpdate(Project project, ProjectDTO projectDTO) {
        project.setTitle(projectDTO.getTitle());
        project.setDescription(projectDTO.getDescription());
        project.setLongDescription(projectDTO.getLongDescription());
        project.setGithubUrl(projectDTO.getGithubUrl());
        project.setDeployedUri(projectDTO.getDeployedUri());
        project.setTools(projectDTO.getTools());
        project.setCategory(projectDTO.getCategory());
        project.setFeatured(projectDTO.isFeatured());
        project.setCoverImageUrl(projectDTO.getCoverImageUrl());
        project.setImages(projectDTO.getImages());
        project.setChallenges(projectDTO.getChallenges());
        project.setSolutions(projectDTO.getSolutions());
        project.setDate(projectDTO.getDate());
        project.setUpdatedAt(LocalDateTime.now());

        return projectRepository.save(project)
                .map(saved -> ApiResponse.<ProjectDTO>success(mapToDTO(saved)))
                .onErrorResume(e -> {
                    log.error("Error saving updated project: {}", e.getMessage());
                    return Mono.just(ApiResponse.<ProjectDTO>error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("SAVE_ERROR", "Failed to save project: " + e.getMessage(), null, null)
                    )));
                });
    }

    @Override
    public Mono<ApiResponse<Void>> deleteProject(String id, String userId) {
        log.info("Deleting project: {} for user: {}", id, userId);

        return projectRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Project not found")))
                .flatMap(project -> {
                    if (!project.getUserId().equals(userId)) {
                        return portfolioUserService.canEditPortfolio(project.getUserId(), userId)
                                .flatMap(canEditResponse -> {
                                    Boolean canEdit = canEditResponse.getData();
                                    if (canEdit == null || !canEdit) {
                                        return Mono.just(ApiResponse.<Void>error(Collections.singletonList(
                                                new ApiResponse.ErrorDetail("FORBIDDEN", "You don't have permission to delete this project", null, null)
                                        )));
                                    }
                                    return projectRepository.deleteById(id)
                                            .thenReturn(ApiResponse.<Void>success(null));
                                });
                    }
                    return projectRepository.deleteById(id)
                            .thenReturn(ApiResponse.<Void>success(null));
                })
                .onErrorResume(e -> {
                    log.error("Error deleting project: {}", e.getMessage());
                    return Mono.just(ApiResponse.<Void>error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("DELETE_ERROR", "Failed to delete project: " + e.getMessage(), null, null)
                    )));
                });
    }

    @Override
    public Mono<ApiResponse<ProjectDTO>> getProjectById(String id) {
        log.info("Fetching project by id: {}", id);

        return projectRepository.findById(id)
                .flatMap(project -> {
                    project.setViewCount(project.getViewCount() + 1);
                    return projectRepository.save(project);
                })
                .map(this::mapToDTO)
                .<ApiResponse<ProjectDTO>>map(ApiResponse::success)
                .switchIfEmpty(Mono.defer(() ->
                        Mono.just(ApiResponse.<ProjectDTO>error(Collections.singletonList(
                                new ApiResponse.ErrorDetail("NOT_FOUND", "Project not found with id: " + id, null, null)
                        )))
                ))
                .onErrorResume(e -> {
                    log.error("Error fetching project: {}", e.getMessage());
                    return Mono.just(ApiResponse.<ProjectDTO>error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("FETCH_ERROR", "Failed to fetch project: " + e.getMessage(), null, null)
                    )));
                });
    }

    @Override
    public Mono<ApiResponse<List<ProjectDTO>>> getProjectsByUser(String userId) {
        log.info("Fetching all projects for user: {}", userId);

        return projectRepository.findByUserId(userId)
                .map(this::mapToDTO)
                .collectList()
                .<ApiResponse<List<ProjectDTO>>>map(ApiResponse::success)
                .defaultIfEmpty(ApiResponse.success(Collections.emptyList()))
                .onErrorResume(e -> {
                    log.error("Error fetching projects: {}", e.getMessage());
                    return Mono.just(ApiResponse.<List<ProjectDTO>>error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("FETCH_ERROR", "Failed to fetch projects: " + e.getMessage(), null, null)
                    )));
                });
    }

    // ==================== NEW METHODS ====================

    @Override
    public Mono<ApiResponse<List<ProjectDTO>>> getFeaturedProjects() {
        log.info("Fetching all featured projects");

        return projectRepository.findByFeaturedTrue()
                .map(this::mapToDTO)
                .collectList()
                .<ApiResponse<List<ProjectDTO>>>map(ApiResponse::success)
                .defaultIfEmpty(ApiResponse.success(Collections.emptyList()))
                .onErrorResume(e -> {
                    log.error("Error fetching featured projects: {}", e.getMessage());
                    return Mono.just(ApiResponse.<List<ProjectDTO>>error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("FETCH_ERROR", "Failed to fetch featured projects: " + e.getMessage(), null, null)
                    )));
                });
    }

    @Override
    public Mono<ApiResponse<List<ProjectDTO>>> getProjectsByCategory(String category) {
        log.info("Fetching projects by category: {}", category);

        return projectRepository.findByCategory(category)
                .map(this::mapToDTO)
                .collectList()
                .<ApiResponse<List<ProjectDTO>>>map(ApiResponse::success)
                .defaultIfEmpty(ApiResponse.success(Collections.emptyList()))
                .onErrorResume(e -> {
                    log.error("Error fetching projects by category: {}", e.getMessage());
                    return Mono.just(ApiResponse.<List<ProjectDTO>>error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("FETCH_ERROR", "Failed to fetch projects by category: " + e.getMessage(), null, null)
                    )));
                });
    }

    @Override
    public Mono<ApiResponse<List<ProjectDTO>>> getPublicProjects(String userId) {
        log.info("Fetching public projects for user: {}", userId);

        return projectRepository.findByUserId(userId)
                .map(this::mapToDTO)
                .collectList()
                .<ApiResponse<List<ProjectDTO>>>map(ApiResponse::success)
                .defaultIfEmpty(ApiResponse.success(Collections.emptyList()))
                .onErrorResume(e -> {
                    log.error("Error fetching public projects: {}", e.getMessage());
                    return Mono.just(ApiResponse.<List<ProjectDTO>>error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("FETCH_ERROR", "Failed to fetch public projects: " + e.getMessage(), null, null)
                    )));
                });
    }

    @Override
    public Mono<ApiResponse<ProjectDTO>> getPublicProjectById(String id) {
        return getProjectById(id);
    }

    @Override
    public Mono<ApiResponse<Long>> getProjectCount(String userId) {
        log.info("Counting projects for user: {}", userId);

        return projectRepository.countByUserId(userId)
                .<ApiResponse<Long>>map(ApiResponse::success)
                .onErrorResume(e -> {
                    log.error("Error counting projects: {}", e.getMessage());
                    return Mono.just(ApiResponse.<Long>error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("COUNT_ERROR", "Failed to count projects: " + e.getMessage(), null, null)
                    )));
                });
    }

    @Override
    public Mono<ApiResponse<List<ProjectDTO>>> getAllPublicProjects() {
        log.info("Fetching all public projects");

        return projectRepository.findAll()
                .map(this::mapToDTO)
                .collectList()
                .<ApiResponse<List<ProjectDTO>>>map(ApiResponse::success)
                .defaultIfEmpty(ApiResponse.success(Collections.emptyList()))
                .onErrorResume(e -> {
                    log.error("Error fetching all projects: {}", e.getMessage());
                    return Mono.just(ApiResponse.<List<ProjectDTO>>error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("FETCH_ERROR", "Failed to fetch projects: " + e.getMessage(), null, null)
                    )));
                });
    }

    // ==================== HELPER METHODS ====================

    private ProjectDTO mapToDTO(Project project) {
        if (project == null) {
            return null;
        }

        return ProjectDTO.builder()
                .id(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .longDescription(project.getLongDescription())
                .githubUrl(project.getGithubUrl())
                .deployedUri(project.getDeployedUri())
                .tools(project.getTools())
                .category(project.getCategory())
                .featured(project.isFeatured())
                .coverImageUrl(project.getCoverImageUrl())
                .images(project.getImages())
                .challenges(project.getChallenges())
                .solutions(project.getSolutions())
                .date(project.getDate())
                .build();
    }
}