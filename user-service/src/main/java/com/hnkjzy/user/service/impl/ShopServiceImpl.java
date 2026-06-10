package com.hnkjzy.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hnkjzy.user.entity.Shop;
import com.hnkjzy.user.entity.User;
import com.hnkjzy.user.mapper.ShopMapper;
import com.hnkjzy.user.mapper.UserMapper;
import com.hnkjzy.user.service.ShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements ShopService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ShopMapper shopMapper;

    @Override
    public Shop getByUserId(Long userId) {
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Shop::getUserId, userId);
        return this.getOne(wrapper);
    }

    @Override
    @Transactional
    public Shop saveOrUpdateShop(Long userId, Shop shopData) {
        Shop existing = getByUserId(userId);
        if (existing != null) {
            // 更新
            if (shopData.getName() != null) existing.setName(shopData.getName());
            if (shopData.getLogo() != null) existing.setLogo(shopData.getLogo());
            if (shopData.getPhone() != null) existing.setPhone(shopData.getPhone());
            if (shopData.getAddress() != null) existing.setAddress(shopData.getAddress());
            if (shopData.getOpenTime() != null) existing.setOpenTime(shopData.getOpenTime());
            if (shopData.getCloseTime() != null) existing.setCloseTime(shopData.getCloseTime());
            if (shopData.getDescription() != null) existing.setDescription(shopData.getDescription());
            this.updateById(existing);
            log.info("[店铺服务] 更新店铺信息，userId: {}, shopName: {}", userId, existing.getName());
            return existing;
        } else {
            // 创建
            shopData.setUserId(userId);
            shopData.setStatus(1);
            shopData.setCreateTime(LocalDateTime.now());
            this.save(shopData);
            log.info("[店铺服务] 创建店铺，userId: {}, shopName: {}", userId, shopData.getName());
            return shopData;
        }
    }

    @Override
    @Transactional
    public Shop registerMerchant(String username, String password, String phone, String shopName) {
        // 1. 检查用户名是否已存在
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getUsername, username);
        if (userMapper.selectCount(userWrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 2. 创建用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(DigestUtils.md5DigestAsHex(
                password.getBytes(StandardCharsets.UTF_8)));
        user.setPhone(phone);
        user.setNickname(shopName);
        user.setRole("merchant");
        userMapper.insert(user);
        log.info("[商家注册] 用户创建成功，userId: {}, username: {}", user.getId(), username);

        // 3. 创建店铺
        Shop shop = new Shop();
        shop.setName(shopName);
        shop.setPhone(phone);
        shop.setUserId(user.getId());
        shop.setStatus(1);
        shop.setCreateTime(LocalDateTime.now());
        shopMapper.insert(shop);
        log.info("[商家注册] 店铺创建成功，shopId: {}, shopName: {}", shop.getId(), shopName);

        return shop;
    }
}
