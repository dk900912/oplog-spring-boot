package io.github.dk900912.oplog.annotation;

import io.github.dk900912.oplog.model.BizCategory;
import io.github.dk900912.oplog.service.Selector;

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
     * @return business operation category
     */
    BizCategory bizCategory();

    /**
     * @return business operation target
     */
    String bizTarget();

    /**
     * @return business operation identifier
     */
    String bizNo();

    /**
     * @return {@link Selector#selectorName()}
     */
    String selectorName() default "";
}
