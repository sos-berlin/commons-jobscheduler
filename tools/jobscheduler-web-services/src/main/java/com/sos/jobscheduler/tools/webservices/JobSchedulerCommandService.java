package com.sos.jobscheduler.tools.webservices;

import com.google.inject.AbstractModule;

public class JobSchedulerCommandService extends AbstractModule{

  protected void  configure() {
       bind(MyWebservice.class);      
  }

}
