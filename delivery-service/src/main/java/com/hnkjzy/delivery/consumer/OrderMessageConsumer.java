package com.hnkjzy.delivery.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.util.Map;

@Slf4j
@Component
public class OrderMessageConsumer {

    @RabbitListener(queues = "order.pay.queue")
    public void handleOrderPaid(Map<String, Object> message) {
        log.info("[物流服务] 收到订单支付消息");

        Long orderId = Long.valueOf(message.get("orderId").toString());
        String orderNo = (String) message.get("orderNo");
        String username = (String) message.get("username");

        log.info("[物流服务] 创建配送任务，订单号: {}, 用户: {}", orderNo, username);
        log.info("[物流服务] 分配骑手中...");
        log.info("[物流服务] 配送任务创建成功，订单ID: {}", orderId);
    }

    @RabbitListener(queues = "order.cancel.queue")
    public void handleOrderCancelled(Map<String, Object> message) {
        log.info("[物流服务] 收到订单取消消息");
        String orderNo = (String) message.get("orderNo");
        String username = (String) message.get("username");
        
        log.info("[物流服务] 取消配送任务，订单号: {}, 用户: {}", orderNo, username);
        log.info("[物流服务] 配送任务已取消");
    }
}