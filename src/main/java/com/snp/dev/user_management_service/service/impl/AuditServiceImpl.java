package com.snp.dev.user_management_service.service.impl;

import com.snp.dev.user_management_service.model.AuditLog;
import com.snp.dev.user_management_service.repository.AuditLogRepository;
import com.snp.dev.user_management_service.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public Mono<AuditLog> logEvent(AuditLog auditLog) {
        return auditLogRepository.save(auditLog);
    }

    @Override
    public Mono<Void> logUserEvent(String userId, String action, String details) {
        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .entityType("USER")
                .entityId(userId)
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .details(Collections.singletonMap("details", details))
                .build();

        return auditLogRepository.save(auditLog).then();
    }

    @Override
    public Mono<Void> logAdminEvent(String adminId, String action, String entityType, String entityId, String details) {
        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .userId(adminId)
                .timestamp(LocalDateTime.now())
                .details(Collections.singletonMap("details", details))
                .build();

        return auditLogRepository.save(auditLog).then();
    }
}
