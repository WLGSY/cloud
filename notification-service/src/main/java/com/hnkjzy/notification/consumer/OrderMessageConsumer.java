package com.hnkjzy.notification.consumer;

import com.hnkjzy.notification.dto.OrderMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderMessageConsumer {

    @RabbitListener(queues = "order.create.queue")
    public void handleOrderCreated(OrderMessageDTO message) {
        log.info("[短信服务] 收到订单创建消息");
        log.info("消息内容: {}", message);
        
        String sms = String.format("【云外卖】%s，您的订单%s已创建成功", 
                message.getUsername(), message.getOrderNo());
        log.info("[短信发送] {}", sms);
    }

    @RabbitListener(queues = "order.pay.queue")
    public void handleOrderPaid(OrderMessageDTO message) {
        log.info("========== 通知服务收到订单支付消息 ==========");
        log.info("[短信服务] 收到订单支付消息，消息内容: {}", message);
        
        try {
            log.info("解析消息 - 用户: {}, 订单号: {}, 金额: {}, 类型: {}", 
                    message.getUsername(), message.getOrderNo(), 
                    message.getTotalAmount(), message.getMessageType());

            String sms = String.format("【云外卖】%s，您的订单%s支付成功，金额%s元，感谢光临！", 
                    message.getUsername(), message.getOrderNo(), message.getTotalAmount());
            log.info("[短信发送] {}", sms);
            log.info("==========================================");
        } catch (Exception e) {
            log.error("[短信服务] 处理订单支付消息失败: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = "order.cancel.queue")
    public void handleOrderCancelled(OrderMessageDTO message) {
        log.info("[短信服务] 收到订单取消消息");
        log.info("消息内容: {}", message);
        
        String sms = String.format("【云外卖】%s，您的订单%s已取消，原因：%s", 
                message.getUsername(), message.getOrderNo(), message.getCancelReason());
        log.info("[短信发送] {}", sms);
    }
}