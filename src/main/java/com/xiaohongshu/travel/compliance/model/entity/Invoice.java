package com.xiaohongshu.travel.compliance.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 发票。
 *
 * @param invoiceNo    发票号（主键）
 * @param amount       金额
 * @param merchant     商户
 * @param invoiceTitle 抬头
 * @param invoiceDate  开票日期
 * @param verifyStatus 查验状态（如 VALID / INVALID / UNKNOWN）
 */
public record Invoice(
        String invoiceNo,
        BigDecimal amount,
        String merchant,
        String invoiceTitle,
        LocalDate invoiceDate,
        String verifyStatus
) {
}
