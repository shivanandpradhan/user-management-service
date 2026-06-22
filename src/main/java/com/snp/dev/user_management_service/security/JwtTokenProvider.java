package com.snp.dev.user_management_service.security;

import com.snp.dev.user_management_service.exception.InvalidTokenException;
import com.snp.dev.user_management_service.exception.TokenExpiredException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;

@Component
@Slf4j
public class JwtTokenProvider {

    // ✅ Using Key interface (not SecretKey) for flexibility
    private final Key accessKey;
    private final Key refreshKey;

    private final long accessTokenValidityMs;
    private final long refreshTokenValidityMs;
    private final String issuer;
    private final ReactiveUserDetailsService userDetailsService;

    @Value("${app.jwt.passwordResetTokenExpirationMs:900000}")
    private long passwordResetTokenExpirationMs;

    @Value("${app.jwt.passwordResetSecret:passwordResetSecret}")
    private String passwordResetSecret;

    public JwtTokenProvider(
            @Value("${jwt.access.secret}") String accessSecret,
            @Value("${jwt.refresh.secret}") String refreshSecret,
            @Value("${jwt.access.expiration:900000}") long accessTokenValidityMs,
            @Value("${jwt.refresh.expiration:604800000}") long refreshTokenValidityMs,
            @Value("${jwt.issuer:user-management-service}") String issuer,
            ReactiveUserDetailsService userDetailsService) {

        // ✅ Different algorithms for different token types
        this.accessKey = Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
        this.refreshKey = Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8));

        this.accessTokenValidityMs = accessTokenValidityMs;
        this.refreshTokenValidityMs = refreshTokenValidityMs;
        this.issuer = issuer;
        this.userDetailsService = userDetailsService;

        log.info("JwtTokenProvider initialized with separate keys");
        log.info("Access token: HS512, expires in {} ms", accessTokenValidityMs);
        log.info("Refresh token: HS384, expires in {} ms", refreshTokenValidityMs);
    }

    // ==================== ACCESS TOKEN METHODS ====================

    /**
     * Generate Access Token with HS512 algorithm (stronger for short-lived tokens)
     */
    public Mono<String> generateAccessToken(String username, Set<String> roles) {
        return Mono.fromCallable(() -> {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + accessTokenValidityMs);

            Map<String, Object> claims = new HashMap<>();
            claims.put("sub", username);
            claims.put("roles", roles);
            claims.put("type", "ACCESS");
            claims.put("jti", UUID.randomUUID().toString());
            claims.put("iat", now.getTime());
            claims.put("iss", issuer);

            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(username)
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(accessKey, SignatureAlgorithm.HS512)  // ✅ HS512 for access tokens
                    .compact();
        });
    }

    /**
     * Validate Access Token
     */
    public Mono<Claims> validateAccessToken(String token) {
        return Mono.fromCallable(() -> Jwts.parserBuilder()
                        .setSigningKey(accessKey)
                        .build()
                        .parseClaimsJws(token)
                        .getBody())
                .onErrorResume(ex -> {
                    log.error("Error during jwt access token validation : {}", ex.getMessage());
                    return Mono.error(ex);
                });
    }

    // ==================== REFRESH TOKEN METHODS ====================

    /**
     * Generate Refresh Token with HS384 algorithm (lighter, good for long-lived tokens)
     */
    public Mono<String> generateRefreshToken(String username, Set<String> roles) {
        return Mono.fromCallable(() -> {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + refreshTokenValidityMs);

            Map<String, Object> claims = new HashMap<>();
            claims.put("sub", username);
            claims.put("roles", roles);
            claims.put("type", "REFRESH");
            claims.put("jti", UUID.randomUUID().toString());
            claims.put("iat", now.getTime());
            claims.put("iss", issuer);

            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(username)
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(refreshKey, SignatureAlgorithm.HS384)  // ✅ HS384 for refresh tokens
                    .compact();
        });
    }

    /**
     * Validate Refresh Token
     */
    public Mono<Boolean> validateRefreshToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(refreshKey)  // ✅ Validate with refresh key
                    .build()
                    .parseClaimsJws(token);

            // ✅ Verify it's a refresh token
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(refreshKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            String type = claims.get("type", String.class);

            if (!"REFRESH".equals(type)) {
                log.warn("Token is not a refresh token");
                return Mono.just(false);
            }

            return Mono.just(true);
        } catch (ExpiredJwtException e) {
            log.warn("Refresh token expired: {}", e.getMessage());
            return Mono.just(false);
        } catch (MalformedJwtException e) {
            log.warn("Malformed refresh token: {}", e.getMessage());
            return Mono.just(false);
        } catch (SignatureException e) {
            log.warn("Invalid refresh token signature: {}", e.getMessage());
            return Mono.just(false);
        } catch (Exception e) {
            log.warn("Invalid refresh token: {}", e.getMessage());
            return Mono.just(false);
        }
    }

    // ==================== PASSWORD RESET TOKEN METHODS ====================

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

    // ==================== COMMON TOKEN METHODS ====================

    /**
     * Extract username from any token (tries access key first, then refresh key)
     */
    public Mono<String> getUsernameFromToken(String token) {
        try {
            // ✅ Try with access key first
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(accessKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return Mono.just(claims.getSubject());
        } catch (Exception e1) {
            try {
                // ✅ Try with refresh key
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(refreshKey)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
                return Mono.just(claims.getSubject());
            } catch (Exception e2) {
                log.warn("Failed to extract username from token: {}", e2.getMessage());
                return Mono.empty();
            }
        }
    }

    /**
     * Get authentication from token (used by JWT filter)
     */
    public Mono<Authentication> getAuthentication(String token) {
        return getUsernameFromToken(token)
                .flatMap(username -> {
                    if (username == null) {
                        return Mono.error(new BadCredentialsException("Username not found in token"));
                    }
                    return userDetailsService.findByUsername(username)
                            .switchIfEmpty(Mono.error(new BadCredentialsException("User not found: " + username)))
                            .map(userDetails -> new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            ));
                });
    }

    /**
     * Get token expiration in seconds for access token
     */
    public Mono<Long> getAccessTokenExpiration() {
        return Mono.just(accessTokenValidityMs / 1000);
    }

    /**
     * Get token expiration in seconds for refresh token
     */
    public Mono<Long> getRefreshTokenExpiration() {
        return Mono.just(refreshTokenValidityMs / 1000);
    }
}