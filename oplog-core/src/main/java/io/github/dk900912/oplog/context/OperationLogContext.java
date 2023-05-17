package io.github.dk900912.oplog.context;

import org.springframework.core.AttributeAccessor;

/**
 * @author dukui
 */
public interface OperationLogContext extends AttributeAccessor {

    String OPERATION_LOG_INFO = "context.operation_log_info";

    String PREVIOUS_CONTENT = "context.previous_content";

    OperationLogContext getParent();

}
