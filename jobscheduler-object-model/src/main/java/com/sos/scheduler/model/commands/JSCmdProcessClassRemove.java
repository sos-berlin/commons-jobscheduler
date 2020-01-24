package com.sos.scheduler.model.commands;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdProcessClassRemove extends ProcessClassRemove {

    @SuppressWarnings("unused")
    private final String conClassName = "JSCmdProcessClassRemove";

    public JSCmdProcessClassRemove(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }

    public void setProcessClassIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setProcessClass(value);
        }
    }
}
