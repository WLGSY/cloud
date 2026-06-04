package com.hnkjzy.user.controller;

import com.hnkjzy.user.entity.User;
import com.hnkjzy.user.service.UserService;
import com.hnkjzy.user.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    // ===== 注册接口 =====
    @PostMapping("/register")
    public String register(@RequestBody User user) {
        boolean success = userService.register(user);
        return success ? "注册成功" : "用户名已存在";
    }

    // ===== 登录接口（返回JWT Token） =====
    @PostMapping("/login")
    public Map<String, Object> login(@RequestParam String username, @RequestParam String password) {
        Map<String, Object> result = new HashMap<>();
        User user = userService.login(username, password);

        if (user != null) {
            // 生成JWT Token
            String token = jwtUtil.generateToken(user.getId(), user.getUsername());
            result.put("code", 200);
            result.put("message", "登录成功");
            result.put("token", token);
            result.put("userId", user.getId());
            result.put("username", user.getUsername());
        } else {
            result.put("code", 401);
            result.put("message", "用户名或密码错误");
        }
        return result;
    }

    // ===== 根据ID查询用户 =====
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    // ===== 模拟异常接口（测试熔断） =====
    @GetMapping("/error/{id}")
    public User getErrorUser(@PathVariable Long id) {
        throw new RuntimeException("用户服务模拟异常");
    }

    // ===== 测试接口 =====
    @GetMapping("/test/header")
    public String testHeader(@RequestHeader(value = "X-Token", required = false) String token) {
        return "Token: " + token;
    }

    @GetMapping("/test/param")
    public String testParam(@RequestParam(required = false) String name,
                            @RequestParam(required = false) Integer age) {
        return "name=" + name + ", age=" + age;
    }

    @GetMapping("/test/method")
    public String testMethodGet() {
        return "GET请求成功";
    }

    @PostMapping("/test/method")
    public String testMethodPost() {
        return "POST请求成功";
    }
}