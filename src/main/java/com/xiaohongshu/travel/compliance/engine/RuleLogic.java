package com.xiaohongshu.travel.compliance.engine;

import com.xiaohongshu.travel.compliance.model.result.Alert;

import java.util.List;

/** 规则逻辑函数式接口，便于用 lambda 集中定义规则。 */
@FunctionalInterface
public interface RuleLogic {
    List<Alert> apply(ComplianceContext context);
}
