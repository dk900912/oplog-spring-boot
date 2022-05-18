package io.github.dk900912.oplog.advisor.advice;

import io.github.dk900912.oplog.annotation.OperationLog;
import io.github.dk900912.oplog.model.BizCategory;
import io.github.dk900912.oplog.model.LogRecord;
import io.github.dk900912.oplog.model.Operator;
import io.github.dk900912.oplog.service.LogRecordPersistenceService;
import io.github.dk900912.oplog.service.OperatorService;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static io.github.dk900912.oplog.model.BizCategory.CREATE;
import static io.github.dk900912.oplog.model.BizCategory.PLACE_ORDER;

/**
 * @author dukui
 */
public class OperationLogInterceptor implements MethodInterceptor {
    /**
     * SpEL 表达式必须要以 # 开头，否则不会走解析逻辑
     */
    private static final String SPEL_PREFIX = "#";

    /**
     * thread-safe
     */
    private static final LocalVariableTableParameterNameDiscoverer PARAMETER_NAME_DISCOVERER =
            new LocalVariableTableParameterNameDiscoverer();

    private static final ConcurrentHashMap<Method, String> LOCAL_CACHE =
            new ConcurrentHashMap<>(200);

    private OperatorService operatorService;

    private LogRecordPersistenceService logRecordPersistenceService;

    /**
     * thread-safe
     */
    private final ExpressionParser expressionParser;

    public OperationLogInterceptor() {
        expressionParser = new SpelExpressionParser();
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
        try {
            result = invocation.proceed();
        } catch (Throwable e) {
            throwable = e;
        }

        // 切面逻辑
        persistOperationLog(invocation, result, throwable);

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

    // +------------------------------------------------+
    // |               private methods                  |
    // +------------------------------------------------+

    private void persistOperationLog(MethodInvocation invocation, Object result, Throwable throwable) {
        Object[] arguments = invocation.getArguments();
        Method method = invocation.getMethod();
        Map<String, Object> operationLogAnnotationAttrMap = getOperationLogAnnotationAttr(method);
        String requestMapping = getRequestMapping(method);
        Operator operator = getOperator();
        BizCategory bizCategory =  (BizCategory) operationLogAnnotationAttrMap.get("bizCategory");
        String bizTarget =  (String) operationLogAnnotationAttrMap.get("bizTarget");
        String originBizNo = (String) operationLogAnnotationAttrMap.get("bizNo");
        String bizNo =  parseBizNo(bizCategory, result, method, originBizNo, arguments);
        LogRecord logRecord = encapsulateLogRecord(operator, bizTarget, bizCategory, bizNo, requestMapping, throwable);
        logRecordPersistenceService.doLogRecordPersistence(logRecord);
    }

    private String getRequestMapping(Method method) {
        String requestMapping = LOCAL_CACHE.get(method);
        if (StringUtils.isNotEmpty(requestMapping)) {
            return requestMapping;
        }
        try {
            RequestMapping requestMappingOnMethod = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
            Class<?> userType = ClassUtils.getUserClass(method.getDeclaringClass());
            RequestMapping requestMappingOnClass = AnnotatedElementUtils.findMergedAnnotation(userType, RequestMapping.class);
            List<String> list = new ArrayList<>(8);
            if (Objects.nonNull(requestMappingOnClass)) {
                String[] pathArrayOnClass = requestMappingOnClass.path();
                if (ArrayUtils.isNotEmpty(pathArrayOnClass)) {
                    list.addAll(Arrays.asList(StringUtils.split(pathArrayOnClass[0], "/")));
                }
            }
            if (Objects.nonNull(requestMappingOnMethod)) {
                String[] pathArrayOnMethod = requestMappingOnMethod.path();
                if (ArrayUtils.isNotEmpty(pathArrayOnMethod)) {
                    list.addAll(Arrays.asList(StringUtils.split(pathArrayOnMethod[0], "/")));
                }
            }
            if (!CollectionUtils.isEmpty(list)) {
                requestMapping = list
                        .stream()
                        .filter(StringUtils::isNotEmpty)
                        .collect(Collectors.joining("/", "/", ""));
                LOCAL_CACHE.put(method, requestMapping);
            }
        } catch (Throwable throwable) {
            // Nothing to do
        }
        return requestMapping;
    }

    private Map<String, Object> getOperationLogAnnotationAttr(Method method) {
        Annotation operationLogAnnotation = AnnotationUtils.findAnnotation(method, OperationLog.class);
        return AnnotationUtils.getAnnotationAttributes(operationLogAnnotation);
    }

    private LogRecord encapsulateLogRecord(Operator operator,
                                          String bizTarget,
                                          BizCategory operationCategory,
                                          String bizNo,
                                          String requestMapping,
                                          Throwable throwable) {
        return LogRecord
                .builder()
                .withOperatorId(operator.getOperatorId())
                .withOperatorName(operator.getOperatorName())
                .withOperationTarget(bizTarget)
                .withOperationCategory(operationCategory)
                .withBizNo(bizNo)
                .withRequestMapping(requestMapping)
                .withOperationContent(encapsulateOperationLogContent(operator, operationCategory, bizTarget, bizNo))
                .withOperationResult(Objects.isNull(throwable))
                .withOperationTime(LocalDateTime.now())
                .build();
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
            if (Objects.nonNull(result)) {
                methodBasedEvaluationContext.setVariable(StringUtils.uncapitalize(result.getClass().getSimpleName()), result);
            }
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
