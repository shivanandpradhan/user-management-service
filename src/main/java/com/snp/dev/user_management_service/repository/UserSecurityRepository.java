package com.snp.dev.user_management_service.repository;

import com.snp.dev.user_management_service.model.UserSecurity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface UserSecurityRepository extends ReactiveMongoRepository<UserSecurity, String> {

    Mono<UserSecurity> findByUserId(String userId);
    Mono<Boolean> deleteByUserId(String userId);
}
