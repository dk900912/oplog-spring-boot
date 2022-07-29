package io.github.dk900912.oplog.service;

/**
 * How to judge execution result of target method, exception or
 * return value's content of target method ?
 *
 * @author dukui
 */
public interface OperationResultAnalyzerService {

    /**
     * @param throwable exception threw by target method
     * @param result the execution result of target method
     * @return true - success, false - failure
     */
    boolean analyzeOperationResult(Throwable throwable, Object result);
}
