package com.snp.dev.user_management_service.security;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAccessDeniedHandler implements ServerAccessDeniedHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException ex) {
        return Mono.fromRunnable(() -> {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        }).then(Mono.defer(() -> {
            DataBuffer buffer = exchange.getResponse().bufferFactory()
                    .wrap(new AuthError("Access Denied", ex.getMessage()).toString().getBytes());
            return exchange.getResponse().writeWith(Mono.just(buffer));
        }));
    }
}
