package com.xiaohongshu.travel.compliance;

import com.xiaohongshu.travel.compliance.analytics.AffinityAnalyzer;
import com.xiaohongshu.travel.compliance.engine.ComplianceContext;
import com.xiaohongshu.travel.compliance.engine.RuleEngine;
import com.xiaohongshu.travel.compliance.engine.rule.DuplicateInvoiceRule;
import com.xiaohongshu.travel.compliance.engine.rule.MissingApprovalRule;
import com.xiaohongshu.travel.compliance.engine.rule.MissingTravelApplyRule;
import com.xiaohongshu.travel.compliance.engine.rule.OverStandardRule;
import com.xiaohongshu.travel.compliance.model.result.AffinityResult;
import com.xiaohongshu.travel.compliance.model.result.Alert;
import com.xiaohongshu.travel.compliance.support.SampleData;

import java.util.Comparator;
import java.util.List;

/**
 * 应用入口。
 *
 * <p>演示：M1 数据模型 + M2 硬规则引擎 + M4 司乘亲密度分析。</p>
 */
public class Application {

    public static void main(String[] args) {
        System.out.println("企业差旅合规数据分析平台 (Travel Compliance Analysis Platform)");

        ComplianceContext ctx = SampleData.buildContext();
        System.out.printf("加载数据：员工 %d，出差申请 %d，报销 %d，发票 %d，差标 %d，用车 %d%n",
                ctx.employees().size(), ctx.travelApplies().size(), ctx.reimbursements().size(),
                ctx.invoices().size(), ctx.policies().size(), ctx.rideRecords().size());

        runHardRules(ctx);
        runAffinity(ctx);
    }

    private static void runHardRules(ComplianceContext ctx) {
        System.out.println();
        System.out.println("== M2 硬规则引擎 ==");
        RuleEngine engine = new RuleEngine()
                .register(new OverStandardRule())
                .register(new MissingApprovalRule())
                .register(new MissingTravelApplyRule())
                .register(new DuplicateInvoiceRule());
        System.out.println("已注册硬规则数：" + engine.ruleCount());

        List<Alert> alerts = engine.run(ctx);
        System.out.println("共命中告警 " + alerts.size() + " 条：");
        for (Alert a : alerts) {
            System.out.printf("[%s][%s] 员工 %s 报销 %s：%s%n",
                    a.ruleId(), a.risk(), a.employeeId(), a.targetId(), a.detail());
        }
    }

    private static void runAffinity(ComplianceContext ctx) {
        System.out.println();
        System.out.println("== M4 司乘亲密度分析 ==");
        AffinityAnalyzer analyzer = new AffinityAnalyzer();
        List<AffinityResult> results = analyzer.analyze(ctx.rideRecords());
        results.sort(Comparator.comparingDouble(AffinityResult::affinityScore).reversed());

        System.out.println("分析对象 " + results.size() + " 个（员工 x 窗口）：");
        for (AffinityResult r : results) {
            System.out.printf(
                    "员工 %s [%s] 行程 %d，最高频车牌 %s×%d，集中度 %.2f，HHI %.2f，期望重复 %.4f/实际 %d，风险分 %.1f，等级 %s%n",
                    r.employeeId(), r.window(), r.totalRides(), r.topPlate(), r.topPlateCount(),
                    r.concentrationRatio(), r.hhi(), r.expectedRepeat(), r.actualRepeat(),
                    r.affinityScore(), r.riskLevel());
        }
    }
}
