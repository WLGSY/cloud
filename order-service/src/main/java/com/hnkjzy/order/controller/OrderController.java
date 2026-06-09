package com.hnkjzy.order.controller;

import com.hnkjzy.order.common.PageResult;
import com.hnkjzy.order.common.Result;
import com.hnkjzy.order.dto.CreateOrderDTO;
import com.hnkjzy.order.dto.OrderVO;
import com.hnkjzy.order.entity.Order;
import com.hnkjzy.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单 — 接收前端 JSON body
     * 前端发送: { userId, items:[{dishId,quantity}], totalAmount, receiver, userPhone, address }
     * 使用 X-User-Id header（Gateway JWT 注入）作为 userId 的 fallback
     */
    @PostMapping("/create")
    public Result<Order> createOrder(@RequestBody CreateOrderDTO dto,
                                      @RequestHeader(value = "X-User-Id", required = false) Long xUserId) {
        // userId fallback: 如果请求体中没有 userId，使用 Gateway JWT 过滤器中注入的 X-User-Id
        if (dto.getUserId() == null) {
            dto.setUserId(xUserId);
        }

        log.info("[订单服务] 收到创建订单请求，userId: {}, 菜品数: {}", dto.getUserId(),
                dto.getItems() != null ? dto.getItems().size() : 0);
        try {
            Order order = orderService.createOrder(dto);
            log.info("[订单服务] 订单创建成功，订单ID: {}, 订单号: {}", order.getId(), order.getOrderNo());
            return Result.success("订单创建成功", order);
        } catch (Exception e) {
            log.error("[订单服务] 订单创建失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    public Result<Order> getOrderById(@PathVariable Long id) {
        Order order = orderService.getById(id);
        return order != null ? Result.success(order) : Result.error("订单不存在");
    }

    /**
     * 根据用户ID查询订单列表（兼容旧接口）
     */
    @GetMapping("/user/{userId}")
    public Result<PageResult<OrderVO>> getOrdersByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {
        PageResult<OrderVO> result = orderService.getOrdersByUserId(userId, pageNum, pageSize, status);
        return Result.success(result);
    }

    /**
     * 订单列表（分页 + 状态筛选）— 用户端调用
     * 前端通过 X-User-Id header 传入 userId（由 Gateway JWT filter 注入）
     */
    @GetMapping("/list")
    public Result<PageResult<OrderVO>> list(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {
        if (userId == null) {
            log.warn("[订单服务] X-User-Id header 为空");
            return Result.error(401, "未获取到用户信息");
        }
        log.info("[订单服务] 用户订单列表，userId: {}", userId);
        PageResult<OrderVO> result = orderService.getOrdersByUserId(userId, pageNum, pageSize, status);
        return Result.success(result);
    }

    /**
     * 商家端：按店铺ID查询订单列表
     */
    @GetMapping("/shop")
    public Result<PageResult<OrderVO>> listByShop(
            @RequestParam Long shopId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {
        log.info("[订单服务] 商家订单列表，shopId: {}", shopId);
        PageResult<OrderVO> result = orderService.getOrdersByShopId(shopId, pageNum, pageSize, status);
        return Result.success(result);
    }

    /**
     * 支付订单 — RESTful 风格
     */
    @PostMapping("/{id}/pay")
    public Result<String> payOrder(@PathVariable Long id) {
        log.info("[订单服务] 开始支付订单，订单ID: {}", id);
        boolean success = orderService.updateStatus(id, 1);
        if (success) {
            log.info("[订单服务] 订单支付完成，订单ID: {}", id);
            return Result.success("支付成功");
        } else {
            log.error("[订单服务] 订单支付失败，订单ID: {}", id);
            return Result.error("支付失败");
        }
    }

    /**
     * 确认收货 — 将已支付订单变为已完成
     */
    @PostMapping("/{id}/complete")
    public Result<String> completeOrder(@PathVariable Long id) {
        log.info("[订单服务] 确认收货，订单ID: {}", id);
        boolean success = orderService.completeOrder(id);
        if (success) {
            log.info("[订单服务] 订单已完成，订单ID: {}", id);
            return Result.success("确认收货成功");
        } else {
            log.error("[订单服务] 确认收货失败，订单ID: {}", id);
            return Result.error("确认收货失败，请检查订单状态");
        }
    }

    /**
     * 取消订单 — RESTful 风格
     */
    @PostMapping("/{id}/cancel")
    public Result<String> cancelOrder(@PathVariable Long id,
                                       @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "用户主动取消") : "用户主动取消";
        log.info("[订单服务] 取消订单，订单ID: {}, 原因: {}", id, reason);
        boolean success = orderService.cancelOrder(id, reason);
        return success ? Result.success("取消成功") : Result.error("取消失败");
    }

    /**
     * 更新订单状态（兼容旧接口）
     */
    @PutMapping("/status/{id}")
    public Result<String> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        boolean success = orderService.updateStatus(id, status);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }
}
