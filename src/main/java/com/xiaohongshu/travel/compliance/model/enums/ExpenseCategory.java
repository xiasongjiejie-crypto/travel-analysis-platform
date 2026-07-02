package com.xiaohongshu.travel.compliance.model.enums;

/**
 * 费用类型（差旅报销的业务环节）。
 */
public enum ExpenseCategory {

    /** 机票 / 交通 */
    FLIGHT("机票"),
    /** 酒店 / 住宿 */
    HOTEL("酒店"),
    /** 用车 / 打车 */
    GROUND_TRANSPORT("用车"),
    /** 餐饮 / 招待 */
    MEAL("餐饮"),
    /** 其他 */
    OTHER("其他");

    private final String desc;

    ExpenseCategory(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
