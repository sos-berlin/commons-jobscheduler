package com.sos.jobstreams.classes;

import java.util.Map;
import java.util.Map.Entry;

public class PublishEventOrder {

    private boolean published=false;
    private String eventKey;
    private Map<String, String> values;

    public String getEventKey() {
        return eventKey;
    }

    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }

    public Map<String, String> getValues() {
        return values;
    }

    public void setValues(Map<String, String> values) {
        this.values = values;
    }

    public String asString() {
        String s = this.eventKey + ": ";
        for (Entry<String, String> entry : values.entrySet()) {
            s = s + entry.getKey() + "=" + entry.getValue() + " ";
        }
        return s;
    }

    
    public boolean isPublished() {
        return published;
    }

    
    public void setPublished(boolean published) {
        this.published = published;
    }

}
