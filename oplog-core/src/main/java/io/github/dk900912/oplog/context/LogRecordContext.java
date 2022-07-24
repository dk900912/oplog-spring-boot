package io.github.dk900912.oplog.context;

import io.github.dk900912.oplog.model.LogRecord;

/**
 * @author dukui
 */
public interface LogRecordContext {

    LogRecord getLogRecord();

    void setLogRecord(LogRecord logRecord);

}
