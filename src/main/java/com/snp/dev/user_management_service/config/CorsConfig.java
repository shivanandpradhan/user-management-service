//package com.snp.dev.user_management_service.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.reactive.CorsConfigurationSource;
//import org.springframework.web.cors.reactive.CorsWebFilter;
//import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
//import org.springframework.web.util.pattern.PathPatternParser;
//
//import java.util.Arrays;
//import java.util.List;
//
//@Configuration
//public class CorsConfig {
//
////    @Bean
////    public CorsWebFilter corsWebFilter() {
////        CorsConfiguration config = new CorsConfiguration();
////
////        // Configure allowed origins
////        config.setAllowedOrigins(allowedOrigins());
////
////        // Configure allowed methods
////        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
////
////        // Configure allowed headers
////        config.setAllowedHeaders(Arrays.asList("*"));
////
////        // Configure exposed headers
////        config.setExposedHeaders(Arrays.asList("Authorization"));
////
////        // Allow credentials
////        config.setAllowCredentials(true);
////
////        // Set max age
////        config.setMaxAge(3600L);
////
////        // Apply to all paths
////        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(new PathPatternParser());
////        source.registerCorsConfiguration("/**", config);
////
////        return new CorsWebFilter(source);
////    }
////
////    private List<String> allowedOrigins() {
////        return Arrays.asList(
////                "http://localhost:3000",
////                "http://127.0.0.1:3000",
////                "https://your-production-domain.com",
////                "http://localhost:5173"
////        );
////    }
//
//
//}