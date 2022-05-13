package io.github.dk900912.oplog.persistence;

import io.github.dk900912.oplog.LogRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 组件提供的默认实现：将操作日志输出到日志文件中。大家可以实现不同的持久化逻辑，比如：MySQL 和 ElasticSearch
 *
 * @author dukui
 */
public class DefaultLogRecordPersistenceServiceImpl implements LogRecordPersistenceService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultLogRecordPersistenceServiceImpl.class);

    /**
     * @param logRecord 操作日志实体
     */
    @Override
    public void doLogRecordPersistence(LogRecord logRecord) {
        if (logger.isDebugEnabled()) {
            logger.info("操作日志 = {}", logRecord);
        }
    }
}
