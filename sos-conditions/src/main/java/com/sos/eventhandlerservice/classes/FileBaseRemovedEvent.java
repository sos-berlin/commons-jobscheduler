package com.sos.eventhandlerservice.classes;

import javax.json.JsonObject;

public class FileBaseRemovedEvent {

    // {"TYPE":"FileBasedRemoved","key":"Job:/conditions/job2","eventId":1564672082770000}
    private String key;
    private String job;
    private Integer eventId;

    public FileBaseRemovedEvent(JsonObject entry) {
        super();

        String key = entry.getString("key");
        if (key != null) {
            String[] keys = key.split(":");
            if (keys.length > 0) {
                this.job = keys[1];
            }
        }
        this.eventId = entry.getInt("eventId");
    }

    public String getKey() {
        return key;
    }

    public Integer getEventId() {
        return eventId;
    }

    public String getJob() {
        return job;
    }

}
