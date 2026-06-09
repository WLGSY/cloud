package com.hnkjzy.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hnkjzy.user.entity.User;

public interface UserService extends IService<User> {

    User login(String username, String password);
    boolean register(User user);
    User getUserByUsername(String username);
    User getUserById(Long id);

    /**
     * 更新用户信息（只更新非空字段）
     */
    User updateUserInfo(Long userId, User updateData);

    /**
     * 修改密码
     * @return true=成功, false=原密码错误
     */
    boolean updatePassword(Long userId, String oldPassword, String newPassword);
}
