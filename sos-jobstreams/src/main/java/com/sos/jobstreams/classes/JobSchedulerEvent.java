package com.sos.jobstreams.classes;

import javax.json.JsonObject;

public class JobSchedulerEvent {

    private String type;
    private Integer eventId;

    public JobSchedulerEvent(JsonObject entry) {
        super();
        this.type = entry.getString("TYPE");
        this.eventId = entry.getInt("eventId");
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }

}
