package com.snp.dev.user_management_service.repository;

import com.snp.dev.user_management_service.model.UserEvent;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface UserEventRepository extends ReactiveMongoRepository<UserEvent, String> {

    Flux<UserEvent> findByUserId(String userId);
}
