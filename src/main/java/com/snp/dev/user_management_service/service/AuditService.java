package com.snp.dev.user_management_service.service;

import com.snp.dev.user_management_service.model.AuditLog;
import reactor.core.publisher.Mono;

public interface AuditService {

    Mono<AuditLog> logEvent(AuditLog auditLog);

    Mono<Void> logUserEvent(String userId, String action, String details);

    Mono<Void> logAdminEvent(String adminId, String action, String entityType, String entityId, String details);
}
