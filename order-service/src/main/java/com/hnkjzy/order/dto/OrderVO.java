package com.hnkjzy.order.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Data
public class OrderVO {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long shopId;
    private String userName;
    private String userPhone;
    private BigDecimal totalAmount;
    private Integer status;
    private String statusText;    // 状态文本
    private String receiver;
    private String address;
    private String remark;
    private List<OrderItemVO> items;
    private Integer totalCount;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private LocalDateTime cancelTime;
    private String cancelReason;

    @Data
    public static class OrderItemVO {
        private Long dishId;
        private String name;
        private BigDecimal price;
        private Integer quantity;
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 将 items JSON 字符串反序列化为 OrderItemVO 列表
     */
    public static List<OrderItemVO> parseItems(String itemsJson) {
        if (itemsJson == null || itemsJson.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(itemsJson, new TypeReference<List<OrderItemVO>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
