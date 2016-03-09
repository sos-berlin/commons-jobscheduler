package com.sos.scheduler.model.commands;

import org.apache.log4j.Logger;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdJobChainModify extends JobChainModify {

    private final String conClassName = "JSCmdJobChainModify";
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(JSCmdJobChainModify.class);

    public static enum enu4State {
        STOPPED, RUNNING

        /**/;

        public String Text() {
            String strT = this.name().toLowerCase();
            return strT;
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

    /** \brief setState
     * 
     * \details
     *
     * @param penuT */
    public void setState(enu4State penuT) {

        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::setState";

        super.setState(penuT.Text());

    } // private void setState
}
