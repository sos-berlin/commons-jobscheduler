package com.sos.scheduler.model.commands;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdSchedulerLogLogCategoriesReset extends SchedulerLogLogCategoriesReset {

    @SuppressWarnings("unused")
    private final String conClassName = "JSCmdSchedulerLogLogCategoriesReset";

    public JSCmdSchedulerLogLogCategoriesReset(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }
}
