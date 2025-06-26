package com.snp.dev.user_management_service.security;

import com.snp.dev.user_management_service.exception.InvalidTokenException;
import com.snp.dev.user_management_service.exception.TokenExpiredException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private final Key key;
    private final long validityInMilliseconds;
    private final String issuer;
    private final ReactiveUserDetailsService userDetailsService;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long validityInMilliseconds,
            @Value("${jwt.issuer}") String issuer, ReactiveUserDetailsService userDetailsService) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.validityInMilliseconds = validityInMilliseconds;
        this.issuer = issuer;
        this.userDetailsService = userDetailsService;
    }

    @Value("${app.jwt.passwordResetTokenExpirationMs}")
    private long passwordResetTokenExpirationMs;

    @Value("${app.jwt.passwordResetSecret}")
    private String passwordResetSecret;

//    @Override
    public String generatePasswordResetToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + passwordResetTokenExpirationMs);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, passwordResetSecret)
                .compact();
    }

//    @Override
    public Mono<String> validatePasswordResetToken(String token) {
        return Mono.fromCallable(() -> {
            try {
                Claims claims = Jwts.parser()
                        .setSigningKey(passwordResetSecret)
                        .parseClaimsJws(token)
                        .getBody();

                return claims.getSubject();
            } catch (ExpiredJwtException ex) {
                throw new TokenExpiredException("Password reset token expired");
            } catch (JwtException | IllegalArgumentException ex) {
                throw new InvalidTokenException("Invalid password reset token");
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<String> createToken(Authentication authentication) {
        String username = authentication.getName();
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", username);
        claims.put("auth", authorities);
        claims.put("iat", now.getTime());
        claims.put("iss", issuer);

        return Mono.fromCallable(() -> Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact());
    }

    public Mono<String> createToken(String username, Set<String> roles) {
        String authorities = String.join(",", roles);


        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", username);
        claims.put("auth", authorities);
        claims.put("iat", now.getTime());
        claims.put("iss", issuer);

        return Mono.fromCallable(() -> Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact());
    }

    public Mono<Claims> validateToken(String token) {
        return Mono.fromCallable(() -> Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody());
    }

    public Mono<String> getUsernameFromToken(String token) {
        return Mono.fromCallable(() ->
                Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody()
                        .getSubject()
        ).onErrorResume(e -> Mono.empty());
    }

    public Mono<Authentication> getAuthentication(String token) {
        return Mono.fromCallable(() -> {
            Claims claims = Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(token)
                    .getBody();

            return new UsernamePasswordAuthenticationToken(
                    claims.getSubject(),
                    null,
                    Collections.emptyList());
        }).flatMap(auth ->
                userDetailsService.findByUsername(auth.getName())
                        .map(userDetails -> new UsernamePasswordAuthenticationToken(
                                userDetails.getUsername(),
                                null,
                                userDetails.getAuthorities()
                        ))
        );
    }
}

