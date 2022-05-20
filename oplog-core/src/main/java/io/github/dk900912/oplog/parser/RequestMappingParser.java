package io.github.dk900912.oplog.parser;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
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

/**
 * <h3> RequestMapping 解析器 </h3>
 *
 * @author dukui
 */
public class RequestMappingParser implements Parser<Method> {

    private static final ConcurrentHashMap<Method, String> LOCAL_CACHE =
            new ConcurrentHashMap<>(200);

    @Override
    public String parse(Method target) {
        String requestMapping = LOCAL_CACHE.get(target);
        if (StringUtils.isNotEmpty(requestMapping)) {
            return requestMapping;
        }
        return doParse(target);
    }

    private String doParse(Method method) {
        try {
            RequestMapping requestMappingOnMethod = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
            Class<?> userType = ClassUtils.getUserClass(method.getDeclaringClass());
            RequestMapping requestMappingOnClass = AnnotatedElementUtils.findMergedAnnotation(userType, RequestMapping.class);
            List<String> list = new ArrayList<>(8);
            if (Objects.nonNull(requestMappingOnClass)) {
                String[] pathArrayOnClass = requestMappingOnClass.path();
                if (ArrayUtils.isNotEmpty(pathArrayOnClass)) {
                    list.addAll(Arrays.asList(StringUtils.split(pathArrayOnClass[0], "/")));
                }
            }
            if (Objects.nonNull(requestMappingOnMethod)) {
                String[] pathArrayOnMethod = requestMappingOnMethod.path();
                if (ArrayUtils.isNotEmpty(pathArrayOnMethod)) {
                    list.addAll(Arrays.asList(StringUtils.split(pathArrayOnMethod[0], "/")));
                }
            }
            if (!CollectionUtils.isEmpty(list)) {
                String requestMapping = list.stream()
                        .filter(StringUtils::isNotEmpty)
                        .collect(Collectors.joining("/", "/", ""));
                LOCAL_CACHE.put(method, requestMapping);
                return requestMapping;
            }
        } catch (Throwable throwable) {
            // Nothing to do
        }
        return null;
    }
}
