package com.sos.scheduler.model.commands;

import org.apache.log4j.Logger;

import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.SchedulerObjectFactory.enu4What;
import com.sos.scheduler.model.answers.State;

public class JSCmdShowState extends ShowState {

    private final String conClassName = "JSCmdShowState";
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(JSCmdShowState.class);

    public JSCmdShowState(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }

    public State getState() {

        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::getState";
        State objState = null;
        objState = this.getAnswer().getState();
        // objState.setParent(objFactory);
        return objState;
    } // private State getState

    /** \brief setWhat
     * 
     * \details
     *
     * \return void
     *
     * @param penuT */
    public void setWhat(enu4What penuT) {

        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::setWhat";

        super.setWhat(penuT.getText());

    } // private void setWhat

    /** \brief setWhat
     * 
     * \details
     *
     * \return void
     *
     * @param penuT */
    public void setWhat(enu4What[] penuT) {

        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::setWhat";

        String strT = "";
        for (enu4What enuState4What : penuT) {
            strT += enuState4What.getText() + " ";
        }
        super.setWhat(strT);

    } // private JSCmdShowTask setWhat

}
