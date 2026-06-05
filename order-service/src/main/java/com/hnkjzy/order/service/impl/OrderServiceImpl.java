package com.hnkjzy.order.service.impl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hnkjzy.order.client.DishClient;
import com.hnkjzy.order.client.UserClient;
import com.hnkjzy.order.dto.DishDTO;
import com.hnkjzy.order.dto.UserDTO;
import com.hnkjzy.order.entity.Order;
import com.hnkjzy.order.mapper.OrderMapper;
import com.hnkjzy.order.service.OrderService;
import com.hnkjzy.order.service.OrderMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private DishClient dishClient;

    @Autowired
    private OrderMessageSender orderMessageSender;

    private final AtomicLong orderNoCounter = new AtomicLong(1);
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private String generateOrderNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long seq = orderNoCounter.getAndIncrement() % 10000;
        return "ORD" + date + String.format("%04d", seq);
    }

    @Override
    @Transactional
    public Order createOrder(Long userId, Long dishId, Integer quantity) {
        // 带超时控制的用户服务调用
        UserDTO user = callUserWithTimeout(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在，userId=" + userId);
        }

        DishDTO dish = getDishByIdWithFallback(dishId);
        if (dish == null) {
            throw new RuntimeException("菜品不存在，dishId=" + dishId);
        }

        BigDecimal totalAmount = dish.getPrice().multiply(new BigDecimal(quantity));

        // 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setUserName(user.getUsername());
        order.setDishId(dishId);
        order.setDishName(dish.getName());
        order.setPrice(dish.getPrice());
        order.setQuantity(quantity);
        order.setTotalAmount(totalAmount);
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());

        this.save(order);

        // 发送订单创建消息
        orderMessageSender.sendOrderCreated(order);

        return order;
    }

    /**
     * 带3秒超时的用户服务调用（超时降级）
     */
    private UserDTO callUserWithTimeout(Long userId) {
        System.out.println("[超时检测] 开始调用用户服务，userId=" + userId);

        Future<UserDTO> future = executor.submit(() -> {
            System.out.println("[异步线程] 开始执行用户服务调用");
            UserDTO result = getUserByIdWithFallback(userId);
            System.out.println("[异步线程] 用户服务调用完成");
            return result;
        });

        try {
            System.out.println("[超时检测] 等待结果，超时阈值: 3000ms");
            UserDTO user = future.get(3, TimeUnit.SECONDS);
            System.out.println("[超时检测] 成功获取结果");
            return user;
        } catch (TimeoutException e) {
            System.out.println("[超时降级] 用户服务响应超过3秒，主动熔断，使用默认用户，userId=" + userId);
            future.cancel(true);

            UserDTO defaultUser = new UserDTO();
            defaultUser.setId(userId);
            defaultUser.setUsername("默认用户(超时降级)");
            defaultUser.setPhone("00000000000");
            return defaultUser;
        } catch (Exception e) {
            System.err.println("[ERROR] 调用用户服务异常: " + e.getMessage());
            UserDTO defaultUser = new UserDTO();
            defaultUser.setId(userId);
            defaultUser.setUsername("默认用户(调用异常)");
            defaultUser.setPhone("00000000000");
            return defaultUser;
        }
    }

    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    @Transactional
    public boolean cancelOrder(Long id, String reason) {
        Order order = this.getById(id);
        if (order == null) {
            log.warn("订单不存在，订单ID: {}", id);
            return false;
        }
        
        // 只允许取消待支付(0)或已支付(1)的订单
        if (order.getStatus() != 0 && order.getStatus() != 1) {
            log.warn("订单状态不允许取消，订单ID: {}, 当前状态: {}", id, order.getStatus());
            return false;
        }
        
        order.setStatus(2);
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(reason != null ? reason : "用户主动取消");
        
        boolean success = this.updateById(order);
        
        if (success) {
            try {
                orderMessageSender.sendOrderCancelled(order);
                log.info("[订单服务] 订单取消消息已发送，订单号: {}, 用户: {}, 原因: {}", 
                        order.getOrderNo(), order.getUserName(), order.getCancelReason());
            } catch (Exception e) {
                log.error("[订单服务] 订单状态已更新，但取消消息发送失败，订单ID: {}", id, e);
            }
        }
        
        return success;
    }

    @Override
    @Transactional
    public boolean cancelOrder(Long id) {
        return cancelOrder(id, "用户主动取消");
    }

    @Override
    @Transactional
    public boolean updateStatus(Long id, Integer status) {
        Order order = this.getById(id);
        if (order == null) {
            log.warn("订单不存在，订单ID: {}", id);
            return false;
        }

        order.setStatus(status);

        if (status == 1) {
            order.setPayTime(LocalDateTime.now());
            boolean updated = this.updateById(order);
            if (updated) {
                try {
                    orderMessageSender.sendOrderPaid(order);
                    log.info("[订单服务] 订单支付成功，订单ID: {}", id);
                } catch (Exception e) {
                    log.error("[订单服务] 订单状态已更新，但消息发送失败，订单ID: {}", id, e);
                }
            }
            return updated;
        } else if (status == 2) {
            order.setCancelTime(LocalDateTime.now());
            boolean updated = this.updateById(order);
            if (updated) {
                try {
                    orderMessageSender.sendOrderCancelled(order);
                    log.info("[订单服务] 订单取消消息已发送，订单号: {}, 用户: {}", 
                            order.getOrderNo(), order.getUserName());
                } catch (Exception e) {
                    log.error("[订单服务] 订单状态已更新，但消息发送失败，订单ID: {}", id, e);
                }
            }
            return updated;
        } else {
            return this.updateById(order);
        }
    }

    // ==================== Sentinel 熔断降级 ====================

    @SentinelResource(value = "getUserById", fallback = "fallbackGetUser")
    public UserDTO getUserByIdWithFallback(Long userId) {
        return userClient.getUserById(userId);
    }

    /**
     * Sentinel 熔断降级方法
     */
    public UserDTO fallbackGetUser(Long userId, Throwable e) {
        System.out.println("[Sentinel熔断降级] 用户服务不可用，触发熔断保护，使用默认用户，userId=" + userId + "，异常原因：" + e.getMessage());

        UserDTO defaultUser = new UserDTO();
        defaultUser.setId(userId);
        defaultUser.setUsername("默认用户(熔断降级)");
        defaultUser.setPhone("00000000000");
        return defaultUser;
    }

    @SentinelResource(value = "getDishById", fallback = "fallbackGetDish")
    public DishDTO getDishByIdWithFallback(Long dishId) {
        return dishClient.getDishById(dishId);
    }

    public DishDTO fallbackGetDish(Long dishId, Throwable e) {
        System.out.println("[Sentinel熔断降级] 菜品服务不可用，触发熔断保护，使用默认菜品，dishId=" + dishId + "，异常原因：" + e.getMessage());

        DishDTO defaultDish = new DishDTO();
        defaultDish.setId(dishId);
        defaultDish.setName("默认菜品(熔断降级)");
        defaultDish.setPrice(new BigDecimal("9.9"));
        defaultDish.setDescription("这是降级后的默认菜品");
        defaultDish.setStatus(1);
        return defaultDish;
    }
}