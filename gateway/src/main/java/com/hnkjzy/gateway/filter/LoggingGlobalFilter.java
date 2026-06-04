package com.hnkjzy.gateway.filter;

import com.hnkjzy.gateway.config.GatewayFilterProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Autowired
    private GatewayFilterProperties filterProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();
        String query = exchange.getRequest().getURI().getQuery();
        String clientIp = getClientIp(exchange);

        if (filterProperties.isEnableDetailLog()) {
            System.out.println(String.format("[请求] %s | %s %s | IP: %s | 参数: %s",
                    FORMATTER.format(LocalDateTime.now()), method, path, clientIp,
                    query != null ? query : "无"));
        }

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long cost = System.currentTimeMillis() - startTime;
            int statusCode = exchange.getResponse().getStatusCode() != null ?
                    exchange.getResponse().getStatusCode().value() : 0;

            if (filterProperties.isEnableDetailLog()) {
                System.out.println(String.format("[响应] %s | %s %s | 状态码: %d | 耗时: %dms",
                        FORMATTER.format(LocalDateTime.now()), method, path, statusCode, cost));
            }

            long slowThreshold = filterProperties.getSlowRequestThreshold();
            if (cost > slowThreshold) {
                System.out.println("[慢请求告警] " + method + " " + path + " 耗时: " + cost + "ms (阈值: " + slowThreshold + "ms)");
            }
        }));
    }

    private String getClientIp(ServerWebExchange exchange) {
        String ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }
        return ip;
    }

    @Override
    public int getOrder() {
        return -2;
    }
}