package com.hnkjzy.order.client;

import com.hnkjzy.order.dto.DishDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "dish-service", path = "/dish")
public interface DishClient {

    @GetMapping("/{id}")
    DishDTO getDishById(@PathVariable("id") Long id);
}