package com.snp.dev.user_management_service.repository;

import com.snp.dev.user_management_service.model.Project;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ProjectRepository extends ReactiveMongoRepository<Project, String> {

    Flux<Project> findByUserId(String userId);

    Flux<Project> findByUserIdAndFeatured(String userId, boolean featured);

    Flux<Project> findByCategory(String category);

    Flux<Project> findByFeaturedTrue();

    Flux<Project> findByFeaturedTrueAndUserId(String userId);

    Mono<Long> countByUserId(String userId);
}