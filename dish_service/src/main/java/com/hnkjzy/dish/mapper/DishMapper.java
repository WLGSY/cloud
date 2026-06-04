package com.hnkjzy.dish.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hnkjzy.dish.entity.Dish;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}