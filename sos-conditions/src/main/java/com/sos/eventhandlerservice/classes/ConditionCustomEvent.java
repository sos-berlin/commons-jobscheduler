package com.sos.eventhandlerservice.classes;

import javax.json.JsonObject;

public class ConditionCustomEvent {
    // {"variables":{"source":"CustomEventsUtilTest"},"TYPE":"VariablesCustomEvent","key":"InitConditionResolver","eventId":1554989954492000}

    private String type;
    private String source;
    private String workflow;
    private String job;
    private String key;
    private Integer eventId;

    public ConditionCustomEvent(JsonObject entry) {
        super();
        this.type = entry.getString("TYPE");
        this.key = entry.getString("key");

        JsonObject variables = entry.getJsonObject("variables");
        if (variables != null) {
            this.source = variables.getString("source","");
            this.workflow = variables.getString("workflow","");
            this.job = variables.getString("job","");
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

    
    public String getWorkflow() {
        return workflow;
    }

    
    public String getJob() {
        return job;
    }

}
