package io.github.dk900912.oplog.context;

import org.springframework.core.AttributeAccessor;

/**
 * @author dukui
 */
public interface OperationLogContext extends AttributeAccessor {

    String LOG_RECORD = "context.log_record";

    OperationLogContext getParent();

}
