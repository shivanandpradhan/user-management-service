package com.snp.dev.user_management_service.repository;

import com.snp.dev.user_management_service.model.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveMongoRepository<User, String> {

    Mono<User> findByUsername(String username);
    Mono<User> findByEmail(String email);
    Mono<User> findByUsernameOrEmail(String username, String email);
    Mono<Boolean> existsByUsername(String username);
    Mono<Boolean> existsByEmail(String email);
}