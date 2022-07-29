package io.github.dk900912.oplog.autoconfigure;

import io.github.dk900912.oplog.advisor.OperationLogPointcutAdvisor;
import io.github.dk900912.oplog.advisor.advice.OperationLogInterceptor;
import io.github.dk900912.oplog.advisor.pointcut.OperationLogPointcut;
import io.github.dk900912.oplog.service.LogRecordPersistenceService;
import io.github.dk900912.oplog.service.OperationResultAnalyzerService;
import io.github.dk900912.oplog.service.OperatorService;
import io.github.dk900912.oplog.service.Selector;
import io.github.dk900912.oplog.service.SelectorFactory;
import io.github.dk900912.oplog.service.impl.DefaultLogRecordPersistenceServiceImpl;
import io.github.dk900912.oplog.service.impl.DefaultOperationResultAnalyzerServiceImpl;
import io.github.dk900912.oplog.service.impl.DefaultOperatorServiceImpl;
import io.github.dk900912.oplog.support.OperationLogTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.context.annotation.Role;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author dukui
 */
@Configuration(proxyBeanMethods = true)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class OperationLogAutoConfigurationImportSelector implements ImportAware {

    protected AnnotationAttributes enableOperationLog;

    private final OpLogProperties opLogProperties;

    private final List<Selector> selectors;

    @Autowired
    public OperationLogAutoConfigurationImportSelector(OpLogProperties opLogProperties,
                                                       List<Selector> selectors) {
        this.opLogProperties = opLogProperties;
        this.selectors = selectors;
    }

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
    @ConditionalOnMissingBean(OperationResultAnalyzerService.class)
    public OperationResultAnalyzerService operationResultAnalyzerService() {
        return new DefaultOperationResultAnalyzerServiceImpl();
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
    public SelectorFactory selectorFactory() {
        return new SelectorFactory(this.selectors);
    }

    @Bean
    public OperationLogTemplate operationLogTemplate() {
        OperationLogTemplate operationLogTemplate = new OperationLogTemplate(opLogProperties.getTenant());
        OperatorService operatorService = operatorService();
        LogRecordPersistenceService logRecordPersistenceService = logRecordPersistenceService();
        OperationResultAnalyzerService operationResultAnalyzerService = operationResultAnalyzerService();
        SelectorFactory selectorFactory = selectorFactory();
        operationLogTemplate.setOperatorService(operatorService);
        operationLogTemplate.setLogRecordPersistenceService(logRecordPersistenceService);
        operationLogTemplate.setOperationResultAnalyzerService(operationResultAnalyzerService);
        operationLogTemplate.setSelectorFactory(selectorFactory);
        return operationLogTemplate;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public OperationLogPointcutAdvisor operationLogPointcutAdvisor() {

        OperationLogPointcutAdvisor operationLogPointcutAdvisor = new OperationLogPointcutAdvisor();

        OperationLogPointcut operationLogPointcut = new OperationLogPointcut();
        operationLogPointcutAdvisor.setPointcut(operationLogPointcut);
        operationLogPointcutAdvisor.setAdvice(new OperationLogInterceptor(operationLogTemplate()));

        Integer logPointcutAdvisorOrderFromAnnotation = Optional.<AnnotationAttributes>ofNullable(this.enableOperationLog)
                .map(annotationAttributes -> annotationAttributes.<Integer>getNumber("order"))
                .orElse(Ordered.LOWEST_PRECEDENCE);
        Integer logPointcutAdvisorOrderFromProperty = Optional.<OpLogProperties>ofNullable(opLogProperties)
                .map(OpLogProperties::getAdvisor)
                .map(OpLogProperties.Advisor::getOrder)
                .orElse(null);
        if (Objects.nonNull(logPointcutAdvisorOrderFromProperty)) {
            operationLogPointcutAdvisor.setOrder(logPointcutAdvisorOrderFromProperty);
        } else {
            operationLogPointcutAdvisor.setOrder(logPointcutAdvisorOrderFromAnnotation);
        }

        return operationLogPointcutAdvisor;
    }
}
