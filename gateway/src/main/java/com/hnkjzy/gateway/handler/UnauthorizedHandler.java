package com.hnkjzy.gateway.handler;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Component
public class UnauthorizedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Mono<Void> handle(ServerWebExchange exchange) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 401);
        result.put("msg", "未登录或Token已过期，请重新登录");
        result.put("timestamp", System.currentTimeMillis());
        result.put("path", exchange.getRequest().getURI().getPath());

        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(result);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }
}