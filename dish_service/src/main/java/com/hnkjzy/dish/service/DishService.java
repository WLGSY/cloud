package com.hnkjzy.dish.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hnkjzy.dish.entity.Dish;
import java.util.List;

public interface DishService extends IService<Dish> {

    // ===== 单个操作 =====
    boolean onSale(Long id);      // 上架
    boolean offSale(Long id);     // 下架
    boolean publish(Long id);     // 发布（等价于上架）
    boolean recall(Long id);      // 撤回（等价于下架）

    // ===== 批量操作 =====
    boolean batchOnSale(List<Long> ids);     // 批量上架
    boolean batchOffSale(List<Long> ids);    // 批量下架
    boolean batchPublish(List<Long> ids);    // 批量发布
    boolean batchRecall(List<Long> ids);     // 批量撤回
    boolean batchDelete(List<Long> ids);     // 批量删除

    // ===== 查询功能 =====
    List<Dish> searchByName(String keyword);      // 模糊查询
    List<Dish> getPublishedDishes();              // 查询已发布菜品
    
    // 新增：获取菜品列表（支持分类、关键词搜索、分页）
    List<Dish> getDishList(Long categoryId, String keyword);
}