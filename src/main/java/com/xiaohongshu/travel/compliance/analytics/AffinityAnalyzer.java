package com.xiaohongshu.travel.compliance.analytics;

import com.xiaohongshu.travel.compliance.model.entity.RideRecord;
import com.xiaohongshu.travel.compliance.model.enums.RiskLevel;
import com.xiaohongshu.travel.compliance.model.result.AffinityResult;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 司乘亲密度分析器（PRD 第 10 章）：集中度 R、HHI、概率显著性 + 风险分级。
 */
public class AffinityAnalyzer {

    private final int minRides;
    private final double mediumRatio;
    private final double highRatio;
    private final double significanceMultiplier;
    private final int cityDriverPool;

    public AffinityAnalyzer() {
        this(5, 0.30, 0.50, 10.0, 3000);
    }

    public AffinityAnalyzer(int minRides, double mediumRatio, double highRatio,
                            double significanceMultiplier, int cityDriverPool) {
        this.minRides = minRides;
        this.mediumRatio = mediumRatio;
        this.highRatio = highRatio;
        this.significanceMultiplier = significanceMultiplier;
        this.cityDriverPool = Math.max(1, cityDriverPool);
    }

    public List<AffinityResult> analyze(List<RideRecord> rides) {
        Map<String, List<RideRecord>> groups = new HashMap<>();
        for (RideRecord r : rides) {
            if (r.employeeId() == null || r.rideTime() == null) continue;
            String window = YearMonth.from(r.rideTime()).toString();
            groups.computeIfAbsent(r.employeeId() + "|" + window, k -> new ArrayList<>()).add(r);
        }
        List<AffinityResult> results = new ArrayList<>();
        for (Map.Entry<String, List<RideRecord>> e : groups.entrySet()) {
            results.add(evaluateGroup(e.getKey(), e.getValue()));
        }
        return results;
    }

    private AffinityResult evaluateGroup(String key, List<RideRecord> rides) {
        String[] parts = key.split("\\|", 2);
        String employeeId = parts[0];
        String window = parts.length > 1 ? parts[1] : "";
        int n = rides.size();

        Map<String, Integer> plateCount = new HashMap<>();
        for (RideRecord r : rides) {
            plateCount.merge(r.plateNo() == null ? "UNKNOWN" : r.plateNo(), 1, Integer::sum);
        }
        String topPlate = null;
        int topCount = 0;
        for (Map.Entry<String, Integer> pc : plateCount.entrySet()) {
            if (pc.getValue() > topCount) { topCount = pc.getValue(); topPlate = pc.getKey(); }
        }
        int distinctPlates = plateCount.size();
        double concentration = n == 0 ? 0.0 : (double) topCount / n;
        double hhi = 0.0;
        for (int c : plateCount.values()) {
            double share = (double) c / n;
            hhi += share * share;
        }
        double expectedRepeat = (double) n * (n - 1) / (2.0 * cityDriverPool);
        int actualRepeat = n - distinctPlates;
        boolean significant = n >= minRides && actualRepeat >= 2
                && actualRepeat > significanceMultiplier * expectedRepeat;

        RiskLevel level;
        if (n < minRides) {
            level = RiskLevel.LOW;
        } else if (concentration >= highRatio || significant) {
            level = RiskLevel.HIGH;
        } else if (concentration >= mediumRatio) {
            level = RiskLevel.MEDIUM;
        } else {
            level = RiskLevel.LOW;
        }
        double score = concentration * 100.0 + (significant ? 20.0 : 0.0);
        if (score > 100.0) score = 100.0;
        if (n < minRides) score = Math.min(score, 40.0);

        return new AffinityResult(employeeId, window, n, topPlate, topCount,
                round4(concentration), round4(hhi), round4(expectedRepeat), actualRepeat,
                round2(score), level);
    }

    private static double round2(double v) { return Math.round(v * 100.0) / 100.0; }
    private static double round4(double v) { return Math.round(v * 10000.0) / 10000.0; }
}
