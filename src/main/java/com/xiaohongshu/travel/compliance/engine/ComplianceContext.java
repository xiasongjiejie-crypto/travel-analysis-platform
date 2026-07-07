package com.xiaohongshu.travel.compliance.engine;

import com.xiaohongshu.travel.compliance.model.entity.BookingOrder;
import com.xiaohongshu.travel.compliance.model.entity.ConsumptionRecord;
import com.xiaohongshu.travel.compliance.model.entity.Employee;
import com.xiaohongshu.travel.compliance.model.entity.Invoice;
import com.xiaohongshu.travel.compliance.model.entity.Reimbursement;
import com.xiaohongshu.travel.compliance.model.entity.RideRecord;
import com.xiaohongshu.travel.compliance.model.entity.TravelApply;
import com.xiaohongshu.travel.compliance.model.entity.TravelPolicy;
import com.xiaohongshu.travel.compliance.model.enums.ExpenseCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** 合规检测上下文：承载全量数据并预建索引，提供便捷查找。 */
public class ComplianceContext {

    private final List<Employee> employees;
    private final List<TravelApply> travelApplies;
    private final List<Reimbursement> reimbursements;
    private final List<Invoice> invoices;
    private final List<TravelPolicy> policies;
    private final List<RideRecord> rideRecords;
    private final List<BookingOrder> bookingOrders;
    private final List<ConsumptionRecord> consumptionRecords;

    private final Map<String, Employee> employeeIndex;
    private final Map<String, TravelApply> travelApplyIndex;
    private final Map<String, Invoice> invoiceIndex;
    private final Map<String, BigDecimal> policyIndex;

    public ComplianceContext(List<Employee> employees,
                             List<TravelApply> travelApplies,
                             List<Reimbursement> reimbursements,
                             List<Invoice> invoices,
                             List<TravelPolicy> policies,
                             List<RideRecord> rideRecords,
                             List<BookingOrder> bookingOrders,
                             List<ConsumptionRecord> consumptionRecords) {
        this.employees = nn(employees);
        this.travelApplies = nn(travelApplies);
        this.reimbursements = nn(reimbursements);
        this.invoices = nn(invoices);
        this.policies = nn(policies);
        this.rideRecords = nn(rideRecords);
        this.bookingOrders = nn(bookingOrders);
        this.consumptionRecords = nn(consumptionRecords);

        this.employeeIndex = index(this.employees, Employee::employeeId);
        this.travelApplyIndex = index(this.travelApplies, TravelApply::travelApplyId);
        this.invoiceIndex = index(this.invoices, Invoice::invoiceNo);

        Map<String, BigDecimal> pIdx = new HashMap<>();
        for (TravelPolicy p : this.policies) {
            pIdx.merge(policyKey(p.employeeLevel(), p.category()), p.standardAmount(), BigDecimal::min);
        }
        this.policyIndex = pIdx;
    }

    private static <T> List<T> nn(List<T> l) { return l == null ? List.of() : l; }

    private static <T> Map<String, T> index(List<T> list, Function<T, String> keyFn) {
        Map<String, T> m = new HashMap<>();
        for (T t : list) {
            String k = keyFn.apply(t);
            if (k != null) m.putIfAbsent(k, t);
        }
        return m;
    }

    private static String policyKey(String level, ExpenseCategory category) {
        return level + "|" + category.name();
    }

    public List<Employee> employees() { return employees; }
    public List<TravelApply> travelApplies() { return travelApplies; }
    public List<Reimbursement> reimbursements() { return reimbursements; }
    public List<Invoice> invoices() { return invoices; }
    public List<TravelPolicy> policies() { return policies; }
    public List<RideRecord> rideRecords() { return rideRecords; }
    public List<BookingOrder> bookingOrders() { return bookingOrders; }
    public List<ConsumptionRecord> consumptionRecords() { return consumptionRecords; }

    public Employee findEmployee(String id) { return id == null ? null : employeeIndex.get(id); }
    public TravelApply findTravelApply(String id) { return id == null ? null : travelApplyIndex.get(id); }
    public Invoice findInvoice(String no) { return no == null ? null : invoiceIndex.get(no); }

    public BigDecimal findStandardAmount(String level, ExpenseCategory category) {
        if (level == null || category == null) return null;
        return policyIndex.get(policyKey(level, category));
    }

    public List<TravelApply> travelAppliesByEmployee(String employeeId) {
        List<TravelApply> r = new ArrayList<>();
        for (TravelApply t : travelApplies) {
            if (t.employeeId() != null && t.employeeId().equals(employeeId)) r.add(t);
        }
        return r;
    }

    public List<ConsumptionRecord> consumptionsByEmployee(String employeeId) {
        List<ConsumptionRecord> r = new ArrayList<>();
        for (ConsumptionRecord c : consumptionRecords) {
            if (c.employeeId() != null && c.employeeId().equals(employeeId)) r.add(c);
        }
        return r;
    }

    public List<BookingOrder> bookingsByEmployee(String employeeId) {
        List<BookingOrder> r = new ArrayList<>();
        for (BookingOrder b : bookingOrders) {
            if (b.employeeId() != null && b.employeeId().equals(employeeId)) r.add(b);
        }
        return r;
    }

    /** 返回某员工在指定日期处于出差期内的申请单（含起止当天），无则 null。 */
    public TravelApply tripActiveOn(String employeeId, LocalDate date) {
        if (employeeId == null || date == null) return null;
        for (TravelApply t : travelApplies) {
            if (!employeeId.equals(t.employeeId())) continue;
            if (t.tripStart() == null || t.tripEnd() == null) continue;
            if (!date.isBefore(t.tripStart()) && !date.isAfter(t.tripEnd())) return t;
        }
        return null;
    }
}
