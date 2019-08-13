package com.sos.eventhandlerservice.resolver;

import java.util.Date;

import com.sos.eventhandlerservice.db.DBItemEvent;

public class JSEvent {

    DBItemEvent itemEvent;

    public JSEventKey getKey() {
        JSEventKey jsEventKey = new JSEventKey();
        jsEventKey.setSession(itemEvent.getSession());
        jsEventKey.setEvent(itemEvent.getEvent());
        jsEventKey.setJobStream(itemEvent.getJobStream());
        return jsEventKey;
    }

    public void setItemEvent(DBItemEvent itemEvent) {
        this.itemEvent = itemEvent;
    }

    public Long getId() {
        return itemEvent.getId();
    }

    public String getSession() {
        return itemEvent.getSession();
    }

    public String getEvent() {
        return itemEvent.getEvent();
    }

    public String getJobStream() {
        return itemEvent.getJobStream();
    }

    public Date getCreated() {
        return itemEvent.getCreated();
    }

}
