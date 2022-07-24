package io.github.dk900912.oplog.model;

import java.util.Map;

public class OperationLogInfo {

    private BizCategory bizCategory;

    private String originBizTarget;

    private String originBizNo;

    private String previousContentSelectorName;

    public OperationLogInfo(Map<String, Object> operationLogAnnotationAttrMap) {
        this.bizCategory = (BizCategory) operationLogAnnotationAttrMap.get("bizCategory");
        this.originBizTarget = (String) operationLogAnnotationAttrMap.get("bizTarget");
        this.originBizNo = (String) operationLogAnnotationAttrMap.get("bizNo");
        this.previousContentSelectorName = (String) operationLogAnnotationAttrMap.get("previousContentSelectorName");
    }

    public BizCategory getBizCategory() {
        return bizCategory;
    }

    public void setBizCategory(BizCategory bizCategory) {
        this.bizCategory = bizCategory;
    }

    public String getOriginBizTarget() {
        return originBizTarget;
    }

    public void setOriginBizTarget(String originBizTarget) {
        this.originBizTarget = originBizTarget;
    }

    public String getOriginBizNo() {
        return originBizNo;
    }

    public void setOriginBizNo(String originBizNo) {
        this.originBizNo = originBizNo;
    }

    public String getPreviousContentSelectorName() {
        return previousContentSelectorName;
    }

    public void setPreviousContentSelectorName(String previousContentSelectorName) {
        this.previousContentSelectorName = previousContentSelectorName;
    }
}
