package com.snp.dev.user_management_service.repository;

import com.snp.dev.user_management_service.model.UserMetadata;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface UserMetadataRepository extends ReactiveMongoRepository<UserMetadata, String> {

    Mono<UserMetadata> findByUserId(String userId);
}
