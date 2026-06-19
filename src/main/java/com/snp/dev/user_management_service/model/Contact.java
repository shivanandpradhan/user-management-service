package com.snp.dev.user_management_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "contacts")
public class Contact {
    @Id
    private String id;
    private String name;
    private String email;
    private String phone;
    private String subject;
    private String message;
    private String userId; // If user is logged in
    private String userEmail; // For tracking
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean read;
    private boolean replied;
    private String replyMessage;
    private LocalDateTime repliedAt;
}
