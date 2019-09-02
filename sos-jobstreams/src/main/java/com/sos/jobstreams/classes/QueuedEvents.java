package com.sos.jobstreams.classes;

import com.sos.jobstreams.resolver.JSEvent;
import com.sos.jobstreams.resolver.JSEvents;

public class QueuedEvents extends JSEvents {

    public QueuedEvents() {
        super();
    }

    public void handleAddEventlistBuffer(JSEvents jsEvents) {
        for (JSEvent jsEvent : jsEvents.getListOfEvents().values()) {
            if (jsEvent.isDbError()) {
                super.addEvent(jsEvent);
            }
        }
    }

    public void handleDeleteEventlistBuffer(JSEvents jsEvents) {
        for (JSEvent jsEvent : jsEvents.getListOfEvents().values()) {
            if (jsEvent.isDbError()) {
                super.removeEvent(jsEvent);
            }
        }
    }

}
