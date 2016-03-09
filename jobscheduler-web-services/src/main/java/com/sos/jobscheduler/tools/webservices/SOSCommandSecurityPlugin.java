package com.sos.jobscheduler.tools.webservices;

import com.sos.scheduler.engine.kernel.plugin.AbstractPlugin;
import com.sos.scheduler.engine.kernel.plugin.UseGuiceModule;

@UseGuiceModule(JobSchedulerCommandSecurityService.class)
public class SOSCommandSecurityPlugin extends AbstractPlugin {

}