package io.github.dk900912.oplog.model;

import org.springframework.lang.Nullable;

import java.util.StringJoiner;
import java.util.function.UnaryOperator;

/**
 * @author dukui
 */
public class OperationLogInfo {

    private BizCategory bizCategory;

    private String bizTarget;

    private Object bizNo;

    /**
     * <li> In the declarative annotation scenario, DiffSelector is represented by String.
     * <li> In programmatic scenarios, DiffSelector is represented by {@link UnaryOperator<Object>}.
     */
    private Object diffSelector;

    public OperationLogInfo(BizCategory bizCategory, String bizTarget, Object bizNo, @Nullable Object diffSelector) {
        this.bizCategory = bizCategory;
        this.bizTarget = bizTarget;
        this.bizNo = bizNo;
        this.diffSelector = diffSelector;
    }

    public BizCategory getBizCategory() {
        return bizCategory;
    }

    public void setBizCategory(BizCategory bizCategory) {
        this.bizCategory = bizCategory;
    }

    public String getBizTarget() {
        return bizTarget;
    }

    public void setBizTarget(String bizTarget) {
        this.bizTarget = bizTarget;
    }

    public Object getBizNo() {
        return bizNo;
    }

    public void setBizNo(Object bizNo) {
        this.bizNo = bizNo;
    }

    public Object getDiffSelector() {
        return diffSelector;
    }

    public void setDiffSelector(Object diffSelector) {
        this.diffSelector = diffSelector;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "{", "}")
                .add("bizCategory=" + bizCategory)
                .add("bizTarget=" + bizTarget)
                .add("bizNo=" + bizNo)
                .add("diffSelector=" + diffSelector)
                .toString();
    }
}
