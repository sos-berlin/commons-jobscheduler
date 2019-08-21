package com.sos.jobstreams.db;

import javax.persistence.Transient;

import com.sos.jobstreams.resolver.interfaces.IJSJobConditionKey;

public class DBItemInConditionWithCommand implements IJSJobConditionKey {

    private DBItemInCondition dbItemInCondition;
    private DBItemInConditionCommand dbItemInConditionCommand;
    private boolean consumed;

    public DBItemInConditionWithCommand(DBItemInCondition dbItemInCondition, DBItemInConditionCommand dbItemInConditionCommand) {
        this.dbItemInCondition = dbItemInCondition;
        this.dbItemInConditionCommand = dbItemInConditionCommand;
    }

    public DBItemInCondition getDbItemInCondition() {
        return dbItemInCondition;
    }

    public void setDbItemInCondition(DBItemInCondition dbItemInCondition) {
        this.dbItemInCondition = dbItemInCondition;
    }

    public DBItemInConditionCommand getDbItemInConditionCommand() {
        return dbItemInConditionCommand;
    }

    public void setDbItemInConditionCommand(DBItemInConditionCommand dbItemInConditionCommand) {
        this.dbItemInConditionCommand = dbItemInConditionCommand;
    }

    @Transient
    public boolean isConsumed() {
        return consumed;
    }

    public void setConsumed(boolean consumed) {
        this.consumed = consumed;
    }

    @Override
    public String getJobSchedulerId() {
        return this.dbItemInCondition.getJobSchedulerId();
    }

    @Override
    public String getJob() {
        return this.dbItemInCondition.getJob();
    }
 

}