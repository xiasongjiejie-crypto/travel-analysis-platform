package com.xiaohongshu.travel.compliance.engine.rule;

import com.xiaohongshu.travel.compliance.engine.ComplianceContext;
import com.xiaohongshu.travel.compliance.engine.ComplianceRule;
import com.xiaohongshu.travel.compliance.model.entity.BookingOrder;
import com.xiaohongshu.travel.compliance.model.entity.ConsumptionRecord;
import com.xiaohongshu.travel.compliance.model.entity.Employee;
import com.xiaohongshu.travel.compliance.model.entity.Reimbursement;
import com.xiaohongshu.travel.compliance.model.entity.TravelApply;
import com.xiaohongshu.travel.compliance.model.enums.BookingStatus;
import com.xiaohongshu.travel.compliance.model.enums.BookingType;
import com.xiaohongshu.travel.compliance.model.enums.ConsumptionType;
import com.xiaohongshu.travel.compliance.model.enums.RiskLevel;
import com.xiaohongshu.travel.compliance.model.enums.RuleLevel;
import com.xiaohongshu.travel.compliance.model.result.Alert;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 五大维度合规规则库（PRD 五维度 + Bad case 指标）。
 * 所有规则集中以 lambda 定义，便于统一维护与扩展。
 */
public final class RuleFactory {

    private static final String DIM1 = "时空偏离度";
    private static final String DIM2 = "消费反常度";
    private static final String DIM3 = "订退猫腻度";
    private static final String DIM4 = "关系关联度";
    private static final String DIM5 = "场景矛盾度";

    private static final BigDecimal ENTERTAIN_LIMIT = new BigDecimal("2000");
    private static final BigDecimal THREE = new BigDecimal("3");

    private RuleFactory() {
    }

    public static List<ComplianceRule> allRules() {
        List<ComplianceRule> rules = new ArrayList<>();
        rules.add(ghostTrip());
        rules.add(overstay());
        rules.add(teleport());
        rules.add(precisionCeiling());
        rules.add(peerConsumptionGap());
        rules.add(localHighFrequency());
        rules.add(refundArbitrage());
        rules.add(frequentRefund());
        rules.add(doublePayment());
        rules.add(splitBill());
        rules.add(travelBuddy());
        rules.add(sequentialInvoice());
        rules.add(fuelVsRide());
        rules.add(missingAccommodation());
        return rules;
    }

    private static Alert a(String id, String name, String dim, RuleLevel lv,
                           String emp, String target, RiskLevel risk, String detail) {
        return new Alert(id, name, dim, lv, emp, target, risk, detail);
    }

    // ============ 维度一：时空偏离度 ============

    private static ComplianceRule ghostTrip() {
        return new ComplianceRule("R-ST-01", "幽灵行程(时空断层)", DIM1, RuleLevel.MODEL, ctx -> {
            List<Alert> out = new ArrayList<>();
            Map<String, Map<LocalDate, Set<String>>> byEmp = new HashMap<>();
            for (ConsumptionRecord c : ctx.consumptionRecords()) {
                if (c.consumeTime() == null || c.city() == null) continue;
                byEmp.computeIfAbsent(c.employeeId(), k -> new HashMap<>())
                        .computeIfAbsent(c.consumeTime().toLocalDate(), k -> new LinkedHashSet<>())
                        .add(c.city());
            }
            for (Map.Entry<String, Map<LocalDate, Set<String>>> e : byEmp.entrySet()) {
                for (Map.Entry<LocalDate, Set<String>> de : e.getValue().entrySet()) {
                    if (de.getValue().size() >= 2) {
                        out.add(a("R-ST-01", "幽灵行程(时空断层)", DIM1, RuleLevel.MODEL, e.getKey(),
                                e.getKey() + "@" + de.getKey(), RiskLevel.HIGH,
                                String.format("同一天(%s)出现多城市消费：%s（疑发票代开/幽灵行程）",
                                        de.getKey(), String.join("、", de.getValue()))));
                    }
                }
            }
            return out;
        });
    }

    private static ComplianceRule overstay() {
        return new ComplianceRule("R-ST-02", "提前/延后停留(公私混同)", DIM1, RuleLevel.SOFT, ctx -> {
            List<Alert> out = new ArrayList<>();
            for (BookingOrder b : ctx.bookingOrders()) {
                if (b.type() != BookingType.FLIGHT && b.type() != BookingType.TRAIN) continue;
                if (b.endTime() == null) continue;
                LocalDate ret = b.endTime().toLocalDate();
                LocalDate maxEnd = null;
                for (TravelApply t : ctx.travelAppliesByEmployee(b.employeeId())) {
                    if (t.tripEnd() != null && (maxEnd == null || t.tripEnd().isAfter(maxEnd))) maxEnd = t.tripEnd();
                }
                if (maxEnd != null && ret.isAfter(maxEnd)) {
                    long days = ChronoUnit.DAYS.between(maxEnd, ret);
                    out.add(a("R-ST-02", "提前/延后停留(公私混同)", DIM1, RuleLevel.SOFT, b.employeeId(),
                            b.orderId(), RiskLevel.MEDIUM,
                            String.format("返程 %s 晚于批准差旅结束日 %s 达 %d 天（疑借公谋私延后停留）", ret, maxEnd, days)));
                }
            }
            return out;
        });
    }

    private static ComplianceRule teleport() {
        return new ComplianceRule("R-ST-03", "时空分身(异地闪现)", DIM1, RuleLevel.MODEL, ctx -> {
            List<Alert> out = new ArrayList<>();
            Map<String, List<ConsumptionRecord>> byEmp = new HashMap<>();
            for (ConsumptionRecord c : ctx.consumptionRecords()) {
                if (c.consumeTime() == null || c.city() == null) continue;
                byEmp.computeIfAbsent(c.employeeId(), k -> new ArrayList<>()).add(c);
            }
            for (Map.Entry<String, List<ConsumptionRecord>> e : byEmp.entrySet()) {
                List<ConsumptionRecord> list = e.getValue();
                list.sort(Comparator.comparing(ConsumptionRecord::consumeTime));
                for (int i = 1; i < list.size(); i++) {
                    ConsumptionRecord x = list.get(i - 1), y = list.get(i);
                    if (x.city().equals(y.city())) continue;
                    long mins = Duration.between(x.consumeTime(), y.consumeTime()).toMinutes();
                    if (mins >= 0 && mins < 30) {
                        out.add(a("R-ST-03", "时空分身(异地闪现)", DIM1, RuleLevel.MODEL, e.getKey(),
                                y.recordId(), RiskLevel.HIGH,
                                String.format("%s 在 %s 消费后 %d 分钟内又在 %s 消费（物理不可达，疑发票借用）",
                                        x.consumeTime(), x.city(), mins, y.city())));
                    }
                }
            }
            return out;
        });
    }

    // ============ 维度二：消费反常度 ============

    private static ComplianceRule precisionCeiling() {
        return new ComplianceRule("R-CONS-01", "精准压线", DIM2, RuleLevel.SOFT, ctx -> {
            List<Alert> out = new ArrayList<>();
            Map<String, List<Reimbursement>> byEmp = new HashMap<>();
            for (Reimbursement r : ctx.reimbursements()) {
                Employee emp = ctx.findEmployee(r.employeeId());
                if (emp == null || r.amount() == null) continue;
                BigDecimal std = ctx.findStandardAmount(emp.level(), r.category());
                if (std == null) continue;
                if (r.amount().compareTo(std) == 0) {
                    byEmp.computeIfAbsent(r.employeeId(), k -> new ArrayList<>()).add(r);
                }
            }
            for (Map.Entry<String, List<Reimbursement>> e : byEmp.entrySet()) {
                if (e.getValue().size() >= 2) {
                    for (Reimbursement r : e.getValue()) {
                        out.add(a("R-CONS-01", "精准压线", DIM2, RuleLevel.SOFT, e.getKey(),
                                r.reimburseId(), RiskLevel.MEDIUM,
                                String.format("报销金额 %s 精准等于标准上限，且该员工出现 %d 次（疑开高票/套现）",
                                        r.amount().toPlainString(), e.getValue().size())));
                    }
                }
            }
            return out;
        });
    }

    private static ComplianceRule peerConsumptionGap() {
        return new ComplianceRule("R-CONS-02", "同行人消费割裂", DIM2, RuleLevel.SOFT, ctx -> {
            List<Alert> out = new ArrayList<>();
            Map<String, Map<String, BigDecimal>> group = new HashMap<>();
            for (ConsumptionRecord c : ctx.consumptionRecords()) {
                if (c.city() == null || c.consumeTime() == null || c.amount() == null) continue;
                String key = c.city() + "|" + c.consumeTime().toLocalDate();
                group.computeIfAbsent(key, k -> new HashMap<>()).merge(c.employeeId(), c.amount(), BigDecimal::add);
            }
            for (Map.Entry<String, Map<String, BigDecimal>> g : group.entrySet()) {
                Map<String, BigDecimal> totals = g.getValue();
                if (totals.size() < 2) continue;
                String maxEmp = null, minEmp = null;
                BigDecimal max = null, min = null;
                for (Map.Entry<String, BigDecimal> et : totals.entrySet()) {
                    if (max == null || et.getValue().compareTo(max) > 0) { max = et.getValue(); maxEmp = et.getKey(); }
                    if (min == null || et.getValue().compareTo(min) < 0) { min = et.getValue(); minEmp = et.getKey(); }
                }
                if (min != null && min.signum() > 0 && max.compareTo(min.multiply(THREE)) >= 0) {
                    out.add(a("R-CONS-02", "同行人消费割裂", DIM2, RuleLevel.SOFT, maxEmp, g.getKey(), RiskLevel.MEDIUM,
                            String.format("%s 同期同地消费 %s 元，达同行 %s(%s元) 的3倍以上",
                                    maxEmp, max.toPlainString(), minEmp, min.toPlainString())));
                }
            }
            return out;
        });
    }

    private static ComplianceRule localHighFrequency() {
        return new ComplianceRule("R-CONS-03", "本地高频消费", DIM2, RuleLevel.SOFT, ctx -> {
            List<Alert> out = new ArrayList<>();
            Map<String, Integer> cnt = new HashMap<>();
            Map<String, String> sample = new HashMap<>();
            for (ConsumptionRecord c : ctx.consumptionRecords()) {
                Employee emp = ctx.findEmployee(c.employeeId());
                if (emp == null || c.city() == null || c.consumeTime() == null) continue;
                if (!c.city().equals(emp.baseCity())) continue;
                TravelApply trip = ctx.tripActiveOn(c.employeeId(), c.consumeTime().toLocalDate());
                if (trip != null && trip.destination() != null && !trip.destination().equals(emp.baseCity())) {
                    cnt.merge(c.employeeId(), 1, Integer::sum);
                    sample.putIfAbsent(c.employeeId(), c.recordId());
                }
            }
            for (Map.Entry<String, Integer> e : cnt.entrySet()) {
                if (e.getValue() >= 2) {
                    out.add(a("R-CONS-03", "本地高频消费", DIM2, RuleLevel.SOFT, e.getKey(),
                            sample.get(e.getKey()), RiskLevel.MEDIUM,
                            String.format("出差在外期间却在常驻城市高频消费 %d 次（疑用日常消费冲抵差旅）", e.getValue())));
                }
            }
            return out;
        });
    }

    // ============ 维度三：订退猫腻度 ============

    private static ComplianceRule refundArbitrage() {
        return new ComplianceRule("R-BK-01", "阴阳票据(买贵退便宜)", DIM3, RuleLevel.HARD, ctx -> {
            List<Alert> out = new ArrayList<>();
            for (BookingOrder b : ctx.bookingOrders()) {
                if (b.status() != BookingStatus.REFUNDED && b.status() != BookingStatus.CHANGED) continue;
                if (b.bookedPrice() == null) continue;
                for (Reimbursement r : ctx.reimbursements()) {
                    if (!b.employeeId().equals(r.employeeId()) || r.amount() == null) continue;
                    if (r.amount().compareTo(b.bookedPrice()) == 0) {
                        out.add(a("R-BK-01", "阴阳票据(买贵退便宜)", DIM3, RuleLevel.HARD, b.employeeId(),
                                r.reimburseId(), RiskLevel.HIGH,
                                String.format("订单 %s 已%s，却仍按原价 %s 元报销(报销单 %s)",
                                        b.orderId(), b.status().getDesc(), b.bookedPrice().toPlainString(), r.reimburseId())));
                    }
                }
            }
            return out;
        });
    }

    private static ComplianceRule frequentRefund() {
        return new ComplianceRule("R-BK-02", "高频退改签", DIM3, RuleLevel.SOFT, ctx -> {
            List<Alert> out = new ArrayList<>();
            Map<String, Integer> cnt = new HashMap<>();
            for (BookingOrder b : ctx.bookingOrders()) {
                if (b.status() == BookingStatus.REFUNDED || b.status() == BookingStatus.CHANGED) {
                    cnt.merge(b.employeeId(), 1, Integer::sum);
                }
            }
            for (Map.Entry<String, Integer> e : cnt.entrySet()) {
                if (e.getValue() >= 2) {
                    out.add(a("R-BK-02", "高频退改签", DIM3, RuleLevel.SOFT, e.getKey(), e.getKey(), RiskLevel.MEDIUM,
                            String.format("退改签订单达 %d 笔，显著高于常态（疑制造报销凭证/冲抵个人消费）", e.getValue())));
                }
            }
            return out;
        });
    }

    private static ComplianceRule doublePayment() {
        return new ComplianceRule("R-BK-03", "双重套现(两头报销)", DIM3, RuleLevel.HARD, ctx -> {
            List<Alert> out = new ArrayList<>();
            for (BookingOrder b : ctx.bookingOrders()) {
                if (!b.paidByCompany()) continue;
                BigDecimal paid = b.actualPrice() != null ? b.actualPrice() : b.bookedPrice();
                if (paid == null) continue;
                for (Reimbursement r : ctx.reimbursements()) {
                    if (!b.employeeId().equals(r.employeeId()) || r.amount() == null) continue;
                    if (r.amount().compareTo(paid) == 0) {
                        out.add(a("R-BK-03", "双重套现(两头报销)", DIM3, RuleLevel.HARD, b.employeeId(),
                                r.reimburseId(), RiskLevel.HIGH,
                                String.format("订单 %s 已由企业账户支付 %s 元，员工又提交报销单 %s 重复报销",
                                        b.orderId(), paid.toPlainString(), r.reimburseId())));
                    }
                }
            }
            return out;
        });
    }

    // ============ 维度四：关系关联度 ============

    private static ComplianceRule splitBill() {
        return new ComplianceRule("R-REL-01", "拆单(化整为零)", DIM4, RuleLevel.SOFT, ctx -> {
            List<Alert> out = new ArrayList<>();
            Map<String, List<ConsumptionRecord>> group = new HashMap<>();
            for (ConsumptionRecord c : ctx.consumptionRecords()) {
                if (c.type() != ConsumptionType.MEAL || c.merchant() == null
                        || c.consumeTime() == null || c.amount() == null) continue;
                String key = c.merchant() + "|" + c.consumeTime().toLocalDate();
                group.computeIfAbsent(key, k -> new ArrayList<>()).add(c);
            }
            for (Map.Entry<String, List<ConsumptionRecord>> g : group.entrySet()) {
                List<ConsumptionRecord> list = g.getValue();
                if (list.size() < 2) continue;
                BigDecimal sum = BigDecimal.ZERO;
                boolean allBelow = true;
                for (ConsumptionRecord c : list) {
                    sum = sum.add(c.amount());
                    if (c.amount().compareTo(ENTERTAIN_LIMIT) >= 0) allBelow = false;
                }
                if (allBelow && sum.compareTo(ENTERTAIN_LIMIT) > 0) {
                    String ids = list.stream().map(ConsumptionRecord::recordId).collect(Collectors.joining(", "));
                    String merchant = g.getKey().split("\\|")[0];
                    for (ConsumptionRecord c : list) {
                        out.add(a("R-REL-01", "拆单(化整为零)", DIM4, RuleLevel.SOFT, c.employeeId(),
                                c.recordId(), RiskLevel.MEDIUM,
                                String.format("%s 同日 %d 笔餐饮各低于上限但合计 %s 元超上限（疑拆单规避审批）：%s",
                                        merchant, list.size(), sum.toPlainString(), ids)));
                    }
                }
            }
            return out;
        });
    }

    private static ComplianceRule travelBuddy() {
        return new ComplianceRule("R-REL-02", "出差搭子异常", DIM4, RuleLevel.MODEL, ctx -> {
            List<Alert> out = new ArrayList<>();
            List<BookingOrder> hotels = new ArrayList<>();
            for (BookingOrder b : ctx.bookingOrders()) {
                if (b.type() == BookingType.HOTEL && b.startTime() != null && b.city() != null) hotels.add(b);
            }
            for (int i = 0; i < hotels.size(); i++) {
                for (int j = i + 1; j < hotels.size(); j++) {
                    BookingOrder x = hotels.get(i), y = hotels.get(j);
                    if (x.employeeId().equals(y.employeeId())) continue;
                    Employee ex = ctx.findEmployee(x.employeeId()), ey = ctx.findEmployee(y.employeeId());
                    if (ex == null || ey == null) continue;
                    if (ex.department() != null && ex.department().equals(ey.department())) continue;
                    if (!x.city().equals(y.city())) continue;
                    if (x.startTime().toLocalDate().equals(y.startTime().toLocalDate())) {
                        out.add(a("R-REL-02", "出差搭子异常", DIM4, RuleLevel.MODEL, x.employeeId(),
                                x.orderId(), RiskLevel.MEDIUM,
                                String.format("与不同部门员工 %s 于 %s 同城(%s)同期入住（业务无交集，疑结伴/借公出游）",
                                        ey.name(), x.startTime().toLocalDate(), x.city())));
                    }
                }
            }
            return out;
        });
    }

    private static ComplianceRule sequentialInvoice() {
        return new ComplianceRule("R-REL-03", "发票连号", DIM4, RuleLevel.HARD, ctx -> {
            List<Alert> out = new ArrayList<>();
            Map<String, List<String>> byEmp = new HashMap<>();
            for (ConsumptionRecord c : ctx.consumptionRecords()) {
                if (c.invoiceNo() != null) byEmp.computeIfAbsent(c.employeeId(), k -> new ArrayList<>()).add(c.invoiceNo());
            }
            for (Reimbursement r : ctx.reimbursements()) {
                if (r.invoiceNo() != null) byEmp.computeIfAbsent(r.employeeId(), k -> new ArrayList<>()).add(r.invoiceNo());
            }
            for (Map.Entry<String, List<String>> e : byEmp.entrySet()) {
                List<Long> vals = new ArrayList<>();
                Map<Long, String> valToInv = new HashMap<>();
                for (String inv : e.getValue()) {
                    String digits = inv.replaceAll("\\D", "");
                    if (digits.isEmpty()) continue;
                    try {
                        long v = Long.parseLong(digits);
                        vals.add(v);
                        valToInv.put(v, inv);
                    } catch (NumberFormatException ignore) {
                    }
                }
                Collections.sort(vals);
                for (int i = 1; i < vals.size(); i++) {
                    if (vals.get(i) - vals.get(i - 1) == 1) {
                        out.add(a("R-REL-03", "发票连号", DIM4, RuleLevel.HARD, e.getKey(),
                                valToInv.get(vals.get(i)), RiskLevel.HIGH,
                                String.format("发票 %s 与 %s 连号（疑一次性索取整沓空白发票自填）",
                                        valToInv.get(vals.get(i - 1)), valToInv.get(vals.get(i)))));
                    }
                }
            }
            return out;
        });
    }

    // ============ 维度五：场景矛盾度 ============

    private static ComplianceRule fuelVsRide() {
        return new ComplianceRule("R-SC-01", "油费与打车矛盾", DIM5, RuleLevel.SOFT, ctx -> {
            List<Alert> out = new ArrayList<>();
            Map<String, Integer> fuel = new HashMap<>(), ride = new HashMap<>();
            Map<String, String> fuelSample = new HashMap<>();
            for (ConsumptionRecord c : ctx.consumptionRecords()) {
                if (c.type() == ConsumptionType.FUEL) {
                    fuel.merge(c.employeeId(), 1, Integer::sum);
                    fuelSample.putIfAbsent(c.employeeId(), c.recordId());
                } else if (c.type() == ConsumptionType.RIDE) {
                    ride.merge(c.employeeId(), 1, Integer::sum);
                }
            }
            for (Map.Entry<String, Integer> e : fuel.entrySet()) {
                int rc = ride.getOrDefault(e.getKey(), 0);
                if (rc >= 3) {
                    out.add(a("R-SC-01", "油费与打车矛盾", DIM5, RuleLevel.SOFT, e.getKey(),
                            fuelSample.get(e.getKey()), RiskLevel.MEDIUM,
                            String.format("报销自驾油费同时又有 %d 笔打车（疑虚构自驾套油补/私车油票冲账）", rc)));
                }
            }
            return out;
        });
    }

    private static ComplianceRule missingAccommodation() {
        return new ComplianceRule("R-SC-02", "机票酒店断档", DIM5, RuleLevel.MODEL, ctx -> {
            List<Alert> out = new ArrayList<>();
            for (BookingOrder f : ctx.bookingOrders()) {
                if (f.type() != BookingType.FLIGHT || f.toCity() == null) continue;
                String dest = f.toCity();
                boolean hasHotel = false;
                for (BookingOrder b : ctx.bookingsByEmployee(f.employeeId())) {
                    if (b.type() == BookingType.HOTEL && dest.equals(b.city())) { hasHotel = true; break; }
                }
                if (!hasHotel) {
                    for (ConsumptionRecord c : ctx.consumptionsByEmployee(f.employeeId())) {
                        if (c.type() == ConsumptionType.HOTEL && dest.equals(c.city())) { hasHotel = true; break; }
                    }
                }
                boolean hasReturn = false;
                for (BookingOrder b : ctx.bookingsByEmployee(f.employeeId())) {
                    if ((b.type() == BookingType.FLIGHT || b.type() == BookingType.TRAIN)
                            && dest.equals(b.fromCity())) { hasReturn = true; break; }
                }
                if (!hasHotel && !hasReturn) {
                    out.add(a("R-SC-02", "机票酒店断档", DIM5, RuleLevel.MODEL, f.employeeId(),
                            f.orderId(), RiskLevel.MEDIUM,
                            String.format("飞往 %s 却无住宿预订/消费且无返程记录（疑现金住宿虚报补助或灰色回程）", dest)));
                }
            }
            return out;
        });
    }
}
