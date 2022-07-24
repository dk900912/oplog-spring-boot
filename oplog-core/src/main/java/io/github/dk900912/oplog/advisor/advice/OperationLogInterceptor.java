package io.github.dk900912.oplog.advisor.advice;

import io.github.dk900912.oplog.context.LogRecordContextHolder;
import io.github.dk900912.oplog.model.BizCategory;
import io.github.dk900912.oplog.model.LogRecord;
import io.github.dk900912.oplog.model.MethodInvocationResult;
import io.github.dk900912.oplog.model.OperationLogInfo;
import io.github.dk900912.oplog.model.Operator;
import io.github.dk900912.oplog.model.ParsableBizInfo;
import io.github.dk900912.oplog.parser.BizAttributeBasedSpExprParser;
import io.github.dk900912.oplog.parser.OperationLogAnnotationAttributeMapParser;
import io.github.dk900912.oplog.parser.RequestMappingParser;
import io.github.dk900912.oplog.service.LogRecordPersistenceService;
import io.github.dk900912.oplog.service.OperationResultAnalyzerService;
import io.github.dk900912.oplog.service.OperatorService;
import io.github.dk900912.oplog.service.PreviousContentSelector;
import io.github.dk900912.oplog.service.PreviousContentSelectorFactory;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author dukui
 */
public final class OperationLogInterceptor implements MethodInterceptor {

    private OperatorService operatorService;

    private LogRecordPersistenceService logRecordPersistenceService;

    private OperationResultAnalyzerService operationResultAnalyzerService;

    private PreviousContentSelectorFactory previousContentSelectorFactory;

    private final BizAttributeBasedSpExprParser bizAttributeBasedSpExprParser;

    private final RequestMappingParser requestMappingParser;

    private final OperationLogAnnotationAttributeMapParser operationLogAnnotationAttributeMapParser;

    private final String tenant;

    public OperationLogInterceptor(String tenant) {
        this.bizAttributeBasedSpExprParser = new BizAttributeBasedSpExprParser();
        this.requestMappingParser = new RequestMappingParser();
        this.operationLogAnnotationAttributeMapParser = new OperationLogAnnotationAttributeMapParser();
        this.tenant = tenant;
    }

    /**
     * @param invocation  连接点，Spring AOP 在连接点周围维护了拦截器链
     * @return            返回目标方法执行的结果
     * @throws Throwable  目标方法执行过程中所抛出的异常
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        LogRecordContextHolder.setContext(LogRecordContextHolder.createEmptyContext());
        
        preProcessBeforeTargetExecution(invocation);

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

        try {
            postProcessAfterTargetExecution(new MethodInvocationResult(invocation, result, throwable, stopWatch));
        } finally {
            LogRecordContextHolder.clearContext();
        }

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

    public void setPreviousContentSelectorFactory(PreviousContentSelectorFactory previousContentSelectorFactory) {
        this.previousContentSelectorFactory = previousContentSelectorFactory;
    }

    public String getTenant() {
        return this.tenant;
    }

    // +------------------------------------------------+
    // |               private methods                  |
    // +------------------------------------------------+

    private void preProcessBeforeTargetExecution(MethodInvocation invocation) {
        @SuppressWarnings("unchecked")
        OperationLogInfo operationLogInfo = new OperationLogInfo(
                (Map<String, Object>) operationLogAnnotationAttributeMapParser.parse(invocation.getMethod()));

        String bizNo = (String) bizAttributeBasedSpExprParser.parse(
                new ParsableBizInfo(invocation, null, operationLogInfo.getOriginBizNo()));
        String bizTarget = (String) bizAttributeBasedSpExprParser.parse(
                new ParsableBizInfo(invocation, null, operationLogInfo.getOriginBizTarget()));
        final LogRecord logRecord = LogRecord.builder()
                .withBizNo(bizNo)
                .withOperationCategory(operationLogInfo.getBizCategory())
                .withOperationTarget(bizTarget)
                .build();

        if (BizCategory.UPDATE == operationLogInfo.getBizCategory()
                && StringUtils.isNotEmpty(operationLogInfo.getPreviousContentSelectorName())) {
            PreviousContentSelector previousContentSelector =
                    previousContentSelectorFactory.getSelector(operationLogInfo.getPreviousContentSelectorName());
            if (Objects.nonNull(previousContentSelector)) {
                Object previousContent = previousContentSelector.selectPreviousContent(bizNo);
                logRecord.setPreviousContent(previousContent);
            }
            logRecord.setCurrentContent(detectCurrentContent(invocation));
        }
        
        LogRecordContextHolder.getContext().setLogRecord(logRecord);
    }

    private void postProcessAfterTargetExecution(MethodInvocationResult methodInvocationResult) {
        final LogRecord logRecord = encapsulateLogRecord(methodInvocationResult);
        logRecordPersistenceService.doLogRecordPersistence(logRecord);
    }

    private LogRecord encapsulateLogRecord(MethodInvocationResult methodInvocationResult) {
        MethodInvocation methodInvocation = methodInvocationResult.getMethodInvocation();
        Method method = methodInvocation.getMethod();
        Object result = methodInvocationResult.getResult();
        StopWatch performance = methodInvocationResult.getPerformance();
        Throwable throwable = methodInvocationResult.getThrowable();
        LogRecord logRecord = LogRecordContextHolder.getContext().getLogRecord();

        Operator operator = getOperator();
        boolean isSuccess = analyzeOperationResult(throwable, result);
        String requestMapping = (String) requestMappingParser.parse(method);

        return logRecord.setTenant(getTenant())
                .setOperatorId(operator.getOperatorId())
                .setOperatorName(operator.getOperatorName())
                .setRequestMapping(requestMapping)
                .setOperationResult(isSuccess)
                .setOperationTime(LocalDateTime.now())
                .setTargetExecutionTime(performance.getTotalTimeMillis());
    }

    private Operator getOperator() {
        return Optional.ofNullable(operatorService.getOperator())
                .orElse(new Operator());
    }

    private boolean analyzeOperationResult(Throwable throwable, Object result) {
        return operationResultAnalyzerService.analyzeOperationResult(throwable, result);
    }

    private Object detectCurrentContent(MethodInvocation invocation) {
        Object[] args = invocation.getArguments();
        for (int i = 0; i < args.length; i++) {
            MethodParameter methodParameter = new MethodParameter(invocation.getMethod(), i);
            if (methodParameter.hasParameterAnnotation(RequestBody.class)) {
                return args[i];
            }
        }
        return null;
    }
}
