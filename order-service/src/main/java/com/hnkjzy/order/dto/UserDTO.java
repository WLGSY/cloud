package com.hnkjzy.order.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String phone;
    private LocalDateTime createTime;
}