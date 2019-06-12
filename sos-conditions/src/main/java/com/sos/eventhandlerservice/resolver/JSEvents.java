package com.sos.eventhandlerservice.resolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sos.eventhandlerservice.db.DBItemEvent;

public class JSEvents {

    Map<JSEventKey, JSEvent> listOfEvents;

    public JSEvents() {
        super();
        this.listOfEvents = new HashMap<JSEventKey, JSEvent>();
    }

    public void addEvent(JSEvent event) {
        this.listOfEvents.put(event.getKey(), event);
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
}
