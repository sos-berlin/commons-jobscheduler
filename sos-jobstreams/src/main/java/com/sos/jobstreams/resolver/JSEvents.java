package com.sos.jobstreams.resolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import com.sos.jitl.jobstreams.classes.JSEvent;
import com.sos.jitl.jobstreams.classes.JSEventKey;
import com.sos.jitl.jobstreams.db.DBItemEvent;
import com.sos.jitl.jobstreams.db.DBItemOutConditionWithEvent;

import sos.util.SOSString;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSEvents {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSEvents.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

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
        for (DBItemOutConditionWithEvent dbItemOutConditionWithEvent : listOfEvents) {
            if (dbItemOutConditionWithEvent != null) {
                DBItemEvent itemEvent = new DBItemEvent();
                itemEvent.setCreated(dbItemOutConditionWithEvent.getCreated());
                itemEvent.setEvent(dbItemOutConditionWithEvent.getEvent());
                itemEvent.setGlobalEvent(dbItemOutConditionWithEvent.getGlobalEvent());
                itemEvent.setId(dbItemOutConditionWithEvent.getEventId());
                itemEvent.setJobStream(dbItemOutConditionWithEvent.getJobStream());
                itemEvent.setJobStreamHistoryId(dbItemOutConditionWithEvent.getJobStreamHistoryId());
                itemEvent.setOutConditionId(dbItemOutConditionWithEvent.getOutConditionId());
                itemEvent.setSession(dbItemOutConditionWithEvent.getSession());
                JSEvent jsEvent = new JSEvent();
                jsEvent.setItemEvent(itemEvent);
                jsEvent.setSchedulerId(dbItemOutConditionWithEvent.getJobSchedulerId());
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
        JSEvent returnEvent;
        if ((jsEventKey.getSession() == null) || (jsEventKey.getEvent() == null)) {
            return null;
        }
        if (!jsEventKey.getGlobalEvent() && jsEventKey.getJobStream() != null && !jsEventKey.getJobStream().isEmpty() && !"*".equals(jsEventKey
                .getSession())) {
            returnEvent = this.getEvent(jsEventKey);
            if (isDebugEnabled) {
                LOGGER.debug("EventKey:" + SOSString.toString(jsEventKey) + " --> Event direct return: " + returnEvent);
            }
            return returnEvent;
        } else {
            Map<JSEventKey, JSEvent> collect = this.listOfEvents.entrySet().stream().filter(jsEvent -> jsEventKey.getEvent().equals(jsEvent.getKey()
                    .getEvent()) && ("*".equals(jsEventKey.getSession()) || jsEventKey.getSession().equals(jsEvent.getKey().getSession()))
                    && (jsEventKey.getJobStream().isEmpty() || jsEventKey.getJobStream().equals(jsEvent.getValue().getJobStream())) && (jsEventKey
                            .getGlobalEvent() && jsEvent.getValue().isGlobalEvent() || !jsEventKey.getGlobalEvent() && !jsEvent.getValue()
                                    .isGlobalEvent() && jsEvent.getKey().getSchedulerId().equals(jsEventKey.getSchedulerId())))

                    .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
            try {
                returnEvent = collect.entrySet().stream().findAny().get().getValue();
                if (isDebugEnabled) {
                    LOGGER.debug("Event return: " + returnEvent.getEvent());
                }
                return returnEvent;
            } catch (NoSuchElementException e) {
                return null;
            }
        }

    }

    public void removeEvent(JSEvent event) {
        this.removeEvent(event.getKey());
    }

    public void newList() {
        this.listOfEvents = new HashMap<JSEventKey, JSEvent>();
    }

    public boolean isEmpty() {
        return listOfEvents.size() == 0;

    }

}
