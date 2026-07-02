package com.xiaohongshu.travel.compliance.model.enums;

/**
 * 城市分级（用于确定差旅标准）。
 */
public enum CityTier {

    /** 一线城市 */
    TIER_1("一线城市"),
    /** 二线城市 */
    TIER_2("二线城市"),
    /** 其他城市 */
    OTHER("其他城市");

    private final String desc;

    CityTier(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
