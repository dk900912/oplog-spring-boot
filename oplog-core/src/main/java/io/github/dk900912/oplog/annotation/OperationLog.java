package io.github.dk900912.oplog.annotation;

import io.github.dk900912.oplog.model.BizCategory;

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
     * 操作种类：新增、更新和删除等
     */
    BizCategory bizCategory();

    /**
     * 业务对象，如：订单、用户、商品等
     */
    String bizTarget();

    /**
     * 业务对象唯一标识；支持 SpEL 表达式，但务必以 # 作为 SpEL 表达式的前缀
     */
    String bizNo();
}
