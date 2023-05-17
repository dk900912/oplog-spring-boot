package io.github.dk900912.oplog.support;

import io.github.dk900912.oplog.context.OperationLogContext;
import io.github.dk900912.oplog.model.BizCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.UnaryOperator;

/**
 * @author dukui
 */
public abstract class SimpleOperationLogCallback<T, E extends Throwable> implements OperationLogCallback<T, E> {

    private static final Logger logger = LoggerFactory.getLogger(SimpleOperationLogCallback.class);

    protected BizCategory bizCategory;

    protected String bizTarget;

    protected Object bizNo;

    private UnaryOperator<Object> diffSelector;

    protected SimpleOperationLogCallback(BizCategory bizCategory, String bizTarget, Object bizNo) {
        this.bizCategory = bizCategory;
        this.bizTarget = bizTarget;
        this.bizNo = bizNo;
    }

    protected SimpleOperationLogCallback(BizCategory bizCategory, String bizTarget, Object bizNo, UnaryOperator<Object> diffSelector) {
        this(bizCategory, bizTarget, bizNo);
        this.diffSelector = diffSelector;
    }

    public final BizCategory getBizCategory() {
        return bizCategory;
    }

    public final String getBizTarget() {
        return bizTarget;
    }

    public final Object getBizNo() {
        return bizNo;
    }

    public UnaryOperator<Object> getDiffSelector() {
        return diffSelector;
    }

    @Override
    public final T doWithOperationLog(OperationLogContext context) throws E {
        if (logger.isDebugEnabled()) {
            logger.debug("0={======> {} <======}=0", context);
        }
        return doBizAction();
    }

    protected abstract T doBizAction();

}