package com.sos.jobscheduler.tools.webservices;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.inject.Inject;

import com.sos.scheduler.engine.kernel.plugin.AbstractPlugin;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;
  
@Path("/commands")
public class MyWebservice {
    
    private SchedulerXmlCommandExecutor xmlCommandExecutor;

    @Inject
    private MyWebservice   (SchedulerXmlCommandExecutor xmlCommandExecutor_)   {
        xmlCommandExecutor = xmlCommandExecutor_;
     }
    
    @GET
    @Path("/test2")
    @Produces( {MediaType.TEXT_PLAIN})
    public String isAuthenticated() {

      

        return "hello";

    }

    @GET
    @Path("/test")
    @Produces( {MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public MyWebserviceAnswer isAuthenticated(@QueryParam("name") String name, @QueryParam("email") String email) {

         
        MyWebserviceAnswer m = new MyWebserviceAnswer("Uwe Risse");
        m.setTelephone("03086479034");
        m.setName(name);
        m.setEmail(email);
       // xmlCommandExecutor.executeXml("<start_job job=\"test\"/>");
        return m;

    }

     

}