package io.github.dk900912.oplog.model;

import org.aopalliance.intercept.MethodInvocation;

/**
 * @author dukui
 */
public class MethodInvocationResult {

    private MethodInvocation methodInvocation;

    private Object result;

    private Throwable throwable;

    public MethodInvocationResult(MethodInvocation methodInvocation, Object result, Throwable throwable) {
        this.methodInvocation = methodInvocation;
        this.result = result;
        this.throwable = throwable;
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
}
