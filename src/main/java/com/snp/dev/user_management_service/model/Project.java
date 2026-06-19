package com.snp.dev.user_management_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "projects")
public class Project {
    @Id
    private String id;
    private String userId;
    private String title;
    private String description;
    private String longDescription;
    private String githubUrl;
    private String deployedUri;
    private List<String> tools;
    private String category;
    private boolean featured;
    private String coverImageUrl;
    private List<String> images;
    private String challenges;
    private String solutions;
    private String date;
    private int viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
