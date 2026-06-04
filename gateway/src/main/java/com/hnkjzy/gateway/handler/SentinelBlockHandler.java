package com.hnkjzy.gateway.handler;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class SentinelBlockHandler {

    @PostConstruct
    public void init() {
        BlockRequestHandler blockRequestHandler = (exchange, t) -> {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 429);
            result.put("msg", "系统繁忙，请稍后再试（网关限流）");
            result.put("timestamp", System.currentTimeMillis());
            result.put("path", exchange.getRequest().getURI().getPath());

            return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(result);
        };
        GatewayCallbackManager.setBlockHandler(blockRequestHandler);
    }
}