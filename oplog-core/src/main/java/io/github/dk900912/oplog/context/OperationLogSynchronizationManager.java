package io.github.dk900912.oplog.context;

import io.github.dk900912.oplog.support.OperationLogOperations;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Constructor;

import static io.github.dk900912.oplog.constant.Constants.CONTEXT_STRATEGY_SYSTEM_PROPERTY;
import static io.github.dk900912.oplog.constant.Constants.DEFAULT_CONTEXT_STRATEGY;

/**
 * The methods {@link #clear()} and {@link #register(OperationLogContext)}
 * should not be used except internally by {@link OperationLogOperations} implementations.
 *
 * @author dukui
 */
public class OperationLogSynchronizationManager {

	private static String strategyName = System.getProperty(CONTEXT_STRATEGY_SYSTEM_PROPERTY);

	private static OperationLogContextImplStrategy strategy;

	static {
		initialize();
	}

	private OperationLogSynchronizationManager() {}

	private static void initialize() {
		if (!StringUtils.hasText(strategyName)) {
			strategyName = DEFAULT_CONTEXT_STRATEGY;
		}

		if (strategyName.equals(DEFAULT_CONTEXT_STRATEGY)) {
			strategy = new ThreadLocalOperationLogContextImplStrategy();
		} else {
			try {
				Class<?> clazz = Class.forName(strategyName);
				Constructor<?> customStrategy = clazz.getConstructor();
				strategy = (OperationLogContextImplStrategy) customStrategy.newInstance();
			} catch (Exception ex) {
				ReflectionUtils.handleReflectionException(ex);
			}
		}
	}

	public static OperationLogContext getContext() {
		return strategy.getContext();
	}

	public static OperationLogContext register(OperationLogContext context) {
		return strategy.setContext(context);
	}

	public static OperationLogContext clear() {
		return strategy.clearContext();
	}
}