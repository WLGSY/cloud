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

    // 新增：获取菜品列表（支持分类、关键词搜索、只返回上架状态）
    @Override
    public List<Dish> getDishList(Long categoryId, String keyword) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询上架状态的菜品
        wrapper.eq(Dish::getStatus, 1);
        
        // 按分类筛选
        if (categoryId != null) {
            wrapper.eq(Dish::getCategoryId, categoryId);
        }
        
        // 按关键词搜索
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Dish::getName, keyword);
        }
        
        // 排序：先按创建时间降序
        wrapper.orderByDesc(Dish::getCreateTime);
        
        return this.list(wrapper);
    }

    @Override
    public boolean addSales(Long id, Integer count) {
        Dish dish = this.getById(id);
        if (dish == null) return false;
        int newSales = (dish.getSales() != null ? dish.getSales() : 0) + count;
        return this.lambdaUpdate()
                .eq(Dish::getId, id)
                .set(Dish::getSales, newSales)
                .update();
    }
}