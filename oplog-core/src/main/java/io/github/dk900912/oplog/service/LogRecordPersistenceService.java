package io.github.dk900912.oplog.service;

import io.github.dk900912.oplog.model.LogRecord;

/**
 * @author dukui
 */
public interface LogRecordPersistenceService {
    /**
     * 持久化操作日志
     *
     * @param logRecord 操作日志实体
     */
    void doLogRecordPersistence(LogRecord logRecord);
}
