package com.snp.dev.user_management_service.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snp.dev.user_management_service.dto.ApiResponse;
import com.snp.dev.user_management_service.dto.portfolio.ResumeDTO;
import com.snp.dev.user_management_service.model.Resume;
import com.snp.dev.user_management_service.repository.ResumeRepository;
import com.snp.dev.user_management_service.service.ResumeService;
import com.snp.dev.user_management_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserService portfolioUserService;
    private final ObjectMapper objectMapper;

    // ==================== Public Method ====================

    @Override
    public Mono<ApiResponse<ResumeDTO>> getPublicResume(String userId) {
        log.info("Fetching public resume for user: {}", userId);

        return resumeRepository.findByUserId(userId)
                .map(this::mapToDTO)
                .<ApiResponse<ResumeDTO>>map(ApiResponse::success)
                .switchIfEmpty(Mono.defer(() ->
                        Mono.just(ApiResponse.<ResumeDTO>error(Collections.singletonList(
                                new ApiResponse.ErrorDetail("NOT_FOUND", "Resume not found for user: " + userId, null, null)
                        )))
                ))
                .onErrorResume(e -> {
                    log.error("Error fetching public resume: {}", e.getMessage());
                    return Mono.just(ApiResponse.<ResumeDTO>error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("FETCH_ERROR", "Failed to fetch resume: " + e.getMessage(), null, null)
                    )));
                });
    }

    // ==================== Protected Methods with Permission Check ====================

    @Override
    public Mono<ApiResponse<ResumeDTO>> getResume(String userId, String loggedInUserId) {
        log.info("Fetching resume for user: {} by user: {}", userId, loggedInUserId);

        return portfolioUserService.canEditPortfolio(userId, loggedInUserId)
                .flatMap(canEditResponse -> {
                    Boolean canEdit = canEditResponse.getData();
                    if (canEdit == null || !canEdit) {
                        return Mono.just(ApiResponse.<ResumeDTO>error(Collections.singletonList(
                                new ApiResponse.ErrorDetail("FORBIDDEN", "You don't have permission to view this resume", null, null)
                        )));
                    }

                    return resumeRepository.findByUserId(userId)
                            .map(this::mapToDTO)
                            .<ApiResponse<ResumeDTO>>map(ApiResponse::success)
                            .switchIfEmpty(Mono.defer(() ->
                                    Mono.just(ApiResponse.<ResumeDTO>error(Collections.singletonList(
                                            new ApiResponse.ErrorDetail("NOT_FOUND", "Resume not found for user: " + userId, null, null)
                                    )))
                            ));
                })
                .onErrorResume(e -> {
                    log.error("Error fetching resume: {}", e.getMessage());
                    return Mono.just(ApiResponse.<ResumeDTO>error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("FETCH_ERROR", "Failed to fetch resume: " + e.getMessage(), null, null)
                    )));
                });
    }

    @Override
    public Mono<ApiResponse<ResumeDTO>> createOrUpdateResume(ResumeDTO resumeDTO, String userId) {
        log.info("Creating/updating resume for user: {}", userId);

        return resumeRepository.findByUserId(userId)
                .flatMap(existingResume -> updateExistingResume(existingResume, resumeDTO))
                .switchIfEmpty(createNewResume(resumeDTO, userId))
                .onErrorResume(throwable -> {
                    log.error("Error saving resume: {}", throwable.getMessage());
                    return Mono.just(ApiResponse.<ResumeDTO>error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("SAVE_ERROR", "Failed to save resume: " + throwable.getMessage(), null, null)
                    )));
                });
    }

    @Override
    public Mono<ApiResponse<ResumeDTO>> updateResume(ResumeDTO resumeDTO, String userId, String loggedInUserId) {
        log.info("Updating resume for user: {} by user: {}", userId, loggedInUserId);

        return portfolioUserService.canEditPortfolio(userId, loggedInUserId)
                .flatMap(canEditResponse -> {
                    Boolean canEdit = canEditResponse.getData();
                    if (canEdit == null || !canEdit) {
                        return Mono.just(ApiResponse.<ResumeDTO>error(Collections.singletonList(
                                new ApiResponse.ErrorDetail("FORBIDDEN", "You don't have permission to update this resume", null, null)
                        )));
                    }

                    return resumeRepository.findByUserId(userId)
                            .flatMap(existingResume -> updateExistingResume(existingResume, resumeDTO))
                            .switchIfEmpty(Mono.defer(() ->
                                    Mono.just(ApiResponse.<ResumeDTO>error(Collections.singletonList(
                                            new ApiResponse.ErrorDetail("NOT_FOUND", "Resume not found for user: " + userId, null, null)
                                    )))
                            ));
                })
                .onErrorResume(throwable -> {
                    log.error("Error updating resume: {}", throwable.getMessage());
                    return Mono.just(ApiResponse.<ResumeDTO>error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("UPDATE_ERROR", "Failed to update resume: " + throwable.getMessage(), null, null)
                    )));
                });
    }

    @Override
    public Mono<ApiResponse<Void>> deleteResume(String userId, String loggedInUserId) {
        log.info("Deleting resume for user: {} by user: {}", userId, loggedInUserId);

        return portfolioUserService.canEditPortfolio(userId, loggedInUserId)
                .flatMap(canEditResponse -> {
                    Boolean canEdit = canEditResponse.getData();
                    if (canEdit == null || !canEdit) {
                        return Mono.just(ApiResponse.<Void>error(Collections.singletonList(
                                new ApiResponse.ErrorDetail("FORBIDDEN", "You don't have permission to delete this resume", null, null)
                        )));
                    }

                    return resumeRepository.findByUserId(userId)
                            .flatMap(resume -> resumeRepository.deleteById(resume.getId())
                                    .thenReturn(ApiResponse.<Void>success(null)))
                            .switchIfEmpty(Mono.defer(() ->
                                    Mono.just(ApiResponse.<Void>error(Collections.singletonList(
                                            new ApiResponse.ErrorDetail("NOT_FOUND", "Resume not found for user: " + userId, null, null)
                                    )))
                            ));
                })
                .onErrorResume(throwable -> {
                    log.error("Error deleting resume: {}", throwable.getMessage());
                    return Mono.just(ApiResponse.<Void>error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("DELETE_ERROR", "Failed to delete resume: " + throwable.getMessage(), null, null)
                    )));
                });
    }

    // ==================== Private Helper Methods with ObjectMapper ====================

    private Mono<ApiResponse<ResumeDTO>> updateExistingResume(Resume existingResume, ResumeDTO resumeDTO) {
        try {
            // Convert ResumeDTO to Resume model using ObjectMapper
            // This will update only the fields present in DTO
            objectMapper.updateValue(existingResume, resumeDTO);

            // Set the updated timestamp
            existingResume.setUpdatedAt(LocalDateTime.now());

            return resumeRepository.save(existingResume)
                    .map(this::mapToDTO)
                    .<ApiResponse<ResumeDTO>>map(ApiResponse::success)
                    .onErrorResume(e -> {
                        log.error("Error updating existing resume: {}", e.getMessage());
                        return Mono.just(ApiResponse.<ResumeDTO>error(Collections.singletonList(
                                new ApiResponse.ErrorDetail("UPDATE_ERROR", "Failed to update resume: " + e.getMessage(), null, null)
                        )));
                    });
        } catch (Exception e) {
            log.error("Error mapping DTO to entity: {}", e.getMessage());
            return Mono.just(ApiResponse.<ResumeDTO>error(Collections.singletonList(
                    new ApiResponse.ErrorDetail("UPDATE_ERROR", "Failed to update resume: " + e.getMessage(), null, null)
            )));
        }
    }

    private Mono<ApiResponse<ResumeDTO>> createNewResume(ResumeDTO resumeDTO, String userId) {
        try {
            // Convert ResumeDTO to Resume model using ObjectMapper
            Resume resume = objectMapper.convertValue(resumeDTO, Resume.class);

            // Set user-specific fields
            resume.setUserId(userId);
            resume.setViewCount(0);
            resume.setUpdatedAt(LocalDateTime.now());

            return resumeRepository.save(resume)
                    .map(this::mapToDTO)
                    .<ApiResponse<ResumeDTO>>map(ApiResponse::success)
                    .onErrorResume(e -> {
                        log.error("Error creating new resume: {}", e.getMessage());
                        return Mono.just(ApiResponse.<ResumeDTO>error(Collections.singletonList(
                                new ApiResponse.ErrorDetail("CREATE_ERROR", "Failed to create resume: " + e.getMessage(), null, null)
                        )));
                    });
        } catch (Exception e) {
            log.error("Error mapping DTO to entity: {}", e.getMessage());
            return Mono.just(ApiResponse.<ResumeDTO>error(Collections.singletonList(
                    new ApiResponse.ErrorDetail("CREATE_ERROR", "Failed to create resume: " + e.getMessage(), null, null)
            )));
        }
    }

    // ==================== Model to DTO Mapping Methods ====================

    private ResumeDTO mapToDTO(Resume resume) {
        try {
            return objectMapper.convertValue(resume, ResumeDTO.class);
        } catch (Exception e) {
            log.error("Error mapping entity to DTO: {}", e.getMessage());
            // Return empty DTO or throw
            return ResumeDTO.builder().build();
        }
    }
}