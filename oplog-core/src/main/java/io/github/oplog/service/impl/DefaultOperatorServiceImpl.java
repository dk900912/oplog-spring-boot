package io.github.oplog.service.impl;

import io.github.oplog.Operator;
import io.github.oplog.service.OperatorService;

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
