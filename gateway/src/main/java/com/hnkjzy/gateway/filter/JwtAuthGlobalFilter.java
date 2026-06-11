package com.hnkjzy.gateway.filter;

import com.hnkjzy.gateway.config.WhitelistProperties;
import com.hnkjzy.gateway.handler.UnauthorizedHandler;
import com.hnkjzy.gateway.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UnauthorizedHandler unauthorizedHandler;

    @Autowired
    private WhitelistProperties whitelistProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isWhiteList(path)) {
            return chain.filter(exchange);
        }

        String token = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (token == null || token.isEmpty()) {
            return unauthorizedHandler.handle(exchange);
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (!jwtUtil.validateToken(token)) {
            return unauthorizedHandler.handle(exchange);
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);
        String userType = jwtUtil.getUserTypeFromToken(token);

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-User-Id", String.valueOf(userId))
                .header("X-Username", username)
                .header("X-User-Role", role != null ? role : "user")
                .header("X-User-Type", userType != null ? userType : "customer")
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        return chain.filter(mutatedExchange);
    }

    private boolean isWhiteList(String path) {
        List<String> whitePaths = whitelistProperties.getPaths();
        if (whitePaths == null || whitePaths.isEmpty()) {
            return false;
        }
        for (String whitePath : whitePaths) {
            if (path.startsWith(whitePath)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return -1;
    }
}