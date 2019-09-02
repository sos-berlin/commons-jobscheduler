package com.sos.jobstreams.classes;

import com.sos.jobstreams.resolver.JSEvent;
import com.sos.jobstreams.resolver.JSEvents;

import sos.util.SOSString;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueuedEvents extends JSEvents {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueuedEvents.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private static final boolean isTraceEnabled = LOGGER.isTraceEnabled();

    public QueuedEvents() {
        super();
    }

    public void handleAddEventlistBuffer(JSEvents jsEvents) {
        for (JSEvent jsEvent : jsEvents.getListOfEvents().values()) {
            if (jsEvent.isDbError()) {
                if (isTraceEnabled) {
                    LOGGER.trace("Adding event into queued events: " + SOSString.toString(jsEvent));
                }
                super.addEvent(jsEvent);
            }
        }
    }

    public void handleDeleteEventlistBuffer(JSEvents jsEvents) {
        for (JSEvent jsEvent : jsEvents.getListOfEvents().values()) {
            if (jsEvent.isDbError()) {
                if (isTraceEnabled) {
                    LOGGER.trace("removing event from queued events: " + SOSString.toString(jsEvent));
                }
                super.removeEvent(jsEvent);
            }
        }
    }

}
