package com.snp.dev.user_management_service.controller;

import com.snp.dev.user_management_service.dto.ApiResponse;
import com.snp.dev.user_management_service.dto.portfolio.ContactReplyRequest;
import com.snp.dev.user_management_service.dto.portfolio.ContactRequest;
import com.snp.dev.user_management_service.dto.portfolio.ContactResponse;
import com.snp.dev.user_management_service.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
@Slf4j
public class ContactController {

    private final ContactService contactService;

    // ==================== Public Endpoints ====================

    @PostMapping("/public")
    public Mono<ApiResponse<ContactResponse>> submitContact(
            @Valid @RequestBody ContactRequest request) {
        log.info("POST /api/contacts/public - New contact from: {}", request.getEmail());
        return contactService.submitContact(request, null, null);
    }

    // ==================== Protected Endpoints (Admin Only) ====================

    @GetMapping
    public Mono<ApiResponse<List<ContactResponse>>> getAllContacts(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("GET /api/contacts - by user: {}", userDetails.getUsername());
        return contactService.getAllContacts();
    }

    @GetMapping("/{id}")
    public Mono<ApiResponse<ContactResponse>> getContactById(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("GET /api/contacts/{} - by user: {}", id, userDetails.getUsername());
        return contactService.getContactById(id);
    }

    @PutMapping("/{id}/read")
    public Mono<ApiResponse<ContactResponse>> markAsRead(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("PUT /api/contacts/{}/read - by user: {}", id, userDetails.getUsername());
        return contactService.markAsRead(id);
    }

    @PutMapping("/{id}/reply")
    public Mono<ApiResponse<ContactResponse>> replyToContact(
            @PathVariable String id,
            @Valid @RequestBody ContactReplyRequest replyRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("PUT /api/contacts/{}/reply - by user: {}", id, userDetails.getUsername());
        return contactService.replyToContact(id, replyRequest);
    }

    @DeleteMapping("/{id}")
    public Mono<ApiResponse<Void>> deleteContact(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("DELETE /api/contacts/{} - by user: {}", id, userDetails.getUsername());
        return contactService.deleteContact(id);
    }

    @GetMapping("/unread/count")
    public Mono<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("GET /api/contacts/unread/count - by user: {}", userDetails.getUsername());
        return contactService.getUnreadCount();
    }
}
