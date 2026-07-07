package com.xiaohongshu.travel.compliance.model.enums;

/** 预订订单状态（用于订退猫腻度审计）。 */
public enum BookingStatus {
    NORMAL("正常"),
    REFUNDED("已退票"),
    CHANGED("已改签");

    private final String desc;
    BookingStatus(String desc) { this.desc = desc; }
    public String getDesc() { return desc; }
}
