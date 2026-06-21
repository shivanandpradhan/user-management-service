package com.snp.dev.user_management_service.config;

import com.snp.dev.user_management_service.security.JwtAuthenticationFilter;
import com.snp.dev.user_management_service.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private final JwtTokenProvider tokenProvider;

    @Value("${app.cors.allowed.uriList}")
    List<String> corsAllowedList;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        // Create a custom matcher for public endpoints
        ServerWebExchangeMatcher publicMatcher = exchange -> {
            String path = exchange.getRequest().getPath().pathWithinApplication().value();
            boolean isPublic = path.contains("/public/") || path.endsWith("/public");
            return isPublic ?
                    ServerWebExchangeMatcher.MatchResult.match() :
                    ServerWebExchangeMatcher.MatchResult.notMatch();
        };


        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange(exchanges -> exchanges
                                .matchers(publicMatcher).permitAll()
                                .pathMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
                                .pathMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                                .pathMatchers(HttpMethod.POST, "/api/auth/forgot-password").permitAll()
                                .pathMatchers(HttpMethod.POST, "/api/auth/reset-password").permitAll()
                                .pathMatchers(HttpMethod.POST, "/api/auth/verify-otp").permitAll()
                                .pathMatchers(HttpMethod.POST, "/api/auth/mfa/verify").permitAll()
                                .pathMatchers(HttpMethod.GET, "/api/portfolio").permitAll()
                                .pathMatchers("/api/auth/refresh-token").authenticated()
                                .pathMatchers(
                                        "/swagger-ui.html",
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**",
                                        "/webjars/**"
                                ).permitAll()
                                .pathMatchers("actuator/**").permitAll()
                                .pathMatchers(("/api/test/**")).permitAll()
        //                        .pathMatchers("/api/**").permitAll()
                                .pathMatchers("/api/admin/**").hasRole("ADMIN")
                                .pathMatchers("/api/super-admin/**").hasRole("SUPER_ADMIN")
                                .anyExchange().authenticated()
                )
                .addFilterAt(jwtAuthenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(tokenProvider);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(corsAllowedList); // Allow specific origin
        corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Allow specific HTTP methods
        corsConfiguration.setAllowedHeaders(Arrays.asList("*")); // Allow all headers
        corsConfiguration.setAllowCredentials(true); // Allow credentials (cookies, etc.)

        // Expose all headers (optional, if needed)
        corsConfiguration.addExposedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }
}

