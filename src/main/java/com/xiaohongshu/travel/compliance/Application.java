package com.xiaohongshu.travel.compliance;

import com.xiaohongshu.travel.compliance.engine.ComplianceContext;
import com.xiaohongshu.travel.compliance.engine.RuleEngine;
import com.xiaohongshu.travel.compliance.engine.rule.DuplicateInvoiceRule;
import com.xiaohongshu.travel.compliance.engine.rule.MissingApprovalRule;
import com.xiaohongshu.travel.compliance.engine.rule.MissingTravelApplyRule;
import com.xiaohongshu.travel.compliance.engine.rule.OverStandardRule;
import com.xiaohongshu.travel.compliance.model.result.Alert;
import com.xiaohongshu.travel.compliance.support.SampleData;

import java.util.List;

/**
 * 应用入口。
 *
 * <p>M1 数据模型 + M2 硬规则引擎演示：加载样例数据 -> 运行规则 -> 输出告警。</p>
 */
public class Application {

    public static void main(String[] args) {
        System.out.println("企业差旅合规数据分析平台 (Travel Compliance Analysis Platform)");
        System.out.println("== M2 硬规则引擎演示 ==");

        ComplianceContext ctx = SampleData.buildContext();
        System.out.printf("加载数据：员工 %d，出差申请 %d，报销 %d，发票 %d，差标 %d%n",
                ctx.employees().size(), ctx.travelApplies().size(), ctx.reimbursements().size(),
                ctx.invoices().size(), ctx.policies().size());

        RuleEngine engine = new RuleEngine()
                .register(new OverStandardRule())
                .register(new MissingApprovalRule())
                .register(new MissingTravelApplyRule())
                .register(new DuplicateInvoiceRule());
        System.out.println("已注册硬规则数：" + engine.ruleCount());

        List<Alert> alerts = engine.run(ctx);

        System.out.println();
        System.out.println("—— 合规检测结果 ——");
        System.out.println("共命中告警 " + alerts.size() + " 条：");
        for (Alert a : alerts) {
            System.out.printf("[%s][%s] 员工 %s 报销 %s：%s%n",
                    a.ruleId(), a.risk(), a.employeeId(), a.targetId(), a.detail());
        }
    }
}
