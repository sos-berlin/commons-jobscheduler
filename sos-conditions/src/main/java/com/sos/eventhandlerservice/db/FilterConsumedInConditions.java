package com.sos.eventhandlerservice.db;

public class FilterConsumedInConditions {

    private Long inConditionId;
    private String masterId="";
    private String workflow="";
    private String job="";
    private String session;

    public Long getInConditionId() {
        return inConditionId;
    }

    public void setInConditionId(Long inConditionId) {
        this.inConditionId = inConditionId;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getWorkflow() {
        return workflow;
    }

    public void setWorkflow(String workflow) {
        this.workflow = workflow;
    }

    
    public String getJob() {
        return job;
    }

    
    public void setJob(String job) {
        this.job = job;
    }

    
    public String getMasterId() {
        return masterId;
    }

    
    public void setMasterId(String masterId) {
        this.masterId = masterId;
    }

}