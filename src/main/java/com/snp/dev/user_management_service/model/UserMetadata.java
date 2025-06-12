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
@Document(collection = "user_metadata")
public class UserMetadata {

    @Id
    private String id;
    private String userId;
    private String ipAddress;
    private String userAgent;
    private String deviceId;
    private String deviceType;
    private String os;
    private String browser;
    private String timezone;
    private Map<String, Object> preferences;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
