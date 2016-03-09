package com.sos.scheduler.model.commands;

import org.apache.log4j.Logger;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdSchedulerLogLogCategoriesSet extends SchedulerLogLogCategoriesSet {

    @SuppressWarnings("unused")
    private final String conClassName = "JSCmdSchedulerLogLogCategoriesSet";
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(JSCmdSchedulerLogLogCategoriesSet.class);

    public JSCmdSchedulerLogLogCategoriesSet(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }
}
