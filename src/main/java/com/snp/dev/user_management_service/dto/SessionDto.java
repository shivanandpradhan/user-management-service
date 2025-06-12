package com.snp.dev.user_management_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionDto {
    private String id;
    private String deviceInfo;
    private String ipAddress;
    private String lastAccessed;
}
