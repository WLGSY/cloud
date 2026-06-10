package com.hnkjzy.order.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderDTO {
    private Long userId;
    private Long shopId;                // 店铺ID
    private List<OrderItemDTO> items;   // 订单菜品明细
    private BigDecimal totalAmount;
    private String receiver;
    private String userPhone;
    private String address;
    private String remark;

    @Data
    public static class OrderItemDTO {
        private Long dishId;
        private Integer quantity;
    }
}
