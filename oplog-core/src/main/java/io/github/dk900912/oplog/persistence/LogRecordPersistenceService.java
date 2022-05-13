package io.github.dk900912.oplog.persistence;

import io.github.dk900912.oplog.LogRecord;

/**
 * @author dukui
 */
public interface LogRecordPersistenceService {
    /**
     * 持久化操作日志
     *
     * @param logRecord 操作日志实体
     */
    public void doLogRecordPersistence(LogRecord logRecord);
}
