package io.github.dk900912.oplog.support;

/**
 * Defines the basic operation implemented by {@link OperationLogOperations} to execute
 * target logic with operation log behaviour.
 *
 * @author dukui
 */
public interface OperationLogOperations {

    /**
     * Execute the supplied {@link OperationLogCallback}.
     * @param <T> the return value
     * @param operationLogCallback the {@link OperationLogCallback}
     * @param <E> the exception to throw
     * @return the value returned by the {@link OperationLogCallback} upon successful invocation.
     * @throws E any {@link Exception} raised by the {@link OperationLogCallback} upon unsuccessful retry.
     * @throws E the exception thrown
     */
    <T, E extends Throwable> T execute(OperationLogCallback<T, E> operationLogCallback) throws E;

}
