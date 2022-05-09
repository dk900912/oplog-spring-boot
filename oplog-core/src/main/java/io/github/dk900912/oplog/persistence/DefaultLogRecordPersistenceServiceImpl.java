package io.github.dk900912.oplog.persistence;

import io.github.dk900912.oplog.LogRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dukui
 */
public class DefaultLogRecordPersistenceServiceImpl implements LogRecordPersistenceService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultLogRecordPersistenceServiceImpl.class);

    /**
     * @param logRecord 操作日志实体
     */
    @Override
    public void doLogRecordPersistence(LogRecord logRecord) {
        if (logger.isInfoEnabled()) {
            logger.info("操作日志 = {}", logRecord);
        }
    }
}
