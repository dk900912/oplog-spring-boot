package io.github.dk900912.oplog.advisor.advice;

import io.github.dk900912.oplog.context.OperationLogContext;
import io.github.dk900912.oplog.support.MethodInvocationOperationLogCallback;
import io.github.dk900912.oplog.support.OperationLogCallback;
import io.github.dk900912.oplog.support.OperationLogOperations;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dukui
 */
public final class OperationLogInterceptor implements MethodInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(OperationLogInterceptor.class);

    private final OperationLogOperations operationLogOperations;

    public OperationLogInterceptor(OperationLogOperations operationLogOperations) {
        this.operationLogOperations = operationLogOperations;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        OperationLogCallback<Object, Throwable> operationLogCallback =
                new MethodInvocationOperationLogCallback<Object, Throwable>(invocation) {
                    @Override
                    public Object doWithOperationLog(OperationLogContext context) throws Throwable {
                        if (logger.isDebugEnabled()) {
                            logger.debug("0={======> {} <======}=0", context);
                        }
                        return invocation.proceed();
                    }
                };

        return this.operationLogOperations.execute(operationLogCallback);
    }
}
