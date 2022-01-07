package io.github.oplog;

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
        return "Operator{" +
                "operatorId='" + operatorId + '\'' +
                ", operatorName='" + operatorName + '\'' +
                '}';
    }
}
