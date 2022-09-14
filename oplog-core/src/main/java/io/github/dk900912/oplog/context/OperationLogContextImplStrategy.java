package io.github.dk900912.oplog.context;

/**
 * @author dukui
 */
public interface OperationLogContextImplStrategy {

    OperationLogContext clearContext();

    OperationLogContext getContext();

    OperationLogContext setContext(OperationLogContext context);

}
