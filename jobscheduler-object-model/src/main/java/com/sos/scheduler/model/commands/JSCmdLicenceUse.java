package com.sos.scheduler.model.commands;

import org.apache.log4j.Logger;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdLicenceUse extends LicenceUse {

    @SuppressWarnings("unused")
    private final String conClassName = "JSCmdLicenceUse";
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(JSCmdLicenceUse.class);

    public JSCmdLicenceUse(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }
}
