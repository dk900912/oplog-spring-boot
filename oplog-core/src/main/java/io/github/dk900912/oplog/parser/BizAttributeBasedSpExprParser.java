package io.github.dk900912.oplog.parser;

import io.github.dk900912.oplog.model.ParsableBizInfo;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * <h3> 基于 Spring EL、面向 BizTarget 和 BizNo 的解析器 </h3>
 *
 * @author dukui
 */
public class BizAttributeBasedSpExprParser implements Parser<ParsableBizInfo> {

    private static final Logger logger = Logger.getLogger(BizAttributeBasedSpExprParser.class.getName());

    /**
     * thread-safe
     */
    private static final LocalVariableTableParameterNameDiscoverer parameterNameDiscoverer =
            new LocalVariableTableParameterNameDiscoverer();

    /**
     * thread-safe
     */
    private static final ExpressionParser expressionParser = new SpelExpressionParser();

    @Override
    public String parse(ParsableBizInfo parsableBizInfo) {
        final Object result = parsableBizInfo.getResult();
        final String originParsableTarget = parsableBizInfo.getOriginParsableTarget();
        final MethodInvocation methodInvocation = parsableBizInfo.getMethodInvocation();
        final Method method = methodInvocation.getMethod();
        final Object[] arguments = methodInvocation.getArguments();
        if (StringUtils.isEmpty(originParsableTarget) || !originParsableTarget.startsWith("#")) {
            return originParsableTarget;
        }

        MethodBasedEvaluationContext methodBasedEvaluationContext = new MethodBasedEvaluationContext(
                null, method, arguments, parameterNameDiscoverer);
        Optional.<Object>ofNullable(result)
                .map(Object::getClass)
                .map(Class::getSimpleName)
                .map(StringUtils::uncapitalize)
                .ifPresent(key -> methodBasedEvaluationContext.setVariable(key, result));
        return doParse(originParsableTarget, methodBasedEvaluationContext);
    }

    private String doParse(String originParsableTarget, MethodBasedEvaluationContext methodBasedEvaluationContext) {
        String bizNo = null;
        try {
            Expression expression = expressionParser.parseExpression(originParsableTarget);
            bizNo = expression.getValue(methodBasedEvaluationContext, String.class);
        } catch (ParseException | EvaluationException e) {
            logger.warning("An error happened while parsing biz-target or biz-no");
        }
        return bizNo;
    }
}
