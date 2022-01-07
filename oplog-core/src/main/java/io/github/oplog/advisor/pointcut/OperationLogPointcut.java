package io.github.oplog.advisor.pointcut;

import io.github.oplog.annotation.OperationLog;
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
     * @param method
     * @param targetClass
     * @return
     */
    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        Annotation operationLogAnnotation = AnnotationUtils.findAnnotation(method, OperationLog.class);
        return Objects.nonNull(operationLogAnnotation);
    }
}
