package io.github.dk900912.oplog.advisor;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;

/**
 * @author dukui
 */
public class OperationLogPointcutAdvisor extends AbstractBeanFactoryPointcutAdvisor {

    private Pointcut pointcut;

    public OperationLogPointcutAdvisor() {
    }

    /**
     * @param pointcut Core Spring pointcut abstraction.
     * @param advice   Tag interface for Advice. Implementations can be any type of advice, such as Interceptors.
     */
    public OperationLogPointcutAdvisor(Pointcut pointcut, Advice advice) {
        this.pointcut = pointcut;
        setAdvice(advice);
    }

    public void setPointcut(Pointcut pointcut) {
        this.pointcut = pointcut;
    }

    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }
}
