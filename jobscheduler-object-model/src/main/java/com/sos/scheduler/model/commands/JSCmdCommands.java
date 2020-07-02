package com.sos.scheduler.model.commands;

import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.objects.JSObjCommands;

public class JSCmdCommands extends JSObjCommands {

    @SuppressWarnings("unused")
    private final String conClassName = "JSCmdCommands";

    public JSCmdCommands(SchedulerObjectFactory schedulerObjectFactory) {
        super(schedulerObjectFactory);
        objFactory = schedulerObjectFactory;

    }

}
