package com.sos.eventhandlerservice.classes;

import javax.json.JsonObject;

public class CustomEvent {
    // {"variables":{"source":"CustomEventsUtilTest"},"TYPE":"VariablesCustomEvent","key":"InitConditionResolver","eventId":1554989954492000}

    private String type;
    private String source;
    private String key;
    private Integer eventId;

    public CustomEvent(JsonObject entry) {
        super();
        this.type = entry.getString("TYPE");
        this.key = entry.getString("key");

        JsonObject variables = entry.getJsonObject("variables");
        if (variables != null) {
            this.source = variables.getString("source","");
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

}
