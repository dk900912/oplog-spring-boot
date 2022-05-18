package io.github.dk900912.oplog.service.impl;

import io.github.dk900912.oplog.service.OperatorService;
import io.github.dk900912.oplog.model.Operator;

/**
 * @author dukui
 */
public class DefaultOperatorServiceImpl implements OperatorService {
    /**
     * @return Operator
     */
    @Override
    public Operator getOperator() {
        throw new RuntimeException("请自行实现OperatorService接口，否则无法获取Operator信息");
    }
}
