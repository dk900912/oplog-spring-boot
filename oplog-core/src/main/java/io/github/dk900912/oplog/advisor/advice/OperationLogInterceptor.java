package io.github.dk900912.oplog.advisor.advice;

import io.github.dk900912.oplog.BizCategory;
import io.github.dk900912.oplog.LogRecord;
import io.github.dk900912.oplog.Operator;
import io.github.dk900912.oplog.annotation.OperationLog;
import io.github.dk900912.oplog.persistence.LogRecordPersistenceService;
import io.github.dk900912.oplog.service.OperatorService;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

import static io.github.dk900912.oplog.BizCategory.CREATE;
import static io.github.dk900912.oplog.BizCategory.PLACE_ORDER;

/**
 * @author dukui
 */
public class OperationLogInterceptor implements MethodInterceptor {

    private static final String SPEL_PREFIX = "#";
    // thread-safe
    private static final LocalVariableTableParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new LocalVariableTableParameterNameDiscoverer();

    private OperatorService operatorService;

    private LogRecordPersistenceService logRecordPersistenceService;
    // thread-safe
    private final ExpressionParser expressionParser;

    public OperationLogInterceptor() {
        expressionParser = new SpelExpressionParser();
    }

    /**
     * @param invocation  连接点，Spring AOP 在连接点周围维护了连接器链
     * @return            返回目标方法执行的结果
     * @throws Throwable  目标方法执行过程中所抛出的异常
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
        // operationLogAnnotation 不可能是 null，只能进入了当前方法中，那么目标方法一定是由 @OperationLog 注解标注
        Annotation operationLogAnnotation = AnnotationUtils.findAnnotation(method, OperationLog.class);
        Map<String, Object> operationLogAnnotationAttrMap = AnnotationUtils.getAnnotationAttributes(operationLogAnnotation);

        Operator operator = getOperator();
        BizCategory bizCategory =  (BizCategory) operationLogAnnotationAttrMap.get("bizCategory");
        String bizTarget =  (String) operationLogAnnotationAttrMap.get("bizTarget");
        String originBizNo = (String) operationLogAnnotationAttrMap.get("bizNo");
        String bizNo =  parseBizNo(bizCategory, result, method, originBizNo, arguments);
        String operationLogContent = encapsulateOperationLogContent(operator, bizCategory, bizTarget, bizNo);
        LogRecord logRecord = encapsulateLogRecord(operator, bizTarget, bizCategory, bizNo, operationLogContent, throwable);
        logRecordPersistenceService.doLogRecordPersistence(logRecord);

        // 如果目标方法执行过程中抛出了异常，那么一定要重新抛出
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
                                          BizCategory operationCategory,
                                          String bizNo,
                                          String content,
                                          Throwable throwable) {
        LogRecord logRecord = new LogRecord();
        logRecord.setOperatorId(operator.getOperatorId());
        logRecord.setOperatorName(operator.getOperatorName());
        logRecord.setOperationTarget(bizTarget);
        logRecord.setOperationCategory(operationCategory);
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

        MethodBasedEvaluationContext methodBasedEvaluationContext = new MethodBasedEvaluationContext(
                null, method, arguments, PARAMETER_NAME_DISCOVERER);
        if (PLACE_ORDER.name().equals(bizCategory.name())
                || CREATE.name().equals(bizCategory.name())) {
            methodBasedEvaluationContext.setVariable(ClassUtils.getSimpleName(result), result);
        }

        return parseSpelValue(bizNo, methodBasedEvaluationContext);
    }

    private String parseSpelValue(String spel, MethodBasedEvaluationContext methodBasedEvaluationContext) {
        String spelVal = null;
        try {
            Expression expression = expressionParser.parseExpression(spel);
            spelVal = expression.getValue(methodBasedEvaluationContext, String.class);
        } catch (ParseException | EvaluationException e) {
            // Nothing to do
        }

        return spelVal;
    }
}
