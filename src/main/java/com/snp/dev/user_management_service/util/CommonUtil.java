package com.snp.dev.user_management_service.util;

import com.snp.dev.user_management_service.model.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
public class CommonUtil {


    // ==================== Helper Methods ====================

    public String extractUserId(UserDetails userDetails) {
        if (userDetails == null) {
            log.error("UserDetails is null - user not authenticated");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        if (userDetails instanceof CustomUserDetails) {
            String userId = ((CustomUserDetails) userDetails).getId();
            if (userId == null || userId.trim().isEmpty()) {
                log.error("User ID is null or empty in CustomUserDetails");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid user data");
            }
            return userId;
        }

        String username = userDetails.getUsername();
        if (username == null || username.trim().isEmpty()) {
            log.error("Username is null or empty in UserDetails");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid user data");
        }

        return username;
    }

}
