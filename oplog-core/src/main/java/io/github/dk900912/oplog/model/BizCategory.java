package io.github.dk900912.oplog.model;

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
     * 暂停
     */
    PAUSE("暂停"),

    /**
     * 恢复
     */
    RESUME("恢复"),

    /**
     * 绑定
     */
    BIND("绑定"),

    /**
     * 解绑
     */
    UNBIND("解绑"),

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
    private final String description;

    BizCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
