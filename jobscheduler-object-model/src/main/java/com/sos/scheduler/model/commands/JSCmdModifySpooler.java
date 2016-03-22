package com.sos.scheduler.model.commands;

import java.math.BigInteger;

import org.apache.log4j.Logger;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdModifySpooler extends ModifySpooler {

    @SuppressWarnings("unused")
    private final String conClassName = "JSCmdModifySpooler";
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(JSCmdModifySpooler.class);

    public static enum enu4Cmd {
        PAUSE, CONTINUE, RELOAD, TERMINATE, TERMINATE_AND_RESTART, LET_RUN_TERMINATE_AND_RESTART, ABORT_IMMEDIATELY, ABORT_IMMEDIATELY_AND_RESTART

        /**/;

        public String Text() {
            String strT = this.name().toLowerCase();
            return strT;
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

}
