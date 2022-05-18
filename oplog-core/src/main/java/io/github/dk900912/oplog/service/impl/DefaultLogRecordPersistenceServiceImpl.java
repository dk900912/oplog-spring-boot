package io.github.dk900912.oplog.service.impl;

import io.github.dk900912.oplog.model.LogRecord;
import io.github.dk900912.oplog.service.LogRecordPersistenceService;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * 操作日志持久化方案的默认实现：将操作日志输出到日志文件中，大家可以实现不同的持久化逻辑
 *
 * @author dukui
 */
public class DefaultLogRecordPersistenceServiceImpl implements LogRecordPersistenceService {
    private static final Logger logger = Logger.getLogger(DefaultLogRecordPersistenceServiceImpl.class.getName());

    /**
     * @param logRecord 操作日志实体
     */
    @Override
    public void doLogRecordPersistence(LogRecord logRecord) {
        if (Objects.nonNull(logRecord)) {
            logger.info(logRecord.toString());
        }
    }
}
