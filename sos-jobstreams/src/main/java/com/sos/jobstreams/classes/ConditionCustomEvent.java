package com.sos.jobstreams.classes;

import java.util.HashMap;
import java.util.Map;

import javax.json.JsonObject;

public class ConditionCustomEvent {
    // {"variables":{"source":"CustomEventsUtilTest"},"TYPE":"VariablesCustomEvent","key":"InitConditionResolver","eventId":1554989954492000}

    private String type;
    private String session;
    private String event;
    private boolean globalEvent;
    private String outConditionId;
    private Long jobStreamStarterId;
    private String source;
    private String jobStream;
    private String job;
    private String key;
    private Integer eventId;
    private String state;
    private String at;
    private Map<String,String>parameters;

    public ConditionCustomEvent(JsonObject entry) {
        super();
        this.parameters = new HashMap<String,String>();
        this.type = entry.getString("TYPE");
        this.key = entry.getString("key");
        JsonObject variables = entry.getJsonObject("variables");
        if (variables != null) {
            
            for (String name : variables.keySet()) {
                String value = variables.getString(name);
                if (name.startsWith("#") && name.length() > 1) {
                    parameters.put(name.substring(1),value); 
                }
            }
            this.event = variables.getString("event", "");
            this.session = variables.getString("session", "");
            this.source = variables.getString("source", "");
            this.jobStream = variables.getString("jobStream", "");
            this.state = variables.getString("state", "");
            this.job = variables.getString("job", "");
            this.at = variables.getString("at", "now");
            this.globalEvent = "true".equals(variables.getString("globalEvent", "false"));
            this.outConditionId = variables.getString("outConditionId", "");
            try {
                this.jobStreamStarterId = Long.valueOf(variables.getString("starterId", ""));
            } catch (NumberFormatException e) {
                this.jobStreamStarterId = -1L;
            }
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

    public String getState() {
        return state;
    }

    
    public Long getJobStreamStarterId() {
        return jobStreamStarterId;
    }

    
    public String getAt() {
        return at;
    }

    
    public Map<String, String> getParameters() {
        return parameters;
    }
 

}
