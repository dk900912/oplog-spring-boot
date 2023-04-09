package io.github.dk900912.oplog.model;

import io.github.dk900912.oplog.annotation.DiffSelector;

import java.util.Map;

import static io.github.dk900912.oplog.constant.Constants.BIZ_CATEGORY;
import static io.github.dk900912.oplog.constant.Constants.BIZ_NO;
import static io.github.dk900912.oplog.constant.Constants.BIZ_TARGET;
import static io.github.dk900912.oplog.constant.Constants.DIFF_SELECTOR;

/**
 * @author dukui
 */
public class OperationLogInfo {

    private BizCategory bizCategory;

    private String originBizTarget;

    private String originBizNo;

    private DiffSelector originDiffSelector;

    public OperationLogInfo(Map<String, Object> operationLogAnnotationAttrMap) {
        this.bizCategory = (BizCategory) operationLogAnnotationAttrMap.get(BIZ_CATEGORY);
        this.originBizTarget = (String) operationLogAnnotationAttrMap.get(BIZ_TARGET);
        this.originBizNo = (String) operationLogAnnotationAttrMap.get(BIZ_NO);
        this.originDiffSelector = (DiffSelector) operationLogAnnotationAttrMap.get(DIFF_SELECTOR);
    }

    public OperationLogInfo(BizCategory bizCategory, String originBizNo, DiffSelector originDiffSelector) {
        this.bizCategory = bizCategory;
        this.originBizNo = originBizNo;
        this.originDiffSelector = originDiffSelector;
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

    public DiffSelector getOriginDiffSelector() {
        return originDiffSelector;
    }

    public void setOriginDiffSelector(DiffSelector originDiffSelector) {
        this.originDiffSelector = originDiffSelector;
    }

}
