package com.sos.scheduler.model.commands;

import org.apache.log4j.Logger;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdLockRemove extends LockRemove {

    @SuppressWarnings("unused")
    private final String conClassName = "JSCmdLockRemove";
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(JSCmdLockRemove.class);

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
