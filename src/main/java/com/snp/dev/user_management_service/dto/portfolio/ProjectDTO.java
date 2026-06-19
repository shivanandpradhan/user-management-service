package com.snp.dev.user_management_service.dto.portfolio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDTO {
    private String id;
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
}

