package com.sos.jobstreams.classes;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.jobstreams.classes.JSEvent;
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
                    LOGGER.trace("Adding event into queued events: " + jsEvent.toStr());
                }
                super.addEvent(jsEvent);
            }
        }
    }

    public boolean isEmpty() {
        return super.getListOfEvents().size() == 0;
    }

    public void storetoDb(SOSHibernateSession sosHibernateSession, JSEvents jsEvents) {
        LOGGER.debug("Store queued events to db");
        boolean dbError = false;
        for (JSEvent jsEvent : super.getListOfEvents().values()) {
            if (jsEvents.getEvent(jsEvent.getKey()) != null) {
                if (isTraceEnabled) {
                    LOGGER.trace("store queued event to db:" + jsEvent.toStr());
                }
                dbError = dbError || jsEvent.store(sosHibernateSession);
            } else {
                if (isTraceEnabled) {
                    LOGGER.trace("storequeued event will not be stored to db as not existing in actual list of events:" + jsEvent.toStr());
                }
            }
        }
        if (!dbError) {
            super.newList();
        }
    }

    public void deleteFromDb(SOSHibernateSession sosHibernateSession, JSEvents jsEvents) {
        LOGGER.debug("Delete queued events from db");
        boolean dbError = false;
        for (JSEvent jsEvent : super.getListOfEvents().values()) {
            if (jsEvents.getEvent(jsEvent.getKey()) == null) {
                if (isTraceEnabled) {
                    LOGGER.trace("delete queued event from db: " + jsEvent.toStr());
                    dbError = dbError || jsEvent.deleteEvent(sosHibernateSession);
                } else {
                    if (isTraceEnabled) {
                        LOGGER.trace("deletequeued event will not be deleted from db as existing in actual list of events:" + jsEvent.toStr());
                    }
                }
            }
        }
        if (!dbError) {
            super.newList();
        }
    }
}
