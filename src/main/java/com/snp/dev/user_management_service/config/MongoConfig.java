package com.snp.dev.user_management_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

@Configuration
public class MongoConfig {

//    @Bean
//    public MongoCustomConversions customConversions() {
//        List<Converter<?, ?>> converters = new ArrayList<>();
//        converters.add(new ListToVectorConverter());
//        return new MongoCustomConversions(converters);
//    }
//
//    static class ListToVectorConverter implements Converter<List<?>, Vector<?>> {
//        @Override
//        public Vector<?> convert(List<?> source) {
//            return new Vector<>(source);
//        }
//    }

    @Bean
    public MongoCustomConversions customConversions() {
        // You can add any required converters here if needed
        return new MongoCustomConversions(Collections.emptyList());
    }

    @Bean
    public ReactiveMongoTemplate reactiveMongoTemplate(ReactiveMongoDatabaseFactory reactiveMongoDatabaseFactory) {
        return new ReactiveMongoTemplate(reactiveMongoDatabaseFactory);
    }


}