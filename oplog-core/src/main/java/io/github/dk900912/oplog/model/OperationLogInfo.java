package io.github.dk900912.oplog.model;

import java.util.Map;

import static io.github.dk900912.oplog.constant.Constants.BIZ_CATEGORY;
import static io.github.dk900912.oplog.constant.Constants.BIZ_NO;
import static io.github.dk900912.oplog.constant.Constants.BIZ_TARGET;
import static io.github.dk900912.oplog.constant.Constants.SELECTOR_NAME;

/**
 * @author dukui
 */
public class OperationLogInfo {

    private BizCategory bizCategory;

    private String originBizTarget;

    private String originBizNo;

    private String selectorName;

    public OperationLogInfo(Map<String, Object> operationLogAnnotationAttrMap) {
        this.bizCategory = (BizCategory) operationLogAnnotationAttrMap.get(BIZ_CATEGORY);
        this.originBizTarget = (String) operationLogAnnotationAttrMap.get(BIZ_TARGET);
        this.originBizNo = (String) operationLogAnnotationAttrMap.get(BIZ_NO);
        this.selectorName = (String) operationLogAnnotationAttrMap.get(SELECTOR_NAME);
    }

    public OperationLogInfo(BizCategory bizCategory, String originBizNo, String selectorName) {
        this.bizCategory = bizCategory;
        this.originBizNo = originBizNo;
        this.selectorName = selectorName;
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

    public String getSelectorName() {
        return selectorName;
    }

    public void setSelectorName(String selectorName) {
        this.selectorName = selectorName;
    }

}
