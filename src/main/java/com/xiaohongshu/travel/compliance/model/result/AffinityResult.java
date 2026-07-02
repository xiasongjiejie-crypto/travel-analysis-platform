package com.xiaohongshu.travel.compliance.model.result;

import com.xiaohongshu.travel.compliance.model.enums.RiskLevel;

/**
 * 司乘亲密度分析结果（按 员工 + 统计窗口 聚合）。
 *
 * @param employeeId         员工 ID
 * @param window             统计窗口（如 2026-06）
 * @param totalRides         窗口内总行程数
 * @param topPlate           最高频车牌
 * @param topPlateCount      最高频车牌出现次数
 * @param concentrationRatio 集中度 R = 最高频车牌行程数 / 总行程数
 * @param hhi                赫芬达尔指数（各车牌占比平方和）
 * @param expectedRepeat     随机情况下的期望重复次数
 * @param actualRepeat       实际重复次数
 * @param affinityScore      综合亲密度风险分
 * @param riskLevel          风险等级
 */
public record AffinityResult(
        String employeeId,
        String window,
        int totalRides,
        String topPlate,
        int topPlateCount,
        double concentrationRatio,
        double hhi,
        double expectedRepeat,
        int actualRepeat,
        double affinityScore,
        RiskLevel riskLevel
) {
}
