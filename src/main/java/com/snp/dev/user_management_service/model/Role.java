package com.snp.dev.user_management_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "roles")
public class Role {

    @Id
    private String id;
    @Indexed(unique = true)
    private String name;
    private String description;
    private Set<String> permissions;
    private boolean systemRole;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
