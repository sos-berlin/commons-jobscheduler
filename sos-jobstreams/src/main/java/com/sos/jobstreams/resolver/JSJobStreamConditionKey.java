package com.sos.jobstreams.resolver;

import com.sos.jitl.jobstreams.interfaces.IJSJobConditionKey;

public class JSJobStreamConditionKey {

    private String jobSchedulerId;
    private String jobStream;


    public JSJobStreamConditionKey(IJSJobConditionKey inCondition) {
        jobSchedulerId = inCondition.getJobSchedulerId();
        jobStream = inCondition.getJobStream();
    }
   
    public JSJobStreamConditionKey() {
     }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof JSJobStreamConditionKey) {
            JSJobStreamConditionKey jobConditionKey = (JSJobStreamConditionKey) obj;
            return jobSchedulerId.equals(jobConditionKey.jobSchedulerId) && jobStream.equals(jobConditionKey.jobStream);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (jobSchedulerId + "." + jobStream).hashCode();
    }

    public String getJobSchedulerId() {
        return jobSchedulerId;
    }

    public void setJobSchedulerId(String jobSchedulerId) {
        this.jobSchedulerId = jobSchedulerId;
    }

    
    public String getJobStream() {
        return jobStream;
    }

    
    public void setJobStream(String jobStream) {
        this.jobStream = jobStream;
    }


}