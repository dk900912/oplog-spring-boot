package io.github.dk900912.oplog.autoconfigure;

import org.springframework.context.annotation.Configuration;

/**
 * @author dukui
 */
@Configuration(proxyBeanMethods = false)
public class OperationLogAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @EnableOperationLog
    public static class EnableOperationLogConfiguration {

    }
}
