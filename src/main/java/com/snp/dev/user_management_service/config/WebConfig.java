//package com.snp.dev.user_management_service.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.reactive.config.EnableWebFlux;
//import org.springframework.web.reactive.config.WebFluxConfigurer;
//import org.springframework.web.reactive.function.server.RouterFunction;
//import org.springframework.web.reactive.function.server.ServerResponse;
//import org.springframework.web.server.WebFilter;
//
//import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
//import static org.springframework.web.reactive.function.server.RouterFunctions.route;
//
//@Configuration
//@EnableWebFlux
//public class WebConfig implements WebFluxConfigurer {
//
//    @Bean
//    public RouterFunction<ServerResponse> routerFunction() {
//        return route(GET("/"), req -> ServerResponse.ok().bodyValue("User Management Service"));
//    }
//
//    @Bean
//    public WebFilter contextPathWebFilter() {
//        String contextPath = "/api";
//        return (exchange, chain) -> {
//            String requestPath = exchange.getRequest().getPath().pathWithinApplication().value();
//            if (requestPath.startsWith(contextPath)) {
//                return chain.filter(
//                        exchange.mutate()
//                                .request(exchange.getRequest().mutate().contextPath(contextPath).build())
//                                .build());
//            }
//            return chain.filter(exchange);
//        };
//    }
//}
//
//
