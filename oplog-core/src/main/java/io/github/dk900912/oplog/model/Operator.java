package io.github.dk900912.oplog.model;

import java.util.StringJoiner;

/**
 * @author dukui
 */
public class Operator {
    /**
     * 操作人唯一标识，一般为当前登录用户的用户ID
     */
    private String operatorId;

    /**
     * 操作人姓名，一般为当前登录用户的用户名
     */
    private String operatorName;

    public Operator() {
    }

    public Operator(String operatorId, String operatorName) {
        this.operatorId = operatorId;
        this.operatorName = operatorName;
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

    @Override
    public String toString() {
        return new StringJoiner(", ", "[", "]")
                .add("operatorId='" + operatorId + "'")
                .add("operatorName='" + operatorName + "'")
                .toString();
    }

    public static OperatorBuilder builder() {
        return new OperatorBuilder();
    }

    public static final class OperatorBuilder {

        private String operatorId;

        private String operatorName;

        private OperatorBuilder() {
        }

        public OperatorBuilder withOperatorId(String operatorId) {
            this.operatorId = operatorId;
            return this;
        }

        public OperatorBuilder withOperatorName(String operatorName) {
            this.operatorName = operatorName;
            return this;
        }

        /**
         * build operator
         *
         * @return operator
         */
        public Operator build() {
            Operator operator = new Operator();
            operator.setOperatorId(operatorId);
            operator.setOperatorName(operatorName);
            return operator;
        }
    }
}
