package com.sos.eventhandlerservice.resolver;

import com.sos.eventhandlerservice.classes.Constants;
import com.sos.eventhandlerservice.db.DBItemOutConditionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSOutConditionEvent {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSOutConditionEvent.class);
    private DBItemOutConditionEvent itemOutConditionEvent;

    public void setItemOutCondition(DBItemOutConditionEvent itemOutConditionEvent) {
        this.itemOutConditionEvent = itemOutConditionEvent;
    }

    public Long getId() {
        return itemOutConditionEvent.getId();
    }

    public Long getOutConditionId() {
        return itemOutConditionEvent.getOutConditionId();
    }

    public String getEvent() {
        return itemOutConditionEvent.getEvent();
    }

    public String getEventValue() {
        String[] s = itemOutConditionEvent.getEvent().split(":");
        String e = itemOutConditionEvent.getEvent();
        if (s.length > 1) {
            e = e.replaceFirst(".*:", "");
        }
            
        return e;
    }
    
    public String getCommand() {
        return itemOutConditionEvent.getCommand();
    }

    public boolean isCreateCommand() {
        return itemOutConditionEvent.isCreate();
    }

    public boolean isDeleteCommand() {
        return itemOutConditionEvent.isDelete();
    }
}
