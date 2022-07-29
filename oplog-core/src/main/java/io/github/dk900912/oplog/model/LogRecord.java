package io.github.dk900912.oplog.model;

import java.time.LocalDateTime;
import java.util.StringJoiner;

/**
 * @author dukui
 */
public class LogRecord {

    private String operationLogId;

    private String tenant;

    private String operatorId;

    private String operatorName;

    /**
     * <pre>
     * public static String getRealClientIp(HttpServletRequest request) {
     *     String xForwardedFor = request.getHeader("X-Forwarded-For");
     *     if (!StringUtils.isBlank(xForwardedFor)) {
     *         return xForwardedFor.split(",")[0].trim();
     *     }
     *     String nginxHeader = request.getHeader("X-Real-IP");
     *     return StringUtils.isBlank(nginxHeader) ? request.getRemoteAddr() : nginxHeader;
     * }
     * </pre>
     */
    private String operatorIp;

    private String operationTarget;

    private BizCategory operationCategory;

    private String bizNo;

    private boolean operationResult;

    /**
     * <li> {@link org.springframework.web.bind.annotation.GetMapping#path()}
     * <li> {@link org.springframework.web.bind.annotation.PostMapping#path()}
     * <li> {@link org.springframework.web.bind.annotation.PutMapping#path()}
     * <li> {@link org.springframework.web.bind.annotation.DeleteMapping#path()}
     */
    private String requestMapping;

    private LocalDateTime operationTime;

    private Long targetExecutionTime;

    private Object previousContent;

    private Object currentContent;

    public String getOperationLogId() {
        return operationLogId;
    }

    public LogRecord setOperationLogId(String operationLogId) {
        this.operationLogId = operationLogId;
        return this;
    }

    public String getTenant() {
        return tenant;
    }

    public LogRecord setTenant(String tenant) {
        this.tenant = tenant;
        return this;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public LogRecord setOperatorId(String operatorId) {
        this.operatorId = operatorId;
        return this;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public LogRecord setOperatorName(String operatorName) {
        this.operatorName = operatorName;
        return this;
    }

    public String getOperatorIp() {
        return operatorIp;
    }

    public LogRecord setOperatorIp(String operatorIp) {
        this.operatorIp = operatorIp;
        return this;
    }

    public String getOperationTarget() {
        return operationTarget;
    }

    public LogRecord setOperationTarget(String operationTarget) {
        this.operationTarget = operationTarget;
        return this;
    }

    public BizCategory getOperationCategory() {
        return operationCategory;
    }

    public LogRecord setOperationCategory(BizCategory operationCategory) {
        this.operationCategory = operationCategory;
        return this;
    }

    public String getBizNo() {
        return bizNo;
    }

    public LogRecord setBizNo(String bizNo) {
        this.bizNo = bizNo;
        return this;
    }

    public boolean isOperationResult() {
        return operationResult;
    }

    public LogRecord setOperationResult(boolean operationResult) {
        this.operationResult = operationResult;
        return this;
    }

    public String getRequestMapping() {
        return requestMapping;
    }

    public LogRecord setRequestMapping(String requestMapping) {
        this.requestMapping = requestMapping;
        return this;
    }

    public LocalDateTime getOperationTime() {
        return operationTime;
    }

    public LogRecord setOperationTime(LocalDateTime operationTime) {
        this.operationTime = operationTime;
        return this;
    }

    public Long getTargetExecutionTime() {
        return targetExecutionTime;
    }

    public LogRecord setTargetExecutionTime(Long targetExecutionTime) {
        this.targetExecutionTime = targetExecutionTime;
        return this;
    }

    public Object getPreviousContent() {
        return previousContent;
    }

    public LogRecord setPreviousContent(Object previousContent) {
        this.previousContent = previousContent;
        return this;
    }

    public Object getCurrentContent() {
        return currentContent;
    }

    public LogRecord setCurrentContent(Object currentContent) {
        this.currentContent = currentContent;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "[", "]")
                .add("tenant='" + tenant + "'")
                .add("operatorId='" + operatorId + "'")
                .add("operatorName='" + operatorName + "'")
                .add("operatorIp='" + operatorIp + "'")
                .add("operationTarget='" + operationTarget + "'")
                .add("requestMapping='" + requestMapping + "'")
                .add("operationCategory='" + operationCategory + "'")
                .add("bizNo='" + bizNo + "'")
                .add("operationResult='" + operationResult + "'")
                .add("targetExecutionTime='" + targetExecutionTime + "'")
                .add("previousContent='" + previousContent + "'")
                .add("currentContent='" + currentContent + "'")
                .toString();
    }

    public static LogRecordBuilder builder() {
        return new LogRecordBuilder();
    }

    public static final class LogRecordBuilder {

        private String operationLogId;

        private String tenant;

        private String operatorId;

        private String operatorName;

        private String operatorIp;

        private String operationTarget;

        private BizCategory operationCategory;

        private String bizNo;

        private boolean operationResult;

        private String requestMapping;

        private LocalDateTime operationTime;

        private Long targetExecutionTime;

        private Object previousContent;

        private Object currentContent;

        private LogRecordBuilder() {
        }

        public LogRecordBuilder withOperationLogId(String operationLogId) {
            this.operationLogId = operationLogId;
            return this;
        }

        public LogRecordBuilder withTenant(String tenant) {
            this.tenant = tenant;
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

        public LogRecordBuilder withOperatorIp(String operatorIp) {
            this.operatorIp = operatorIp;
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

        public LogRecordBuilder withTargetExecutionTime(Long targetExecutionTime) {
            this.targetExecutionTime = targetExecutionTime;
            return this;
        }

        public LogRecordBuilder withPreviousContent(Object previousContent) {
            this.previousContent = previousContent;
            return this;
        }

        public LogRecordBuilder withCurrentContent(Object currentContent) {
            this.currentContent = currentContent;
            return this;
        }

        public LogRecord build() {
            LogRecord logRecord = new LogRecord();
            logRecord.setOperationLogId(operationLogId);
            logRecord.setTenant(tenant);
            logRecord.setOperatorId(operatorId);
            logRecord.setOperatorName(operatorName);
            logRecord.setOperatorIp(operatorIp);
            logRecord.setOperationCategory(operationCategory);
            logRecord.setBizNo(bizNo);
            logRecord.setOperationTarget(operationTarget);
            logRecord.setRequestMapping(requestMapping);
            logRecord.setOperationResult(operationResult);
            logRecord.setOperationTime(operationTime);
            logRecord.setTargetExecutionTime(targetExecutionTime);
            logRecord.setPreviousContent(previousContent);
            logRecord.setCurrentContent(currentContent);
            return logRecord;
        }
    }

}
