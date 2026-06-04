package com.hnkjzy.order.controller;

import com.hnkjzy.order.config.FeatureConfigProperties;
import com.hnkjzy.order.config.OrderConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置测试控制器
 *
 * 作用：提供HTTP接口，用于验证Nacos Config配置的动态刷新功能
 * 访问地址：
 *   - GET /config/discount  查看优惠配置
 *   - GET /config/features  查看功能开关配置
 */
@RestController                    // ① 标记为REST控制器，所有方法返回JSON格式
@RequestMapping("/config")        // ② 设置基础路径，所有接口都以 /config 开头
public class ConfigController {

    /**
     * 注入优惠配置类
     * 从Nacos Config中读取 order.discount 开头的配置
     * 使用 @RefreshScope 支持动态刷新
     */
    @Autowired                     // ③ 自动注入Spring Bean
    private OrderConfigProperties orderConfig;

    /**
     * 注入功能开关配置类
     * 从Nacos Config中读取 feature 开头的配置
     * 使用 @RefreshScope 支持动态刷新
     */
    @Autowired                     // ③ 自动注入Spring Bean
    private FeatureConfigProperties featureConfig;

    /**
     * 获取优惠配置
     *
     * 请求方式：GET
     * 请求路径：/config/discount
     * 响应格式：JSON
     *
     * 用途：验证Nacos Config配置是否正确加载，以及动态刷新是否生效
     */
    @GetMapping("/discount")       // ④ 处理GET请求，路径为 /config/discount
    public Map<String, Object> getDiscountConfig() {
        // 创建返回结果Map
        Map<String, Object> config = new HashMap<>();

        // 从配置类中读取优惠配置
        config.put("enabled", orderConfig.getEnabled());           // 是否开启优惠
        config.put("rate", orderConfig.getRate());                 // 折扣率
        config.put("minAmount", orderConfig.getMinAmount());       // 满减门槛
        config.put("reduceAmount", orderConfig.getReduceAmount()); // 满减金额

        // 提示信息
        config.put("message", "配置支持动态刷新，修改Nacos配置后，无需重启服务");

        return config;  // Spring Boot 自动将Map转换为JSON格式返回
    }

    /**
     * 获取功能开关配置
     *
     * 请求方式：GET
     * 请求路径：/config/features
     * 响应格式：JSON
     *
     * 用途：验证Nacos Config配置是否正确加载，以及动态刷新是否生效
     */
    @GetMapping("/features")       // ④ 处理GET请求，路径为 /config/features
    public Map<String, Object> getFeaturesConfig() {
        // 创建返回结果Map
        Map<String, Object> config = new HashMap<>();

        // 从配置类中读取功能开关配置
        config.put("enableSmsNotification", featureConfig.getEnableSmsNotification());  // 短信开关
        config.put("enablePoints", featureConfig.getEnablePoints());                    // 积分开关

        // 提示信息
        config.put("message", "修改Nacos配置后，此接口会立即返回最新值");

        return config;  // Spring Boot 自动将Map转换为JSON格式返回
    }
}