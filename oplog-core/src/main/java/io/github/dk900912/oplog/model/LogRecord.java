package io.github.dk900912.oplog.model;

import java.time.LocalDateTime;
import java.util.StringJoiner;

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
     * 被操作对象中文标识，比如：订单、用户、商品等
     */
    private String operationTarget;

    /**
     * 业务操作类型，比如：订购、退订等
     */
    private BizCategory operationCategory;

    /**
     * 被操作对象的业务唯一标识，一般是业务对象的唯一标识，比如：订单ID、用户ID、商品ID等
     */
    private String bizNo;

    /**
     * 操作日志结果：true，即成功；false，即失败
     */
    private boolean operationResult;

    /**
     * 请求路径
     *
     * <li> {@link org.springframework.web.bind.annotation.GetMapping#path()}
     * <li> {@link org.springframework.web.bind.annotation.PostMapping#path()}
     * <li> {@link org.springframework.web.bind.annotation.PutMapping#path()}
     * <li> {@link org.springframework.web.bind.annotation.DeleteMapping#path()}
     */
    private String requestMapping;

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

    public BizCategory getOperationCategory() {
        return operationCategory;
    }

    public void setOperationCategory(BizCategory operationCategory) {
        this.operationCategory = operationCategory;
    }

    public String getBizNo() {
        return bizNo;
    }

    public void setBizNo(String bizNo) {
        this.bizNo = bizNo;
    }

    public boolean isOperationResult() {
        return operationResult;
    }

    public void setOperationResult(boolean operationResult) {
        this.operationResult = operationResult;
    }

    public String getRequestMapping() {
        return requestMapping;
    }

    public void setRequestMapping(String requestMapping) {
        this.requestMapping = requestMapping;
    }

    public LocalDateTime getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(LocalDateTime operationTime) {
        this.operationTime = operationTime;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "[", "]")
                .add("operatorId='" + operatorId + "'")
                .add("operatorName='" + operatorName + "'")
                .add("operationTarget='" + operationTarget + "'")
                .add("requestMapping='" + requestMapping + "'")
                .add("operationCategory=" + operationCategory)
                .add("bizNo='" + bizNo + "'")
                .add("operationResult=" + operationResult)
                .toString();
    }

    public static LogRecordBuilder builder() {
        return new LogRecordBuilder();
    }

    public static final class LogRecordBuilder {

        private String operationLogId;

        private String operatorId;

        private String operatorName;

        private String operationTarget;

        private BizCategory operationCategory;

        private String bizNo;

        private boolean operationResult;

        private String requestMapping;

        private LocalDateTime operationTime;

        private LogRecordBuilder() {
        }

        public LogRecordBuilder withOperationLogId(String operationLogId) {
            this.operationLogId = operationLogId;
            return this;
        }

        public LogRecordBuilder withOperatorId(String operatorId) {
            this.operatorId = operatorId;
            return this;
        }

        public LogRecordBuilder withOperatorName(String operatorName) {
            this.operatorName = operatorName;
            return this;
        }

        public LogRecordBuilder withOperationTarget(String operationTarget) {
            this.operationTarget = operationTarget;
            return this;
        }

        public LogRecordBuilder withOperationCategory(BizCategory operationCategory) {
            this.operationCategory = operationCategory;
            return this;
        }

        public LogRecordBuilder withBizNo(String bizNo) {
            this.bizNo = bizNo;
            return this;
        }

        public LogRecordBuilder withOperationResult(boolean operationResult) {
            this.operationResult = operationResult;
            return this;
        }

        public LogRecordBuilder withRequestMapping(String requestMapping) {
            this.requestMapping = requestMapping;
            return this;
        }

        public LogRecordBuilder withOperationTime(LocalDateTime operationTime) {
            this.operationTime = operationTime;
            return this;
        }

        public LogRecord build() {
            LogRecord logRecord = new LogRecord();
            logRecord.setOperationLogId(operationLogId);
            logRecord.setOperatorId(operatorId);
            logRecord.setOperatorName(operatorName);
            logRecord.setOperationCategory(operationCategory);
            logRecord.setBizNo(bizNo);
            logRecord.setOperationTarget(operationTarget);
            logRecord.setRequestMapping(requestMapping);
            logRecord.setOperationResult(operationResult);
            logRecord.setOperationTime(operationTime);
            return logRecord;
        }
    }
}
