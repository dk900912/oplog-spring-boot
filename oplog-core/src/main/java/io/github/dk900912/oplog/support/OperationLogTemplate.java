package io.github.dk900912.oplog.support;

import io.github.dk900912.oplog.context.OperationLogContext;
import io.github.dk900912.oplog.context.OperationLogContextSupport;
import io.github.dk900912.oplog.context.OperationLogSynchronizationManager;
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
import io.github.dk900912.oplog.service.Selector;
import io.github.dk900912.oplog.service.SelectorFactory;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.github.dk900912.oplog.context.OperationLogContext.LOG_RECORD;

/**
 * Template class that simplifies the execution of operations with operation log semantics.
 * <p>
 * This class is thread-safe and suitable for concurrent access.
 *
 * @author dukui
 */
public class OperationLogTemplate implements OperationLogOperations {

    private OperatorService operatorService;

    private LogRecordPersistenceService logRecordPersistenceService;

    private OperationResultAnalyzerService operationResultAnalyzerService;

    private SelectorFactory selectorFactory;

    private final BizAttributeBasedSpExprParser bizAttributeBasedSpExprParser;

    private final RequestMappingParser requestMappingParser;

    private final OperationLogAnnotationAttributeMapParser operationLogAnnotationAttributeMapParser;

    private final String tenant;

    public OperationLogTemplate(String tenant) {
        this.bizAttributeBasedSpExprParser = new BizAttributeBasedSpExprParser();
        this.requestMappingParser = new RequestMappingParser();
        this.operationLogAnnotationAttributeMapParser = new OperationLogAnnotationAttributeMapParser();
        this.tenant = tenant;
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

    public void setSelectorFactory(SelectorFactory selectorFactory) {
        this.selectorFactory = selectorFactory;
    }

    @Override
    public final <T, E extends Throwable> T execute(OperationLogCallback<T, E> operationLogCallback) throws E {
        Assert.notNull(operationLogCallback, "Callback object must not be null");
        return doExecute(operationLogCallback);
    }

    @SuppressWarnings("unchecked")
    protected <T, E extends Throwable> T doExecute(OperationLogCallback<T, E> operationLogCallback) throws E {

        OperationLogContext context = bind();

        preProcessBeforeTargetExecution(operationLogCallback);

        E targetThrowable = null;
        T result = null;
        StopWatch stopWatch = new StopWatch();
        try {
            stopWatch.start();
            result = operationLogCallback.doWithOperationLog(context);
        } catch (Throwable e) {
            targetThrowable = (E) e;
        } finally {
            stopWatch.stop();
        }

        try {
            postProcessAfterTargetExecution(new MethodInvocationResult(operationLogCallback, result, targetThrowable, stopWatch));
        } finally {
            unBind();
        }

        if (Objects.nonNull(targetThrowable)) {
            throw targetThrowable;
        }

        return result;
    }

    // +------------------------------------------------+
    // |               private methods                  |
    // +------------------------------------------------+

    private OperationLogContext bind() {
        OperationLogContext parent = OperationLogSynchronizationManager.getContext();
        OperationLogContext context = new OperationLogContextSupport(parent);
        OperationLogSynchronizationManager.register(context);
        return context;
    }

    private OperationLogContext unBind() {
        return OperationLogSynchronizationManager.clear();
    }

    private <T, E extends Throwable> void preProcessBeforeTargetExecution(OperationLogCallback<T, E> operationLogCallback) {
        BizCategory bizCategory = null;
        String bizTarget = null;
        String bizNo = null;
        String selectorName = null;
        Object previousContent = null;
        Object currentContent = null;
        if (operationLogCallback instanceof MethodInvocationOperationLogCallback) {
            MethodInvocation invocation = ((MethodInvocationOperationLogCallback<?, ?>) operationLogCallback).getInvocation();
            @SuppressWarnings("unchecked")
            OperationLogInfo operationLogInfo = new OperationLogInfo(
                    (Map<String, Object>) operationLogAnnotationAttributeMapParser.parse(invocation.getMethod()));

            bizCategory = operationLogInfo.getBizCategory();
            bizTarget = (String) bizAttributeBasedSpExprParser.parse(
                    new ParsableBizInfo(invocation, null, operationLogInfo.getOriginBizTarget()));
            bizNo = (String) bizAttributeBasedSpExprParser.parse(
                    new ParsableBizInfo(invocation, null, operationLogInfo.getOriginBizNo()));
            selectorName = operationLogInfo.getSelectorName();
        } else if (operationLogCallback instanceof SimpleOperationLogCallback) {
            SimpleOperationLogCallback<?, ?> simpleOperationLogCallback = (SimpleOperationLogCallback<?, ?>) operationLogCallback;
            bizCategory = simpleOperationLogCallback.getBizCategory();
            bizTarget = simpleOperationLogCallback.getBizTarget();
            bizNo = simpleOperationLogCallback.getBizNo();
            selectorName = simpleOperationLogCallback.getSelectorName();
        } else {
            throw new IllegalStateException("Unsupported OperationLogCallback");
        }

        if (BizCategory.UPDATE == bizCategory && StringUtils.isNotEmpty(selectorName)) {
            Selector selector = selectorFactory.getSelector(selectorName);
            if (Objects.nonNull(selector)) {
                previousContent = selector.select(bizNo);
            }
            currentContent = detectCurrentContent(operationLogCallback);
        }
        final LogRecord logRecord = LogRecord.builder()
                .withBizNo(bizNo)
                .withOperationCategory(bizCategory)
                .withOperationTarget(bizTarget)
                .withPreviousContent(previousContent)
                .withCurrentContent(currentContent)
                .build();
        OperationLogSynchronizationManager.getContext().setAttribute(LOG_RECORD, logRecord);
    }

    private void postProcessAfterTargetExecution(MethodInvocationResult methodInvocationResult) {
        final LogRecord logRecord = encapsulateLogRecord(methodInvocationResult);
        logRecordPersistenceService.doLogRecordPersistence(logRecord);
    }

    private LogRecord encapsulateLogRecord(MethodInvocationResult methodInvocationResult) {
        Object result = methodInvocationResult.getResult();
        StopWatch performance = methodInvocationResult.getPerformance();
        Throwable throwable = methodInvocationResult.getThrowable();
        LogRecord logRecord = (LogRecord) OperationLogSynchronizationManager.getContext().getAttribute(LOG_RECORD);

        Operator operator = getOperator();
        boolean isSuccess = analyzeOperationResult(throwable, result);
        String requestMapping = Optional.ofNullable(methodInvocationResult.getMethodInvocation())
                .map(MethodInvocation::getMethod)
                .map(mtd -> (String) requestMappingParser.parse(mtd))
                .orElse(null);

        return logRecord.setTenant(this.tenant)
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

    private <T, E extends Throwable> Object detectCurrentContent(OperationLogCallback<T, E> operationLogCallback) {
        if (operationLogCallback instanceof MethodInvocationOperationLogCallback) {
            MethodInvocationOperationLogCallback<?, ?> methodInvocationOperationLogCallback = (MethodInvocationOperationLogCallback<?, ?>) operationLogCallback;
            MethodInvocation invocation = methodInvocationOperationLogCallback.getInvocation();
            Object[] args = invocation.getArguments();
            for (int i = 0; i < args.length; i++) {
                MethodParameter methodParameter = new MethodParameter(invocation.getMethod(), i);
                if (methodParameter.hasParameterAnnotation(RequestBody.class)) {
                    return args[i];
                }
            }
        } else if (operationLogCallback instanceof SimpleOperationLogCallback) {
            return ((SimpleOperationLogCallback<?, ?>) operationLogCallback).getCurrentContent();
        }
        return null;
    }
}