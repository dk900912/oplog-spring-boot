package io.github.dk900912.oplog.support.diff;

import io.github.dk900912.oplog.annotation.DiffSelector;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * @author dukui
 */
public class DiffSelectorSupport extends ApplicationObjectSupport implements InitializingBean {

    private static final String DIFF_SELECTOR_SUPPORT_NAME = "diffSelectorSupport";

    @Override
    public void afterPropertiesSet() throws Exception {
        initHandlerMethods();
    }

    protected void initHandlerMethods() {
        for (String beanName : getCandidateBeanNames()) {
            if (!shouldSkip(beanName)) {
                processCandidateBean(beanName);
            }
        }
    }

    protected String[] getCandidateBeanNames() {
        return obtainApplicationContext().getBeanNamesForType(Object.class);
    }

    protected void processCandidateBean(String beanName) {
        Object bean = null;
        try {
            bean = obtainApplicationContext().getBean(beanName);
        } catch (Throwable ex) {
            // An unresolvable bean type, probably from a lazy bean - let's ignore it.
            logger.warn("Could not resolve type for bean '" + beanName + "'", ex);
        }
        if (bean != null) {
            detectDiffSelectorMethods(bean);
        }
    }

    protected boolean shouldSkip(String beanName) {
        return DIFF_SELECTOR_SUPPORT_NAME.equals(beanName);
    }

    protected void detectDiffSelectorMethods(Object bean) {
        Class<?> diffSelectorType = bean.getClass();
        Class<?> userType = ClassUtils.getUserClass(diffSelectorType);
        Set<Method> methods =
                MethodIntrospector.selectMethods(
                        userType,
                        (ReflectionUtils.MethodFilter) method -> AnnotatedElementUtils.hasAnnotation(method, DiffSelector.class)
                );
        methods.forEach(method -> {
            Method invocableMethod = AopUtils.selectInvocableMethod(method, userType);
            registerDiffSelectorMethod(bean, invocableMethod);
        });
    }

    protected void registerDiffSelectorMethod(Object bean, Method method) {
        DiffSelectorRegistry.getInstance().register(bean, method);
    }
}
