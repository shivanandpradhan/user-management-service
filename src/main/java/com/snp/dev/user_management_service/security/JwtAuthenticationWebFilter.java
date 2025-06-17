package com.snp.dev.user_management_service.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;

public class JwtAuthenticationWebFilter extends AuthenticationWebFilter {

    public JwtAuthenticationWebFilter(JwtTokenProvider jwtTokenUtil,
                                      ReactiveUserDetailsService userDetailsService) {
        super(new JwtAuthenticationManager(jwtTokenUtil, userDetailsService));
        setServerAuthenticationConverter(new JwtAuthenticationConverter(jwtTokenUtil));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             org.springframework.web.server.WebFilterChain chain) {
        return super.filter(exchange, chain)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(new UsernamePasswordAuthenticationToken(
                        "anonymous", null, Collections.emptyList())));
    }
}
