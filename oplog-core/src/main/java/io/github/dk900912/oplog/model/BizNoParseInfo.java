package io.github.dk900912.oplog.model;

import org.aopalliance.intercept.MethodInvocation;

/**
 * @author dukui
 */
public class BizNoParseInfo {

    private MethodInvocation methodInvocation;

    private Object result;

    private String originBizNo;

    public BizNoParseInfo(MethodInvocation methodInvocation, Object result, String originBizNo) {
        this.methodInvocation = methodInvocation;
        this.result = result;
        this.originBizNo = originBizNo;
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

    public String getOriginBizNo() {
        return originBizNo;
    }

    public void setOriginBizNo(String originBizNo) {
        this.originBizNo = originBizNo;
    }
}
