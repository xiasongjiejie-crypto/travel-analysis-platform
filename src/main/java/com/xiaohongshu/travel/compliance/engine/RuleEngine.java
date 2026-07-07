package com.xiaohongshu.travel.compliance.engine;

import com.xiaohongshu.travel.compliance.model.result.Alert;

import java.util.ArrayList;
import java.util.List;

/** 规则引擎：注册并批量执行规则，汇总告警；单条规则异常不影响其他规则。 */
public class RuleEngine {

    private final List<ComplianceRule> rules = new ArrayList<>();

    public RuleEngine register(ComplianceRule rule) {
        rules.add(rule);
        return this;
    }

    public RuleEngine registerAll(List<ComplianceRule> ruleList) {
        rules.addAll(ruleList);
        return this;
    }

    public List<Alert> run(ComplianceContext context) {
        List<Alert> alerts = new ArrayList<>();
        for (ComplianceRule rule : rules) {
            try {
                alerts.addAll(rule.evaluate(context));
            } catch (Exception e) {
                System.err.println("[规则执行失败] " + rule.ruleId() + ": " + e.getMessage());
            }
        }
        return alerts;
    }

    public int ruleCount() {
        return rules.size();
    }
}
