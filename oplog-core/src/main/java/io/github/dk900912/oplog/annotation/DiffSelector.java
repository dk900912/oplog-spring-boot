package io.github.dk900912.oplog.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author dukui
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface DiffSelector {

    /**
     * @return singleton bean name
     */
    String bean() default "";

    /**
     * e.g. findUserById
     *
     * @return diff-selector's method name
     */
    String method() default "";

    /**
     * e.g. #userModifyReq.userId
     *
     * @return the para of method()
     */
    String param() default "";
}
