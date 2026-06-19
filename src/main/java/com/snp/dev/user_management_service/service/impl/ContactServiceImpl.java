package com.snp.dev.user_management_service.service.impl;

import com.snp.dev.user_management_service.dto.ApiResponse;
import com.snp.dev.user_management_service.dto.portfolio.ContactReplyRequest;
import com.snp.dev.user_management_service.dto.portfolio.ContactRequest;
import com.snp.dev.user_management_service.dto.portfolio.ContactResponse;
import com.snp.dev.user_management_service.model.Contact;
import com.snp.dev.user_management_service.repository.ContactRepository;
import com.snp.dev.user_management_service.service.ContactService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;

    @Override
    public Mono<ApiResponse<ContactResponse>> submitContact(ContactRequest request, String userId, String userEmail) {
        log.info("Submitting contact from: {}", request.getEmail());

        Contact contact = Contact.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .subject(request.getSubject())
                .message(request.getMessage())
                .userId(userId)
                .userEmail(userEmail)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .read(false)
                .replied(false)
                .build();

        return contactRepository.save(contact)
                .map(this::mapToResponse)
                .<ApiResponse<ContactResponse>>map(ApiResponse::success)
                .onErrorResume(e -> {
                    log.error("Error submitting contact: {}", e.getMessage());
                    return Mono.just(ApiResponse.<ContactResponse>error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("SUBMIT_ERROR", "Failed to submit contact: " + e.getMessage(), null, null)
                    )));
                });
    }

    @Override
    public Mono<ApiResponse<List<ContactResponse>>> getAllContacts() {
        log.info("Fetching all contacts");

        return contactRepository.findAllByOrderByCreatedAtDesc()
                .map(this::mapToResponse)
                .collectList()
                .<ApiResponse<List<ContactResponse>>>map(ApiResponse::success)
                .defaultIfEmpty(ApiResponse.success(Collections.emptyList()))
                .onErrorResume(e -> {
                    log.error("Error fetching contacts: {}", e.getMessage());
                    return Mono.just(ApiResponse.<List<ContactResponse>>error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("FETCH_ERROR", "Failed to fetch contacts: " + e.getMessage(), null, null)
                    )));
                });
    }

    @Override
    public Mono<ApiResponse<ContactResponse>> getContactById(String id) {
        log.info("Fetching contact by id: {}", id);

        return contactRepository.findById(id)
                .map(this::mapToResponse)
                .<ApiResponse<ContactResponse>>map(ApiResponse::success)
                .switchIfEmpty(Mono.just(ApiResponse.<ContactResponse>error(Collections.singletonList(
                        new ApiResponse.ErrorDetail("NOT_FOUND", "Contact not found with id: " + id, null, null)
                ))))
                .onErrorResume(e -> {
                    log.error("Error fetching contact: {}", e.getMessage());
                    return Mono.just(ApiResponse.<ContactResponse>error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("FETCH_ERROR", "Failed to fetch contact: " + e.getMessage(), null, null)
                    )));
                });
    }

    @Override
    public Mono<ApiResponse<ContactResponse>> markAsRead(String id) {
        log.info("Marking contact as read: {}", id);

        return contactRepository.findById(id)
                .flatMap(contact -> {
                    contact.setRead(true);
                    contact.setUpdatedAt(LocalDateTime.now());
                    return contactRepository.save(contact);
                })
                .map(this::mapToResponse)
                .<ApiResponse<ContactResponse>>map(ApiResponse::success)
                .switchIfEmpty(Mono.just(ApiResponse.<ContactResponse>error(Collections.singletonList(
                        new ApiResponse.ErrorDetail("NOT_FOUND", "Contact not found with id: " + id, null, null)
                ))))
                .onErrorResume(e -> {
                    log.error("Error marking contact as read: {}", e.getMessage());
                    return Mono.just(ApiResponse.<ContactResponse>error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("UPDATE_ERROR", "Failed to mark contact as read: " + e.getMessage(), null, null)
                    )));
                });
    }

    @Override
    public Mono<ApiResponse<ContactResponse>> replyToContact(String id, ContactReplyRequest replyRequest) {
        log.info("Replying to contact: {}", id);

        return contactRepository.findById(id)
                .flatMap(contact -> {
                    contact.setReplied(true);
                    contact.setReplyMessage(replyRequest.getReplyMessage());
                    contact.setRepliedAt(LocalDateTime.now());
                    contact.setUpdatedAt(LocalDateTime.now());
                    return contactRepository.save(contact);
                })
                .map(this::mapToResponse)
                .<ApiResponse<ContactResponse>>map(ApiResponse::success)
                .switchIfEmpty(Mono.just(ApiResponse.<ContactResponse>error(Collections.singletonList(
                        new ApiResponse.ErrorDetail("NOT_FOUND", "Contact not found with id: " + id, null, null)
                ))))
                .onErrorResume(e -> {
                    log.error("Error replying to contact: {}", e.getMessage());
                    return Mono.just(ApiResponse.<ContactResponse>error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("REPLY_ERROR", "Failed to reply to contact: " + e.getMessage(), null, null)
                    )));
                });
    }

    @Override
    public Mono<ApiResponse<Void>> deleteContact(String id) {
        log.info("Deleting contact: {}", id);

        return contactRepository.findById(id)
                .flatMap(contact -> contactRepository.deleteById(id)
                        .thenReturn(ApiResponse.<Void>success(null)))
                .switchIfEmpty(Mono.just(ApiResponse.<Void>error(Collections.singletonList(
                        new ApiResponse.ErrorDetail("NOT_FOUND", "Contact not found with id: " + id, null, null)
                ))))
                .onErrorResume(e -> {
                    log.error("Error deleting contact: {}", e.getMessage());
                    return Mono.just(ApiResponse.<Void>error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("DELETE_ERROR", "Failed to delete contact: " + e.getMessage(), null, null)
                    )));
                });
    }

    @Override
    public Mono<ApiResponse<Long>> getUnreadCount() {
        log.info("Fetching unread contact count");

        return contactRepository.findByReadFalseOrderByCreatedAtDesc()
                .count()
                .<ApiResponse<Long>>map(ApiResponse::success)
                .onErrorResume(e -> {
                    log.error("Error counting unread contacts: {}", e.getMessage());
                    return Mono.just(ApiResponse.<Long>error(Collections.singletonList(
                            new ApiResponse.ErrorDetail("COUNT_ERROR", "Failed to count unread contacts: " + e.getMessage(), null, null)
                    )));
                });
    }

    private ContactResponse mapToResponse(Contact contact) {
        return ContactResponse.builder()
                .id(contact.getId())
                .name(contact.getName())
                .email(contact.getEmail())
                .phone(contact.getPhone())
                .subject(contact.getSubject())
                .message(contact.getMessage())
                .createdAt(contact.getCreatedAt())
                .read(contact.isRead())
                .replied(contact.isReplied())
                .replyMessage(contact.getReplyMessage())
                .repliedAt(contact.getRepliedAt())
                .build();
    }
}
