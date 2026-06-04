package com.hnkjzy.notification;

import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class NotificationServiceApplication {

    @Autowired(required = false)
    private RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry;

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }

    @EventListener(ContextRefreshedEvent.class)
    public void onApplicationEvent() {
        if (rabbitListenerEndpointRegistry != null) {
            System.out.println("[RabbitMQ] 已注册的监听器数量: " + rabbitListenerEndpointRegistry.getListenerContainerIds().size());
            rabbitListenerEndpointRegistry.getListenerContainerIds().forEach(id -> {
                System.out.println("[RabbitMQ] 监听器ID: " + id);
                System.out.println("[RabbitMQ] 监听器状态: " + rabbitListenerEndpointRegistry.getListenerContainer(id).isRunning());
            });
        }
    }
}