package com.hnkjzy.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "gateway.filter")
public class GatewayFilterProperties {

    private long slowRequestThreshold = 500;
    private boolean enableDetailLog = true;

    public long getSlowRequestThreshold() {
        return slowRequestThreshold;
    }

    public void setSlowRequestThreshold(long slowRequestThreshold) {
        this.slowRequestThreshold = slowRequestThreshold;
    }

    public boolean isEnableDetailLog() {
        return enableDetailLog;
    }

    public void setEnableDetailLog(boolean enableDetailLog) {
        this.enableDetailLog = enableDetailLog;
    }
}