package io.github.dk900912.oplog.context;

/**
 * @author dukui
 */
public interface LogRecordContextHolderStrategy {

    /**
     * Clears the current context.
     */
    void clearContext();

    /**
     * Obtains the current context.
     *
     * @return a context
     */
    LogRecordContext getContext();

    /**
     * Sets the current context.
     *
     * @param context to the new argument (should never be <code>null</code>)
     */
    void setContext(LogRecordContext context);

    /**
     * Creates a new, empty context implementation.
     *
     * @return the empty context.
     */
    LogRecordContext createEmptyContext();

}
