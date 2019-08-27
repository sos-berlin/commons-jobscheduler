package com.sos.jobstreams.resolver;

import java.util.Date;

import com.sos.jobstreams.db.DBItemEvent;

public class JSEvent {

    DBItemEvent itemEvent;
    boolean storedInDatabase;

    
    public JSEventKey getKey() {
        JSEventKey jsEventKey = new JSEventKey();
        jsEventKey.setSession(itemEvent.getSession());
        jsEventKey.setEvent(itemEvent.getEvent());
        jsEventKey.setJobStream(itemEvent.getJobStream());
        return jsEventKey;
    }

    public void setItemEvent(DBItemEvent itemEvent) {
        this.storedInDatabase = true;
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

    public boolean isStoredInDatabase() {
        return storedInDatabase;
    }
    
    public void setStoredInDatabase(boolean storedInDatabase) {
        this.storedInDatabase = storedInDatabase;
    }
    
    public long getOutConditionId() {
        return itemEvent.getOutConditionId();
    }

}
