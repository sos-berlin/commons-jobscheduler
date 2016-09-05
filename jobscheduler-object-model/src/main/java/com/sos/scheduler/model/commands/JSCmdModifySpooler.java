package com.sos.scheduler.model.commands;

import java.math.BigInteger;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdModifySpooler extends ModifySpooler {

    public static enum enu4Cmd {
        PAUSE, CONTINUE, RELOAD, TERMINATE, TERMINATE_AND_RESTART, LET_RUN_TERMINATE_AND_RESTART, ABORT_IMMEDIATELY, ABORT_IMMEDIATELY_AND_RESTART;

        public String getText() {
            return this.name().toLowerCase();
        }
    }

    public JSCmdModifySpooler(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }

    public void setCmdIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setCmd(value);
        }
    }

    public void setTimeoutIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            BigInteger b = new BigInteger(value);
            super.setTimeout(b);
        }
    }
    public void setTimeoutIfNotEmpty(Integer value) {
        if (value != null) {
            BigInteger b =  BigInteger.valueOf(value.intValue());
            super.setTimeout(b);
        }
    }

}