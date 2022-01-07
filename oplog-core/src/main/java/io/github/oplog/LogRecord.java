package io.github.oplog;

import java.time.LocalDateTime;

/**
 * @author dukui
 */
public class LogRecord {
    /**
     * 操作日志唯一标识，一般用于数据库主键
     */
    private String operationLogId;

    /**
     * 操作用户ID
     */
    private String operatorId;

    /**
     * 操作用户名
     */
    private String operatorName;

    /**
     * 被操作对象唯一标识，一般是业务对象的唯一标识，比如：订单ID、用户ID、商品ID等
     */
    private String operationTarget;

    /**
     * 操作日志详情
     */
    private String operationContent;

    /**
     * 操作日志结果：true，即成功；false，即失败
     */
    private boolean operationResult;

    /**
     * 操作日志发生时间
     */
    private LocalDateTime operationTime;

    public String getOperationLogId() {
        return operationLogId;
    }

    public void setOperationLogId(String operationLogId) {
        this.operationLogId = operationLogId;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getOperationTarget() {
        return operationTarget;
    }

    public void setOperationTarget(String operationTarget) {
        this.operationTarget = operationTarget;
    }

    public String getOperationContent() {
        return operationContent;
    }

    public void setOperationContent(String operationContent) {
        this.operationContent = operationContent;
    }

    public boolean isOperationResult() {
        return operationResult;
    }

    public void setOperationResult(boolean operationResult) {
        this.operationResult = operationResult;
    }

    public LocalDateTime getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(LocalDateTime operationTime) {
        this.operationTime = operationTime;
    }

    @Override
    public String toString() {
        return "LogRecord{" +
                "operationLogId='" + operationLogId + '\'' +
                ", operatorId='" + operatorId + '\'' +
                ", operatorName='" + operatorName + '\'' +
                ", operationTarget='" + operationTarget + '\'' +
                ", operationContent='" + operationContent + '\'' +
                ", operationResult=" + operationResult +
                ", operationTime=" + operationTime +
                '}';
    }
}
