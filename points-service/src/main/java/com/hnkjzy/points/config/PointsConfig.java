package com.hnkjzy.points.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "points")
public class PointsConfig {
    private Integer rate = 10;
    private Boolean enabled = true;
    private Integer maxPerOrder = 1000;
}