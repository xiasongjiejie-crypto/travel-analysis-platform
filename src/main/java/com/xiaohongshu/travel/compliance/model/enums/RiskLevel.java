package com.xiaohongshu.travel.compliance.model.enums;

/**
 * 风险等级。
 */
public enum RiskLevel {

    /** 低风险 */
    LOW("低风险"),
    /** 中风险 */
    MEDIUM("中风险"),
    /** 高风险 */
    HIGH("高风险");

    private final String desc;

    RiskLevel(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
