package com.hnkjzy.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hnkjzy.order.common.PageResult;
import com.hnkjzy.order.dto.CreateOrderDTO;
import com.hnkjzy.order.dto.OrderVO;
import com.hnkjzy.order.entity.Order;
import java.util.List;

public interface OrderService extends IService<Order> {

    /**
     * 创建订单（支持多菜品）
     */
    Order createOrder(CreateOrderDTO dto);

    /**
     * 根据用户ID分页查询订单
     */
    PageResult<OrderVO> getOrdersByUserId(Long userId, Integer pageNum, Integer pageSize, Integer status);

    /**
     * 取消订单
     */
    boolean cancelOrder(Long id, String reason);

    boolean cancelOrder(Long id);

    /**
     * 更新订单状态
     */
    boolean updateStatus(Long id, Integer status);

    /**
     * 确认收货（状态 1→3）
     */
    boolean completeOrder(Long id);

    /**
     * 根据店铺ID查询订单（商家端用）
     */
    PageResult<OrderVO> getOrdersByShopId(Long shopId, Integer pageNum, Integer pageSize, Integer status);
}
