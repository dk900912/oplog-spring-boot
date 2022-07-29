package io.github.dk900912.oplog.advisor;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;

/**
 * @author dukui
 */
public class OperationLogPointcutAdvisor extends AbstractBeanFactoryPointcutAdvisor {

    private transient Pointcut pointcut;

    public OperationLogPointcutAdvisor() {
    }

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
