package com.sos.scheduler.model.commands;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdShowOrder extends ShowOrder {

    @SuppressWarnings("unused")
    private final String conClassName = "JSCmdShowOrder";

    public JSCmdShowOrder(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }

}
