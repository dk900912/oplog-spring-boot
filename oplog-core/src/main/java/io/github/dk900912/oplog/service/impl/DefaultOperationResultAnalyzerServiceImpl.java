package io.github.dk900912.oplog.service.impl;

import io.github.dk900912.oplog.service.OperationResultAnalyzerService;

import java.util.Objects;

/**
 * @author dukui
 */
public class DefaultOperationResultAnalyzerServiceImpl implements OperationResultAnalyzerService {

    @Override
    public boolean analyzeOperationResult(Throwable throwable, Object result) {
        return Objects.isNull(throwable);
    }
}
