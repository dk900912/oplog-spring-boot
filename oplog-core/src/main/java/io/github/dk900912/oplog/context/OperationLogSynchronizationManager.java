package io.github.dk900912.oplog.context;

import io.github.dk900912.oplog.support.OperationLogOperations;

/**
 * The mutator methods ({@link #clear()} and {@link #register(OperationLogContext)}
 * should not be used except internally by {@link OperationLogOperations} implementations.
 *
 * @author dukui
 */
public final class OperationLogSynchronizationManager {

	private OperationLogSynchronizationManager() {}

	private static final ThreadLocal<OperationLogContext> OPERATION_LOG_CONTEXT = new ThreadLocal<>();

	public static OperationLogContext getContext() {
		return OPERATION_LOG_CONTEXT.get();
	}

	public static OperationLogContext register(OperationLogContext context) {
		OperationLogContext oldContext = getContext();
		OPERATION_LOG_CONTEXT.set(context);
		return oldContext;
	}

	public static OperationLogContext clear() {
		OperationLogContext value = getContext();
		OperationLogContext parent = value == null ? null : value.getParent();
		OPERATION_LOG_CONTEXT.set(parent);
		return value;
	}
}