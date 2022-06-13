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
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * <h3> RequestMapping 解析器 </h3>
 *
 * @author dukui
 */
public class RequestMappingParser implements Parser<Method> {

    private static final Logger logger = Logger.getLogger(RequestMappingParser.class.getName());

    private static final ConcurrentHashMap<Method, String> cache =
            new ConcurrentHashMap<>(200);

    @Override
    public String parse(Method target) {
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
                    list.addAll(Arrays.asList(StringUtils.split(pathArrayOnClass[0], "/")));
                }
            }

            RequestMapping requestMappingOnMethod = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
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
                cache.put(method, requestMapping);
                return requestMapping;
            }
        } catch (Throwable throwable) {
            logger.warning("An error happened while parsing request mapping info");
        }
        return null;
    }
}
