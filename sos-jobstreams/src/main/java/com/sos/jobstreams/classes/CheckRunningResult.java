package com.sos.jobstreams.classes;


public class CheckRunningResult {
    private boolean jobstreamCompleted;
    private String jobStream;
    
   
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
