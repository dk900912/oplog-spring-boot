package io.github.dk900912.oplog.annotation;

import io.github.dk900912.oplog.model.BizCategory;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author dukui
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface OperationLog {
    /**
     * 租户隔离，此时的租户可以是控制器层级、包层级、服务层级的
     *
     * @return tenant
     */
    String tenant() default StringUtils.EMPTY;

    /**
     * 返回操作种类：新增、更新和删除等
     *
     * @return bizCategory
     */
    BizCategory bizCategory();

    /**
     * 返回业务对象，如：订单、用户、商品等
     *
     * @return bizTarget
     */
    String bizTarget();

    /**
     * 返回业务唯一标识，支持 SpEL 表达式
     *
     * @return bizNo
     */
    String bizNo();
}
