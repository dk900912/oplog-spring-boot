package io.github.dk900912.oplog.context;

/**
 * @author dukui
 */
public class ThreadLocalOperationLogContextImplStrategy implements OperationLogContextImplStrategy {

    private static final ThreadLocal<OperationLogContext> CONTEXT = new ThreadLocal<>();

    ThreadLocalOperationLogContextImplStrategy() {}

    @Override
    public OperationLogContext clearContext() {
        OperationLogContext value = getContext();
        OperationLogContext parent = value == null ? null : value.getParent();
        CONTEXT.set(parent);
        return value;
    }

    @Override
    public OperationLogContext getContext() {
        return CONTEXT.get();
    }

    @Override
    public OperationLogContext setContext(OperationLogContext context) {
        OperationLogContext oldContext = getContext();
        CONTEXT.set(context);
        return oldContext;
    }

}
