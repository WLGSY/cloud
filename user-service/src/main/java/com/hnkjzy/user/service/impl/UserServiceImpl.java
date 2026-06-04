package com.hnkjzy.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hnkjzy.user.entity.User;
import com.hnkjzy.user.mapper.UserMapper;
import com.hnkjzy.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

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
        String md5Password = DigestUtils.md5DigestAsHex(user.getPassword().getBytes());
        user.setPassword(md5Password);
        return this.save(user);
    }

    @Override
    public User login(String username, String password) {
        User user = getUserByUsername(username);
        if (user == null) {
            return null;
        }
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (md5Password.equals(user.getPassword())) {
            user.setPassword(null);
            return user;
        }
        return null;
    }

    @Override
    public User getUserById(Long id) {
        return this.getById(id);
    }
}