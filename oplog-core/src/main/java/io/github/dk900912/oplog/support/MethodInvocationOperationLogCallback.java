package io.github.dk900912.oplog.support;

import org.aopalliance.intercept.MethodInvocation;

/**
 * @author dukui
 */
public abstract class MethodInvocationOperationLogCallback<T, E extends Throwable> implements OperationLogCallback<T, E> {

	protected final MethodInvocation invocation;

	protected MethodInvocationOperationLogCallback(MethodInvocation invocation) {
		this.invocation = invocation;
	}

	public MethodInvocation getInvocation() {
		return invocation;
	}

}
