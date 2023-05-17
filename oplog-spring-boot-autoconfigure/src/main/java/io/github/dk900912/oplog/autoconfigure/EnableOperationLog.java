package io.github.dk900912.oplog.autoconfigure;

import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author dukui
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(OperationLogAutoConfigurationImportSelector.class)
public @interface EnableOperationLog {

    String tenant() default "";

    /**
     * <p>Indicate the ordering of the execution of the operation-log advisor
     * when multiple advices are applied at a specific join point.</p>
     */
    int order() default Ordered.LOWEST_PRECEDENCE;
}
