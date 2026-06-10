package com.hnkjzy.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hnkjzy.user.entity.Shop;

public interface ShopService extends IService<Shop> {

    /**
     * 根据店主用户ID查询店铺
     */
    Shop getByUserId(Long userId);

    /**
     * 创建或更新店铺信息
     */
    Shop saveOrUpdateShop(Long userId, Shop shopData);

    /**
     * 商家注册（创建用户+店铺）
     */
    Shop registerMerchant(String username, String password, String phone, String shopName);
}
