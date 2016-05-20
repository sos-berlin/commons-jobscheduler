package com.sos.scheduler.model.commands;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdModifyJob extends ModifyJob {

    public static enum enu4Cmd {
        STOP, UNSTOP, START, WAKE, END, SUSPEND, CONTINUE, REMOVE;

        public String getText() {
            return this.name().toLowerCase();
        }
    }

    public JSCmdModifyJob(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }

    public void setCmdIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setCmd(value);
        }
    }

    public void setJobIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setJob(value);
        }
    }

    public void setCmd(enu4Cmd penuT) {
        super.setCmd(penuT.getText());
    }

}