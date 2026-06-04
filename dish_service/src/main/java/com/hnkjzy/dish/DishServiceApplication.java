package com.hnkjzy.dish;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.hnkjzy.dish.mapper")
public class DishServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DishServiceApplication.class, args);
    }
}