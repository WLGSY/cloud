package com.hnkjzy.order.client;

import com.hnkjzy.order.dto.DishDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "dish-service", path = "/dish", configuration = com.hnkjzy.order.config.FeignResultConfig.class)
public interface DishClient {

    @GetMapping("/{id}")
    DishDTO getDishById(@PathVariable("id") Long id);

    @PutMapping("/sales/add/{id}")
    void addSales(@PathVariable("id") Long id, @RequestParam("count") Integer count);
}