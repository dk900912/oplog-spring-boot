package io.github.dk900912.oplog.support;

import de.danielbechler.diff.ObjectDifferBuilder;
import de.danielbechler.diff.node.DiffNode;
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
import io.github.dk900912.oplog.support.diff.DiffMapVisitor;
import io.github.dk900912.oplog.support.diff.DiffSelectorMethod;
import io.github.dk900912.oplog.support.diff.DiffSelectorRegistry;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StopWatch;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static io.github.dk900912.oplog.constant.Constants.BIZ_CATEGORY;
import static io.github.dk900912.oplog.constant.Constants.BIZ_NO;
import static io.github.dk900912.oplog.constant.Constants.BIZ_TARGET;
import static io.github.dk900912.oplog.constant.Constants.DIFF_SELECTOR;
import static io.github.dk900912.oplog.context.OperationLogContext.OPERATION_LOG_INFO;
import static io.github.dk900912.oplog.context.OperationLogContext.PREVIOUS_CONTENT;

/**
 * Template class that simplifies the execution of business logics with operation log semantics.
 * <p>
 * This class is thread-safe and suitable for concurrent access.
 *
 * @author dukui
 */
public class OperationLogTemplate implements OperationLogOperations {

    private static final Logger logger = LoggerFactory.getLogger(OperationLogTemplate.class);

    private OperatorService operatorService;

    private LogRecordPersistenceService logRecordPersistenceService;

    private OperationResultAnalyzerService operationResultAnalyzerService;

    private final BizAttributeBasedSpExprParser bizAttributeBasedSpExprParser;

    private final RequestMappingParser requestMappingParser;

    private final OperationLogAnnotationAttributeMapParser operationLogAnnotationAttributeMapParser;

    private final String tenant;

    private final ConversionService conversionService;

    public OperationLogTemplate(String tenant, ConversionService conversionService) {
        this.bizAttributeBasedSpExprParser = new BizAttributeBasedSpExprParser();
        this.requestMappingParser = new RequestMappingParser();
        this.operationLogAnnotationAttributeMapParser = new OperationLogAnnotationAttributeMapParser();
        this.tenant = tenant;
        this.conversionService = conversionService;
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

    @Override
    public final <T, E extends Throwable> T execute(OperationLogCallback<T, E> operationLogCallback) throws E {
        Assert.notNull(operationLogCallback, "Callback instance must not be null");
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

    private void unBind() {
        OperationLogSynchronizationManager.clear();
    }

    private <T, E extends Throwable> void preProcessBeforeTargetExecution(OperationLogCallback<T, E> operationLogCallback) {
        BizCategory bizCategory = null;
        String bizTarget = null;
        Object bizNo = null;
        Object previousContent = null;
        OperationLogInfo operationLogInfo = null;
        if (operationLogCallback instanceof MethodInvocationOperationLogCallback) {
            MethodInvocation invocation = ((MethodInvocationOperationLogCallback<?, ?>) operationLogCallback).getInvocation();
            @SuppressWarnings("unchecked")
            final Map<String, Object> operationLogAnnotationAttrMap =
                    (Map<String, Object>) operationLogAnnotationAttributeMapParser.parse(invocation.getMethod());
            bizCategory = (BizCategory) operationLogAnnotationAttrMap.get(BIZ_CATEGORY);
            bizTarget = (String) bizAttributeBasedSpExprParser.parse(
                    new ParsableBizInfo(invocation, null, (String) operationLogAnnotationAttrMap.get(BIZ_TARGET)));
            bizNo = (String) bizAttributeBasedSpExprParser.parse(
                    new ParsableBizInfo(invocation, null, (String) operationLogAnnotationAttrMap.get(BIZ_NO)));
            String diffSelector = (String) operationLogAnnotationAttrMap.get(DIFF_SELECTOR);
            operationLogInfo = new OperationLogInfo(bizCategory, bizTarget, bizNo, diffSelector);
            if (BizCategory.UPDATE == bizCategory && StringUtils.isNotEmpty(diffSelector)) {
                previousContent = prepareDiff(diffSelector, bizNo);
            }
        } else if (operationLogCallback instanceof SimpleOperationLogCallback<?, ?> simpleOperationLogCallback) {
            bizCategory = simpleOperationLogCallback.getBizCategory();
            bizTarget = simpleOperationLogCallback.getBizTarget();
            bizNo = simpleOperationLogCallback.getBizNo();
            UnaryOperator<Object> diffSelector = simpleOperationLogCallback.getDiffSelector();
            operationLogInfo = new OperationLogInfo(bizCategory, bizTarget, bizNo, diffSelector);
            previousContent = diffSelector.apply(bizNo);
        } else {
            throw new IllegalStateException("Unsupported OperationLogCallback");
        }

        final OperationLogContext operationLogContext = OperationLogSynchronizationManager.getContext();
        operationLogContext.setAttribute(OPERATION_LOG_INFO, operationLogInfo);
        operationLogContext.setAttribute(PREVIOUS_CONTENT, previousContent);
    }

    private void postProcessAfterTargetExecution(MethodInvocationResult methodInvocationResult) {
        final LogRecord logRecord = encapsulateLogRecord(methodInvocationResult);
        logRecordPersistenceService.doLogRecordPersistence(logRecord);
    }

    @SuppressWarnings({"all"})
    private LogRecord encapsulateLogRecord(MethodInvocationResult methodInvocationResult) {
        Object result = methodInvocationResult.getResult();
        StopWatch performance = methodInvocationResult.getPerformance();
        Throwable throwable = methodInvocationResult.getThrowable();
        final OperationLogContext operationLogContext = OperationLogSynchronizationManager.getContext();
        OperationLogInfo operationLogInfo = (OperationLogInfo) operationLogContext.getAttribute(OPERATION_LOG_INFO);
        Object previousContent = operationLogContext.getAttribute(PREVIOUS_CONTENT);
        Object currentContent = null;
        Object diffSelector = operationLogInfo.getDiffSelector();
        if (diffSelector instanceof String selector && StringUtils.isNotBlank(selector)) {
            currentContent = prepareDiff(selector, operationLogInfo.getBizNo());
        } else if (diffSelector instanceof UnaryOperator selector) {
            currentContent = selector.apply(operationLogInfo.getBizNo());
        }

        Operator operator = getOperator();
        boolean isSuccess = analyzeOperationResult(throwable, result);
        String requestMapping = Optional.ofNullable(methodInvocationResult.getMethodInvocation())
                .map(MethodInvocation::getMethod)
                .map(mtd -> (String) requestMappingParser.parse(mtd))
                .orElse(null);

        return LogRecord.builder()
                .withBizNo(String.valueOf(operationLogInfo.getBizNo()))
                .withOperationCategory(operationLogInfo.getBizCategory())
                .withOperationTarget(operationLogInfo.getBizTarget())
                .withPreviousContent(previousContent)
                .withCurrentContent(currentContent)
                .withContentDiff(doDiff(previousContent, currentContent))
                .withTenant(this.tenant)
                .withOperatorId(operator.getOperatorId())
                .withOperatorName(operator.getOperatorName())
                .withRequestMapping(requestMapping)
                .withOperationResult(isSuccess)
                .withOperationTime(LocalDateTime.now())
                .withTargetExecutionTime(performance.getTotalTimeMillis())
                .build();
    }

    private Operator getOperator() {
        return Optional.ofNullable(operatorService.getOperator())
                .orElse(new Operator());
    }

    private boolean analyzeOperationResult(Throwable throwable, Object result) {
        return operationResultAnalyzerService.analyzeOperationResult(throwable, result);
    }

    private Object prepareDiff(String diffSelector, Object bizNo) {
        Object content = null;
        try {
            DiffSelectorMethod diffSelectorMethod = DiffSelectorRegistry.getInstance().getDiffSelectorMethod(diffSelector);
            if (Objects.isNull(diffSelectorMethod)) {
                logger.warn("No matching DiffSelectorMethod instance found, diff-selector = {}", diffSelector);
                return content;
            }
            Method invocableMethod = diffSelectorMethod.getMethod();
            Object invocableTarget = diffSelectorMethod.getBean();
            MethodParameter invocableMethodParameter = diffSelectorMethod.getMethodParameter();
            Class<?> bizNoClazz = invocableMethodParameter.getParameter().getType();
            Object convertedBizNo = convertBizNoIfNecessary(bizNo, bizNoClazz);
            ReflectionUtils.makeAccessible(invocableMethod);
            content = ReflectionUtils.invokeMethod(invocableMethod, invocableTarget, convertedBizNo);
        } catch (RuntimeException e) {
            // Ignore
        }
        return content;
    }

    private Map<String, Map<String, Object>> doDiff(Object base, Object modified) {
        DiffNode root = ObjectDifferBuilder.buildDefault().compare(base, modified);
        DiffMapVisitor diffMapVisitor = new DiffMapVisitor(base, modified);
        root.visit(diffMapVisitor);
        return diffMapVisitor.getDiffMap();
    }

    private Object convertBizNoIfNecessary(Object bizNo, Class<?> bizNoClazz) {
        if (conversionService.canConvert(bizNoClazz, bizNoClazz)) {
            try {
                return conversionService.convert(bizNo, bizNoClazz);
            } catch (ConversionFailedException e) {
                logger.warn("BizNo convert failed, from {} to {}", bizNo.getClass(), bizNoClazz);
            }
        } else {
            logger.warn("ConversionService can not convert this bizNo, bizNo = {}", bizNo);
        }
        return bizNo;
    }
}