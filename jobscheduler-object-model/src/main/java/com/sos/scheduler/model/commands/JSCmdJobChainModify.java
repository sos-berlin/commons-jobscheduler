package com.sos.scheduler.model.commands;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdJobChainModify extends JobChainModify {

    public static enum enu4State {
        STOPPED, RUNNING;

        public String getText() {
            return this.name().toLowerCase();
        }
    }

    public JSCmdJobChainModify(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }

    public void setJobChainIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setJobChain(value);
        }
    }

    public void setStateIfNotEmpte(String value) {
        if (!isEmpty(value)) {
            super.setState(value);
        }
    }

    public void setState(enu4State penuT) {
        super.setState(penuT.getText());
    }

}