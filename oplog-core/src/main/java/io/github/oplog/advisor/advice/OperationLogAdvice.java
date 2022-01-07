package io.github.oplog.advisor.advice;

import io.github.oplog.BizCategory;
import io.github.oplog.LogRecord;
import io.github.oplog.Operator;
import io.github.oplog.annotation.OperationLog;
import io.github.oplog.persistence.LogRecordPersistenceService;
import io.github.oplog.service.OperatorService;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * @author dukui
 */
public class OperationLogAdvice implements MethodInterceptor {

    private OperatorService operatorService;

    private LogRecordPersistenceService logRecordPersistenceService;

    /**
     * @param invocation
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object result = null;
        Throwable throwable = null;
        try {
            result = invocation.proceed();
        } catch (Throwable e) {
            throwable = e;
        }

        Method method = invocation.getMethod();
        Annotation operationLogAnnotation = AnnotationUtils.findAnnotation(method, OperationLog.class);
        Map<String, Object> operationLogAnnotationAttrMap = AnnotationUtils.getAnnotationAttributes(operationLogAnnotation);
        Operator operator = getOperator();
        BizCategory bizCategory =  (BizCategory) operationLogAnnotationAttrMap.get("bizCategory");
        String bizTarget =  (String) operationLogAnnotationAttrMap.get("bizTarget");
        String operation = String.format("%s %s %s", operator.getOperatorName(), bizCategory.getName(), bizTarget);
        LogRecord logRecord = encapsulateLogRecord(operator, bizTarget, operation, throwable);
        logRecordPersistenceService.doLogRecordPersistence(logRecord);

        if (Objects.nonNull(throwable)) {
            throw throwable;
        }

        return result;
    }

    public void setOperatorService(OperatorService operatorService) {
        this.operatorService = operatorService;
    }

    public void setLogRecordPersistenceService(LogRecordPersistenceService logRecordPersistenceService) {
        this.logRecordPersistenceService = logRecordPersistenceService;
    }

    public Operator getOperator() {
        return operatorService.getOperator();
    }

    public LogRecord encapsulateLogRecord(Operator operator, String bizTarget, String content, Throwable throwable) {
        LogRecord logRecord = new LogRecord();
        logRecord.setOperatorId(operator.getOperatorId());
        logRecord.setOperatorName(operator.getOperatorName());
        logRecord.setOperationTarget(bizTarget);
        logRecord.setOperationContent(content);
        logRecord.setOperationResult(Objects.isNull(throwable));
        logRecord.setOperationTime(LocalDateTime.now());
        return logRecord;
    }
}
