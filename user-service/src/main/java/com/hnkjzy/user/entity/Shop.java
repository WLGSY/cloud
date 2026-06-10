package com.hnkjzy.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("shop")
public class Shop {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String logo;
    private String phone;
    private String address;
    private String openTime;
    private String closeTime;
    private String description;
    private Long userId;
    private Integer status;
    private LocalDateTime createTime;
}
