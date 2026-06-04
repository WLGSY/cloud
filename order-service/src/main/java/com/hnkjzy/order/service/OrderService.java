package com.hnkjzy.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hnkjzy.order.entity.Order;
import java.util.List;

public interface OrderService extends IService<Order> {

    Order createOrder(Long userId, Long dishId, Integer quantity);
    List<Order> getOrdersByUserId(Long userId);
    boolean cancelOrder(Long id);
    boolean cancelOrder(Long id, String reason);
    boolean updateStatus(Long id, Integer status);
}