package com.sos.jobstreams.resolver;

import java.util.Date;

import com.sos.jobstreams.db.DBItemEvent;

public class JSEvent {

    
    
    public DBItemEvent getItemEvent() {
        return itemEvent;
    }

    public String getSchedulerId() {
        return schedulerId;
    }

    DBItemEvent itemEvent;
    private String schedulerId;

    public JSEventKey getKey() {
        JSEventKey jsEventKey = new JSEventKey();
        jsEventKey.setSession(itemEvent.getSession());
        jsEventKey.setEvent(itemEvent.getEvent());
        jsEventKey.setJobStream(itemEvent.getJobStream());
        return jsEventKey;
    }

    public JSEvent() {
        super();
        itemEvent = new DBItemEvent();
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

    public long getOutConditionId() {
        return itemEvent.getOutConditionId();
    }

    public void setCreated(Date created) {
        itemEvent.setCreated(created);
    }

    public void setEvent(String event) {
        itemEvent.setEvent(event);
    }

    public void setSession(String session) {
        itemEvent.setSession(session);
    }

    public void setJobStream(String jobStream) {
        itemEvent.setJobStream(jobStream);
    }

    public void setSchedulerId(String schedulerId) {
        this.schedulerId = schedulerId;
    }

    public void setOutConditionId(Long outConditionId) {
        itemEvent.setOutConditionId(outConditionId);
    }

}
