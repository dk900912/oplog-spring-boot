package io.github.dk900912.oplog.parser;

import io.github.dk900912.oplog.annotation.OperationLog;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * <h3> OperationLog 注解属性解析器 </h3>
 *
 * @author dukui
 */
public class OperationLogAnnotationAttributeMapParser implements Parser<Method> {

    private static final Logger logger = Logger.getLogger(OperationLogAnnotationAttributeMapParser.class.getName());

    private static final ConcurrentHashMap<Method, Map<String, Object>> cache = new ConcurrentHashMap<>(200);

    @Override
    public Object parse(Method target) {
        Map<String, Object> annotationAttributes = cache.get(target);
        if (annotationAttributes != null
                && !annotationAttributes.isEmpty()) {
            return annotationAttributes;
        }
        return doParse(target);
    }

    private Map<String, Object> doParse(Method method) {
        try {
            Map<String, Object> annotationAttributes =
                    Optional.<Annotation>ofNullable(AnnotationUtils.findAnnotation(method, OperationLog.class))
                            .map(AnnotationUtils::getAnnotationAttributes)
                            .orElse(new HashMap<>(0));
            cache.put(method, annotationAttributes);
            return annotationAttributes;
        } catch (Exception throwable) {
            logger.warning("An error happened while parsing operation log annotation info");
        }
        return new HashMap<>(0);
    }
}
