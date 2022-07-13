package io.github.dk900912.oplog.advisor.advice;

import io.github.dk900912.oplog.annotation.OperationLog;
import io.github.dk900912.oplog.model.BizCategory;
import io.github.dk900912.oplog.model.LogRecord;
import io.github.dk900912.oplog.model.MethodInvocationResult;
import io.github.dk900912.oplog.model.Operator;
import io.github.dk900912.oplog.model.ParsableBizInfo;
import io.github.dk900912.oplog.parser.BizAttributeBasedSpExprParser;
import io.github.dk900912.oplog.parser.RequestMappingParser;
import io.github.dk900912.oplog.service.LogRecordPersistenceService;
import io.github.dk900912.oplog.service.OperationResultAnalyzerService;
import io.github.dk900912.oplog.service.OperatorService;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StopWatch;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author dukui
 */
public class OperationLogInterceptor implements MethodInterceptor {

    private OperatorService operatorService;

    private LogRecordPersistenceService logRecordPersistenceService;

    private OperationResultAnalyzerService operationResultAnalyzerService;

    private final BizAttributeBasedSpExprParser bizAttributeBasedSpExprParser;

    private final RequestMappingParser requestMappingParser;

    public OperationLogInterceptor() {
        this.bizAttributeBasedSpExprParser = new BizAttributeBasedSpExprParser();
        this.requestMappingParser = new RequestMappingParser();
    }

    /**
     * @param invocation  连接点，Spring AOP 在连接点周围维护了拦截器链
     * @return            返回目标方法执行的结果
     * @throws Throwable  目标方法执行过程中所抛出的异常
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object result = null;
        Throwable throwable = null;
        StopWatch stopWatch = new StopWatch();
        try {
            stopWatch.start();
            result = invocation.proceed();
        } catch (Throwable e) {
            throwable = e;
        } finally {
            stopWatch.stop();
        }

        // 切面逻辑
        persistOperationLog(new MethodInvocationResult(invocation, result, throwable, stopWatch));

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

    public void setOperationResultAnalyzerService(OperationResultAnalyzerService operationResultAnalyzerService) {
        this.operationResultAnalyzerService = operationResultAnalyzerService;
    }

    // +------------------------------------------------+
    // |               private methods                  |
    // +------------------------------------------------+

    private void persistOperationLog(MethodInvocationResult methodInvocationResult) {
        final LogRecord logRecord = encapsulateLogRecord(methodInvocationResult);
        logRecordPersistenceService.doLogRecordPersistence(logRecord);
    }

    private LogRecord encapsulateLogRecord(MethodInvocationResult methodInvocationResult) {
        MethodInvocation methodInvocation = methodInvocationResult.getMethodInvocation();
        Method method = methodInvocation.getMethod();
        Operator operator = getOperator();
        Object result = methodInvocationResult.getResult();
        StopWatch performance = methodInvocationResult.getPerformance();
        Throwable throwable = methodInvocationResult.getThrowable();
        boolean isSuccess = analyzeOperationResult(throwable, result);

        Map<String, Object> operationLogAnnotationAttrMap = getOperationLogAnnotationAttr(method);
        String requestMapping = requestMappingParser.parse(method);
        String tenant = (String) operationLogAnnotationAttrMap.get("tenant");
        BizCategory bizCategory = (BizCategory) operationLogAnnotationAttrMap.get("bizCategory");
        String originBizTarget = (String) operationLogAnnotationAttrMap.get("bizTarget");
        String originBizNo = (String) operationLogAnnotationAttrMap.get("bizNo");
        String bizTarget = bizAttributeBasedSpExprParser.parse(new ParsableBizInfo(methodInvocation, result, originBizTarget));
        String bizNo = bizAttributeBasedSpExprParser.parse(new ParsableBizInfo(methodInvocation, result, originBizNo));

        return LogRecord.builder()
                .withTenant(tenant)
                .withOperatorId(operator.getOperatorId())
                .withOperatorName(operator.getOperatorName())
                .withOperationTarget(bizTarget)
                .withOperationCategory(bizCategory)
                .withBizNo(bizNo)
                .withRequestMapping(requestMapping)
                .withOperationResult(isSuccess)
                .withOperationTime(LocalDateTime.now())
                .withTargetExecutionTime(performance.getTotalTimeMillis())
                .build();
    }

    private Map<String, Object> getOperationLogAnnotationAttr(Method method) {
        Annotation operationLogAnnotation = AnnotationUtils.findAnnotation(method, OperationLog.class);
        // Never null
        return AnnotationUtils.getAnnotationAttributes(operationLogAnnotation);
    }

    private Operator getOperator() {
        return Optional.ofNullable(operatorService.getOperator())
                .orElse(new Operator());
    }

    private boolean analyzeOperationResult(Throwable throwable, Object result) {
        return operationResultAnalyzerService.analyzeOperationResult(throwable, result);
    }
}
