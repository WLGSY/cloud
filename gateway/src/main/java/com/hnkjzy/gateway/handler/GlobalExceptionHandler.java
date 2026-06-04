package com.hnkjzy.gateway.handler;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
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
@Order(-1)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", System.currentTimeMillis());
        result.put("path", exchange.getRequest().getURI().getPath());

        String message = ex.getMessage();
        if (message != null && message.contains("404")) {
            result.put("code", 404);
            result.put("msg", "接口不存在");
            exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
        } else if (message != null && message.contains("Timeout")) {
            result.put("code", 504);
            result.put("msg", "服务超时，请稍后再试");
            exchange.getResponse().setStatusCode(HttpStatus.GATEWAY_TIMEOUT);
        } else {
            result.put("code", 500);
            result.put("msg", "系统内部错误");
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return writeResponse(exchange, result);
    }

    private Mono<Void> writeResponse(ServerWebExchange exchange, Map<String, Object> result) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(result);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }
}