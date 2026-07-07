package com.xiaohongshu.travel.compliance.model.result;

import com.xiaohongshu.travel.compliance.model.enums.RiskLevel;
import com.xiaohongshu.travel.compliance.model.enums.RuleLevel;

/**
 * 合规告警。
 *
 * @param ruleId     规则 ID
 * @param ruleName   规则名称
 * @param dimension  所属分析维度（如 时空偏离度）
 * @param ruleLevel  规则层级（硬/软/模型）
 * @param employeeId 涉及员工 ID
 * @param targetId   命中的目标单据/记录 ID
 * @param risk       风险等级
 * @param detail     命中依据描述
 */
public record Alert(
        String ruleId,
        String ruleName,
        String dimension,
        RuleLevel ruleLevel,
        String employeeId,
        String targetId,
        RiskLevel risk,
        String detail
) {
}
