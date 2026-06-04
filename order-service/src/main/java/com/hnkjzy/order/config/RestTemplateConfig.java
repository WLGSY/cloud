package com.hnkjzy.order.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    @LoadBalanced  // 开启负载均衡（关键！注释不能省）
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}