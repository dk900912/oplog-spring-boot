package io.github.dk900912.oplog.context;

import org.springframework.util.Assert;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author dukui
 */
public final class InheritableThreadLocalLogRecordContextHolderStrategy implements LogRecordContextHolderStrategy {

    /**
     * <p>A more complete and consistent set of LIFO stack operations is
     * provided by the {@link java.util.Deque} interface and its implementations, which
     * should be used in preference to {@link java.util.Stack}.
     */
    private static final ThreadLocal<Deque<LogRecordContext>> STACK_INHERITABLE_THREAD_LOCAL = InheritableThreadLocal.withInitial(ArrayDeque::new);

    @Override
    public void clearContext() {
        Deque<LogRecordContext> stack = STACK_INHERITABLE_THREAD_LOCAL.get();
        stack.pop();
        if (stack.isEmpty()){
            STACK_INHERITABLE_THREAD_LOCAL.remove();
        }
    }

    @Override
    public LogRecordContext getContext() {
        return STACK_INHERITABLE_THREAD_LOCAL.get().peek();
    }

    @Override
    public void setContext(LogRecordContext context) {
        Assert.notNull(context, "Only non-null LogRecordContext instances are permitted");
        STACK_INHERITABLE_THREAD_LOCAL.get().push(context);
    }

    @Override
    public LogRecordContext createEmptyContext() {
        return new LogRecordContextImpl();
    }
}
