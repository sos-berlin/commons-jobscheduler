package com.sos.jobstreams.db;

import com.sos.jobstreams.resolver.interfaces.IJSJobConditionKey;

public class DBItemOutConditionWithEvent implements IJSJobConditionKey {

    private DBItemOutCondition dbItemOutCondition;
    private DBItemEvent dbItemEvent;

    public DBItemOutConditionWithEvent(DBItemOutCondition dbItemOutCondition, DBItemEvent dbItemEvent) {
        this.dbItemOutCondition = dbItemOutCondition;
        this.dbItemEvent = dbItemEvent;
    }

    public DBItemOutCondition getDbItemOutCondition() {
        return dbItemOutCondition;
    }

    public void setDbItemOutCondition(DBItemOutCondition dbItemOutCondition) {
        this.dbItemOutCondition = dbItemOutCondition;
    }

    public DBItemEvent getDbItemEvent() {
        return dbItemEvent;
    }

    public void setDbItemEvent(DBItemEvent dbItemEvent) {
        this.dbItemEvent = dbItemEvent;
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