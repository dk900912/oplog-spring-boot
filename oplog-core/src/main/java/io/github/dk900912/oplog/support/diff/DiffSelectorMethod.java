package io.github.dk900912.oplog.support.diff;

import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.StringJoiner;

/**
 * In the data update scenario, the method marked by the @DiffSelector annotation
 * will be called twice, the first is used to obtain the view before the update,
 * and the second is used to obtain the updated view.
 * <p>
 * In the application startup phase, each DiffSelector method will be encapsulated
 * as a DiffSelectorMethod instance.
 *
 * @author dukui
 */
public class DiffSelectorMethod {

    private final Object bean;

    private final Class<?> beanType;

    private final Method method;

    private final MethodParameter methodParameter;

    private final String description;

    /**
     * Create an instance from a bean instance and a method.
     */
    public DiffSelectorMethod(Object bean, Method method) {
        Assert.notNull(bean, "Bean is required");
        Assert.notNull(method, "Method is required");
        this.bean = bean;
        this.beanType = ClassUtils.getUserClass(bean);
        this.method = method;
        ReflectionUtils.makeAccessible(this.method);
        this.methodParameter = new MethodParameter(method, 0);
        this.description = initDescription(this.beanType, this.method);
    }

    private static String initDescription(Class<?> beanType, Method method) {
        StringJoiner joiner = new StringJoiner(", ", "(", ")");
        for (Class<?> paramType : method.getParameterTypes()) {
            joiner.add(paramType.getSimpleName());
        }
        return beanType.getName() + "#" + method.getName() + joiner.toString();
    }

    public Object getBean() {
        return this.bean;
    }

    public Method getMethod() {
        return this.method;
    }

    /**
     * This method returns the type of the handler for this diff selector method.
     * <p>Note that if the bean type is a CGLIB-generated class, the original
     * user-defined class is returned.
     */
    public Class<?> getBeanType() {
        return this.beanType;
    }

    public boolean isVoid() {
        return Void.TYPE.equals(this.method.getReturnType());
    }

    /**
     * Return the method parameters for this diff selector method.
     */
    public MethodParameter getMethodParameter() {
        return this.methodParameter;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Return whether the parameter is declared with the given annotation type.
     */
    public <A extends Annotation> boolean hasMethodAnnotation(Class<A> annotationType) {
        return AnnotatedElementUtils.hasAnnotation(this.method, annotationType);
    }
}
