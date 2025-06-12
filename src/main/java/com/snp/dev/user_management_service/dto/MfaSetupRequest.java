package com.snp.dev.user_management_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MfaSetupRequest {

    @NotBlank(message = "MFA type cannot be blank")
    private String mfaType;
}
