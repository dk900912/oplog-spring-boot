package io.github.dk900912.oplog.context;

import io.github.dk900912.oplog.model.LogRecord;

/**
 * @author dukui
 */
public class LogRecordContextImpl implements LogRecordContext {

    private LogRecord logRecord;

    public LogRecordContextImpl() {
    }

    public LogRecordContextImpl(LogRecord logRecord) {
        this.logRecord = logRecord;
    }

    @Override
    public LogRecord getLogRecord() {
        return logRecord;
    }

    @Override
    public void setLogRecord(LogRecord logRecord) {
        this.logRecord = logRecord;
    }

}
