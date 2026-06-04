package com.hnkjzy.notification.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderMessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String messageId;
    private Long orderId;
    private String orderNo;
    private Long userId;
    private String username;
    private String userPhone;
    private BigDecimal totalAmount;
    private Integer status;
    private String statusDesc;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private LocalDateTime cancelTime;
    private String cancelReason;
    private String messageType;
    private Integer version;
}
