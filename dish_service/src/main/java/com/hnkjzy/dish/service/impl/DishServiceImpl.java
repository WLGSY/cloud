package com.hnkjzy.dish.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hnkjzy.dish.entity.Dish;
import com.hnkjzy.dish.mapper.DishMapper;
import com.hnkjzy.dish.service.DishService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    // ===== 单个操作 =====

    @Override
    public boolean onSale(Long id) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setStatus(1);
        return this.updateById(dish);
    }

    @Override
    public boolean offSale(Long id) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setStatus(0);
        return this.updateById(dish);
    }

    @Override
    public boolean publish(Long id) {
        return onSale(id);
    }

    @Override
    public boolean recall(Long id) {
        return offSale(id);
    }

    // ===== 批量操作 =====

    @Override
    public boolean batchOnSale(List<Long> ids) {
        return this.lambdaUpdate()
                .in(Dish::getId, ids)
                .set(Dish::getStatus, 1)
                .update();
    }

    @Override
    public boolean batchOffSale(List<Long> ids) {
        return this.lambdaUpdate()
                .in(Dish::getId, ids)
                .set(Dish::getStatus, 0)
                .update();
    }

    @Override
    public boolean batchPublish(List<Long> ids) {
        return batchOnSale(ids);
    }

    @Override
    public boolean batchRecall(List<Long> ids) {
        return batchOffSale(ids);
    }

    @Override
    public boolean batchDelete(List<Long> ids) {
        return this.removeByIds(ids);
    }

    // ===== 查询功能 =====

    @Override
    public List<Dish> searchByName(String keyword) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Dish::getName, keyword);
        }
        return this.list(wrapper);
    }

    @Override
    public List<Dish> getPublishedDishes() {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dish::getStatus, 1);
        return this.list(wrapper);
    }
}