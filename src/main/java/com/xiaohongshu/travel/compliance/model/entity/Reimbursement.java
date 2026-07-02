package com.xiaohongshu.travel.compliance.model.entity;

import com.xiaohongshu.travel.compliance.model.enums.ExpenseCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 报销明细。
 *
 * @param reimburseId   报销单 ID（主键）
 * @param employeeId    员工 ID
 * @param travelApplyId 关联出差申请（可为 null，用于“报销无申请”检测）
 * @param category      费用类型
 * @param amount        金额
 * @param invoiceNo     发票号
 * @param submitTime    提交时间
 * @param approvalId    审批单 ID（可为 null，用于“无审批”检测）
 */
public record Reimbursement(
        String reimburseId,
        String employeeId,
        String travelApplyId,
        ExpenseCategory category,
        BigDecimal amount,
        String invoiceNo,
        LocalDateTime submitTime,
        String approvalId
) {
}
