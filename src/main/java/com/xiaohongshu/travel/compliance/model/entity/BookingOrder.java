package com.xiaohongshu.travel.compliance.model.entity;

import com.xiaohongshu.travel.compliance.model.enums.BookingStatus;
import com.xiaohongshu.travel.compliance.model.enums.BookingType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预订订单（机票/酒店/火车），来自携程等企业预订平台，用于订退猫腻度与预订-报销撞库。
 *
 * @param orderId       订单 ID
 * @param employeeId    员工 ID
 * @param type          预订类型
 * @param city          目的地城市（酒店所在城市）
 * @param fromCity      出发城市（交通类）
 * @param toCity        到达城市（交通类）
 * @param bookedPrice   初始预订价
 * @param actualPrice   最终实际价（退改后）
 * @param status        订单状态（正常/已退票/已改签）
 * @param channel       预订渠道（如 协议OTA / 第三方）
 * @param paidByCompany 是否企业账户支付
 * @param startTime     开始时间（入住/出发）
 * @param endTime       结束时间（离店/到达）
 * @param roomType      房型（酒店）
 * @param travelerCount 出行/入住人数
 */
public record BookingOrder(
        String orderId,
        String employeeId,
        BookingType type,
        String city,
        String fromCity,
        String toCity,
        BigDecimal bookedPrice,
        BigDecimal actualPrice,
        BookingStatus status,
        String channel,
        boolean paidByCompany,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String roomType,
        int travelerCount
) {
}
