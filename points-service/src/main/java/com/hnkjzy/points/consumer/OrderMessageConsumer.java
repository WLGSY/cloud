package com.hnkjzy.points.consumer;

import com.hnkjzy.points.config.PointsConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
public class OrderMessageConsumer {

    @Autowired
    private PointsConfig pointsConfig;

    @RabbitListener(queues = "points.order.create.queue", containerFactory = "rabbitListenerContainerFactory")
    public void handleOrderCreated(Map<String, Object> message) {
        log.info("========== 积分服务收到订单创建消息 ==========");
        log.info("消息内容: {}", message);
        log.info("[积分服务] 订单创建（预增积分，支付后生效）");
        log.info("==========================================");
    }

    @RabbitListener(queues = "points.order.pay.queue", containerFactory = "rabbitListenerContainerFactory")
    public void handleOrderPaid(Map<String, Object> message) {
        log.info("========== 积分服务收到订单支付消息 ==========");
        log.info("消息内容: {}", message);

        if (!pointsConfig.getEnabled()) {
            log.warn("[积分服务] 积分功能未开启");
            log.info("==========================================");
            return;
        }

        try {
            Long orderId = Long.valueOf(message.get("orderId").toString());
            Long userId = Long.valueOf(message.get("userId").toString());
            String username = getString(message, "username");
            BigDecimal amount = new BigDecimal(message.get("totalAmount").toString());

            int rate = pointsConfig.getRate();
            int earned = Math.min(amount.intValue() * rate, pointsConfig.getMaxPerOrder());

            log.info("[积分增加] 用户: {} (ID: {}), 订单ID: {}, 订单金额: {}元, 获得积分: {}分",
                    username, userId, orderId, amount, earned);
            log.info("[积分服务] 订单支付成功，积分增加完成");
        } catch (Exception e) {
            log.error("[积分服务] 处理订单支付消息失败: {}", e.getMessage(), e);
        }

        log.info("==========================================");
    }

    @RabbitListener(queues = "points.order.cancel.queue", containerFactory = "rabbitListenerContainerFactory")
    public void handleOrderCancelled(Map<String, Object> message) {
        log.info("========== 积分服务收到订单取消消息 ==========");
        log.info("消息内容: {}", message);
        log.info("[积分服务] 订单取消，不增加积分");
        log.info("==========================================");
    }

    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }
}
