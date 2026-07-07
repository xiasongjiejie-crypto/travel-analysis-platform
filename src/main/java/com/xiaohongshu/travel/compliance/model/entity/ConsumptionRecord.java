package com.xiaohongshu.travel.compliance.model.entity;

import com.xiaohongshu.travel.compliance.model.common.GeoPoint;
import com.xiaohongshu.travel.compliance.model.enums.ConsumptionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 实际消费记录（打车/餐饮/超市/加油等），来自第三方支付/发票数据，用于与对公行程做时空交叉验证。
 *
 * @param recordId    记录 ID
 * @param employeeId  员工 ID
 * @param type        消费类型
 * @param amount      金额
 * @param merchant    商户
 * @param city        消费城市
 * @param geo         消费地理坐标（可为 null）
 * @param consumeTime 消费时间
 * @param invoiceNo   发票号（可为 null）
 */
public record ConsumptionRecord(
        String recordId,
        String employeeId,
        ConsumptionType type,
        BigDecimal amount,
        String merchant,
        String city,
        GeoPoint geo,
        LocalDateTime consumeTime,
        String invoiceNo
) {
}
