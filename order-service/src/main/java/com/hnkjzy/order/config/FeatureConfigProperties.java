package com.hnkjzy.order.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 功能开关配置类
 *
 * 作用：从Nacos Config中读取 feature 开头的配置
 * 配置示例：
 *   feature:
 *     enable-sms-notification: true    # 是否开启短信通知
 *     enable-points: true              # 是否开启积分功能
 */
@Component                    // ① 将当前类注册为Spring Bean，交给Spring容器管理
@RefreshScope                // ② 开启配置动态刷新，修改Nacos配置后自动更新
@ConfigurationProperties(prefix = "feature")  // ③ 绑定配置文件中 feature 开头的属性
public class FeatureConfigProperties {

    /**
     * 是否开启短信通知
     * 对应配置：feature.enable-sms-notification
     * true: 订单创建成功后发送短信通知
     * false: 不发送短信通知
     */
    private Boolean enableSmsNotification = false;

    /**
     * 是否开启积分功能
     * 对应配置：feature.enable-points
     * true: 订单创建成功后增加用户积分
     * false: 不增加积分
     */
    private Boolean enablePoints = false;

    // ========== getter/setter ==========
    // 注意：Spring Boot 通过 setter 方法注入配置值，必须要有！

    public Boolean getEnableSmsNotification() {
        return enableSmsNotification;
    }

    public void setEnableSmsNotification(Boolean enableSmsNotification) {
        this.enableSmsNotification = enableSmsNotification;
    }

    public Boolean getEnablePoints() {
        return enablePoints;
    }

    public void setEnablePoints(Boolean enablePoints) {
        this.enablePoints = enablePoints;
    }
}