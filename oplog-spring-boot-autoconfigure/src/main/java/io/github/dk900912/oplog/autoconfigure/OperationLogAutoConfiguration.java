package io.github.dk900912.oplog.autoconfigure;

import io.github.dk900912.oplog.support.OperationLogTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import static io.github.dk900912.oplog.autoconfigure.OpLogProperties.OPLOG_PREFIX;

/**
 * @author dukui
 */
@AutoConfiguration(after = {WebMvcAutoConfiguration.class})
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = OPLOG_PREFIX, name = "enabled", havingValue = "true",
        matchIfMissing = true)
@ConditionalOnClass(OperationLogTemplate.class)
@EnableConfigurationProperties(OpLogProperties.class)
public class OperationLogAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @EnableOperationLog
    public static class EnableOperationLogConfiguration {

    }
}
