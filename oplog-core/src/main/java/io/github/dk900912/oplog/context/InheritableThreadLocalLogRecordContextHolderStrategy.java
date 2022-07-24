package io.github.dk900912.oplog.context;

import org.springframework.util.Assert;

/**
 * @author dukui
 */
public final class InheritableThreadLocalLogRecordContextHolderStrategy implements LogRecordContextHolderStrategy {

    private static final ThreadLocal<LogRecordContext> logRecordContextHolder = new InheritableThreadLocal<>();

    @Override
    public void clearContext() {
        logRecordContextHolder.remove();
    }

    @Override
    public LogRecordContext getContext() {
        LogRecordContext ctx = logRecordContextHolder.get();

        if (ctx == null) {
            ctx = createEmptyContext();
            logRecordContextHolder.set(ctx);
        }

        return ctx;
    }

    @Override
    public void setContext(LogRecordContext context) {
        Assert.notNull(context, "Only non-null LogRecordContext instances are permitted");
        logRecordContextHolder.set(context);
    }

    @Override
    public LogRecordContext createEmptyContext() {
        return new LogRecordContextImpl();
    }
}
