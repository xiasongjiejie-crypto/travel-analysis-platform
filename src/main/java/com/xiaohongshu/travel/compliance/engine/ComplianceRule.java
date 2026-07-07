package com.xiaohongshu.travel.compliance.engine;

import com.xiaohongshu.travel.compliance.model.enums.RuleLevel;
import com.xiaohongshu.travel.compliance.model.result.Alert;

import java.util.List;

/**
 * 合规规则。以数据 + 逻辑（lambda）组合表达，便于集中维护。
 *
 * @param ruleId    规则 ID
 * @param ruleName  规则名称
 * @param dimension 所属分析维度
 * @param level     规则层级
 * @param logic     规则逻辑
 */
public record ComplianceRule(
        String ruleId,
        String ruleName,
        String dimension,
        RuleLevel level,
        RuleLogic logic
) {
    public List<Alert> evaluate(ComplianceContext context) {
        List<Alert> r = logic.apply(context);
        return r == null ? List.of() : r;
    }
}
