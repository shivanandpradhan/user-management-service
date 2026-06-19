package com.snp.dev.user_management_service.dto.portfolio;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactReplyRequest {
    @NotBlank(message = "Reply message is required")
    private String replyMessage;
}
