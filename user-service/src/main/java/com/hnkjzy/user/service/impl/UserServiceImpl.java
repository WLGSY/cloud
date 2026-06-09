package com.hnkjzy.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hnkjzy.user.entity.User;
import com.hnkjzy.user.mapper.UserMapper;
import com.hnkjzy.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return this.getOne(wrapper);
    }

    @Override
    public boolean register(User user) {
        User existUser = getUserByUsername(user.getUsername());
        if (existUser != null) {
            return false;
        }
        String md5Password = DigestUtils.md5DigestAsHex(
                user.getPassword().getBytes(StandardCharsets.UTF_8));
        user.setPassword(md5Password);
        // 设置默认值
        if (user.getNickname() == null) {
            user.setNickname(user.getUsername());
        }
        if (user.getRole() == null) {
            user.setRole("user");
        }
        return this.save(user);
    }

    @Override
    public User login(String username, String password) {
        User user = getUserByUsername(username);
        if (user == null) {
            return null;
        }
        String md5Password = DigestUtils.md5DigestAsHex(
                password.getBytes(StandardCharsets.UTF_8));
        if (md5Password.equals(user.getPassword())) {
            user.setPassword(null);
            return user;
        }
        return null;
    }

    @Override
    public User getUserById(Long id) {
        User user = this.getById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    @Override
    public User updateUserInfo(Long userId, User updateData) {
        User user = this.getById(userId);
        if (user == null) {
            return null;
        }

        // 只更新非空字段
        if (updateData.getNickname() != null) {
            user.setNickname(updateData.getNickname());
        }
        if (updateData.getPhone() != null) {
            user.setPhone(updateData.getPhone());
        }
        if (updateData.getEmail() != null) {
            user.setEmail(updateData.getEmail());
        }
        if (updateData.getGender() != null) {
            user.setGender(updateData.getGender());
        }
        if (updateData.getBirthday() != null) {
            user.setBirthday(updateData.getBirthday());
        }
        if (updateData.getAvatar() != null) {
            user.setAvatar(updateData.getAvatar());
        }

        this.updateById(user);

        user.setPassword(null);
        return user;
    }

    @Override
    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = this.getById(userId);
        if (user == null) {
            return false;
        }

        // 验证原密码
        String oldMd5 = DigestUtils.md5DigestAsHex(
                oldPassword.getBytes(StandardCharsets.UTF_8));
        if (!oldMd5.equals(user.getPassword())) {
            return false;
        }

        // 更新为新密码
        String newMd5 = DigestUtils.md5DigestAsHex(
                newPassword.getBytes(StandardCharsets.UTF_8));
        user.setPassword(newMd5);
        return this.updateById(user);
    }
}
