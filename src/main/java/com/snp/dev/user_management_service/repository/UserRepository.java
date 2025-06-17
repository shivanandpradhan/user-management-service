package com.snp.dev.user_management_service.repository;

import com.snp.dev.user_management_service.model.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveMongoRepository<User, String> {

    Mono<User> findByUsername(String username);
    Mono<User> findByEmail(String email);
    Mono<User> findByUsernameOrEmail(String username, String email);
    Mono<Boolean> existsByUsername(String username);
    Mono<Boolean> existsByEmail(String email);

    // MongoDB Pagination Query
    @Query(value = "{}", sort = "{_id: 1}") // Default sort by _id (ascending)
    Flux<User> findAllBy(Pageable pageable);

    // Alternative with dynamic sorting (if needed)
    @Query(value = "{}")
    Flux<User> findAllBy(Pageable pageable, @Param("sortField") String sortField, @Param("sortDirection") int sortDirection);
}