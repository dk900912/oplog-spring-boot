package io.github.dk900912.oplog.parser;

import io.github.dk900912.oplog.model.BizNoParseInfo;
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

/**
 * <h3> BizNo 解析器 </h3>
 *
 * @author dukui
 */
public class BizNoParser implements Parser<BizNoParseInfo> {
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
    public String parse(BizNoParseInfo target) {
        final Object result = target.getResult();
        final String originBizNo = target.getOriginBizNo();
        final MethodInvocation methodInvocation = target.getMethodInvocation();
        final Method method = methodInvocation.getMethod();
        final Object[] arguments = methodInvocation.getArguments();
        if (StringUtils.isEmpty(originBizNo) || !originBizNo.startsWith("#")) {
            return originBizNo;
        }

        MethodBasedEvaluationContext methodBasedEvaluationContext = new MethodBasedEvaluationContext(
                null, method, arguments, parameterNameDiscoverer);
        Optional.<Object>ofNullable(result)
                .map(Object::getClass)
                .map(Class::getSimpleName)
                .map(StringUtils::uncapitalize)
                .ifPresent(key -> methodBasedEvaluationContext.setVariable(key, result));
        return doParse(originBizNo, methodBasedEvaluationContext);
    }

    private String doParse(String originBizNo, MethodBasedEvaluationContext methodBasedEvaluationContext) {
        String bizNo = null;
        try {
            Expression expression = expressionParser.parseExpression(originBizNo);
            bizNo = expression.getValue(methodBasedEvaluationContext, String.class);
        } catch (ParseException | EvaluationException e) {
            // Nothing to do
        }
        return bizNo;
    }
}
