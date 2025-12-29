package com.hotelbooking.api_gateway.filter;

import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.hotelbooking.api_gateway.security.GatewayJwtUtil;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final GatewayJwtUtil jwtUtil;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/login",
            "/auth/register",
            "/auth/register/guest",
            "/auth/activate",
            "/actuator"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        boolean isPublic = PUBLIC_PATHS.stream().anyMatch(path::startsWith);
        if (isPublic) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);

        try {
            // Validate JWT
            Claims claims = jwtUtil.validate(token);

            // Start request mutation
            ServerHttpRequest.Builder requestBuilder = exchange.getRequest()
                    .mutate()
                    .header("X-User-Id", claims.get("userId").toString())
                    .header("X-User-Role", claims.get("role").toString())
                    .header("X-User-Email", claims.getSubject());

            // Conditionally add hotelId for staff
            if (claims.get("hotelId") != null) {
                requestBuilder.header(
                        "X-Hotel-Id",
                        claims.get("hotelId").toString()
                );
            }

            // Build mutated request
            ServerHttpRequest mutatedRequest = requestBuilder.build();

            // Continue filter chain
            return chain.filter(
                    exchange.mutate().request(mutatedRequest).build()
            );

        } catch (Exception e) {
            return unauthorized(exchange);
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1; // run before routing
    }
}