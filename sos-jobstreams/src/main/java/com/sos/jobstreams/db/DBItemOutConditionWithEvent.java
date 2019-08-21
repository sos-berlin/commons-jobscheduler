package com.sos.jobstreams.db;

import com.sos.jobstreams.resolver.interfaces.IJSJobConditionKey;

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
    public String getJobSchedulerId() {
        return dbItemOutCondition.getJobSchedulerId();
    }

    @Override
    public String getJob() {
        return dbItemOutCondition.getJob();
    }

}