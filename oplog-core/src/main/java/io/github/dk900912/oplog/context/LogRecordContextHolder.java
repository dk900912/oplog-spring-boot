package io.github.dk900912.oplog.context;

import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Constructor;

/**
 * @author dukui
 */
public class LogRecordContextHolder {

    public static final String MODE_THREADLOCAL = "MODE_THREADLOCAL";
    public static final String MODE_INHERITABLETHREADLOCAL = "MODE_INHERITABLETHREADLOCAL";
    public static final String SYSTEM_PROPERTY = "spring.oplog.context.strategy";
    private static String strategyName = System.getProperty(SYSTEM_PROPERTY);
    private static LogRecordContextHolderStrategy strategy;

    static {
        initialize();
    }

    /**
     * Explicitly clears the context value from the current thread.
     */
    public static void clearContext() {
        strategy.clearContext();
    }

    /**
     * Obtain the current <code>LogRecordContext</code>.
     *
     * @return the log record context
     */
    public static LogRecordContext getContext() {
        return strategy.getContext();
    }

    private static void initialize() {
        if (!StringUtils.hasText(strategyName)) {
            strategyName = MODE_THREADLOCAL;
        }

        if (strategyName.equals(MODE_THREADLOCAL)) {
            strategy = new ThreadLocalLogRecordContextHolderStrategy();
        } else if (strategyName.equals(MODE_INHERITABLETHREADLOCAL)) {
            strategy = new InheritableThreadLocalLogRecordContextHolderStrategy();
        } else {
            try {
                Class<?> clazz = Class.forName(strategyName);
                Constructor<?> customStrategy = clazz.getConstructor();
                strategy = (LogRecordContextHolderStrategy) customStrategy.newInstance();
            } catch (Exception ex) {
                ReflectionUtils.handleReflectionException(ex);
            }
        }
    }

    /**
     * Associates a new <code>LogRecordContext</code> with the current thread of execution.
     *
     * @param context the new <code>LogRecordContext</code>
     */
    public static void setContext(LogRecordContext context) {
        strategy.setContext(context);
    }

    /**
     * Allows retrieval of the context strategy.
     *
     * @return the configured strategy for storing the log record context.
     */
    public static LogRecordContextHolderStrategy getContextHolderStrategy() {
        return strategy;
    }

    /**
     * Delegates the creation of a new, empty context to the configured strategy.
     *
     * @return an empty <code>LogRecordContext</code>
     */
    public static LogRecordContext createEmptyContext() {
        return strategy.createEmptyContext();
    }

    @Override
    public String toString() {
        return "LogRecordContextHolder[strategy='" + strategyName + "']";
    }
}
