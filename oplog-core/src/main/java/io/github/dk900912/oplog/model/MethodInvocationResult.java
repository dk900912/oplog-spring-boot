package io.github.dk900912.oplog.model;

import io.github.dk900912.oplog.support.MethodInvocationOperationLogCallback;
import io.github.dk900912.oplog.support.OperationLogCallback;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.util.StopWatch;

/**
 * @author dukui
 */
public class MethodInvocationResult {

    private MethodInvocation methodInvocation;

    private Object result;

    private Throwable throwable;

    private StopWatch performance;

    public MethodInvocationResult(MethodInvocation methodInvocation, Object result, Throwable throwable) {
        this.methodInvocation = methodInvocation;
        this.result = result;
        this.throwable = throwable;
    }

    public MethodInvocationResult(MethodInvocation methodInvocation, Object result, Throwable throwable, StopWatch performance) {
        this(methodInvocation, result, throwable);
        this.performance = performance;
    }

    public MethodInvocationResult(OperationLogCallback<?, ?> operationLogCallback, Object result, Throwable throwable, StopWatch performance) {
        this(
                operationLogCallback instanceof MethodInvocationOperationLogCallback ?
                        ((MethodInvocationOperationLogCallback<?, ?>) operationLogCallback).getInvocation() : null,
                result,
                throwable
        );
        this.performance = performance;
    }


    public MethodInvocation getMethodInvocation() {
        return methodInvocation;
    }

    public void setMethodInvocation(MethodInvocation methodInvocation) {
        this.methodInvocation = methodInvocation;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public StopWatch getPerformance() {
        return performance;
    }

    public void setPerformance(StopWatch performance) {
        this.performance = performance;
    }
}
