package com.snp.dev.user_management_service.dto.portfolio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeDTO {
    private String id;
    private PersonalInfoDTO personalInfo;
    private List<EducationDTO> education;
    private List<ExperienceDTO> experience;
    private Map<String, List<SkillDTO>> skills;
    private List<CertificationDTO> certifications;
    private List<AchievementDTO> achievements;
    private String pdfUrl;
    private String userId; // Added for reference

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonalInfoDTO {
        private String name;
        private String title;
        private String bio;
        private String longBio;
        private String photoUrl;
        private String coverImageUrl;
        private String email;
        private String phone;
        private String birthday;
        private String location;
        private List<String> languages;
        private List<String> interests;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EducationDTO {
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
    public static class ExperienceDTO {
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
    public static class SkillDTO {
        private String name;
        private int level;
        private int years;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CertificationDTO {
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
    public static class AchievementDTO {
        private String title;
        private String description;
        private String date;
    }
}