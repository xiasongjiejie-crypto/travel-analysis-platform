package com.xiaohongshu.travel.compliance.model.entity;

import com.xiaohongshu.travel.compliance.model.common.GeoPoint;

/**
 * 员工。
 *
 * @param employeeId 员工 ID
 * @param name       姓名
 * @param department 部门（用于关系关联分析）
 * @param level      职级（对应差标）
 * @param baseCity   常驻办公城市
 * @param homeGeo    居住地坐标（用于本地/轨迹异常判定，可为 null）
 */
public record Employee(
        String employeeId,
        String name,
        String department,
        String level,
        String baseCity,
        GeoPoint homeGeo
) {
}
