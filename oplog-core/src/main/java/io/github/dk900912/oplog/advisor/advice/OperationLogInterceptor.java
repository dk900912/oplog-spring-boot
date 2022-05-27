package io.github.dk900912.oplog.advisor.advice;

import io.github.dk900912.oplog.annotation.OperationLog;
import io.github.dk900912.oplog.model.BizCategory;
import io.github.dk900912.oplog.model.BizNoParseInfo;
import io.github.dk900912.oplog.model.LogRecord;
import io.github.dk900912.oplog.model.MethodInvocationResult;
import io.github.dk900912.oplog.model.Operator;
import io.github.dk900912.oplog.parser.BizNoParser;
import io.github.dk900912.oplog.parser.RequestMappingParser;
import io.github.dk900912.oplog.service.LogRecordPersistenceService;
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

    private final BizNoParser bizNoParser;

    private final RequestMappingParser requestMappingParser;

    public OperationLogInterceptor() {
        this.bizNoParser = new BizNoParser();
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

    // +------------------------------------------------+
    // |               private methods                  |
    // +------------------------------------------------+

    private void persistOperationLog(MethodInvocationResult methodInvocationResult) {
        LogRecord logRecord = encapsulateLogRecord(methodInvocationResult);
        logRecordPersistenceService.doLogRecordPersistence(logRecord);
    }

    private LogRecord encapsulateLogRecord(MethodInvocationResult methodInvocationResult) {
        MethodInvocation methodInvocation = methodInvocationResult.getMethodInvocation();
        Method method = methodInvocation.getMethod();
        Operator operator = getOperator();
        Object result = methodInvocationResult.getResult();
        StopWatch performance = methodInvocationResult.getPerformance();

        Map<String, Object> operationLogAnnotationAttrMap = getOperationLogAnnotationAttr(method);
        String requestMapping = requestMappingParser.parse(method);
        BizCategory bizCategory =  (BizCategory) operationLogAnnotationAttrMap.get("bizCategory");
        String bizTarget =  (String) operationLogAnnotationAttrMap.get("bizTarget");
        String originBizNo = (String) operationLogAnnotationAttrMap.get("bizNo");
        String bizNo =  bizNoParser.parse(new BizNoParseInfo(methodInvocation, result, originBizNo));

        return LogRecord.builder()
                .withOperatorId(operator.getOperatorId())
                .withOperatorName(operator.getOperatorName())
                .withOperationTarget(bizTarget)
                .withOperationCategory(bizCategory)
                .withBizNo(bizNo)
                .withRequestMapping(requestMapping)
                .withOperationResult(Objects.isNull(methodInvocationResult.getThrowable()))
                .withOperationTime(LocalDateTime.now())
                .withTargetExecutionTime(performance.getTotalTimeMillis())
                .build();
    }

    private Map<String, Object> getOperationLogAnnotationAttr(Method method) {
        Annotation operationLogAnnotation = AnnotationUtils.findAnnotation(method, OperationLog.class);
        return AnnotationUtils.getAnnotationAttributes(operationLogAnnotation);
    }

    private Operator getOperator() {
        return Optional.ofNullable(operatorService.getOperator())
                .orElse(new Operator());
    }
}
