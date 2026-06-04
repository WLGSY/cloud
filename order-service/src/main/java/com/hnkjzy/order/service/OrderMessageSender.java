package com.hnkjzy.order.service;

import com.hnkjzy.order.config.RabbitMQConfig;
import com.hnkjzy.order.dto.OrderMessageDTO;
import com.hnkjzy.order.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class OrderMessageSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendOrderCreated(Order order) {
        log.info("[订单服务] 开始发送订单创建消息，订单ID: {}", order.getId());
        
        try {
            OrderMessageDTO message = OrderMessageDTO.builder()
                    .messageId(UUID.randomUUID().toString())
                    .orderId(order.getId())
                    .orderNo(order.getOrderNo())
                    .userId(order.getUserId())
                    .username(order.getUserName())
                    .userPhone(order.getUserPhone())
                    .totalAmount(order.getTotalAmount())
                    .status(order.getStatus())
                    .statusDesc("待支付")
                    .createTime(order.getCreateTime())
                    .messageType("ORDER_CREATED")
                    .version(1)
                    .build();

            log.info("[订单服务] 消息内容: {}", message);
            
            rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.ROUTING_KEY_CREATE, message);
            log.info("[订单服务] ✓ 订单创建消息发送成功，订单ID: {}", order.getId());
        } catch (Exception e) {
            log.error("[订单服务] ✗ 订单创建消息发送失败，订单ID: {}", order.getId(), e);
            throw new RuntimeException("发送订单创建消息失败", e);
        }
    }

    public void sendOrderPaid(Order order) {
        log.info("========== 开始发送订单支付消息 ==========");
        log.info("订单ID: {}, 订单号: {}, 用户: {}", order.getId(), order.getOrderNo(), order.getUserName());
        
        try {
            OrderMessageDTO message = OrderMessageDTO.builder()
                    .messageId(UUID.randomUUID().toString())
                    .orderId(order.getId())
                    .orderNo(order.getOrderNo())
                    .userId(order.getUserId())
                    .username(order.getUserName())
                    .userPhone(order.getUserPhone())
                    .totalAmount(order.getTotalAmount())
                    .status(order.getStatus())
                    .statusDesc("已支付")
                    .createTime(order.getCreateTime())
                    .payTime(order.getPayTime())
                    .messageType("ORDER_PAID")
                    .version(1)
                    .build();

            log.info("消息内容: {}", message);
            log.info("发送到交换机: {}, 路由键: {}", RabbitMQConfig.ORDER_EXCHANGE, RabbitMQConfig.ROUTING_KEY_PAY);
            
            rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.ROUTING_KEY_PAY, message);
            
            log.info("[订单服务] ✓ 订单支付消息发送成功，订单ID: {}", order.getId());
            log.info("==========================================");
        } catch (Exception e) {
            log.error("[订单服务] ✗ 订单支付消息发送失败，订单ID: {}", order.getId(), e);
            log.error("错误详情: {}", e.getMessage());
            throw new RuntimeException("发送订单支付消息失败", e);
        }
    }

    public void sendOrderCancelled(Order order) {
        log.info("[订单服务] 开始发送订单取消消息，订单ID: {}", order.getId());
        
        try {
            OrderMessageDTO message = OrderMessageDTO.builder()
                    .messageId(UUID.randomUUID().toString())
                    .orderId(order.getId())
                    .orderNo(order.getOrderNo())
                    .userId(order.getUserId())
                    .username(order.getUserName())
                    .userPhone(order.getUserPhone())
                    .totalAmount(order.getTotalAmount())
                    .status(order.getStatus())
                    .statusDesc("已取消")
                    .createTime(order.getCreateTime())
                    .cancelTime(order.getCancelTime())
                    .cancelReason(order.getCancelReason())
                    .messageType("ORDER_CANCELLED")
                    .version(1)
                    .build();

            log.info("[订单服务] 消息内容: {}", message);
            
            rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.ROUTING_KEY_CANCEL, message);
            log.info("[订单服务] ✓ 订单取消消息发送成功，订单ID: {}", order.getId());
        } catch (Exception e) {
            log.error("[订单服务] ✗ 订单取消消息发送失败，订单ID: {}", order.getId(), e);
            throw new RuntimeException("发送订单取消消息失败", e);
        }
    }
}