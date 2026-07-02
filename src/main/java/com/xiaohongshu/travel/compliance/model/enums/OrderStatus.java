package com.xiaohongshu.travel.compliance.model.enums;

/**
 * 用车订单状态。
 */
public enum OrderStatus {

    /** 正常 */
    NORMAL("正常"),
    /** 退改 */
    REFUNDED("退改"),
    /** 异常 */
    ABNORMAL("异常");

    private final String desc;

    OrderStatus(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
