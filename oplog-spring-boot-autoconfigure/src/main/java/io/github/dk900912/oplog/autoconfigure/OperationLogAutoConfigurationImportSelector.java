package io.github.dk900912.oplog.autoconfigure;

import io.github.dk900912.oplog.advisor.OperationLogPointcutAdvisor;
import io.github.dk900912.oplog.advisor.advice.OperationLogInterceptor;
import io.github.dk900912.oplog.advisor.pointcut.OperationLogPointcut;
import io.github.dk900912.oplog.service.LogRecordPersistenceService;
import io.github.dk900912.oplog.service.OperationResultAnalyzerService;
import io.github.dk900912.oplog.service.OperatorService;
import io.github.dk900912.oplog.service.impl.DefaultLogRecordPersistenceServiceImpl;
import io.github.dk900912.oplog.service.impl.DefaultOperationResultAnalyzerServiceImpl;
import io.github.dk900912.oplog.service.impl.DefaultOperatorServiceImpl;
import io.github.dk900912.oplog.support.OperationLogTemplate;
import io.github.dk900912.oplog.support.diff.DiffSelectorSupport;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Objects;

import static io.github.dk900912.oplog.constant.Constants.ORDER;
import static io.github.dk900912.oplog.constant.Constants.TENANT;

/**
 * @author dukui
 */
@Configuration(proxyBeanMethods = false)
public class OperationLogAutoConfigurationImportSelector implements ImportAware {

    private AnnotationAttributes enableOperationLog;

    /*
        When the automatic configuration feature is disabled through 'spring.oplog.enabled=false',
        the operation log component is enabled through the @EnableOperationLog annotation,
        if OpLogProperties is not wrapped by ObjectProvider, an exception will be thrown during
        the startup phase: Parameter 0 of constructor in OperationLogAutoConfigurationImportSelector
        required a bean of type 'OpLogProperties' that could not be found.
     */
    private final ObjectProvider<OpLogProperties> opLogProperties;

    @Autowired
    public OperationLogAutoConfigurationImportSelector(ObjectProvider<OpLogProperties> opLogProperties) {
        this.opLogProperties = opLogProperties;
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
    public OperationLogTemplate operationLogTemplate(@Qualifier("operationLogConversionService") ConversionService operationLogConversionService,
                                                     OperatorService operatorService,
                                                     LogRecordPersistenceService logRecordPersistenceService,
                                                     OperationResultAnalyzerService operationResultAnalyzerService) {
        String tenant = selectTenant();
        OperationLogTemplate operationLogTemplate = new OperationLogTemplate(tenant, operationLogConversionService);
        operationLogTemplate.setOperatorService(operatorService);
        operationLogTemplate.setLogRecordPersistenceService(logRecordPersistenceService);
        operationLogTemplate.setOperationResultAnalyzerService(operationResultAnalyzerService);
        return operationLogTemplate;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public OperationLogPointcutAdvisor operationLogPointcutAdvisor(OperationLogTemplate operationLogTemplate) {
        OperationLogPointcutAdvisor operationLogPointcutAdvisor = new OperationLogPointcutAdvisor();
        OperationLogPointcut operationLogPointcut = new OperationLogPointcut();
        operationLogPointcutAdvisor.setPointcut(operationLogPointcut);
        operationLogPointcutAdvisor.setAdvice(new OperationLogInterceptor(operationLogTemplate));
        operationLogPointcutAdvisor.setOrder(selectOrder());
        return operationLogPointcutAdvisor;
    }

    @Bean
    public ConversionService operationLogConversionService() {
        return new DefaultConversionService();
    }

    @Bean
    public DiffSelectorSupport diffSelectorSupport() {
        return new DiffSelectorSupport();
    }

    @SuppressWarnings("null")
    private String selectTenant() {
        if (Objects.isNull(this.opLogProperties.getIfAvailable())) {
            return this.enableOperationLog.getString(TENANT);
        }
        return this.opLogProperties.getIfAvailable().getTenant();
    }

    @SuppressWarnings("null")
    private Integer selectOrder() {
        if (Objects.isNull(this.opLogProperties.getIfAvailable())) {
            return this.enableOperationLog.getNumber(ORDER);
        }
        return this.opLogProperties.getIfAvailable().getAdvisor().getOrder();
    }
}
