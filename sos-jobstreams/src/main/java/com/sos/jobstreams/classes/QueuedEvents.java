package com.sos.jobstreams.classes;

import com.sos.hibernate.classes.SOSHibernateSession;
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

    public void handleEventlistBuffer(JSEvents jsEvents) {
        for (JSEvent jsEvent : jsEvents.getListOfEvents().values()) {
            if (jsEvent.isDbError()) {
                if (isTraceEnabled) {
                    LOGGER.trace("Adding event into queued events: " + SOSString.toString(jsEvent));
                }
                super.addEvent(jsEvent);
            }
        }
    }

    public boolean isEmpty() {
        return super.getListOfEvents().size() == 0;
    }

    public void storetoDb(SOSHibernateSession sosHibernateSession) {
        LOGGER.debug("Store queued events to db");
        for (JSEvent jsEvent : super.getListOfEvents().values()) {
            if (isTraceEnabled) {
                LOGGER.trace("store queued event to db:" + SOSString.toString(jsEvent));
            }
            jsEvent.store(sosHibernateSession);
            super.removeEvent(jsEvent);
        }
    }

    public void deleteFromDb(SOSHibernateSession sosHibernateSession) {
        LOGGER.debug("Delete queued events from db");
        for (JSEvent jsEvent : super.getListOfEvents().values()) {
            if (isTraceEnabled) {
                LOGGER.trace("delete queued event from db: " + SOSString.toString(jsEvent));
            }
            jsEvent.deleteEvent(sosHibernateSession);
            super.removeEvent(jsEvent);
        }
    }
}
