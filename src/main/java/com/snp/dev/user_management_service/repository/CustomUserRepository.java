package com.snp.dev.user_management_service.repository;

import com.snp.dev.user_management_service.model.User;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomUserRepository {
    Flux<User> findAllWithPagination(Pageable pageable);

    Mono<Long> countTotalUsers();
}

