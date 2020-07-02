package com.sos.jobstreams.classes;

import com.sos.jobstreams.resolver.JSCondition;
import java.time.LocalDateTime;

public class CheckHistoryValue {

    private Boolean validateResult;
    private JSCondition jsCondition;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public CheckHistoryValue(Boolean validateResult, JSCondition jsCondition) {
        super();
        this.validateResult = validateResult;
        this.jsCondition = jsCondition;
    }

    public Boolean getValidateResult() {
        return validateResult;
    }

    public void setValidateResult(Boolean validateResult) {
        this.validateResult = validateResult;
    }

    public JSCondition getJsCondition() {
        return jsCondition;
    }

    public void setJsCondition(JSCondition jsCondition) {
        this.jsCondition = jsCondition;
    }

    
    public LocalDateTime getStartTime() {
        return startTime;
    }

    
    public LocalDateTime getEndTime() {
        return endTime;
    }

    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

}
