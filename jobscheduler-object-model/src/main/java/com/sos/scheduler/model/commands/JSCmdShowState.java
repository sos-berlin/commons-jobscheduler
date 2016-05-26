package com.sos.scheduler.model.commands;

import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.SchedulerObjectFactory.enu4What;
import com.sos.scheduler.model.answers.State;

public class JSCmdShowState extends ShowState {

    public JSCmdShowState(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }

    public State getState() {
        State objState = null;
        objState = this.getAnswer().getState();
        return objState;
    }

    public void setWhat(enu4What penuT) {
        super.setWhat(penuT.getText());
    }

    public void setWhat(enu4What[] penuT) {
        String strT = "";
        for (enu4What enuState4What : penuT) {
            strT += enuState4What.getText() + " ";
        }
        super.setWhat(strT);
    }

}