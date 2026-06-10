package com.snp.dev.user_management_service.config;

import com.snp.dev.user_management_service.security.JwtAuthenticationConverter;
import com.snp.dev.user_management_service.security.JwtAuthenticationFilter;
import com.snp.dev.user_management_service.security.JwtAuthenticationWebFilter;
import com.snp.dev.user_management_service.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableWebFluxSecurity
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
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/auth/forgot-password").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/auth/reset-password").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/auth/verify-otp").permitAll()
                                .pathMatchers(HttpMethod.POST, "/api/auth/mfa/verify").permitAll()
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

