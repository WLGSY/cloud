package com.hnkjzy.points;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class PointsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PointsServiceApplication.class, args);
        System.out.println("积分服务启动成功，端口: 8085");
    }
}