package com.hnkjzy.points.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("points_log")
public class PointsLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long orderId;
    private String orderNo;
    private Integer points;       // 正数=获得，负数=扣除
    private String type;          // ORDER_PAID / ORDER_CANCELLED
    private String remark;
    private LocalDateTime createTime;
}
