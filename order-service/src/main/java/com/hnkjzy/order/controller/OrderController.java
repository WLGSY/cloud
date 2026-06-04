package com.hnkjzy.order.controller;

import com.hnkjzy.order.entity.Order;
import com.hnkjzy.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public Order createOrder(@RequestParam Long userId,
                             @RequestParam Long dishId,
                             @RequestParam Integer quantity) {
        return orderService.createOrder(userId, dishId, quantity);
    }

    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Long id) {
        return orderService.getById(id);
    }

    @GetMapping("/user/{userId}")
    public List<Order> getOrdersByUserId(@PathVariable Long userId) {
        return orderService.getOrdersByUserId(userId);
    }

    @GetMapping("/list")
    public List<Order> listAll() {
        return orderService.list();
    }

    @PostMapping("/cancel")
    public String cancelOrder(@RequestParam Long id, 
                              @RequestParam(required = false, defaultValue = "用户主动取消") String reason) {
        boolean success = orderService.cancelOrder(id, reason);
        return success ? "取消成功" : "取消失败";
    }

    @PostMapping("/pay")
    public String payOrder(@RequestParam Long id) {
        log.info("[订单服务] 开始支付订单，订单ID: {}", id);
        boolean success = orderService.updateStatus(id, 1);
        if (success) {
            log.info("[订单服务] 订单支付完成，订单ID: {}", id);
            return "支付成功";
        } else {
            log.error("[订单服务] 订单支付失败，订单ID: {}", id);
            return "支付失败";
        }
    }

    @PutMapping("/status/{id}")
    public String updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        boolean success = orderService.updateStatus(id, status);
        return success ? "更新成功" : "更新失败";
    }
}