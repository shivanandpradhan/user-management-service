//package com.snp.dev.user_management_service.util;
//
//import io.github.bucket4j.Bandwidth;
//import io.github.bucket4j.Bucket;
//import io.github.bucket4j.Refill;
//
//import java.time.Duration;
//
//public class RateLimiter {
//
//    public Bucket createNewBucket() {
//        long capacity = 100; // 100 requests
//        Refill refill = Refill.intervally(100, Duration.ofHours(1)); // 100 requests per hour
//        Bandwidth limit = Bandwidth.classic(capacity, refill);
//        return Bucket.builder().addLimit(limit).build();
//    }
//}