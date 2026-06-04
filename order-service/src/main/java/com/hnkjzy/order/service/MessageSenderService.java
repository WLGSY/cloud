package com.hnkjzy.order.service;

import com.hnkjzy.order.config.RabbitMQConfig;
import com.hnkjzy.order.dto.OrderMessageDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageSenderService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送订单创建消息
     * @param orderMessage 订单消息
     */
    public void sendOrderCreateMessage(OrderMessageDTO orderMessage) {
        try {
            rabbitTemplate.convertAndSend(
                    "order.exchange",           // 交换机
                    "order.create",             // 路由键
                    orderMessage               // 消息体
            );
            System.out.println("[消息发送] 订单创建消息已发送，订单ID: " + orderMessage.getOrderId());
        } catch (Exception e) {
            System.err.println("[消息发送失败] " + e.getMessage());
        }
    }
}