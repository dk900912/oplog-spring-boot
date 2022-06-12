package io.github.dk900912.oplog.autoconfigure;

import io.github.dk900912.oplog.advisor.OperationLogPointcutAdvisor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author dukui
 */
@ConfigurationProperties(prefix = OpLogProperties.OPLOG_PREFIX)
public class OpLogProperties {
    public static final String OPLOG_PREFIX = "oplog";
    /**
     * 是否启用操作日志组件，默认值为 true
     */
    private Boolean enabled = true;

    /**
     * {@link OperationLogPointcutAdvisor} 优先级
     */
    @NestedConfigurationProperty
    private Advisor advisor;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Advisor getAdvisor() {
        return advisor;
    }

    public void setAdvisor(Advisor advisor) {
        this.advisor = advisor;
    }

    public static class Advisor {
        /**
         * Indicate the order of operation-log advisor
         */
        private Integer order;

        public Integer getOrder() {
            return order;
        }

        public void setOrder(Integer order) {
            this.order = order;
        }
    }
}
