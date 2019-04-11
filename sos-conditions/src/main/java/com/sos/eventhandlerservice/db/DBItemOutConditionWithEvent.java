package com.sos.eventhandlerservice.db;

import com.sos.eventhandlerservice.resolver.interfaces.IJSJobConditionKey;

public class DBItemOutConditionWithEvent implements IJSJobConditionKey {

    private DBItemOutCondition dbItemOutCondition;
    private DBItemOutConditionEvent dbItemOutConditionEvent;

    public DBItemOutConditionWithEvent(DBItemOutCondition dbItemOutCondition, DBItemOutConditionEvent dbItemOutConditionEvent) {
        this.dbItemOutCondition = dbItemOutCondition;
        this.dbItemOutConditionEvent = dbItemOutConditionEvent;
    }

    public DBItemOutCondition getDbItemOutCondition() {
        return dbItemOutCondition;
    }

    public void setDbItemOutCondition(DBItemOutCondition dbItemOutCondition) {
        this.dbItemOutCondition = dbItemOutCondition;
    }

    public DBItemOutConditionEvent getDbItemOutConditionEvent() {
        return dbItemOutConditionEvent;
    }

    public void setDbItemOutConditionEvent(DBItemOutConditionEvent dbItemOutConditionEvent) {
        this.dbItemOutConditionEvent = dbItemOutConditionEvent;
    }

    @Override
    public String getMasterId() {
        return dbItemOutCondition.getMasterId();
    }

    @Override
    public String getJob() {
        return dbItemOutCondition.getJob();
    }

}