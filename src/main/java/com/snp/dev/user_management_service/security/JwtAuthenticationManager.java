package com.snp.dev.user_management_service.security;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import reactor.core.publisher.Mono;

class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtTokenProvider jwtTokenUtil;
    private final ReactiveUserDetailsService userDetailsService;

    public JwtAuthenticationManager(JwtTokenProvider jwtTokenUtil,
                                    ReactiveUserDetailsService userDetailsService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.just(authentication)
                .map(auth -> auth.getCredentials().toString())
                .flatMap(token -> jwtTokenUtil.getUsernameFromToken(token)
                        .flatMap(userDetailsService::findByUsername)
                        .flatMap(userDetails -> jwtTokenUtil.validateToken(token)
                                .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid token")))
                                .map(valid -> new UsernamePasswordAuthenticationToken(
                                        userDetails.getUsername(),
                                        token,
                                        userDetails.getAuthorities()))));
    }
}