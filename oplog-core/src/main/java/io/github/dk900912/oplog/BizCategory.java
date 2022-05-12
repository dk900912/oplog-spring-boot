package io.github.dk900912.oplog;

/**
 * @author dukui
 */
public enum BizCategory {
    /**
     * 新增
     */
    CREATE("新增"),

    /**
     * 更新
     */
    UPDATE("更新"),

    /**
     * 删除
     */
    DELETE("删除"),

    /**
     * 订购
     */
    PLACE_ORDER("订购"),

    /**
     * 退订
     */
    CANCEL_ORDER("退订"),

    /**
     * 续订
     */
    RENEW_ORDER("续订"),

    /**
     * 资源变更
     */
    ADJUST_RESOURCE("资源变更");

    /**
     * 业务操作种类描述
     */
    private final String name;

    private BizCategory(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
