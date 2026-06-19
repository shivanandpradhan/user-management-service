package com.snp.dev.user_management_service.service;

import com.snp.dev.user_management_service.dto.ApiResponse;
import com.snp.dev.user_management_service.dto.portfolio.ContactReplyRequest;
import com.snp.dev.user_management_service.dto.portfolio.ContactRequest;
import com.snp.dev.user_management_service.dto.portfolio.ContactResponse;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ContactService {
    Mono<ApiResponse<ContactResponse>> submitContact(ContactRequest request, String userId, String userEmail);

    Mono<ApiResponse<List<ContactResponse>>> getAllContacts();

    Mono<ApiResponse<ContactResponse>> getContactById(String id);

    Mono<ApiResponse<ContactResponse>> markAsRead(String id);

    Mono<ApiResponse<ContactResponse>> replyToContact(String id, ContactReplyRequest replyRequest);

    Mono<ApiResponse<Void>> deleteContact(String id);

    Mono<ApiResponse<Long>> getUnreadCount();
}
