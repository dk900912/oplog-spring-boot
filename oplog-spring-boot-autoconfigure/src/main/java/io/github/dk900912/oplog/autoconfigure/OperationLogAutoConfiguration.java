package io.github.dk900912.oplog.autoconfigure;

import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot 自动配置类，大家无需在 Spring Boot 启动类中追加 {@link EnableOperationLog} 注解了。
 *
 * @author dukui
 */
@Configuration(proxyBeanMethods = false)
public class OperationLogAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @EnableOperationLog
    public static class EnableOperationLogConfiguration {

    }
}
