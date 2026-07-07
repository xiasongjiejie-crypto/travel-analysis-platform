package com.xiaohongshu.travel.compliance.model.enums;

/** 预订类型。 */
public enum BookingType {
    FLIGHT("机票"),
    HOTEL("酒店"),
    TRAIN("火车");

    private final String desc;
    BookingType(String desc) { this.desc = desc; }
    public String getDesc() { return desc; }
}
