package io.github.dk900912.oplog.parser;

import io.github.dk900912.oplog.model.ParsableBizInfo;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;
import java.util.Optional;

import static io.github.dk900912.oplog.constant.Constants.SPRING_EL_PREFIX;

/**
 * @author dukui
 */
public class BizAttributeBasedSpExprParser implements Parser<ParsableBizInfo> {

    private static final Logger logger = LoggerFactory.getLogger(BizAttributeBasedSpExprParser.class);

    /**
     * thread-safe
     */
    private static final StandardReflectionParameterNameDiscoverer parameterNameDiscoverer =
            new StandardReflectionParameterNameDiscoverer();

    /**
     * thread-safe
     */
    private static final ExpressionParser expressionParser = new SpelExpressionParser();

    @Override
    public Object parse(ParsableBizInfo parsableBizInfo) {
        final Object result = parsableBizInfo.getResult();
        final String originParsableTarget = (String) parsableBizInfo.getOriginParsableTarget();
        final MethodInvocation methodInvocation = parsableBizInfo.getMethodInvocation();
        final Method method = methodInvocation.getMethod();
        final Object[] arguments = methodInvocation.getArguments();
        if (!shouldParse(originParsableTarget)) {
            return originParsableTarget;
        }

        MethodBasedEvaluationContext methodBasedEvaluationContext =
                new MethodBasedEvaluationContext(null, method, arguments, parameterNameDiscoverer);
        Optional.<Object>ofNullable(result)
                .map(Object::getClass)
                .map(Class::getSimpleName)
                .map(StringUtils::uncapitalize)
                .ifPresent(key -> methodBasedEvaluationContext.setVariable(key, result));
        return doParse(originParsableTarget, methodBasedEvaluationContext);
    }

    private String doParse(String originParsableTarget, MethodBasedEvaluationContext methodBasedEvaluationContext) {
        String attribute = null;
        try {
            Expression expression = expressionParser.parseExpression(originParsableTarget);
            attribute = expression.getValue(methodBasedEvaluationContext, String.class);
        } catch (ParseException | EvaluationException e) {
            logger.warn("An error happened while parsing biz-target or biz-no.");
        }
        return attribute;
    }

    private boolean shouldParse(String originParsableTarget) {
        return StringUtils.isNotEmpty(originParsableTarget) && originParsableTarget.contains(SPRING_EL_PREFIX);
    }
}
