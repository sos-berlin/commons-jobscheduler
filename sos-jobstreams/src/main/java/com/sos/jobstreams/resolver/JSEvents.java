package com.sos.jobstreams.resolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sos.jobstreams.db.DBItemEvent;

public class JSEvents {

    private Map<JSEventKey, JSEvent> listOfEvents;

    public JSEvents() {
        super();
        this.listOfEvents = new HashMap<JSEventKey, JSEvent>();
    }

    public void addEvent(JSEvent event) {
        this.listOfEvents.put(event.getKey(), event);
        JSEventKey key = event.getKey();
        key.setSession("*");
        this.listOfEvents.put(key, event);

    }

    public void removeEvent(JSEventKey eventKey) {
        this.listOfEvents.remove(eventKey);
    }

    public JSEvent getEvent(JSEventKey eventKey) {
        return this.listOfEvents.get(eventKey);
    }

    public void setListOfEvents(List<DBItemEvent> listOfEvents) {
        for (DBItemEvent itemEvent : listOfEvents) {
            JSEvent jsEvent = new JSEvent();
            jsEvent.setItemEvent(itemEvent);
            addEvent(jsEvent);
        }
    }

    public Map<JSEventKey, JSEvent> getListOfEvents() {
        return listOfEvents;
    }

    public void addAll(Map<JSEventKey, JSEvent> listOfNewEvents) {
        listOfEvents.putAll(listOfNewEvents);

    }

    public JSEvent getEventByJobStream(JSEventKey jsEventKey, String conditionJobStream) {
        if (conditionJobStream.isEmpty()) {
            for (JSEventKey eventKey : this.listOfEvents.keySet()) {
                jsEventKey.setJobStream(eventKey.getJobStream());
                JSEvent jsEvent = getEvent(jsEventKey);
                if (jsEvent != null) {
                    return jsEvent;
                }
            }
        } else {
            jsEventKey.setJobStream(conditionJobStream);
            return getEvent(jsEventKey);
        }
        return null;
    }

    public void removeEvent(JSEvent event) {
        JSEventKey jsEventKey = new JSEventKey();
        jsEventKey.setEvent(event.getEvent());
        jsEventKey.setJobStream(event.getJobStream());
        jsEventKey.setSession(event.getSession());
        this.removeEvent(jsEventKey);
        jsEventKey.setSession("*");
        this.removeEvent(jsEventKey);
    }

   
}
