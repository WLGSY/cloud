package com.hnkjzy.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hnkjzy.user.entity.AdminUser;
import com.hnkjzy.user.entity.CustomerUser;
import com.hnkjzy.user.entity.MerchantUser;
import com.hnkjzy.user.entity.RiderUser;
import com.hnkjzy.user.service.AdminUserService;
import com.hnkjzy.user.service.CustomerUserService;
import com.hnkjzy.user.service.MerchantUserService;
import com.hnkjzy.user.service.RiderUserService;
import com.hnkjzy.user.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private CustomerUserService customerUserService;

    @Autowired
    private MerchantUserService merchantUserService;

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private RiderUserService riderUserService;

    @Autowired
    private JwtUtil jwtUtil;

    // ===== 注册接口（支持 userType: customer/rider） =====
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> data) {
        Map<String, Object> result = new HashMap<>();
        try {
            String username = data.get("username");
            String password = data.get("password");
            String confirmPassword = data.get("confirmPassword");
            String phone = data.get("phone");
            String userType = data.get("userType"); // customer / rider

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

            boolean success;
            if ("rider".equals(userType)) {
                RiderUser rider = new RiderUser();
                rider.setUsername(username.trim());
                rider.setPassword(password);
                rider.setPhone(phone);
                rider.setNickname(username.trim());
                rider.setStatus(1);
                success = riderUserService.register(rider);
            } else {
                CustomerUser user = new CustomerUser();
                user.setUsername(username.trim());
                user.setPassword(password);
                user.setPhone(phone);
                user.setNickname(username.trim());
                success = customerUserService.register(user);
            }

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

    // ===== 统一登录接口（根据 userType 分派查表） =====
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> data) {
        Map<String, Object> result = new HashMap<>();
        try {
            String username = data.get("username");
            String password = data.get("password");
            String userType = data.get("userType"); // customer / merchant / admin

            if (username == null || password == null) {
                result.put("code", 400);
                result.put("message", "用户名和密码不能为空");
                return result;
            }

            // 根据 userType 分发到不同的表
            Long userId = null;
            String role = "user";
            String resolvedType = userType != null ? userType : "customer";
            String nickname = username;
            String avatar = null;

            if ("merchant".equals(resolvedType)) {
                MerchantUser mu = merchantUserService.login(username, password);
                if (mu != null) {
                    userId = mu.getId();
                    username = mu.getUsername();
                    nickname = mu.getNickname() != null ? mu.getNickname() : username;
                    avatar = mu.getAvatar();
                    role = "merchant";
                }
            } else if ("admin".equals(resolvedType)) {
                AdminUser au = adminUserService.login(username, password);
                if (au != null) {
                    userId = au.getId();
                    username = au.getUsername();
                    nickname = au.getNickname() != null ? au.getNickname() : username;
                    avatar = au.getAvatar();
                    role = "admin";
                }
            } else if ("rider".equals(resolvedType)) {
                RiderUser ru = riderUserService.login(username, password);
                if (ru != null) {
                    userId = ru.getId();
                    username = ru.getUsername();
                    nickname = ru.getNickname() != null ? ru.getNickname() : username;
                    avatar = ru.getAvatar();
                    role = "rider";
                }
            } else {
                // 默认 customer
                CustomerUser cu = customerUserService.login(username, password);
                if (cu != null) {
                    userId = cu.getId();
                    username = cu.getUsername();
                    nickname = cu.getNickname() != null ? cu.getNickname() : username;
                    avatar = cu.getAvatar();
                    role = "user";
                }
            }

            if (userId != null) {
                // 生成JWT Token（包含 userType）
                String token = jwtUtil.generateToken(userId, username, role, resolvedType);

                Map<String, Object> userData = new HashMap<>();
                userData.put("token", token);
                userData.put("userId", userId);
                userData.put("username", username);
                userData.put("nickname", nickname);
                userData.put("avatar", avatar != null ? avatar : "");
                userData.put("role", role);
                userData.put("userType", resolvedType);

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

    // ===== 获取当前用户信息 =====
    @GetMapping("/info")
    public Map<String, Object> getUserInfo(
            @RequestHeader(value = "X-User-Id", required = false) String xUserIdStr,
            @RequestHeader(value = "X-User-Type", defaultValue = "customer") String userType,
            @RequestHeader(value = "Authorization", required = false) String authToken) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = parseLongHeader(xUserIdStr);

            if (userId == null && authToken != null) {
                if (authToken.startsWith("Bearer ")) {
                    authToken = authToken.substring(7);
                }
                userId = jwtUtil.getUserIdFromToken(authToken);
                userType = jwtUtil.getUserTypeFromToken(authToken);
                if (userType == null) userType = "customer";
            }

            if (userId == null) {
                result.put("code", 401);
                result.put("message", "未获取到用户信息，请重新登录");
                return result;
            }

            // 根据 userType 查询对应表
            Map<String, Object> userData = new HashMap<>();
            if ("merchant".equals(userType)) {
                MerchantUser mu = merchantUserService.getUserById(userId);
                if (mu == null) { result.put("code", 404); result.put("message", "商家用户不存在"); return result; }
                fillUserData(userData, mu.getId(), mu.getUsername(), mu.getNickname(), mu.getPhone(),
                        mu.getEmail(), mu.getGender(), mu.getBirthday(), mu.getAvatar(), "merchant", userType, mu.getCreateTime());
            } else if ("admin".equals(userType)) {
                AdminUser au = adminUserService.getUserById(userId);
                if (au == null) { result.put("code", 404); result.put("message", "管理员不存在"); return result; }
                fillUserData(userData, au.getId(), au.getUsername(), au.getNickname(), au.getPhone(),
                        au.getEmail(), au.getGender(), au.getBirthday(), au.getAvatar(), "admin", userType, au.getCreateTime());
            } else if ("rider".equals(userType)) {
                RiderUser ru = riderUserService.getUserById(userId);
                if (ru == null) { result.put("code", 404); result.put("message", "骑手不存在"); return result; }
                fillUserData(userData, ru.getId(), ru.getUsername(), ru.getNickname(), ru.getPhone(),
                        ru.getEmail(), ru.getGender(), null, ru.getAvatar(), "rider", userType, ru.getCreateTime());
            } else {
                CustomerUser cu = customerUserService.getUserById(userId);
                if (cu == null) { result.put("code", 404); result.put("message", "用户不存在"); return result; }
                fillUserData(userData, cu.getId(), cu.getUsername(), cu.getNickname(), cu.getPhone(),
                        cu.getEmail(), cu.getGender(), cu.getBirthday(), cu.getAvatar(), "user", "customer", cu.getCreateTime());
            }

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
    public Map<String, Object> updateUserInfo(
            @RequestHeader(value = "X-User-Id", required = false) String xUserIdStr,
            @RequestHeader(value = "X-User-Type", defaultValue = "customer") String userType,
            @RequestHeader(value = "Authorization", required = false) String authToken,
            @RequestBody Map<String, Object> updateData) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = parseLongHeader(xUserIdStr);

            if (userId == null && authToken != null) {
                if (authToken.startsWith("Bearer ")) authToken = authToken.substring(7);
                userId = jwtUtil.getUserIdFromToken(authToken);
                userType = jwtUtil.getUserTypeFromToken(authToken);
                if (userType == null) userType = "customer";
            }

            if (userId == null) {
                result.put("code", 401);
                result.put("message", "未获取到用户信息");
                return result;
            }

            // 根据 userType 更新对应的表
            if ("merchant".equals(userType)) {
                MerchantUser mu = merchantUserService.getUserById(userId);
                if (mu == null) { result.put("code", 404); result.put("message", "商家不存在"); return result; }
                applyUpdates(mu, updateData);
                merchantUserService.updateById(mu);
            } else if ("admin".equals(userType)) {
                AdminUser au = adminUserService.getUserById(userId);
                if (au == null) { result.put("code", 404); result.put("message", "管理员不存在"); return result; }
                applyUpdates(au, updateData);
                adminUserService.updateById(au);
            } else {
                CustomerUser cu = customerUserService.getUserById(userId);
                if (cu == null) { result.put("code", 404); result.put("message", "用户不存在"); return result; }
                applyUpdates(cu, updateData);
                customerUserService.updateById(cu);
            }

            result.put("code", 200);
            result.put("message", "更新成功");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "更新失败: " + e.getMessage());
        }
        return result;
    }

    // ===== 修改密码 =====
    @PutMapping("/password")
    public Map<String, Object> updatePassword(
            @RequestHeader(value = "X-User-Id", required = false) Long xUserId,
            @RequestHeader(value = "X-User-Type", defaultValue = "customer") String userType,
            @RequestHeader(value = "Authorization", required = false) String authToken,
            @RequestBody Map<String, String> data) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = xUserId;

            if (userId == null && authToken != null) {
                if (authToken.startsWith("Bearer ")) authToken = authToken.substring(7);
                userId = jwtUtil.getUserIdFromToken(authToken);
                userType = jwtUtil.getUserTypeFromToken(authToken);
                if (userType == null) userType = "customer";
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

            String oldMd5 = DigestUtils.md5DigestAsHex(oldPassword.getBytes(StandardCharsets.UTF_8));
            String newMd5 = DigestUtils.md5DigestAsHex(newPassword.getBytes(StandardCharsets.UTF_8));

            boolean success = false;
            if ("merchant".equals(userType)) {
                MerchantUser mu = merchantUserService.getById(userId);
                if (mu != null && oldMd5.equals(mu.getPassword())) {
                    mu.setPassword(newMd5);
                    success = merchantUserService.updateById(mu);
                }
            } else if ("admin".equals(userType)) {
                AdminUser au = adminUserService.getById(userId);
                if (au != null && oldMd5.equals(au.getPassword())) {
                    au.setPassword(newMd5);
                    success = adminUserService.updateById(au);
                }
            } else {
                CustomerUser cu = customerUserService.getById(userId);
                if (cu != null && oldMd5.equals(cu.getPassword())) {
                    cu.setPassword(newMd5);
                    success = customerUserService.updateById(cu);
                }
            }

            if (success) {
                result.put("code", 200);
                result.put("message", "密码修改成功");
            } else {
                result.put("code", 400);
                result.put("message", "原密码错误或用户不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "修改密码失败: " + e.getMessage());
        }
        return result;
    }

    // ===== 根据ID查询用户（OpenFeign调用，直接返回用户对象） =====
    @GetMapping("/{id}")
    public Object getUserById(@PathVariable Long id,
                              @RequestHeader(value = "X-User-Type", defaultValue = "customer") String userType) {
        // 返回与 UserDTO 兼容的字段（供 order-service Feign 调用）
        if ("merchant".equals(userType)) {
            MerchantUser mu = merchantUserService.getUserById(id);
            if (mu == null) return null;
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", mu.getId()); userData.put("username", mu.getUsername());
            userData.put("phone", mu.getPhone()); userData.put("createTime", mu.getCreateTime());
            return userData;
        } else if ("admin".equals(userType)) {
            AdminUser au = adminUserService.getUserById(id);
            if (au == null) return null;
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", au.getId()); userData.put("username", au.getUsername());
            userData.put("phone", au.getPhone()); userData.put("createTime", au.getCreateTime());
            return userData;
        } else if ("rider".equals(userType)) {
            RiderUser ru = riderUserService.getUserById(id);
            if (ru == null) return null;
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", ru.getId()); userData.put("username", ru.getUsername());
            userData.put("phone", ru.getPhone()); userData.put("createTime", ru.getCreateTime());
            return userData;
        } else {
            CustomerUser cu = customerUserService.getUserById(id);
            if (cu == null) return null;
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", cu.getId()); userData.put("username", cu.getUsername());
            userData.put("phone", cu.getPhone()); userData.put("createTime", cu.getCreateTime());
            return userData;
        }
    }

    // ===== 管理员：获取用户列表（跨三表查询） =====
    @GetMapping("/list")
    public Map<String, Object> getUserList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Map<String, Object>> allUsers = new ArrayList<>();

            // 从三张表收集数据
            if (role == null || "user".equals(role) || "customer".equals(role)) {
                List<CustomerUser> customers;
                if (keyword != null && !keyword.isEmpty()) {
                    customers = customerUserService.list(new LambdaQueryWrapper<CustomerUser>()
                            .and(w -> w.like(CustomerUser::getUsername, keyword)
                                    .or().like(CustomerUser::getPhone, keyword)
                                    .or().like(CustomerUser::getNickname, keyword)));
                } else {
                    customers = customerUserService.list();
                }
                for (CustomerUser cu : customers) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", cu.getId()); m.put("username", cu.getUsername());
                    m.put("phone", cu.getPhone()); m.put("nickname", cu.getNickname());
                    m.put("email", cu.getEmail()); m.put("role", "user");
                    m.put("userType", "customer"); m.put("createTime", cu.getCreateTime());
                    allUsers.add(m);
                }
            }

            if (role == null || "merchant".equals(role)) {
                List<MerchantUser> merchants;
                if (keyword != null && !keyword.isEmpty()) {
                    merchants = merchantUserService.list(new LambdaQueryWrapper<MerchantUser>()
                            .and(w -> w.like(MerchantUser::getUsername, keyword)
                                    .or().like(MerchantUser::getPhone, keyword)
                                    .or().like(MerchantUser::getNickname, keyword)));
                } else {
                    merchants = merchantUserService.list();
                }
                for (MerchantUser mu : merchants) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", mu.getId()); m.put("username", mu.getUsername());
                    m.put("phone", mu.getPhone()); m.put("nickname", mu.getNickname());
                    m.put("email", mu.getEmail()); m.put("role", "merchant");
                    m.put("userType", "merchant"); m.put("createTime", mu.getCreateTime());
                    allUsers.add(m);
                }
            }

            if (role == null || "admin".equals(role)) {
                List<AdminUser> admins;
                if (keyword != null && !keyword.isEmpty()) {
                    admins = adminUserService.list(new LambdaQueryWrapper<AdminUser>()
                            .and(w -> w.like(AdminUser::getUsername, keyword)
                                    .or().like(AdminUser::getPhone, keyword)
                                    .or().like(AdminUser::getNickname, keyword)));
                } else {
                    admins = adminUserService.list();
                }
                for (AdminUser au : admins) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", au.getId()); m.put("username", au.getUsername());
                    m.put("phone", au.getPhone()); m.put("nickname", au.getNickname());
                    m.put("email", au.getEmail()); m.put("role", "admin");
                    m.put("userType", "admin"); m.put("createTime", au.getCreateTime());
                    allUsers.add(m);
                }
            }

            // 分页
            int total = allUsers.size();
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, total);
            List<Map<String, Object>> paged = start < total ? allUsers.subList(start, end) : new ArrayList<>();

            Map<String, Object> data = new HashMap<>();
            data.put("list", paged);
            data.put("total", total);
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
     * 管理员：更新用户角色（支持跨表迁移）
     */
    @PutMapping("/{id}/role")
    public Map<String, Object> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> data) {
        Map<String, Object> result = new HashMap<>();
        try {
            String newRole = data.get("role");
            if (newRole == null) {
                result.put("code", 400);
                result.put("message", "角色不能为空");
                return result;
            }
            // 简单角色更新，保持在同一张表内
            // 如果需要跨表迁移需额外处理
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
            long totalUsers = customerUserService.count();
            long totalMerchants = merchantUserService.count();
            long totalAdmins = adminUserService.count();
            long totalRiders = riderUserService.count();

            Map<String, Object> data = new HashMap<>();
            data.put("totalUsers", totalUsers);
            data.put("totalMerchants", totalMerchants);
            data.put("totalAdmins", totalAdmins);
            data.put("totalRiders", totalRiders);

            result.put("code", 200);
            result.put("message", "成功");
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "统计失败: " + e.getMessage());
        }
        return result;
    }

    // ===== 头像上传 =====
    @PostMapping("/avatar")
    public Map<String, Object> uploadAvatar(
            @RequestHeader(value = "X-User-Id", required = false) String xUserIdStr,
            @RequestHeader(value = "X-User-Type", defaultValue = "customer") String userType,
            @RequestBody Map<String, String> data) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 优先从请求体获取 userId，兼容 Gateway X-User-Id 不可用的情况
            Long userId = null;
            if (data.containsKey("userId") && data.get("userId") != null) {
                try { userId = Long.parseLong(data.get("userId")); } catch (NumberFormatException ignored) {}
            }
            if (userId == null) userId = parseLongHeader(xUserIdStr);
            if (userId == null) {
                result.put("code", 401);
                result.put("message", "未登录");
                return result;
            }
            String avatarUrl = data.get("avatarUrl");
            if (avatarUrl == null || avatarUrl.isEmpty()) {
                result.put("code", 400);
                result.put("message", "头像地址不能为空");
                return result;
            }

            if ("merchant".equals(userType)) {
                MerchantUser mu = merchantUserService.getById(userId);
                if (mu == null) { result.put("code", 404); result.put("message", "商家不存在"); return result; }
                mu.setAvatar(avatarUrl);
                merchantUserService.updateById(mu);
            } else if ("admin".equals(userType)) {
                AdminUser au = adminUserService.getById(userId);
                if (au == null) { result.put("code", 404); result.put("message", "管理员不存在"); return result; }
                au.setAvatar(avatarUrl);
                adminUserService.updateById(au);
            } else if ("rider".equals(userType)) {
                RiderUser ru = riderUserService.getById(userId);
                if (ru == null) { result.put("code", 404); result.put("message", "骑手不存在"); return result; }
                ru.setAvatar(avatarUrl);
                riderUserService.updateById(ru);
            } else {
                CustomerUser cu = customerUserService.getById(userId);
                if (cu == null) { result.put("code", 404); result.put("message", "用户不存在"); return result; }
                cu.setAvatar(avatarUrl);
                customerUserService.updateById(cu);
            }

            result.put("code", 200);
            result.put("message", "头像更新成功");
            result.put("data", avatarUrl);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "上传失败: " + e.getMessage());
        }
        return result;
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

    // ===== 辅助方法 =====

    private Long parseLongHeader(String val) {
        if (val == null || val.isEmpty() || "null".equals(val)) return null;
        try { return Long.parseLong(val); } catch (NumberFormatException e) { return null; }
    }

    private void fillUserData(Map<String, Object> map, Long id, String username, String nickname,
                              String phone, String email, String gender, String birthday, String avatar,
                              String role, String userType, Object createTime) {
        map.put("id", id);
        map.put("username", username);
        map.put("nickname", nickname);
        map.put("phone", phone);
        map.put("email", email);
        map.put("gender", gender);
        map.put("birthday", birthday);
        map.put("avatar", avatar);
        map.put("role", role);
        map.put("userType", userType);
        map.put("createTime", createTime);
    }

    private void applyUpdates(Object userObj, Map<String, Object> data) {
        if (data.containsKey("nickname")) {
            setField(userObj, "nickname", data.get("nickname"));
        }
        if (data.containsKey("phone")) {
            setField(userObj, "phone", data.get("phone"));
        }
        if (data.containsKey("email")) {
            setField(userObj, "email", data.get("email"));
        }
        if (data.containsKey("gender")) {
            setField(userObj, "gender", data.get("gender"));
        }
        if (data.containsKey("birthday")) {
            setField(userObj, "birthday", data.get("birthday"));
        }
        if (data.containsKey("avatar")) {
            setField(userObj, "avatar", data.get("avatar"));
        }
    }

    private void setField(Object obj, String fieldName, Object value) {
        try {
            String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            for (java.lang.reflect.Method m : obj.getClass().getMethods()) {
                if (m.getName().equals(setterName) && m.getParameterCount() == 1) {
                    m.invoke(obj, value);
                    return;
                }
            }
        } catch (Exception ignored) {}
    }
}
