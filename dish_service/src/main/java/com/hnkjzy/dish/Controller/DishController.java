package com.hnkjzy.dish.Controller;

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

    @GetMapping("/list")
    public List<Dish> list() {
        return dishService.list();
    }

    @GetMapping("/{id}")
    public Dish getById(@PathVariable Long id) {
        return dishService.getById(id);
    }

    @PostMapping
    public String add(@RequestBody Dish dish) {
        return dishService.save(dish) ? "添加成功" : "添加失败";
    }

    @PutMapping
    public String update(@RequestBody Dish dish) {
        return dishService.updateById(dish) ? "修改成功" : "修改失败";
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        return dishService.removeById(id) ? "删除成功" : "删除失败";
    }

    // ===== 发布/撤回（单个） =====

    @PutMapping("/publish/{id}")
    public String publish(@PathVariable Long id) {
        return dishService.publish(id) ? "发布成功" : "发布失败";
    }

    @PutMapping("/recall/{id}")
    public String recall(@PathVariable Long id) {
        return dishService.recall(id) ? "撤回成功" : "撤回失败";
    }

    // ===== 批量操作 =====

    @PutMapping("/batch/publish")
    public String batchPublish(@RequestBody List<Long> ids) {
        return dishService.batchPublish(ids) ? "批量发布成功" : "批量发布失败";
    }

    @PutMapping("/batch/recall")
    public String batchRecall(@RequestBody List<Long> ids) {
        return dishService.batchRecall(ids) ? "批量撤回成功" : "批量撤回失败";
    }

    @DeleteMapping("/batch")
    public String batchDelete(@RequestBody List<Long> ids) {
        return dishService.batchDelete(ids) ? "批量删除成功" : "批量删除失败";
    }

    // ===== 查询功能 =====

    @GetMapping("/search")
    public List<Dish> searchByName(@RequestParam(required = false) String keyword) {
        return dishService.searchByName(keyword);
    }

    @GetMapping("/published")
    public List<Dish> getPublishedDishes() {
        return dishService.getPublishedDishes();
    }

    // ===== 05-2 新增：测试接口（用于Host断言测试） =====

    @GetMapping("/test/host")
    public String testHost() {
        return "Host匹配成功";
    }
}