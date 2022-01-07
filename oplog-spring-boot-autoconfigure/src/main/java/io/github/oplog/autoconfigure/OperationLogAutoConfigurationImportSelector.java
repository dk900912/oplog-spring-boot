package io.github.oplog.autoconfigure;

import io.github.oplog.advisor.OperationLogPointcutAdvisor;
import io.github.oplog.advisor.advice.OperationLogAdvice;
import io.github.oplog.advisor.pointcut.OperationLogPointcut;
import io.github.oplog.persistence.DefaultLogRecordPersistenceServiceImpl;
import io.github.oplog.persistence.LogRecordPersistenceService;
import io.github.oplog.service.OperatorService;
import io.github.oplog.service.impl.DefaultOperatorServiceImpl;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * @author dukui
 */
@Configuration
public class OperationLogAutoConfigurationImportSelector {

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
    public OperationLogPointcutAdvisor operationLogPointcutAdvisor(OperatorService operatorService,
                                                                   LogRecordPersistenceService logRecordPersistenceService) {
        OperationLogPointcutAdvisor operationLogPointcutAdvisor = new OperationLogPointcutAdvisor();
        OperationLogPointcut operationLogPointcut = new OperationLogPointcut();
        operationLogPointcutAdvisor.setPointcut(operationLogPointcut);
        OperationLogAdvice operationLogAdvice = new OperationLogAdvice();
        operationLogAdvice.setOperatorService(operatorService);
        operationLogAdvice.setLogRecordPersistenceService(logRecordPersistenceService);
        operationLogPointcutAdvisor.setAdvice(operationLogAdvice);
        return operationLogPointcutAdvisor;
    }
}
