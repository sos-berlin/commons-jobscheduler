package com.sos.eventhandlerservice.resolver;

import java.util.Date;

import com.sos.eventhandlerservice.db.DBItemEvent;
import com.sos.eventhandlerservice.db.DBLayerEvents;

public class JSEvent {

    DBItemEvent itemEvent;

    public JSEventKey getKey() {
        JSEventKey jsEventKey = new JSEventKey();
        jsEventKey.setSession(itemEvent.getSession());
        jsEventKey.setEvent(itemEvent.getEvent());
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

    public String getWorkflow() {
        return itemEvent.getWorkflow();
    }

    public Date getCreated() {
        return itemEvent.getCreated();
    }

}
