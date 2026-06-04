package com.hnkjzy.notification.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class OrderMessageConsumer {

    @RabbitListener(queues = "order.create.queue", containerFactory = "rabbitListenerContainerFactory")
    public void handleOrderCreated(Map<String, Object> message) {
        log.info("========== 通知服务收到订单创建消息 ==========");
        log.info("消息内容: {}", message);

        try {
            String username = getString(message, "username");
            String orderNo = getString(message, "orderNo");

            String sms = String.format("【云外卖】%s，您的订单%s已创建成功", username, orderNo);
            log.info("[短信发送] {}", sms);
        } catch (Exception e) {
            log.error("[短信服务] 处理订单创建消息失败: {}", e.getMessage(), e);
        }

        log.info("==========================================");
    }

    @RabbitListener(queues = "order.pay.queue", containerFactory = "rabbitListenerContainerFactory")
    public void handleOrderPaid(Map<String, Object> message) {
        log.info("========== 通知服务收到订单支付消息 ==========");
        log.info("消息内容: {}", message);

        try {
            String username = getString(message, "username");
            String orderNo = getString(message, "orderNo");
            String totalAmount = getString(message, "totalAmount");

            String sms = String.format("【云外卖】%s，您的订单%s支付成功，金额%s元，感谢光临！",
                    username, orderNo, totalAmount);
            log.info("[短信发送] {}", sms);
        } catch (Exception e) {
            log.error("[短信服务] 处理订单支付消息失败: {}", e.getMessage(), e);
        }

        log.info("==========================================");
    }

    @RabbitListener(queues = "order.cancel.queue", containerFactory = "rabbitListenerContainerFactory")
    public void handleOrderCancelled(Map<String, Object> message) {
        log.info("========== 通知服务收到订单取消消息 ==========");
        log.info("消息内容: {}", message);

        try {
            String username = getString(message, "username");
            String orderNo = getString(message, "orderNo");
            String cancelReason = getString(message, "cancelReason");

            String sms = String.format("【云外卖】%s，您的订单%s已取消，原因：%s",
                    username, orderNo, cancelReason);
            log.info("[短信发送] {}", sms);
        } catch (Exception e) {
            log.error("[短信服务] 处理订单取消消息失败: {}", e.getMessage(), e);
        }

        log.info("==========================================");
    }

    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }
}
