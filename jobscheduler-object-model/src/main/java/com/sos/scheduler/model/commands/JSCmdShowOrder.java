package com.sos.scheduler.model.commands;

import org.apache.log4j.Logger;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdShowOrder extends ShowOrder {

    @SuppressWarnings("unused")
    private final String conClassName = "JSCmdShowOrder";
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(JSCmdShowOrder.class);

    public JSCmdShowOrder(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }

}
