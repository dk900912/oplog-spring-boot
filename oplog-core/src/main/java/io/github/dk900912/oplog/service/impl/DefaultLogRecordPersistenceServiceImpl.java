package io.github.dk900912.oplog.service.impl;

import io.github.dk900912.oplog.model.LogRecord;
import io.github.dk900912.oplog.service.LogRecordPersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author dukui
 */
public class DefaultLogRecordPersistenceServiceImpl implements LogRecordPersistenceService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultLogRecordPersistenceServiceImpl.class);

    @Override
    public void doLogRecordPersistence(LogRecord logRecord) {
        if (Objects.nonNull(logRecord)) {
            logger.info("0={======> {} <======}=0", logRecord);
        }
    }
}
