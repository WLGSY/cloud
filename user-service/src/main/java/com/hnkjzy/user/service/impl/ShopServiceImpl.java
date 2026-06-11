package com.hnkjzy.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hnkjzy.user.entity.MerchantUser;
import com.hnkjzy.user.entity.Shop;
import com.hnkjzy.user.mapper.MerchantUserMapper;
import com.hnkjzy.user.mapper.ShopMapper;
import com.hnkjzy.user.service.ShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements ShopService {

    @Autowired
    private MerchantUserMapper merchantUserMapper;

    @Autowired
    private ShopMapper shopMapper;

    @Override
    public List<Shop> getByUserId(Long userId) {
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Shop::getUserId, userId);
        wrapper.orderByDesc(Shop::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    @Transactional
    public Shop saveOrUpdateShop(Long userId, Shop shopData) {
        // 如果 shopData 有 id，直接更新那个店铺
        if (shopData.getId() != null) {
            Shop existing = this.getById(shopData.getId());
            if (existing != null && existing.getUserId().equals(userId)) {
                if (shopData.getName() != null) existing.setName(shopData.getName());
                if (shopData.getLogo() != null) existing.setLogo(shopData.getLogo());
                if (shopData.getPhone() != null) existing.setPhone(shopData.getPhone());
                if (shopData.getAddress() != null) existing.setAddress(shopData.getAddress());
                if (shopData.getOpenTime() != null) existing.setOpenTime(shopData.getOpenTime());
                if (shopData.getCloseTime() != null) existing.setCloseTime(shopData.getCloseTime());
                if (shopData.getDescription() != null) existing.setDescription(shopData.getDescription());
                if (shopData.getStatus() != null) existing.setStatus(shopData.getStatus());
                this.updateById(existing);
                log.info("[店铺服务] 更新店铺信息，shopId: {}, shopName: {}", existing.getId(), existing.getName());
                return existing;
            }
            log.warn("[店铺服务] 店铺不属于该商家，shopId: {}, userId: {}", shopData.getId(), userId);
            return null;
        }

        // 无 id 时，查找商家第一个店铺进行更新（兼容旧逻辑）
        List<Shop> shops = getByUserId(userId);
        if (!shops.isEmpty()) {
            Shop existing = shops.get(0);
            if (shopData.getName() != null) existing.setName(shopData.getName());
            if (shopData.getLogo() != null) existing.setLogo(shopData.getLogo());
            if (shopData.getPhone() != null) existing.setPhone(shopData.getPhone());
            if (shopData.getAddress() != null) existing.setAddress(shopData.getAddress());
            if (shopData.getOpenTime() != null) existing.setOpenTime(shopData.getOpenTime());
            if (shopData.getCloseTime() != null) existing.setCloseTime(shopData.getCloseTime());
            if (shopData.getDescription() != null) existing.setDescription(shopData.getDescription());
            if (shopData.getStatus() != null) existing.setStatus(shopData.getStatus());
            this.updateById(existing);
            log.info("[店铺服务] 更新第一个店铺信息，shopId: {}, shopName: {}", existing.getId(), existing.getName());
            return existing;
        }

        // 创建新店铺
        return createShop(userId, shopData);
    }

    @Override
    @Transactional
    public Shop createShop(Long userId, Shop shopData) {
        shopData.setUserId(userId);
        shopData.setStatus(shopData.getStatus() != null ? shopData.getStatus() : 1);
        shopData.setCreateTime(LocalDateTime.now());
        this.save(shopData);
        log.info("[店铺服务] 创建新店铺，userId: {}, shopId: {}, shopName: {}", userId, shopData.getId(), shopData.getName());
        return shopData;
    }

    @Override
    @Transactional
    public Shop registerMerchant(String username, String password, String phone, String shopName) {
        // 1. 检查用户名是否已存在
        LambdaQueryWrapper<MerchantUser> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(MerchantUser::getUsername, username);
        if (merchantUserMapper.selectCount(userWrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 2. 创建商家用户（merchant_user 表）
        MerchantUser user = new MerchantUser();
        user.setUsername(username);
        user.setPassword(DigestUtils.md5DigestAsHex(
                password.getBytes(StandardCharsets.UTF_8)));
        user.setPhone(phone);
        user.setNickname(shopName);
        merchantUserMapper.insert(user);
        log.info("[商家注册] 商家用户创建成功，userId: {}, username: {}", user.getId(), username);

        // 3. 创建初始店铺
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
