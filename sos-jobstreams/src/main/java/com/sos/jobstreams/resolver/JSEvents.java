package com.sos.jobstreams.resolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import com.sos.jitl.jobstreams.db.DBItemOutConditionWithEvent;

public class JSEvents {

    private Map<JSEventKey, JSEvent> listOfEvents;

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

    public void setListOfEvents(List<DBItemOutConditionWithEvent> listOfEvents) {
        for (DBItemOutConditionWithEvent itemEvent : listOfEvents) {
            if (itemEvent != null) {
                JSEvent jsEvent = new JSEvent();
                jsEvent.setItemEvent(itemEvent.getDbItemEvent());
                jsEvent.setSchedulerId(itemEvent.getJobSchedulerId());
                addEvent(jsEvent);
            }
        }
    }

    public Map<JSEventKey, JSEvent> getListOfEvents() {
        return listOfEvents;
    }

    public void addAll(Map<JSEventKey, JSEvent> listOfNewEvents) {
        listOfEvents.putAll(listOfNewEvents);

    }

    public JSEvent getEventByJobStream(JSEventKey jsEventKey) {
        if (jsEventKey.getJobStream() != null && !jsEventKey.getJobStream().isEmpty() && !"*".equals(jsEventKey.getSession())) {
            return this.getEvent(jsEventKey);
        } else {
            Map<JSEventKey, JSEvent> collect = this.listOfEvents.entrySet().stream().filter(jsEvent -> jsEventKey.getEvent().equals(jsEvent.getKey()
                    .getEvent()) && ("*".equals(jsEventKey.getSession()) || jsEventKey.getSession().equals(jsEvent.getKey().getSession()))
                    && (jsEventKey.getJobStream().isEmpty() || jsEventKey.getJobStream().equals(jsEvent.getValue().getJobStream())) && (jsEventKey
                            .getGlobalEvent() && jsEvent.getValue().isGlobalEvent() || !jsEventKey.getGlobalEvent() && !jsEvent.getValue()
                                    .isGlobalEvent() && jsEvent.getKey().getSchedulerId().equals(jsEventKey.getSchedulerId())))

                    .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
            try {
                return collect.entrySet().stream().findAny().get().getValue();
            } catch (NoSuchElementException e) {
                return null;
            }
        }

    }

    public void removeEvent(JSEvent event) {
        JSEventKey jsEventKey = new JSEventKey();
        jsEventKey.setEvent(event.getEvent());
        jsEventKey.setJobStream(event.getJobStream());
        jsEventKey.setSession(event.getSession());
        jsEventKey.setSchedulerId(event.getSchedulerId());
        jsEventKey.setGlobalEvent(event.isGlobalEvent());
        this.removeEvent(jsEventKey);
    }

    public void newList() {
        this.listOfEvents = new HashMap<JSEventKey, JSEvent>();
    }

    public boolean isEmpty() {
        return listOfEvents.size() == 0;

    }

}
