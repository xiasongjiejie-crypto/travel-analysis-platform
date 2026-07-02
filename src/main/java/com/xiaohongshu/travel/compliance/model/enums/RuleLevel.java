package com.xiaohongshu.travel.compliance.model.enums;

/**
 * 规则层级（对应 PRD 的三层规则体系）。
 */
public enum RuleLevel {

    /** 硬规则：命中即告警 */
    HARD("硬规则"),
    /** 软规则：命中打分并进入人工复核 */
    SOFT("软规则"),
    /** 模型层：异常检测 / 关联分析 */
    MODEL("模型层");

    private final String desc;

    RuleLevel(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
