package com.sos.scheduler.model.commands;

import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.SchedulerObjectFactory.enu4What;

public class JSCmdShowTask extends ShowTask {

    private final String conClassName = "JSCmdShowTask";

    public JSCmdShowTask(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }

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
