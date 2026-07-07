package com.xiaohongshu.travel.compliance.model.enums;

/** 实际消费类型（用于轨迹/场景校验）。 */
public enum ConsumptionType {
    RIDE("打车"),
    MEAL("餐饮"),
    SUPERMARKET("超市"),
    FUEL("加油"),
    HOTEL("住宿"),
    OTHER("其他");

    private final String desc;
    ConsumptionType(String desc) { this.desc = desc; }
    public String getDesc() { return desc; }
}
