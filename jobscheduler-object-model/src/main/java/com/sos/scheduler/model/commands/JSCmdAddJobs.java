package com.sos.scheduler.model.commands;

import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.objects.JSObjJob;

public class JSCmdAddJobs extends AddJobs {

    private final String conClassName = "JSCmdAddJobs";

    public JSCmdAddJobs(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }

    public void add(JSObjJob pobjJob) {

        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::add";

        this.getJob().add(pobjJob);

    } // private void add

}
