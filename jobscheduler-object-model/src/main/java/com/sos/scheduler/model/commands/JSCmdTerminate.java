package com.sos.scheduler.model.commands;

import java.math.BigInteger;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdTerminate extends Terminate {

    @SuppressWarnings("unused")
    private final String conClassName = "JSCmdTerminate";

    public JSCmdTerminate(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }

    public void setAllSchedulersIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setAllSchedulers(value);
        }
    }

    public void setContinueExclusiveOperationIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setContinueExclusiveOperation(value);
        }
    }

    public void setRestartIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setRestart(value);
        }
    }

    public void setTimeoutIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setTimeout(new BigInteger(value));
        }
    }
    public void setTimeoutIfNotEmpty(Integer value) {
        if (value != null) {
            super.setTimeout( BigInteger.valueOf(value.intValue()));
        }
    }
}
