package com.sos.scheduler.model.commands;

import java.math.BigInteger;

import org.apache.log4j.Logger;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdKillTask extends KillTask {

    @SuppressWarnings("unused")
    private final String conClassName = "JSCmdKillTask";
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(JSCmdKillTask.class);

    public JSCmdKillTask(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }

    public void setJobIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setJob(value);
        }
    }

    public void setTimeoutIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setTimeout(value);
        }
    }

    public void setTimeout(Integer value) {
        if (value != null) {
            super.setTimeout(String.valueOf(value));
        }
    }

     
    
    public void setImmediatelyIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setImmediately(value);
        }
    }

    public void setIdIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            BigInteger b = new BigInteger(value);
            super.setId(b);
        }
    }

    public void setIdIfNotEmpty(Integer value) {
        if (value != null) {
             super.setId(BigInteger.valueOf(value));
        }
    }
}
