package com.snp.dev.user_management_service.repository;

import com.snp.dev.user_management_service.model.UserProfile;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface UserProfileRepository extends ReactiveMongoRepository<UserProfile, String> {

    Mono<UserProfile> findByUserId(String userId);
    Mono<Boolean> deleteByUserId(String userId);
}
