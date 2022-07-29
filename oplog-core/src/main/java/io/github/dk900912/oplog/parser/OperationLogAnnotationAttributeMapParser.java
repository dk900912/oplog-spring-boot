package io.github.dk900912.oplog.parser;

import io.github.dk900912.oplog.annotation.OperationLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Parsing {@link OperationLog} annotation's attributes to map.
 *
 * @author dukui
 */
public class OperationLogAnnotationAttributeMapParser implements Parser<Method> {

    private static final Logger logger = LoggerFactory.getLogger(OperationLogAnnotationAttributeMapParser.class);

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
        } catch (Exception exception) {
            logger.warn("An error happened while parsing operation log annotation info.");
        }
        return new HashMap<>(0);
    }
}
