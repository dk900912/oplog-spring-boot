package io.github.dk900912.oplog.service;

import io.github.dk900912.oplog.model.Operator;

/**
 * @author dukui
 */
public interface OperatorService {
    /**
     * 用于获取操作人信息，操作人信息一般会通过过滤器提前得到，然后将其保存在 {@link ThreadLocal} 中
     *
     * @return Operator
     */
    Operator getOperator();
}
