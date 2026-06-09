package com.hnkjzy.points.consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hnkjzy.points.config.PointsConfig;
import com.hnkjzy.points.entity.PointsLog;
import com.hnkjzy.points.mapper.PointsLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
public class OrderMessageConsumer {

    @Autowired
    private PointsConfig pointsConfig;

    @Autowired
    private PointsLogMapper pointsLogMapper;

    @RabbitListener(queues = "points.order.create.queue", containerFactory = "rabbitListenerContainerFactory")
    public void handleOrderCreated(Map<String, Object> message) {
        log.info("========== 积分服务收到订单创建消息 ==========");
        log.info("消息内容: {}", message);
        log.info("[积分服务] 订单创建（积分将在支付后生效）");
        log.info("==========================================");
    }

    @RabbitListener(queues = "points.order.pay.queue", containerFactory = "rabbitListenerContainerFactory")
    @Transactional
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
            String orderNo = getString(message, "orderNo");
            BigDecimal amount = new BigDecimal(message.get("totalAmount").toString());

            int rate = pointsConfig.getRate();
            int earned = Math.min(amount.intValue() * rate, pointsConfig.getMaxPerOrder());

            // 检查是否已经记录过积分（防止重复消费）
            LambdaQueryWrapper<PointsLog> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PointsLog::getOrderId, orderId)
                    .eq(PointsLog::getType, "ORDER_PAID");
            Long count = pointsLogMapper.selectCount(wrapper);
            if (count > 0) {
                log.warn("[积分服务] 订单 {} 的积分已记录，跳过重复处理", orderId);
                log.info("==========================================");
                return;
            }

            // 写入积分日志
            PointsLog pointsLog = new PointsLog();
            pointsLog.setUserId(userId);
            pointsLog.setOrderId(orderId);
            pointsLog.setOrderNo(orderNo);
            pointsLog.setPoints(earned);
            pointsLog.setType("ORDER_PAID");
            pointsLog.setRemark(String.format("订单支付获得积分，金额: %s元，倍率: %d", amount, rate));
            pointsLog.setCreateTime(LocalDateTime.now());

            pointsLogMapper.insert(pointsLog);

            log.info("[积分增加] 用户: {} (ID: {}), 订单ID: {}, 订单金额: {}元, 获得积分: {}分",
                    username, userId, orderId, amount, earned);
            log.info("[积分服务] 积分记录已写入数据库");
        } catch (Exception e) {
            log.error("[积分服务] 处理订单支付消息失败: {}", e.getMessage(), e);
        }

        log.info("==========================================");
    }

    @RabbitListener(queues = "points.order.cancel.queue", containerFactory = "rabbitListenerContainerFactory")
    @Transactional
    public void handleOrderCancelled(Map<String, Object> message) {
        log.info("========== 积分服务收到订单取消消息 ==========");
        log.info("消息内容: {}", message);

        try {
            Long orderId = Long.valueOf(message.get("orderId").toString());
            Long userId = Long.valueOf(message.get("userId").toString());
            String username = getString(message, "username");
            String orderNo = getString(message, "orderNo");
            String cancelReason = getString(message, "cancelReason");

            // 检查该订单是否已支付（已给积分），如果有则扣回
            LambdaQueryWrapper<PointsLog> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PointsLog::getOrderId, orderId)
                    .eq(PointsLog::getType, "ORDER_PAID");
            PointsLog paidLog = pointsLogMapper.selectOne(wrapper);

            if (paidLog != null) {
                // 检查是否已扣回（防止重复消费）
                LambdaQueryWrapper<PointsLog> cancelWrapper = new LambdaQueryWrapper<>();
                cancelWrapper.eq(PointsLog::getOrderId, orderId)
                        .eq(PointsLog::getType, "ORDER_CANCELLED");
                Long cancelCount = pointsLogMapper.selectCount(cancelWrapper);
                if (cancelCount > 0) {
                    log.warn("[积分服务] 订单 {} 的积分已扣回，跳过重复处理", orderId);
                    log.info("==========================================");
                    return;
                }

                // 扣回积分
                PointsLog cancelLog = new PointsLog();
                cancelLog.setUserId(userId);
                cancelLog.setOrderId(orderId);
                cancelLog.setOrderNo(orderNo);
                cancelLog.setPoints(-paidLog.getPoints());  // 负数扣回
                cancelLog.setType("ORDER_CANCELLED");
                cancelLog.setRemark(String.format("订单取消扣回积分，原因: %s", cancelReason));
                cancelLog.setCreateTime(LocalDateTime.now());

                pointsLogMapper.insert(cancelLog);

                log.info("[积分扣回] 用户: {} (ID: {}), 订单ID: {}, 扣回积分: {}分, 原因: {}",
                        username, userId, orderId, paidLog.getPoints(), cancelReason);
            } else {
                log.info("[积分服务] 订单 {} 未支付，无需扣回积分", orderId);
            }
        } catch (Exception e) {
            log.error("[积分服务] 处理订单取消消息失败: {}", e.getMessage(), e);
        }

        log.info("==========================================");
    }

    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }
}
