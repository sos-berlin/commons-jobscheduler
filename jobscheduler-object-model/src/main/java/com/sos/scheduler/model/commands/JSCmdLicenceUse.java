package com.sos.scheduler.model.commands;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdLicenceUse extends LicenceUse {

    @SuppressWarnings("unused")
    private final String conClassName = "JSCmdLicenceUse";

    public JSCmdLicenceUse(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }
}
