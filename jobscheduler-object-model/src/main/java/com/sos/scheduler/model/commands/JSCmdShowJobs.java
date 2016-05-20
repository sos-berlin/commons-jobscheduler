package com.sos.scheduler.model.commands;

import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.SchedulerObjectFactory.enu4What;

public class JSCmdShowJobs extends ShowJobs {

    public JSCmdShowJobs(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }

    public com.sos.scheduler.model.answers.Jobs getJobs() {
        com.sos.scheduler.model.answers.Jobs objJobs = null;
        objJobs = this.getAnswer().getJobs();
        return objJobs;
    }

    public void setWhat(enu4What penuT) {
        super.setWhat(penuT.getText());
    }

    public void setWhat(enu4What... penuT) {
        String strT = "";
        for (enu4What enuState4What : penuT) {
            strT += enuState4What.getText() + " ";
        }
        super.setWhat(strT);
    }

}