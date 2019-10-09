package com.sos.jobstreams.db;

public class FilterEvents {

    private String event;
    private Boolean globalEvent;
    private Boolean includingGlobalEvent=false;
    private Long outConditionId;
    private String jobStream;
    private String session;
    private String job;

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public Long getOutConditionId() {
        return outConditionId;
    }

    public void setOutConditionId(Long outConditionId) {
        this.outConditionId = outConditionId;
    }

    public String getJobStream() {
        return jobStream;
    }

    public void setJobStream(String jobStream) {
        this.jobStream = jobStream;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    private String schedulerId;

    public String getSchedulerId() {
        return schedulerId;
    }

    public void setSchedulerId(String schedulerId) {
        this.schedulerId = schedulerId;
    }

    public Boolean getGlobalEvent() {
        return globalEvent;
    }

    public void setGlobalEvent(Boolean globalEvent) {
        if (globalEvent == null) {
            globalEvent = false;
        }
        this.globalEvent = globalEvent;
    }

    public String getGlobalEventAsString() {
      
        if (globalEvent) {
            return "true";
        } else {
            return "false";
        }
    }

    
    public Boolean getIncludingGlobalEvent() {
        return includingGlobalEvent;
    }

    
    public void setIncludingGlobalEvent(Boolean includingGlobalEvent) {
        this.includingGlobalEvent = includingGlobalEvent;
    }

}
