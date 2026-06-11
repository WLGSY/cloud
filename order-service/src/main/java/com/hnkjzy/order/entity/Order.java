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
    private BigDecimal totalAmount;
    private Long shopId;            // 店铺ID
    private Integer status;         // 0=待支付, 1=已支付, 2=已取消, 3=已完成, 4=配送中, 5=已送达

    // 配送信息
    private Long riderId;           // 骑手ID
    private Integer deliveryStatus; // 配送状态: 0=待取货, 1=配送中, 2=已送达

    // 收货信息
    private String receiver;
    private String address;
    private String remark;

    // 订单菜品明细 (JSON字符串)
    private String items;

    // 时间字段
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private LocalDateTime cancelTime;
    private String cancelReason;
}
