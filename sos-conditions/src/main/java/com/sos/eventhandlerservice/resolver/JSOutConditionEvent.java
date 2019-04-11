package com.sos.eventhandlerservice.resolver;

import com.sos.eventhandlerservice.db.DBItemOutConditionEvent;

public class JSOutConditionEvent {

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
}
