package com.hnkjzy.order.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 订单优惠配置类
 *
 * 作用：从Nacos Config中读取 order.discount 开头的配置
 * 配置示例：
 *   order:
 *     discount:
 *       enabled: true
 *       rate: 0.9
 *       min-amount: 100
 *       reduce-amount: 10
 */
@Component                    // ① 将当前类注册为Spring Bean，交给Spring容器管理
@RefreshScope                // ② 开启配置动态刷新，修改Nacos配置后自动更新
@ConfigurationProperties(prefix = "order.discount")  // ③ 绑定配置文件中 order.discount 开头的属性
public class OrderConfigProperties {

    /**
     * 是否开启优惠活动
     * 对应配置：order.discount.enabled
     */
    private Boolean enabled = false;

    /**
     * 折扣率（如 0.9 表示9折）
     * 对应配置：order.discount.rate
     */
    private Double rate = 1.0;

    /**
     * 满减门槛（满多少元减）
     * 对应配置：order.discount.min-amount
     */
    private Integer minAmount = 0;

    /**
     * 满减金额（减多少元）
     * 对应配置：order.discount.reduce-amount
     */
    private Integer reduceAmount = 0;

    // ========== getter/setter ==========
    // 注意：Spring Boot 通过 setter 方法注入配置值，必须要有！

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public Integer getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(Integer minAmount) {
        this.minAmount = minAmount;
    }

    public Integer getReduceAmount() {
        return reduceAmount;
    }

    public void setReduceAmount(Integer reduceAmount) {
        this.reduceAmount = reduceAmount;
    }
}