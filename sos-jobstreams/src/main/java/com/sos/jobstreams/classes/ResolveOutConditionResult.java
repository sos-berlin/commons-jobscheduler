package com.sos.jobstreams.classes;


public class ResolveOutConditionResult {
    private boolean dbChange;
    private boolean jobstreamCompleted;
    private String jobStream;
    
    public boolean isDbChange() {
        return dbChange;
    }
    
    public void setDbChange(boolean dbChange) {
        this.dbChange = dbChange;
    }
    
    public boolean isJobstreamCompleted() {
        return jobstreamCompleted;
    }
    
    public void setJobstreamCompleted(boolean jobstreamCompleted) {
        this.jobstreamCompleted = jobstreamCompleted;
    }

    
    public String getJobStream() {
        return jobStream;
    }

    
    public void setJobStream(String jobStream) {
        this.jobStream = jobStream;
    }

}
