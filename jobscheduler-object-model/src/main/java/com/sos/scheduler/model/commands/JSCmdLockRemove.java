package com.sos.scheduler.model.commands;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdLockRemove extends LockRemove {

    @SuppressWarnings("unused")
    private final String conClassName = "JSCmdLockRemove";

    public JSCmdLockRemove(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }

    public void setLockIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setLock(value);
        }
    }

}
