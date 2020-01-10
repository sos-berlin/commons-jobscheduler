package com.sos.scheduler.model.commands;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdSchedulerLogLogCategoriesSet extends SchedulerLogLogCategoriesSet {

    @SuppressWarnings("unused")
    private final String conClassName = "JSCmdSchedulerLogLogCategoriesSet";

    public JSCmdSchedulerLogLogCategoriesSet(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }
}
