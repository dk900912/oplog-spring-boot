package io.github.dk900912.oplog.service.impl;

import io.github.dk900912.oplog.service.OperatorService;
import io.github.dk900912.oplog.Operator;

/**
 * @author dukui
 */
public class DefaultOperatorServiceImpl implements OperatorService {
    /**
     * @return
     */
    @Override
    public Operator getOperator() {
        throw new RuntimeException("请自行实现OperatorService接口，否则无法获取Operator信息");
    }
}
