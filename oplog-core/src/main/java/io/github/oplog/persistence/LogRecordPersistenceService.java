package io.github.oplog.persistence;

import io.github.oplog.LogRecord;

/**
 * @author dukui
 */
public interface LogRecordPersistenceService {
    /**
     * @param logRecord
     */
    public void doLogRecordPersistence(LogRecord logRecord);
}
