package com.hnkjzy.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("customer_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private String password;
    private String phone;

    // 新增扩展字段
    private String nickname;
    private String email;
    private String gender;
    private String birthday;
    private String avatar;
    private String role;

    private LocalDateTime createTime;
}
