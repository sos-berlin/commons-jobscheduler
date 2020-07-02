package com.sos.jobstreams.classes;

import javax.json.JsonObject;

public class ConditionCustomEvent {
    // {"variables":{"source":"CustomEventsUtilTest"},"TYPE":"VariablesCustomEvent","key":"InitConditionResolver","eventId":1554989954492000}

    private String type;
    private String session;
    private String event;
    private boolean globalEvent;
    private String outConditionId;
    private String source;
    private String jobStream;
    private String job;
    private String key;
    private Integer eventId;

    public ConditionCustomEvent(JsonObject entry) {
        super();
        this.type = entry.getString("TYPE");
        this.key = entry.getString("key");

        JsonObject variables = entry.getJsonObject("variables");
        if (variables != null) {
            this.event = variables.getString("event", "");
            this.session = variables.getString("session", "");
            this.source = variables.getString("source", "");
            this.jobStream = variables.getString("jobStream", "");
            this.job = variables.getString("job", "");
            this.globalEvent = "true".equals(variables.getString("globalEvent","false"));
            this.outConditionId = variables.getString("outConditionId", "");
        }
        this.eventId = entry.getInt("eventId");
    }

    public String getType() {
        return type;
    }

    public String getSource() {
        return source;
    }

    public String getKey() {
        return key;
    }

    public Integer getEventId() {
        return eventId;
    }

    public String getJobStream() {
        return jobStream;
    }

    public String getJob() {
        return job;
    }

    public String getSession() {
        return session;
    }

    public String getEvent() {
        return event;
    }

    public boolean isGlobalEvent() {
        return globalEvent;
    }

    
    public String getOutConditionId() {
        return outConditionId;
    }

}
