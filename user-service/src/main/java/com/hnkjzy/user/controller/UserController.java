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
    public Map<String, Object> register(@RequestBody Map<String, String> data) {
        Map<String, Object> result = new HashMap<>();
        try {
            String username = data.get("username");
            String password = data.get("password");
            String confirmPassword = data.get("confirmPassword");
            String phone = data.get("phone");

            // 参数校验
            if (username == null || username.trim().isEmpty()) {
                result.put("code", 400);
                result.put("message", "用户名不能为空");
                return result;
            }
            if (password == null || password.length() < 6) {
                result.put("code", 400);
                result.put("message", "密码长度不能小于6位");
                return result;
            }
            if (!password.equals(confirmPassword)) {
                result.put("code", 400);
                result.put("message", "两次密码输入不一致");
                return result;
            }

            // 构建 User 实体
            User user = new User();
            user.setUsername(username.trim());
            user.setPassword(password);
            user.setPhone(phone);
            user.setNickname(username.trim());  // 默认昵称=用户名
            user.setRole("user");               // 默认角色

            boolean success = userService.register(user);
            if (success) {
                result.put("code", 200);
                result.put("message", "注册成功");
            } else {
                result.put("code", 400);
                result.put("message", "用户名已存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "注册失败: " + e.getMessage());
        }
        return result;
    }

    // ===== 登录接口（返回JWT Token） =====
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> data) {
        Map<String, Object> result = new HashMap<>();
        try {
            String username = data.get("username");
            String password = data.get("password");

            if (username == null || password == null) {
                result.put("code", 400);
                result.put("message", "用户名和密码不能为空");
                return result;
            }

            User loginUser = userService.login(username, password);

            if (loginUser != null) {
                // 生成JWT Token（包含角色信息）
                // 【修复】防御性空值检查：确保角色不为null，避免前端角色校验拦截商家登录
                String role = loginUser.getRole() != null ? loginUser.getRole() : "user";
                String token = jwtUtil.generateToken(loginUser.getId(), loginUser.getUsername(), role);

                // 构建 data 对象
                Map<String, Object> userData = new HashMap<>();
                userData.put("token", token);
                userData.put("userId", loginUser.getId());
                userData.put("username", loginUser.getUsername());
                userData.put("role", role);

                result.put("code", 200);
                result.put("message", "登录成功");
                result.put("data", userData);
            } else {
                result.put("code", 401);
                result.put("message", "用户名或密码错误");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "登录失败: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    // ===== 获取当前用户信息（从JWT Token解析） =====
    @GetMapping("/info")
    public Map<String, Object> getUserInfo(@RequestHeader(value = "X-User-Id", required = false) Long xUserId,
                                            @RequestHeader(value = "Authorization", required = false) String authToken) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = xUserId;

            // 兼容：如果没有 X-User-Id header，从 Authorization token 解析
            if (userId == null && authToken != null) {
                if (authToken.startsWith("Bearer ")) {
                    authToken = authToken.substring(7);
                }
                userId = jwtUtil.getUserIdFromToken(authToken);
            }

            if (userId == null) {
                result.put("code", 401);
                result.put("message", "未获取到用户信息，请重新登录");
                return result;
            }

            User user = userService.getUserById(userId);
            if (user == null) {
                result.put("code", 404);
                result.put("message", "用户不存在");
                return result;
            }

            // 构建返回数据（与前端 userStore 期望的格式一致）
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("username", user.getUsername());
            userData.put("nickname", user.getNickname());
            userData.put("phone", user.getPhone());
            userData.put("email", user.getEmail());
            userData.put("gender", user.getGender());
            userData.put("birthday", user.getBirthday());
            userData.put("avatar", user.getAvatar());
            userData.put("role", user.getRole());
            userData.put("createTime", user.getCreateTime());

            result.put("code", 200);
            result.put("message", "成功");
            result.put("data", userData);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取用户信息失败: " + e.getMessage());
        }
        return result;
    }

    // ===== 更新用户信息 =====
    @PutMapping("/info")
    public Map<String, Object> updateUserInfo(@RequestHeader(value = "X-User-Id", required = false) Long xUserId,
                                               @RequestHeader(value = "Authorization", required = false) String authToken,
                                               @RequestBody User updateData) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = xUserId;

            if (userId == null && authToken != null) {
                if (authToken.startsWith("Bearer ")) {
                    authToken = authToken.substring(7);
                }
                userId = jwtUtil.getUserIdFromToken(authToken);
            }

            if (userId == null) {
                result.put("code", 401);
                result.put("message", "未获取到用户信息");
                return result;
            }

            User updated = userService.updateUserInfo(userId, updateData);
            if (updated == null) {
                result.put("code", 404);
                result.put("message", "用户不存在");
                return result;
            }

            result.put("code", 200);
            result.put("message", "更新成功");
            result.put("data", updated);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "更新失败: " + e.getMessage());
        }
        return result;
    }

    // ===== 修改密码 =====
    @PutMapping("/password")
    public Map<String, Object> updatePassword(@RequestHeader(value = "X-User-Id", required = false) Long xUserId,
                                               @RequestHeader(value = "Authorization", required = false) String authToken,
                                               @RequestBody Map<String, String> data) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = xUserId;

            if (userId == null && authToken != null) {
                if (authToken.startsWith("Bearer ")) {
                    authToken = authToken.substring(7);
                }
                userId = jwtUtil.getUserIdFromToken(authToken);
            }

            if (userId == null) {
                result.put("code", 401);
                result.put("message", "未获取到用户信息");
                return result;
            }

            String oldPassword = data.get("oldPassword");
            String newPassword = data.get("newPassword");

            if (oldPassword == null || newPassword == null) {
                result.put("code", 400);
                result.put("message", "请输入原密码和新密码");
                return result;
            }

            boolean success = userService.updatePassword(userId, oldPassword, newPassword);
            if (success) {
                result.put("code", 200);
                result.put("message", "密码修改成功");
            } else {
                result.put("code", 400);
                result.put("message", "原密码错误");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "修改密码失败: " + e.getMessage());
        }
        return result;
    }

    // ===== 根据ID查询用户（OpenFeign调用） =====
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

    // ==============================
    // 管理后台接口
    // ==============================

    /**
     * 管理员：获取用户列表（分页）
     */
    @GetMapping("/list")
    public Map<String, Object> getUserList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role) {
        Map<String, Object> result = new HashMap<>();
        try {
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> page =
                    new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User> wrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();

            if (keyword != null && !keyword.isEmpty()) {
                wrapper.and(w -> w.like(User::getUsername, keyword)
                        .or().like(User::getPhone, keyword)
                        .or().like(User::getNickname, keyword));
            }
            if (role != null && !role.isEmpty()) {
                wrapper.eq(User::getRole, role);
            }
            wrapper.orderByDesc(User::getCreateTime);

            com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> resultPage =
                    userService.page(page, wrapper);

            // 脱敏：清除密码
            resultPage.getRecords().forEach(u -> u.setPassword(null));

            Map<String, Object> data = new HashMap<>();
            data.put("list", resultPage.getRecords());
            data.put("total", resultPage.getTotal());
            data.put("pageNum", pageNum);
            data.put("pageSize", pageSize);

            result.put("code", 200);
            result.put("message", "成功");
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "查询失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 管理员：更新用户角色
     */
    @PutMapping("/{id}/role")
    public Map<String, Object> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> data) {
        Map<String, Object> result = new HashMap<>();
        try {
            String role = data.get("role");
            if (role == null) {
                result.put("code", 400);
                result.put("message", "角色不能为空");
                return result;
            }
            User user = userService.getUserById(id);
            if (user == null) {
                result.put("code", 404);
                result.put("message", "用户不存在");
                return result;
            }
            user.setRole(role);
            userService.updateById(user);
            result.put("code", 200);
            result.put("message", "角色更新成功");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "更新失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 管理员：获取统计数据
     */
    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> result = new HashMap<>();
        try {
            long totalUsers = userService.count();
            long totalMerchants = userService.count(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                            .eq(User::getRole, "merchant"));
            long totalAdmins = userService.count(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                            .eq(User::getRole, "admin"));

            Map<String, Object> data = new HashMap<>();
            data.put("totalUsers", totalUsers);
            data.put("totalMerchants", totalMerchants);
            data.put("totalAdmins", totalAdmins);

            result.put("code", 200);
            result.put("message", "成功");
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "统计失败: " + e.getMessage());
        }
        return result;
    }
}
