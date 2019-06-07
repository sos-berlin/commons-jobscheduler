package com.sos.eventhandlerservice.resolver;

import com.sos.eventhandlerservice.resolver.interfaces.IJSJobConditionKey;

public class JSJobConditionKey {

    private String masterId;
    private String job;


    public JSJobConditionKey(IJSJobConditionKey jsJobConditionKey) {
        masterId = jsJobConditionKey.getMasterId();
        job = jsJobConditionKey.getJob();
    }

    public JSJobConditionKey() {
    }

   
    public JSJobConditionKey(JSOutCondition outCondition) {
        masterId = outCondition.getMasterId();
        job = outCondition.getJob();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof JSJobConditionKey) {
            JSJobConditionKey jobConditionKey = (JSJobConditionKey) obj;
            return masterId.equals(jobConditionKey.masterId) && job.equals(jobConditionKey.job);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (masterId + "." + job).hashCode();
    }

    public String getMasterId() {
        return masterId;
    }

    public void setMasterId(String masterId) {
        this.masterId = masterId;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

}