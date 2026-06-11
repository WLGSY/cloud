package com.hnkjzy.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hnkjzy.user.entity.Shop;

import java.util.List;

public interface ShopService extends IService<Shop> {

    /**
     * 根据店主用户ID查询所有店铺（支持多店铺）
     */
    List<Shop> getByUserId(Long userId);

    /**
     * 创建或更新店铺信息
     */
    Shop saveOrUpdateShop(Long userId, Shop shopData);

    /**
     * 创建新店铺（商家已有账号，额外创建店铺）
     */
    Shop createShop(Long userId, Shop shopData);

    /**
     * 商家注册（创建商家用户+初始化店铺）
     */
    Shop registerMerchant(String username, String password, String phone, String shopName);
}
