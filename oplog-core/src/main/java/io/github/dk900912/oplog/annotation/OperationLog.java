package io.github.dk900912.oplog.annotation;

import io.github.dk900912.oplog.model.BizCategory;
import io.github.dk900912.oplog.service.PreviousContentSelector;

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

    /**
     * 在更新场景中，用于查询之前的内容
     *
     * @return {@link PreviousContentSelector#selectorName()}
     */
    String previousContentSelectorName() default "";
}
