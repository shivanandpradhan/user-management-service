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
@Document(collection = "user_events")
public class UserEvent {

    @Id
    private String id;
    private String eventType;
    private String userId;
    private Map<String, Object> payload;
    private LocalDateTime createdAt;
}
