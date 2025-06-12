package com.snp.dev.user_management_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "audit_logs")
public class AuditLog {

    @Id
    private String id;
    private String action;
    private String entityType;
    private String entityId;
    private String userId;
    private String username;
    private String ipAddress;
    private Map<String, Object> details;
    private LocalDateTime timestamp;
}
