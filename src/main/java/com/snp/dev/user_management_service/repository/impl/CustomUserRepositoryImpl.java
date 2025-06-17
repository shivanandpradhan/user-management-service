package com.snp.dev.user_management_service.repository.impl;

import com.snp.dev.user_management_service.model.User;
import com.snp.dev.user_management_service.repository.CustomUserRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class CustomUserRepositoryImpl implements CustomUserRepository {

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public CustomUserRepositoryImpl(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    @Override
    public Flux<User> findAllWithPagination(Pageable pageable) {
//        // Build the query for pagination
//        Query query = new Query()
//                .with(pageable); // Apply sorting and pagination (skip and limit)
//
//        // Return paginated Flux<User>
//        return reactiveMongoTemplate.find(query, User.class);
        // Calculate skip and limit for manual pagination
        int skip = (int) pageable.getOffset(); // Calculate offset using (pageNumber * pageSize)
        int limit = pageable.getPageSize();    // Number of items per page

        // Build the query
        Query query = new Query()
                .skip(skip) // Apply offset
                .limit(limit); // Apply limit

        // Apply sorting manually if necessary
        if (pageable.getSort() != null) {
            pageable.getSort().forEach(order -> {
                query.with(Sort.by(order.getDirection(), order.getProperty()));
            });
        }

        // Return paginated Flux<User>
        return reactiveMongoTemplate.find(query, User.class);


    }

    @Override
    public Mono<Long> countTotalUsers() {
        // Return total count for the User collection
        return reactiveMongoTemplate.count(new Query(), User.class);
    }
}
