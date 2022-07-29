package io.github.dk900912.oplog.support;

import io.github.dk900912.oplog.context.OperationLogContext;

/**
 * Callback interface.
 *
 * @author dukui
 */
public interface OperationLogCallback<T, E extends Throwable> {

    /**
     * Execute an operation with operation log semantics.
     * @param context the current operation log context.
     * @return the result of the successful operation.
     * @throws E of type E if processing fails
     */
    T doWithOperationLog(OperationLogContext context) throws E;

}
