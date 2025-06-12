package com.snp.dev.user_management_service.repository;

import com.snp.dev.user_management_service.model.Role;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface RoleRepository extends ReactiveMongoRepository<Role, String> {

    Mono<Role> findByName(String name);
}
