package com.sos.eventhandlerservice.db;

public class FilterOutConditionEvents {

    private Long outConditionId;
    private String event;
    private String jobStream;
    private String session;
    private String command;

    public Long getOutConditionId() {
        return outConditionId;
    }

    public void setOutConditionId(Long outConditionId) {
        this.outConditionId = outConditionId;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getJobStream() {
        return jobStream;
    }

    public void setJobStream(String jobStream) {
        this.jobStream = jobStream;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

}
