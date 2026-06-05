package com.hnkjzy.delivery.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class OrderMessageConsumer {

    @RabbitListener(queues = "delivery.order.pay.queue", containerFactory = "rabbitListenerContainerFactory")
    public void handleOrderPaid(Map<String, Object> message) {
        log.info("========== 物流服务收到订单支付消息 ==========");
        log.info("消息内容: {}", message);

        try {
            Long orderId = Long.valueOf(message.get("orderId").toString());
            String orderNo = getString(message, "orderNo");
            String username = getString(message, "username");

            log.info("[物流服务] 创建配送任务，订单号: {}, 用户: {}", orderNo, username);
            log.info("[物流服务] 分配骑手中...");
            log.info("[物流服务] 配送任务创建成功，订单ID: {}", orderId);
        } catch (Exception e) {
            log.error("[物流服务] 处理订单支付消息失败: {}", e.getMessage(), e);
        }

        log.info("==========================================");
    }

    @RabbitListener(queues = "delivery.order.cancel.queue", containerFactory = "rabbitListenerContainerFactory")
    public void handleOrderCancelled(Map<String, Object> message) {
        log.info("========== 物流服务收到订单取消消息 ==========");
        log.info("消息内容: {}", message);

        try {
            String orderNo = getString(message, "orderNo");
            String username = getString(message, "username");

            log.info("[物流服务] 取消配送任务，订单号: {}, 用户: {}", orderNo, username);
            log.info("[物流服务] 配送任务已取消");
        } catch (Exception e) {
            log.error("[物流服务] 处理订单取消消息失败: {}", e.getMessage(), e);
        }

        log.info("==========================================");
    }

    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }
}
