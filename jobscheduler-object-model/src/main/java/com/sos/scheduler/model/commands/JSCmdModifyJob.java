package com.sos.scheduler.model.commands;

import org.apache.log4j.Logger;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdModifyJob extends ModifyJob {

    private final String conClassName = "JSCmdModifyJob";
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(JSCmdModifyJob.class);

    public static enum enu4Cmd {
        STOP, UNSTOP, START, WAKE, END, SUSPEND, CONTINUE, REMOVE

        /**/;

        public String Text() {
            String strT = this.name().toLowerCase();
            return strT;
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

    /** \brief setCmd
     * 
     * \details
     *
     * @param penuT */
    public void setCmd(enu4Cmd penuT) {

        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::setCmd";

        super.setCmd(penuT.Text());

    } // private void setCmd
}
