package io.github.dk900912.oplog.support;

import io.github.dk900912.oplog.context.OperationLogContext;
import io.github.dk900912.oplog.model.BizCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dukui
 */
public abstract class SimpleOperationLogCallback<T, E extends Throwable> implements OperationLogCallback<T, E> {

    private static final Logger logger = LoggerFactory.getLogger(SimpleOperationLogCallback.class);

    protected BizCategory bizCategory;

    protected String bizTarget;

    protected String bizNo;

    private Object previousContent;

    protected Object currentContent;

    public SimpleOperationLogCallback(BizCategory bizCategory, String bizTarget, String bizNo) {
        this.bizCategory = bizCategory;
        this.bizTarget = bizTarget;
        this.bizNo = bizNo;
    }

    public SimpleOperationLogCallback(BizCategory bizCategory, String bizTarget, String bizNo, Object previousContent, Object currentContent) {
        this(bizCategory, bizTarget, bizNo);
        this.previousContent = previousContent;
        this.currentContent = currentContent;
    }

    public final BizCategory getBizCategory() {
        return bizCategory;
    }

    public final String getBizTarget() {
        return bizTarget;
    }

    public final String getBizNo() {
        return bizNo;
    }

    public Object getPreviousContent() {
        return previousContent;
    }

    public final Object getCurrentContent() {
        return currentContent;
    }

    @Override
    public final T doWithOperationLog(OperationLogContext context) throws E {
        logger.info("0={======> {} <======}=0", context);
        return doBizAction();
    }

    public abstract T doBizAction();

}