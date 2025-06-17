package com.snp.dev.user_management_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.embedded.RedisServer;


//@Profile("dev") // Only active in dev profile
@Configuration
public class EmbeddedRedisConfig {

    @Bean
    public RedisServer redisServer() {
        RedisServer redisServer = RedisServer.builder()
                .port(6379)
                .setting("maxmemory 128M")
                .build();
        redisServer.start();
        return redisServer;
    }
}
