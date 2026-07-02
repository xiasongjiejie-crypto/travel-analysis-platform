package com.xiaohongshu.travel.compliance.model.entity;

import com.xiaohongshu.travel.compliance.model.enums.CityTier;
import com.xiaohongshu.travel.compliance.model.enums.ExpenseCategory;

import java.math.BigDecimal;

/**
 * 员工差旅标准（按职级 + 城市分级 + 费用类型确定上限）。
 *
 * @param employeeLevel  职级
 * @param cityTier       城市分级
 * @param category       费用类型
 * @param standardAmount 标准上限
 */
public record TravelPolicy(
        String employeeLevel,
        CityTier cityTier,
        ExpenseCategory category,
        BigDecimal standardAmount
) {
}
