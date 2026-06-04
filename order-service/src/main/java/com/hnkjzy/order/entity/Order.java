package com.hnkjzy.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("`order`")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private String userName;
    private String userPhone;
    private Long dishId;
    private String dishName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalAmount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private LocalDateTime cancelTime;
    private String cancelReason;
}