package com.hnkjzy.order.enums;

public enum OrderStatusEnum {
    PENDING(0, "待支付"),
    PAID(1, "已支付"),
    CANCELLED(2, "已取消"),
    COMPLETED(3, "已完成");

    private final int code;
    private final String description;

    OrderStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() { return code; }
    public String getDescription() { return description; }

    public static OrderStatusEnum fromCode(int code) {
        for (OrderStatusEnum status : values()) {
            if (status.code == code) return status;
        }
        return null;
    }
}