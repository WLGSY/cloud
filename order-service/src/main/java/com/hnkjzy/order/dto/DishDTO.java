package com.hnkjzy.order.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DishDTO {
    private Long id;
    private String name;
    private BigDecimal price;
    private String description;
    private Integer status;
}