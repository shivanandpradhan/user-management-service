package com.snp.dev.user_management_service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "resumes")
public class Resume {
    @Id
    private String id;
    private String userId;

    private PersonalInfo personalInfo;
    private List<Education> education;
    private List<WorkExperience> experience;
    private Map<String, List<Skill>> skills;
    private List<Certification> certifications;
    private List<Achievement> achievements;
    private String pdfUrl;
    private boolean downloadEnabled;
    private int viewCount;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonalInfo {
        private String name;
        private String title;
        private String bio;
        private String longBio;
        private String photoUrl;
        private String email;
        private String phone;
        private String location;
        private List<String> languages;
        private List<String> interests;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Education {
        private String id;
        private String degree;
        private String institution;
        private String year;
        private String gpa;
        private List<String> achievements;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WorkExperience {
        private String id;
        private String company;
        private String position;
        private String startDate;
        private String location;
        private List<String> responsibilities;
        private List<String> technologies;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Skill {
        private String name;
        private int level;
        private int years;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Certification {
        private String name;
        private String issuer;
        private String date;
        private String credentialId;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Achievement {
        private String title;
        private String description;
        private String date;
    }
}
