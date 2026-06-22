package com.snp.dev.user_management_service.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snp.dev.user_management_service.dto.ApiResponse;
import com.snp.dev.user_management_service.exception.JWTExceptionHandler;
import com.snp.dev.user_management_service.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtTokenProvider tokenProvider;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String path = exchange.getRequest().getPath().value();

        // Skip JWT validation for public endpoints
        if (path.startsWith("/api/auth/") ||
                path.contains("/public/") ||
                path.endsWith("/public") ||
                path.startsWith("/api/test")) {
            return chain.filter(exchange);
        }

        String token = resolveToken(exchange.getRequest());

        // ✅ No token - continue without authentication
        if (!StringUtils.hasText(token)) {
            return chain.filter(exchange);
        }

        // ✅ Validate token and set authentication
        return tokenProvider.validateAccessToken(token)
                .flatMap(valid -> {
                    return tokenProvider.getAuthentication(token)
                            .flatMap(auth -> {
                                if (auth == null) {
                                    log.warn("Failed to extract authentication from token");
                                    return Mono.error(new UnauthorizedException("Failed to authenticate"));
                                }
                                log.debug("Authenticated user: {}", auth.getName());
                                return chain.filter(exchange)
                                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
                            });
                })
                // ✅ Propagate error to GlobalExceptionHandler
                .onErrorResume(ex -> {
                    log.error("JWT authentication error: {}", ex.getMessage(), ex);
                    Pair<HttpStatus, ApiResponse<Void>> jwtErrorResponseWithStatusCode = JWTExceptionHandler.handle(ex);
                    exchange.getResponse().setStatusCode(jwtErrorResponseWithStatusCode.getLeft());
                    try {
                        return exchange.getResponse()
                                .writeWith(Mono.just(exchange.getResponse()
                                        .bufferFactory()
                                        .wrap(objectMapper.writeValueAsBytes(jwtErrorResponseWithStatusCode.getRight()))));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private String resolveToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}