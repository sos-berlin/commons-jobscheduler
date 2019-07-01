package com.sos.eventhandlerservice.db;

public class FilterEvents {

    private String event;
    private Long outConditionId;
    private String workflow;
    private String session;

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

    
    public String getWorkflow() {
        return workflow;
    }

    
    public void setWorkflow(String workflow) {
        this.workflow = workflow;
    }

    

}
