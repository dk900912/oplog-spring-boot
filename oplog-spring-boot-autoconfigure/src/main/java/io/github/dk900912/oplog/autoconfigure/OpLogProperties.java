package io.github.dk900912.oplog.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.Ordered;

/**
 * @author dukui
 */
@ConfigurationProperties(prefix = OpLogProperties.OPLOG_PREFIX)
public class OpLogProperties {

    public static final String OPLOG_PREFIX = "spring.oplog";

    private Boolean enabled = true;

    @NestedConfigurationProperty
    private Advisor advisor = new Advisor();

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
        private Integer order = Ordered.LOWEST_PRECEDENCE;

        public Integer getOrder() {
            return order;
        }

        public void setOrder(Integer order) {
            this.order = order;
        }
    }
}
