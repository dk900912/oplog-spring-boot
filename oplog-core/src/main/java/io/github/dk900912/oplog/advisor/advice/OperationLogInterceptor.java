package io.github.dk900912.oplog.advisor.advice;

import io.github.dk900912.oplog.LogRecord;
import io.github.dk900912.oplog.BizCategory;
import io.github.dk900912.oplog.Operator;
import io.github.dk900912.oplog.annotation.OperationLog;
import io.github.dk900912.oplog.persistence.LogRecordPersistenceService;
import io.github.dk900912.oplog.service.OperatorService;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * @author dukui
 */
public class OperationLogInterceptor implements MethodInterceptor {

    private OperatorService operatorService;

    private LogRecordPersistenceService logRecordPersistenceService;

    private final ExpressionParser expressionParser;

    public OperationLogInterceptor() {
        expressionParser = new SpelExpressionParser();
    }

    /**
     * @param invocation  A method invocation is a joinpoint and can be intercepted by a method interceptor.
     * @return            The result of target method's invocation
     * @throws Throwable  Exception
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object result = null;
        Throwable throwable = null;
        Object[] arguments = invocation.getArguments();
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
        String bizNo =  bizNoParser((String) operationLogAnnotationAttrMap.get("bizNo"), arguments);
        String operation = String.format("%s %s %s，bizNo = %s", operator.getOperatorName(), bizCategory.getName(), bizTarget, bizNo);
        LogRecord logRecord = encapsulateLogRecord(operator, bizTarget, bizNo, operation, throwable);
        logRecordPersistenceService.doLogRecordPersistence(logRecord);

        // Rethrow the exception
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

    private LogRecord encapsulateLogRecord(Operator operator,
                                          String bizTarget,
                                          String bizNo,
                                          String content,
                                          Throwable throwable) {
        LogRecord logRecord = new LogRecord();
        logRecord.setOperatorId(operator.getOperatorId());
        logRecord.setOperatorName(operator.getOperatorName());
        logRecord.setOperationTarget(bizTarget);
        logRecord.setBizNo(bizNo);
        logRecord.setOperationContent(content);
        logRecord.setOperationResult(Objects.isNull(throwable));
        logRecord.setOperationTime(LocalDateTime.now());
        return logRecord;
    }

    private String bizNoParser(String bizNo, Object[] arguments) {
        Expression bizNoExpression = expressionParser.parseExpression(bizNo);
        String finalBizNo = null;
        for (Object arg : arguments) {
            try {
                finalBizNo = bizNoExpression.getValue(arg, String.class);
                if (StringUtils.isNotEmpty(finalBizNo)) {
                    break;
                }
            } catch (EvaluationException e) {
                // Nothing to do
            }
        }
        return finalBizNo;
    }
}
