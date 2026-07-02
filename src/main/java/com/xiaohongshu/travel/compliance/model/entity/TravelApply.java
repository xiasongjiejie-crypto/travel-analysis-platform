package com.xiaohongshu.travel.compliance.model.entity;

import com.xiaohongshu.travel.compliance.model.enums.ApprovalStatus;

import java.time.LocalDate;

/**
 * 出差申请单。
 *
 * @param travelApplyId  申请单 ID（主键）
 * @param employeeId     员工 ID
 * @param tripStart      出差开始日期
 * @param tripEnd        出差结束日期
 * @param origin         出发地
 * @param destination    目的地
 * @param purpose        出差事由
 * @param approvalStatus 审批状态
 */
public record TravelApply(
        String travelApplyId,
        String employeeId,
        LocalDate tripStart,
        LocalDate tripEnd,
        String origin,
        String destination,
        String purpose,
        ApprovalStatus approvalStatus
) {
}
