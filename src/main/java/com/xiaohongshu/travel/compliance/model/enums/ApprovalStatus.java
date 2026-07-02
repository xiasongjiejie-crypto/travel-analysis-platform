package com.xiaohongshu.travel.compliance.model.enums;

/**
 * 审批状态。
 */
public enum ApprovalStatus {

    /** 无审批单 */
    NONE("无审批"),
    /** 待审批 */
    PENDING("待审批"),
    /** 已通过 */
    APPROVED("已通过"),
    /** 已驳回 */
    REJECTED("已驳回");

    private final String desc;

    ApprovalStatus(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
