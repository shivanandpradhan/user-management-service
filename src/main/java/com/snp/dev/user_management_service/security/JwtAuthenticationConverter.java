package com.snp.dev.user_management_service.security;

import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JwtAuthenticationConverter implements ServerAuthenticationConverter {

    private final JwtTokenProvider tokenProvider;

    public JwtAuthenticationConverter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst("Authorization"))
                .filter(authHeader -> authHeader.startsWith("Bearer "))
                .map(authHeader -> authHeader.substring(7))
                .flatMap(token -> tokenProvider.validateToken(token)
                        .map(claims -> {
                            String username = claims.getSubject();
                            List<SimpleGrantedAuthority> grantedAuthorities = getAuthorities(claims);
                            return new UsernamePasswordAuthenticationToken(username, null, grantedAuthorities);
                        }));
    }

    private List<SimpleGrantedAuthority> getAuthorities(Claims claims) {
        // Extract the "auth" claim
        Object authClaim = claims.get("auth");

        // Initialize the granted authorities as an empty list
        List<SimpleGrantedAuthority> grantedAuthorities = new ArrayList<>();

        if (authClaim instanceof String) {
            // If "auth" is a single string
            grantedAuthorities = List.of(new SimpleGrantedAuthority((String) authClaim));
        } else if (authClaim instanceof List) {
            // If "auth" is a list of strings
            List<String> authorities = (List<String>) authClaim;
            grantedAuthorities = authorities.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        } else {
            // Handle unexpected formats of the auth claim
            throw new IllegalArgumentException("Invalid auth claim format in JWT");
        }

        return grantedAuthorities;
    }


}

