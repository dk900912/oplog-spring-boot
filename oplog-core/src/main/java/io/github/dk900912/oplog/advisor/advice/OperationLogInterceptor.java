package io.github.dk900912.oplog.advisor.advice;

import io.github.dk900912.oplog.BizCategory;
import io.github.dk900912.oplog.LogRecord;
import io.github.dk900912.oplog.Operator;
import io.github.dk900912.oplog.annotation.OperationLog;
import io.github.dk900912.oplog.persistence.LogRecordPersistenceService;
import io.github.dk900912.oplog.service.OperatorService;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static io.github.dk900912.oplog.BizCategory.CREATE;
import static io.github.dk900912.oplog.BizCategory.PLACE_ORDER;

/**
 * @author dukui
 */
public class OperationLogInterceptor implements MethodInterceptor {

    private static final String SPEL_PREFIX = "#";

    private OperatorService operatorService;

    private LogRecordPersistenceService logRecordPersistenceService;

    // thread-safe
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
        // The operationLogAnnotation will never be null
        Annotation operationLogAnnotation = AnnotationUtils.findAnnotation(method, OperationLog.class);
        Map<String, Object> operationLogAnnotationAttrMap = AnnotationUtils.getAnnotationAttributes(operationLogAnnotation);

        Operator operator = getOperator();
        BizCategory bizCategory =  (BizCategory) operationLogAnnotationAttrMap.get("bizCategory");
        String bizTarget =  (String) operationLogAnnotationAttrMap.get("bizTarget");
        String originBizNo = (String) operationLogAnnotationAttrMap.get("bizNo");
        String bizNo =  parseBizNo(bizCategory, result, method, originBizNo, arguments);
        String operationLogContent = encapsulateOperationLogContent(operator, bizCategory, bizTarget, bizNo);
        LogRecord logRecord = encapsulateLogRecord(operator, bizTarget, bizNo, operationLogContent, throwable);
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

    private String encapsulateOperationLogContent(Operator operator,
                                                  BizCategory bizCategory,
                                                  String bizTarget,
                                                  String bizNo) {
        return String.format("%s %s %s, bizNo = %s",
                StringUtils.isNotEmpty(operator.getOperatorName()) ? operator.getOperatorName() : operator.getOperatorId(),
                bizCategory.getName(),
                bizTarget,
                bizNo);

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

    private String parseBizNo(BizCategory bizCategory,
                              Object result,
                              Method method,
                              String bizNo,
                              Object[] arguments) {
        if (StringUtils.isEmpty(bizNo) || !bizNo.startsWith(SPEL_PREFIX)) {
            return bizNo;
        }

        String finalBizNo = null;
        if (PLACE_ORDER.name().equals(bizCategory.name())
                || CREATE.name().equals(bizCategory.name())) {
            finalBizNo = parseSpelValue(bizNo, result);
        } else {
            for (int index = 0; index < arguments.length; index++) {
                Object arg = arguments[index];
                if (arg instanceof BindingResult) {
                    // Skip parse
                } else if (arg instanceof Collection<?>) {
                    // Unsupported collection argument
                } else {
                    MethodParameter methodParameter = new MethodParameter(method, index);
                    if (methodParameter.hasParameterAnnotation(RequestBody.class)) {
                        finalBizNo = parseSpelValue(bizNo, arg);
                        break;
                    }
                    finalBizNo = parseSpelValue(bizNo, arg);
                }
                if (StringUtils.isNotEmpty(finalBizNo)) {
                    break;
                }
            }
        }

        return finalBizNo;
    }

    private String parseSpelValue(String spel, Object rootObj) {
        String spelVal = null;
        try {
            Expression expression = expressionParser.parseExpression(spel);
            spelVal = expression.getValue(rootObj, String.class);
        } catch (ParseException | EvaluationException e) {
            // Nothing to do
        }

        return spelVal;
    }
}
