package com.snp.dev.user_management_service.repository;

import com.snp.dev.user_management_service.model.Resume;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ResumeRepository extends ReactiveMongoRepository<Resume, String> {
    Mono<Resume> findByUserId(String userId);
}
