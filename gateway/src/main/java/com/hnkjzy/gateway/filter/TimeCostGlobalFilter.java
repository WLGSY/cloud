package com.hnkjzy.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class TimeCostGlobalFilter implements GlobalFilter, Ordered {

    private static final String START_TIME = "startTime";
    private static final long SLOW_THRESHOLD = 100;  // 慢请求阈值（毫秒）

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 记录开始时间
        exchange.getAttributes().put(START_TIME, System.currentTimeMillis());

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            Long startTime = exchange.getAttribute(START_TIME);
            if (startTime != null) {
                long cost = System.currentTimeMillis() - startTime;
                String path = exchange.getRequest().getURI().getPath();
                // 只记录耗时超过阈值的请求
                if (cost > SLOW_THRESHOLD) {
                    System.out.println("[慢请求] " + path + " 耗时: " + cost + "ms");
                }
            }
        }));
    }

    @Override
    public int getOrder() {
        return 0;  // 在鉴权过滤器之后执行
    }
}