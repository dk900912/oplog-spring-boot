package io.github.dk900912.oplog.parser;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static io.github.dk900912.oplog.constant.Constants.REQUEST_MAPPING_DELIMITER;

/**
 * Parsing {@link RequestMapping} annotation's path attribute.
 *
 * @author dukui
 */
public class RequestMappingParser implements Parser<Method> {

    private static final Logger logger = LoggerFactory.getLogger(RequestMappingParser.class);

    private static final ConcurrentHashMap<Method, String> cache =
            new ConcurrentHashMap<>(200);

    @Override
    public Object parse(Method target) {
        String requestMapping = cache.get(target);
        if (StringUtils.isNotEmpty(requestMapping)) {
            return requestMapping;
        }
        return doParse(target);
    }

    private String doParse(Method method) {
        try {
            List<String> list = new ArrayList<>(8);

            Class<?> userType = ClassUtils.getUserClass(method.getDeclaringClass());
            RequestMapping requestMappingOnClass = AnnotatedElementUtils.findMergedAnnotation(userType, RequestMapping.class);
            if (Objects.nonNull(requestMappingOnClass)) {
                String[] pathArrayOnClass = requestMappingOnClass.path();
                if (ArrayUtils.isNotEmpty(pathArrayOnClass)) {
                    list.addAll(Arrays.asList(StringUtils.split(pathArrayOnClass[0], REQUEST_MAPPING_DELIMITER)));
                }
            }

            RequestMapping requestMappingOnMethod = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
            if (Objects.nonNull(requestMappingOnMethod)) {
                String[] pathArrayOnMethod = requestMappingOnMethod.path();
                if (ArrayUtils.isNotEmpty(pathArrayOnMethod)) {
                    list.addAll(Arrays.asList(StringUtils.split(pathArrayOnMethod[0], REQUEST_MAPPING_DELIMITER)));
                }
            }

            if (!CollectionUtils.isEmpty(list)) {
                String requestMapping = list.stream()
                        .filter(StringUtils::isNotEmpty)
                        .collect(Collectors.joining(REQUEST_MAPPING_DELIMITER, REQUEST_MAPPING_DELIMITER, ""));
                cache.put(method, requestMapping);
                return requestMapping;
            }
        } catch (Exception exception) {
            logger.warn("An error happened while parsing request mapping info.");
        }
        return null;
    }
}
