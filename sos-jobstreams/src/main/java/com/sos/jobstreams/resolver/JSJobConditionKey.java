package com.sos.jobstreams.resolver;

import com.sos.jitl.jobstreams.interfaces.IJSJobConditionKey;

public class JSJobConditionKey {

    private String jobSchedulerId;
    private String job;


    public JSJobConditionKey(IJSJobConditionKey jsJobConditionKey) {
        jobSchedulerId = jsJobConditionKey.getJobSchedulerId();
        job = jsJobConditionKey.getJob();
    }

    public JSJobConditionKey() {
    }

   
    public JSJobConditionKey(JSOutCondition outCondition) {
        jobSchedulerId = outCondition.getJobSchedulerId();
        job = outCondition.getJob();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof JSJobConditionKey) {
            JSJobConditionKey jobConditionKey = (JSJobConditionKey) obj;
            return jobSchedulerId.equals(jobConditionKey.jobSchedulerId) && job.equals(jobConditionKey.job);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (jobSchedulerId + "." + job).hashCode();
    }

    public String getJobSchedulerId() {
        return jobSchedulerId;
    }

    public void setJobSchedulerId(String jobSchedulerId) {
        this.jobSchedulerId = jobSchedulerId;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

}