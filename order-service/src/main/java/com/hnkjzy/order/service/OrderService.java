package com.hnkjzy.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hnkjzy.order.common.PageResult;
import com.hnkjzy.order.dto.CreateOrderDTO;
import com.hnkjzy.order.dto.OrderVO;
import com.hnkjzy.order.entity.Order;
import java.util.List;

public interface OrderService extends IService<Order> {

    Order createOrder(CreateOrderDTO dto);

    PageResult<OrderVO> getOrdersByUserId(Long userId, Integer pageNum, Integer pageSize, Integer status);

    boolean cancelOrder(Long id, String reason);
    boolean cancelOrder(Long id);

    boolean updateStatus(Long id, Integer status);
    boolean completeOrder(Long id);

    PageResult<OrderVO> getOrdersByShopId(Long shopId, Integer pageNum, Integer pageSize, Integer status, String keyword);

    // ===== 骑手相关 =====

    /** 可接订单池（已支付 + 未分配骑手） */
    PageResult<OrderVO> getAvailableOrders(Integer pageNum, Integer pageSize);

    /** 骑手接单 */
    boolean acceptOrder(Long orderId, Long riderId);

    /** 骑手取货 */
    boolean pickupOrder(Long orderId, Long riderId);

    /** 骑手送达 */
    boolean deliverOrder(Long orderId, Long riderId);

    /** 骑手配送列表 */
    PageResult<OrderVO> getRiderOrders(Long riderId, Integer deliveryStatus, Integer pageNum, Integer pageSize);
}
