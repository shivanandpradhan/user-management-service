package com.snp.dev.user_management_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing MFA setup details")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MfaSetupResponse {

    @Schema(description = "The secret key for MFA setup", example = "JBSWY3DPEHPK3PXP")
    private String secret;

    @Schema(description = "QR code image as base64 encoded data URI",
            example = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...")
    private String qrCode;

    @Schema(description = "Manual entry code for MFA apps", example = "JBSW Y3DP EHPK 3PXP")
    private String manualEntryCode;
}
