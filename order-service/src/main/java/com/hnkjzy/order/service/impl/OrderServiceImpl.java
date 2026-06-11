package com.hnkjzy.order.service.impl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hnkjzy.order.client.DishClient;
import com.hnkjzy.order.client.UserClient;
import com.hnkjzy.order.common.PageResult;
import com.hnkjzy.order.dto.CreateOrderDTO;
import com.hnkjzy.order.dto.DishDTO;
import com.hnkjzy.order.dto.OrderVO;
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
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

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
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String generateOrderNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long seq = orderNoCounter.getAndIncrement() % 10000;
        return "ORD" + date + String.format("%04d", seq);
    }

    @Override
    @Transactional
    public Order createOrder(CreateOrderDTO dto) {
        Long userId = dto.getUserId();

        // 1. 查询用户信息（带超时控制）
        UserDTO user = callUserWithTimeout(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在，userId=" + userId);
        }

        // 2. 遍历菜品，查询价格，构建 items JSON
        List<Map<String, Object>> itemList = new ArrayList<>();
        BigDecimal calculatedTotal = BigDecimal.ZERO;

        for (CreateOrderDTO.OrderItemDTO item : dto.getItems()) {
            DishDTO dish = getDishByIdWithFallback(item.getDishId());
            if (dish == null) {
                throw new RuntimeException("菜品不存在，dishId=" + item.getDishId());
            }

            BigDecimal itemTotal = dish.getPrice().multiply(new BigDecimal(item.getQuantity()));
            calculatedTotal = calculatedTotal.add(itemTotal);

            Map<String, Object> itemMap = new LinkedHashMap<>();
            itemMap.put("dishId", dish.getId());
            itemMap.put("name", dish.getName());
            itemMap.put("price", dish.getPrice());
            itemMap.put("quantity", item.getQuantity());
            itemList.add(itemMap);
        }

        // 3. 前端传的 totalAmount 优先，否则用计算的
        BigDecimal finalAmount = dto.getTotalAmount() != null && dto.getTotalAmount().compareTo(BigDecimal.ZERO) > 0
                ? dto.getTotalAmount() : calculatedTotal;

        // 4. 序列化 items 为 JSON
        String itemsJson;
        try {
            itemsJson = objectMapper.writeValueAsString(itemList);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("订单明细序列化失败", e);
        }

        // 5. 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setShopId(dto.getShopId());
        order.setUserName(user.getUsername());
        order.setUserPhone(dto.getUserPhone() != null ? dto.getUserPhone() : user.getPhone());
        order.setTotalAmount(finalAmount);
        order.setStatus(0);
        order.setReceiver(dto.getReceiver());
        order.setAddress(dto.getAddress());
        order.setRemark(dto.getRemark());
        order.setItems(itemsJson);
        order.setCreateTime(LocalDateTime.now());

        this.save(order);

        // 6. 异步更新菜品销量（最终一致性，失败不影响主流程）
        for (CreateOrderDTO.OrderItemDTO item : dto.getItems()) {
            try {
                dishClient.addSales(item.getDishId(), item.getQuantity());
                log.info("[订单服务] 菜品销量已更新，dishId: {}, 增加: {}", item.getDishId(), item.getQuantity());
            } catch (Exception e) {
                log.error("[订单服务] 更新菜品销量失败，dishId: {}", item.getDishId(), e);
            }
        }

        // 7. 发送订单创建消息
        try {
            orderMessageSender.sendOrderCreated(order);
        } catch (Exception e) {
            log.error("[订单服务] 订单创建消息发送失败", e);
        }

        return order;
    }

    @Override
    public PageResult<OrderVO> getOrdersByUserId(Long userId, Integer pageNum, Integer pageSize, Integer status) {
        // 构建分页查询
        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);

        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }

        wrapper.orderByDesc(Order::getCreateTime);

        Page<Order> resultPage = this.page(page, wrapper);

        // 转换为 OrderVO
        List<OrderVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(voList, resultPage.getTotal());
    }

    @Override
    public PageResult<OrderVO> getOrdersByShopId(Long shopId, Integer pageNum, Integer pageSize, Integer status, String keyword) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getShopId, shopId);

        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }

        // 关键词搜索：订单号、用户名、收货人、地址
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(Order::getOrderNo, keyword)
                    .or().like(Order::getUserName, keyword)
                    .or().like(Order::getReceiver, keyword)
                    .or().like(Order::getAddress, keyword));
        }

        wrapper.orderByDesc(Order::getCreateTime);

        Page<Order> resultPage = this.page(page, wrapper);
        List<OrderVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return new PageResult<>(voList, resultPage.getTotal());
    }

    // ==================== 骑手相关方法 ====================

    @Override
    public PageResult<OrderVO> getAvailableOrders(Integer pageNum, Integer pageSize) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, 1)          // 已支付
               .isNull(Order::getRiderId)         // 未分配骑手
               .orderByDesc(Order::getCreateTime);
        Page<Order> resultPage = this.page(page, wrapper);
        List<OrderVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO).collect(Collectors.toList());
        return new PageResult<>(voList, resultPage.getTotal());
    }

    @Override
    @Transactional
    public boolean acceptOrder(Long orderId, Long riderId) {
        Order order = this.getById(orderId);
        if (order == null || order.getStatus() != 1) return false;
        if (order.getRiderId() != null) return false; // 已被其他骑手接单
        order.setRiderId(riderId);
        order.setDeliveryStatus(0); // 待取货
        return this.updateById(order);
    }

    @Override
    @Transactional
    public boolean pickupOrder(Long orderId, Long riderId) {
        Order order = this.getById(orderId);
        if (order == null || !riderId.equals(order.getRiderId())) return false;
        if (order.getDeliveryStatus() != 0) return false;
        order.setDeliveryStatus(1); // 配送中
        order.setStatus(4);         // 订单状态 → 配送中
        return this.updateById(order);
    }

    @Override
    @Transactional
    public boolean deliverOrder(Long orderId, Long riderId) {
        Order order = this.getById(orderId);
        if (order == null || !riderId.equals(order.getRiderId())) return false;
        if (order.getDeliveryStatus() != 1) return false;
        order.setDeliveryStatus(2); // 已送达
        order.setStatus(3);         // 订单状态 → 已完成
        return this.updateById(order);
    }

    @Override
    public PageResult<OrderVO> getRiderOrders(Long riderId, Integer deliveryStatus, Integer pageNum, Integer pageSize) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getRiderId, riderId);
        if (deliveryStatus != null) {
            wrapper.eq(Order::getDeliveryStatus, deliveryStatus);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        Page<Order> resultPage = this.page(page, wrapper);
        List<OrderVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO).collect(Collectors.toList());
        return new PageResult<>(voList, resultPage.getTotal());
    }
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
            } catch (Exception e) {
                log.error("[订单服务] 订单取消消息发送失败，订单ID: {}", id, e);
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
                } catch (Exception e) {
                    log.error("[订单服务] 支付消息发送失败，订单ID: {}", id, e);
                }
            }
            return updated;
        } else if (status == 2) {
            order.setCancelTime(LocalDateTime.now());
            boolean updated = this.updateById(order);
            if (updated) {
                try {
                    orderMessageSender.sendOrderCancelled(order);
                } catch (Exception e) {
                    log.error("[订单服务] 取消消息发送失败，订单ID: {}", id, e);
                }
            }
            return updated;
        } else {
            return this.updateById(order);
        }
    }

    // ==================== Order → OrderVO 转换 ====================

    private OrderVO convertToVO(Order order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setUserId(order.getUserId());
        vo.setShopId(order.getShopId());
        vo.setUserName(order.getUserName());
        vo.setUserPhone(order.getUserPhone());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setStatusText(getStatusText(order.getStatus()));
        vo.setRiderId(order.getRiderId());
        vo.setDeliveryStatus(order.getDeliveryStatus());
        vo.setDeliveryStatusText(getDeliveryStatusText(order.getDeliveryStatus()));
        vo.setReceiver(order.getReceiver());
        vo.setAddress(order.getAddress());
        vo.setRemark(order.getRemark());
        vo.setCreateTime(order.getCreateTime());
        vo.setPayTime(order.getPayTime());
        vo.setCancelTime(order.getCancelTime());
        vo.setCancelReason(order.getCancelReason());

        // 解析 items JSON
        List<OrderVO.OrderItemVO> itemVOs = OrderVO.parseItems(order.getItems());
        vo.setItems(itemVOs);
        vo.setTotalCount(itemVOs.stream().mapToInt(OrderVO.OrderItemVO::getQuantity).sum());

        return vo;
    }

    @Override
    @Transactional
    public boolean completeOrder(Long id) {
        Order order = this.getById(id);
        if (order == null) {
            log.warn("[订单服务] 订单不存在，订单ID: {}", id);
            return false;
        }
        if (order.getStatus() != 1) {
            log.warn("[订单服务] 只有已支付订单可以确认收货，订单ID: {}, 当前状态: {}", id, order.getStatus());
            return false;
        }
        order.setStatus(3);  // 已完成
        return this.updateById(order);
    }

    private String getStatusText(Integer status) {
        switch (status) {
            case 0: return "待支付";
            case 1: return "已支付";
            case 2: return "已取消";
            case 3: return "已完成";
            case 4: return "配送中";
            default: return "未知";
        }
    }

    private String getDeliveryStatusText(Integer ds) {
        if (ds == null) return null;
        switch (ds) {
            case 0: return "待取货";
            case 1: return "配送中";
            case 2: return "已送达";
            default: return "未知";
        }
    }

    // ==================== Sentinel 熔断降级 ====================

    /**
     * 带3秒超时的用户服务调用
     * 超时或异常时抛出 RuntimeException，不创建有误订单
     */
    private UserDTO callUserWithTimeout(Long userId) {
        Future<UserDTO> future = executor.submit(() -> getUserByIdWithFallback(userId));

        try {
            return future.get(3, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.error("[超时降级] 用户服务响应超过3秒，userId={}", userId);
            future.cancel(true);
            throw new RuntimeException("用户服务繁忙，请稍后重试");
        } catch (Exception e) {
            log.error("[ERROR] 调用用户服务异常: {}", e.getMessage());
            throw new RuntimeException("用户服务不可用，请稍后重试");
        }
    }

    @SentinelResource(value = "getUserById", fallback = "fallbackGetUser")
    public UserDTO getUserByIdWithFallback(Long userId) {
        return userClient.getUserById(userId);
    }

    public UserDTO fallbackGetUser(Long userId, Throwable e) {
        log.error("[Sentinel熔断降级] 用户服务不可用，userId={}", userId);
        throw new RuntimeException("用户服务暂不可用，请稍后重试");
    }

    @SentinelResource(value = "getDishById", fallback = "fallbackGetDish")
    public DishDTO getDishByIdWithFallback(Long dishId) {
        return dishClient.getDishById(dishId);
    }

    public DishDTO fallbackGetDish(Long dishId, Throwable e) {
        log.error("[Sentinel熔断降级] 菜品服务不可用，dishId={}", dishId);
        throw new RuntimeException("菜品服务暂不可用，请稍后重试");
    }
}
