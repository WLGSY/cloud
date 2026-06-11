package com.hnkjzy.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hnkjzy.user.entity.Shop;
import com.hnkjzy.user.service.ShopService;
import com.hnkjzy.user.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/shop")
public class ShopController {

    @Autowired
    private ShopService shopService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 商家注册（创建商家用户+店铺）
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
     * 获取当前商家的所有店铺信息（支持多店铺，返回列表）
     */
    @GetMapping("/my")
    public Map<String, Object> getMyShops(@RequestHeader(value = "X-User-Id", required = false) Long xUserId,
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

            // 返回店铺列表（支持多店铺）
            List<Shop> shops = shopService.getByUserId(userId);
            result.put("code", 200);
            result.put("message", "成功");
            result.put("data", shops);
            result.put("count", shops.size());
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
            if (saved == null) {
                result.put("code", 403);
                result.put("message", "无权限编辑该店铺");
                return result;
            }
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
     * 商家创建新店铺
     */
    @PostMapping("/create")
    public Map<String, Object> createShop(@RequestHeader(value = "X-User-Id", required = false) Long xUserId,
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

            Shop created = shopService.createShop(userId, shopData);
            result.put("code", 200);
            result.put("message", "店铺创建成功");
            result.put("data", created);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "创建失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 公开接口：获取所有营业中的店铺列表（用户端浏览商家用）
     */
    @GetMapping("/all")
    public Map<String, Object> getAllShops() {
        Map<String, Object> result = new HashMap<>();
        try {
            LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Shop::getStatus, 1)
                   .orderByDesc(Shop::getCreateTime);
            List<Shop> shops = shopService.list(wrapper);

            result.put("code", 200);
            result.put("message", "成功");
            result.put("data", shops);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "查询失败: " + e.getMessage());
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
            LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<>();
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
