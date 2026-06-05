package com.hnkjzy.dish.Controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hnkjzy.dish.common.PageResult;
import com.hnkjzy.dish.common.Result;
import com.hnkjzy.dish.entity.Dish;
import com.hnkjzy.dish.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    // ===== 基础CRUD =====

    // 新增：首页菜品列表查询接口（支持分页、分类、关键词搜索）
    @GetMapping("/list")
    public Result<PageResult<Dish>> list(@RequestParam(required = false) Long categoryId,
                                          @RequestParam(required = false) String keyword,
                                          @RequestParam(defaultValue = "1") Integer pageNum,
                                          @RequestParam(defaultValue = "12") Integer pageSize) {
        // 构建分页对象
        Page<Dish> page = new Page<>(pageNum, pageSize);
        
        // 构建查询条件
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dish::getStatus, 1); // 只查询上架状态
        
        if (categoryId != null) {
            wrapper.eq(Dish::getCategoryId, categoryId);
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Dish::getName, keyword);
        }
        
        wrapper.orderByDesc(Dish::getCreateTime);
        
        // 执行分页查询
        Page<Dish> resultPage = dishService.page(page, wrapper);
        
        // 封装返回结果
        PageResult<Dish> pageResult = new PageResult<>(resultPage.getRecords(), resultPage.getTotal());
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<Dish> getById(@PathVariable Long id) {
        Dish dish = dishService.getById(id);
        return dish != null ? Result.success(dish) : Result.error("菜品不存在");
    }

    @PostMapping
    public Result<String> add(@RequestBody Dish dish) {
        return dishService.save(dish) ? Result.success("添加成功") : Result.error("添加失败");
    }

    @PutMapping
    public Result<String> update(@RequestBody Dish dish) {
        return dishService.updateById(dish) ? Result.success("修改成功") : Result.error("修改失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        return dishService.removeById(id) ? Result.success("删除成功") : Result.error("删除失败");
    }

    // ===== 发布/撤回（单个） =====

    @PutMapping("/publish/{id}")
    public Result<String> publish(@PathVariable Long id) {
        return dishService.publish(id) ? Result.success("发布成功") : Result.error("发布失败");
    }

    @PutMapping("/recall/{id}")
    public Result<String> recall(@PathVariable Long id) {
        return dishService.recall(id) ? Result.success("撤回成功") : Result.error("撤回失败");
    }

    // ===== 批量操作 =====

    @PutMapping("/batch/publish")
    public Result<String> batchPublish(@RequestBody List<Long> ids) {
        return dishService.batchPublish(ids) ? Result.success("批量发布成功") : Result.error("批量发布失败");
    }

    @PutMapping("/batch/recall")
    public Result<String> batchRecall(@RequestBody List<Long> ids) {
        return dishService.batchRecall(ids) ? Result.success("批量撤回成功") : Result.error("批量撤回失败");
    }

    @DeleteMapping("/batch")
    public Result<String> batchDelete(@RequestBody List<Long> ids) {
        return dishService.batchDelete(ids) ? Result.success("批量删除成功") : Result.error("批量删除失败");
    }

    // ===== 查询功能 =====

    @GetMapping("/search")
    public Result<List<Dish>> searchByName(@RequestParam(required = false) String keyword) {
        List<Dish> dishes = dishService.searchByName(keyword);
        return Result.success(dishes);
    }

    // ===== 05-2 新增：测试接口（用于Host断言测试） =====

    @GetMapping("/test/host")
    public String testHost() {
        return "Host匹配成功";
    }
}