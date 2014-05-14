package com.sos.jobscheduler.tools.webservices;

import com.google.inject.AbstractModule;

public class JobSchedulerCommandSecurityService extends AbstractModule{

  protected void  configure() {
       bind(SOSCommandSecurityWebservice.class);      
  }

}
