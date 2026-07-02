package com.xiaohongshu.travel.compliance.model.entity;

import com.xiaohongshu.travel.compliance.model.common.GeoPoint;
import com.xiaohongshu.travel.compliance.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用车（企业滴滴）行程记录，司乘亲密度分析的核心数据。
 *
 * @param rideId      行程 ID（主键）
 * @param employeeId  乘客（员工）ID
 * @param driverId    司机 ID
 * @param plateNo     车牌号（司乘亲密度核心关联键）
 * @param rideTime    上车时间
 * @param pickupGeo   起点坐标
 * @param dropoffGeo  终点坐标
 * @param distance    里程（公里）
 * @param amount      金额
 * @param city        城市（用于司机密度基线）
 * @param orderStatus 订单状态
 */
public record RideRecord(
        String rideId,
        String employeeId,
        String driverId,
        String plateNo,
        LocalDateTime rideTime,
        GeoPoint pickupGeo,
        GeoPoint dropoffGeo,
        BigDecimal distance,
        BigDecimal amount,
        String city,
        OrderStatus orderStatus
) {
}
