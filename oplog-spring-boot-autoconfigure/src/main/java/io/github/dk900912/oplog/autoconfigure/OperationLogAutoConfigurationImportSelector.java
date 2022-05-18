package io.github.dk900912.oplog.autoconfigure;

import io.github.dk900912.oplog.advisor.OperationLogPointcutAdvisor;
import io.github.dk900912.oplog.advisor.advice.OperationLogInterceptor;
import io.github.dk900912.oplog.advisor.pointcut.OperationLogPointcut;
import io.github.dk900912.oplog.service.impl.DefaultLogRecordPersistenceServiceImpl;
import io.github.dk900912.oplog.service.LogRecordPersistenceService;
import io.github.dk900912.oplog.service.OperatorService;
import io.github.dk900912.oplog.service.impl.DefaultOperatorServiceImpl;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Objects;

/**
 * @author dukui
 */
@Configuration(proxyBeanMethods = true)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class OperationLogAutoConfigurationImportSelector implements ImportAware {

    protected AnnotationAttributes enableOperationLog;

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.enableOperationLog = AnnotationAttributes.fromMap(
                importMetadata.getAnnotationAttributes(EnableOperationLog.class.getName(), false));
        if (this.enableOperationLog == null) {
            throw new IllegalArgumentException(
                    "@EnableOperationLog is not present on importing class " + importMetadata.getClassName());
        }
    }

    @Bean
    @ConditionalOnMissingBean(OperatorService.class)
    public OperatorService operatorService() {
        return new DefaultOperatorServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(LogRecordPersistenceService.class)
    public LogRecordPersistenceService logRecordPersistenceService() {
        return new DefaultLogRecordPersistenceServiceImpl();
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public OperationLogPointcutAdvisor operationLogPointcutAdvisor() {
        OperationLogPointcutAdvisor operationLogPointcutAdvisor = new OperationLogPointcutAdvisor();

        OperationLogPointcut operationLogPointcut = new OperationLogPointcut();
        operationLogPointcutAdvisor.setPointcut(operationLogPointcut);

        OperationLogInterceptor operationLogInterceptor = new OperationLogInterceptor();
        OperatorService operatorService = operatorService();
        LogRecordPersistenceService logRecordPersistenceService = logRecordPersistenceService();
        operationLogInterceptor.setOperatorService(operatorService);
        operationLogInterceptor.setLogRecordPersistenceService(logRecordPersistenceService);
        operationLogPointcutAdvisor.setAdvice(operationLogInterceptor);

        if (Objects.nonNull(this.enableOperationLog)) {
            operationLogPointcutAdvisor.setOrder(this.enableOperationLog.<Integer>getNumber("order"));
        }

        return operationLogPointcutAdvisor;
    }
}
