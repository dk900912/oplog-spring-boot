package io.github.dk900912.oplog.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author dukui
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(OpLogProperties.class)
public class OperationLogAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @EnableOperationLog
    @ConditionalOnProperty(prefix = "oplog", name = "enabled", havingValue = "true",
            matchIfMissing = true)
    public static class EnableOperationLogConfiguration {

    }
}
