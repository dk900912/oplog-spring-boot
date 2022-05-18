package io.github.dk900912.oplog.autoconfigure;

import io.github.dk900912.oplog.advisor.OperationLogPointcutAdvisor;
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

    /**
     * Indicate the ordering of the execution of the operation-log advisor
     * when multiple advices are applied at a specific joinpoint.
     * <p>The default is {@link Ordered#LOWEST_PRECEDENCE}.</p>
     *
     * <p>为什么要为 {@link OperationLogPointcutAdvisor} 设定 order 属性呢？</p>
     * 切记 OperationLogPointcutAdvisor 一定要排在 BeanFactoryTransactionAttributeSourceAdvisor 前面，
     * 否则 OperationLogPointcutAdvisor 中的切面逻辑（持久化、RPC调用等逻辑）会拉长事务，这是要避免的。
     * 如果大家在使用过程中，违背了这一规则，建议大家显式地设定该值。
     */
    int order() default Ordered.LOWEST_PRECEDENCE;
}
