package com.sos.scheduler.model.commands;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdRemoveOrder extends RemoveOrder {

    @SuppressWarnings("unused")
    private final String conClassName = "JSCmdRemoveOrder";

    public JSCmdRemoveOrder(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }

    public void setJobChainIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            this.jobChain = value;
        }
    }

    public void setOrderIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            this.order = value;
        }
    }

}
