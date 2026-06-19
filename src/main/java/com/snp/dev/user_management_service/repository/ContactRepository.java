package com.snp.dev.user_management_service.repository;

import com.snp.dev.user_management_service.model.Contact;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ContactRepository extends ReactiveMongoRepository<Contact, String> {
    Flux<Contact> findAllByOrderByCreatedAtDesc();

    Flux<Contact> findByReadFalseOrderByCreatedAtDesc();
}
