package io.github.dk900912.oplog.advisor.pointcut;

import io.github.dk900912.oplog.annotation.OperationLog;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author dukui
 */
public class OperationLogPointcut extends StaticMethodMatcherPointcut {
    /**
     * @param method       目标方法
     * @param targetClass  目标类
     * @return             该目标方法是否由 @OperationLog 注解标记，若是，则返回 true
     */
    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        Annotation operationLogAnnotation = AnnotationUtils.findAnnotation(method, OperationLog.class);
        return Objects.nonNull(operationLogAnnotation);
    }
}
