package io.github.dk900912.oplog.service.impl;

import io.github.dk900912.oplog.service.OperationResultAnalyzerService;

import java.util.Objects;

/**
 * 默认的目标方法执行结果分析器，只要目标方法没有抛出异常，那么就认为目标方法执行成功了。
 *
 * @author dukui
 */
public class DefaultOperationResultAnalyzerServiceImpl implements OperationResultAnalyzerService {

    @Override
    public boolean analyzeOperationResult(Throwable throwable, Object result) {
        return Objects.isNull(throwable);
    }
}
