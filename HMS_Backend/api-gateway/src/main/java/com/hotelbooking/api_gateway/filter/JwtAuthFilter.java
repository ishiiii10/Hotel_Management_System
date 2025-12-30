package com.hotelbooking.api_gateway.filter;

import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.hotelbooking.api_gateway.security.GatewayJwtUtil;
import com.hotelbooking.api_gateway.security.PublicRoute;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final GatewayJwtUtil jwtUtil;

    private static final List<PublicRoute> PUBLIC_ROUTES = List.of(

            // Auth service
            new PublicRoute("POST", "/auth/login"),
            new PublicRoute("POST", "/auth/register"),
            new PublicRoute("POST", "/auth/activate"),

            // Hotel public APIs
            new PublicRoute("GET", "/hotels"),
            
            new PublicRoute("GET", "/hotels/"),
            new PublicRoute("GET", "/room-categories")
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();
        HttpMethod httpMethod = exchange.getRequest().getMethod();

        // Safety check
        if (httpMethod == null) {
            return unauthorized(exchange);
        }

        String method = httpMethod.name();

        boolean isPublic = PUBLIC_ROUTES.stream().anyMatch(route ->
                route.getMethod().equalsIgnoreCase(method)
                        && path.startsWith(route.getPathPrefix())
        );

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
            Claims claims = jwtUtil.validate(token);

            // 🔒 VERY IMPORTANT: strip any incoming identity headers
            ServerHttpRequest.Builder requestBuilder = exchange.getRequest()
                    .mutate()
                    .headers(headers -> {
                        headers.remove("X-User-Id");
                        headers.remove("X-User-Role");
                        headers.remove("X-User-Email");
                        headers.remove("X-Hotel-Id");
                    });

            // Add trusted headers from JWT
            requestBuilder
                    .header("X-User-Id", claims.get("userId").toString())
                    .header("X-User-Role", claims.get("role").toString())
                    .header("X-User-Email", claims.getSubject());

            // Add hotelId ONLY if present (staff users)
            Object hotelId = claims.get("hotelId");
            if (hotelId != null) {
                requestBuilder.header("X-Hotel-Id", hotelId.toString());
            }

            ServerHttpRequest mutatedRequest = requestBuilder.build();

            return chain.filter(
                    exchange.mutate().request(mutatedRequest).build()
            );

        } catch (Exception ex) {
            return unauthorized(exchange);
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}