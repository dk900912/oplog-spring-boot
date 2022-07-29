package io.github.dk900912.oplog.autoconfigure;

import io.github.dk900912.oplog.advisor.OperationLogPointcutAdvisor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author dukui
 */
@ConfigurationProperties(prefix = OpLogProperties.OPLOG_PREFIX)
public class OpLogProperties {

    public static final String OPLOG_PREFIX = "spring.oplog";

    /**
     * Whether oplog component enabled, default true.
     */
    private Boolean enabled = true;

    /**
     * {@link OperationLogPointcutAdvisor} priority
     */
    @NestedConfigurationProperty
    private Advisor advisor;

    /**
     * multi-tenant
     */
    private String tenant;

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

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
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
