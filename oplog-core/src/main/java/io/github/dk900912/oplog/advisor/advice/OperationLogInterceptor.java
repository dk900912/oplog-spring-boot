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
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.github.dk900912.oplog.BizCategory.CREATE;
import static io.github.dk900912.oplog.BizCategory.PLACE_ORDER;

/**
 * @author dukui
 */
public class OperationLogInterceptor implements MethodInterceptor {

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
        String originBizTarget =  (String) operationLogAnnotationAttrMap.get("bizTarget");
        String originBizNo = (String) operationLogAnnotationAttrMap.get("bizNo");
        List<String> bizTargetList =  parseBizTarget(originBizTarget, arguments);
        List<String> bizNoList =  parseBizNo(bizCategory, result, originBizNo, arguments);

        for (int index = 0; index < bizNoList.size(); index ++) {
            String bizTarget = bizTargetList.get(index);
            String bizNo = bizNoList.get(index);
            String operationLogContent = encapsulateOperationLogContent(operator, bizCategory, bizTarget, bizNo);
            LogRecord logRecord = encapsulateLogRecord(operator, bizTarget, bizNo, operationLogContent, throwable);

            logRecordPersistenceService.doLogRecordPersistence(logRecord);
        }

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

    private List<String> parseBizNo(BizCategory bizCategory, Object result, String bizNo, Object[] arguments) {
        List<String> finalBizNoList = new ArrayList<>();
        if (PLACE_ORDER.name().equals(bizCategory.getName())
                || CREATE.name().equals(bizCategory.getName())) {
            try {
                finalBizNoList.addAll(parseSpelValue(bizNo, Arrays.asList(result)));
            } catch (EvaluationException e) {
                // Nothing to do
            }
        } else {
            for (Object arg : arguments) {
                if (arg instanceof BindingResult) {
                    // Skip BindingResult
                } else if (arg instanceof Collection<?>) {
                    Collection<?> collection = (Collection<?>) arg;
                    finalBizNoList.addAll(parseSpelValue(bizNo, new ArrayList<>(collection)));
                } else if (arg instanceof String) {
                    finalBizNoList.add(bizNo);
                } else {
                    finalBizNoList.addAll(parseSpelValue(bizNo, Collections.singletonList(arg)));
                }
                if (!CollectionUtils.isEmpty(finalBizNoList)) {
                    break;
                }
            }
        }

        return finalBizNoList;
    }

    private List<String> parseBizTarget(String bizTarget, Object[] arguments) {
        List<String> finalBizTargetList = new ArrayList<>();
        for (Object arg : arguments) {
            if (arg instanceof BindingResult) {
                // Skip BindingResult
            } else if (arg instanceof Collection<?>) {
                Collection<?> collection = (Collection<?>) arg;
                finalBizTargetList.addAll(parseSpelValue(bizTarget, new ArrayList<>(collection)));
            } else if (arg instanceof String) {
                finalBizTargetList.add(bizTarget);
            } else {
                finalBizTargetList.addAll(parseSpelValue(bizTarget, Collections.singletonList(arg)));
            }
            if (!CollectionUtils.isEmpty(finalBizTargetList)) {
                break;
            }
        }

        return finalBizTargetList;
    }

    private List<String> parseSpelValue(String spel, List<?> rootObjList) {
        List<String> spelValList= new ArrayList<>();
        Expression expression = null;
        try {
            expression = expressionParser.parseExpression(spel);
        } catch (ParseException e) {
            return spelValList;
        }
        for (Object rootObj : rootObjList) {
            try {
                String finalSpelVal = expression.getValue(rootObj, String.class);
                if (StringUtils.isNotEmpty(finalSpelVal)) {
                    spelValList.add(finalSpelVal);
                }
            } catch (EvaluationException e) {
                // Nothing to do
            }
        }

        return spelValList;
    }
}
