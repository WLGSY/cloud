package com.hnkjzy.user.controller;

import com.hnkjzy.user.entity.Shop;
import com.hnkjzy.user.service.ShopService;
import com.hnkjzy.user.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/shop")
public class ShopController {

    @Autowired
    private ShopService shopService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 商家注册（创建用户+店铺）
     */
    @PostMapping("/register")
    public Map<String, Object> registerMerchant(@RequestBody Map<String, String> data) {
        Map<String, Object> result = new HashMap<>();
        try {
            String username = data.get("username");
            String password = data.get("password");
            String phone = data.get("phone");
            String shopName = data.get("shopName");

            if (username == null || password == null || shopName == null) {
                result.put("code", 400);
                result.put("message", "用户名、密码、店铺名称不能为空");
                return result;
            }
            if (password.length() < 6) {
                result.put("code", 400);
                result.put("message", "密码长度不能小于6位");
                return result;
            }

            Shop shop = shopService.registerMerchant(username, password, phone, shopName);
            result.put("code", 200);
            result.put("message", "商家注册成功");
            result.put("data", shop);
        } catch (RuntimeException e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "注册失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取当前商家的店铺信息
     */
    @GetMapping("/my")
    public Map<String, Object> getMyShop(@RequestHeader(value = "X-User-Id", required = false) Long xUserId,
                                          @RequestHeader(value = "Authorization", required = false) String authToken) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = xUserId;
            if (userId == null && authToken != null) {
                if (authToken.startsWith("Bearer ")) authToken = authToken.substring(7);
                userId = jwtUtil.getUserIdFromToken(authToken);
            }
            if (userId == null) {
                result.put("code", 401);
                result.put("message", "未登录");
                return result;
            }

            Shop shop = shopService.getByUserId(userId);
            result.put("code", 200);
            result.put("message", "成功");
            result.put("data", shop);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "查询失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 保存或更新店铺信息
     */
    @PostMapping("/save")
    public Map<String, Object> saveShop(@RequestHeader(value = "X-User-Id", required = false) Long xUserId,
                                         @RequestHeader(value = "Authorization", required = false) String authToken,
                                         @RequestBody Shop shopData) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = xUserId;
            if (userId == null && authToken != null) {
                if (authToken.startsWith("Bearer ")) authToken = authToken.substring(7);
                userId = jwtUtil.getUserIdFromToken(authToken);
            }
            if (userId == null) {
                result.put("code", 401);
                result.put("message", "未登录");
                return result;
            }

            Shop saved = shopService.saveOrUpdateShop(userId, shopData);
            result.put("code", 200);
            result.put("message", "保存成功");
            result.put("data", saved);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "保存失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 管理员：获取所有店铺列表
     */
    @GetMapping("/list")
    public Map<String, Object> listShops(@RequestParam(defaultValue = "1") Integer pageNum,
                                          @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<Shop> page =
                    new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Shop> wrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            wrapper.orderByDesc(Shop::getCreateTime);

            com.baomidou.mybatisplus.extension.plugins.pagination.Page<Shop> resultPage =
                    shopService.page(page, wrapper);

            Map<String, Object> data = new HashMap<>();
            data.put("list", resultPage.getRecords());
            data.put("total", resultPage.getTotal());

            result.put("code", 200);
            result.put("message", "成功");
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "查询失败: " + e.getMessage());
        }
        return result;
    }
}
