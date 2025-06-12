package com.snp.dev.user_management_service.repository;

import com.snp.dev.user_management_service.model.AuditLog;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface AuditLogRepository extends ReactiveMongoRepository<AuditLog, String> {
}
