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

    BizCategory bizCategory();

    String bizTarget();

    String bizNo();

    /**
     * see {@link io.github.dk900912.oplog.support.diff.DiffSelectorMethod}
     * <p>
     * e.g. "xxx.VpcService#findVpcById(Long)"
     */
    String diffSelector() default "";

}
