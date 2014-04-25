package com.sos.jobscheduler.tools.webservices;

import java.net.MalformedURLException;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.inject.Inject;

import com.sos.auth.rest.SOSShiroCurrentUserAnswer;
import com.sos.auth.rest.client.SOSRestShiroClient;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;
  
@Path("/commands")
public class SOSSecurityWebservice {
    
    private static final String PERMISSION_START_ORDER_PROMPT = "excecute add_order";
    private static final String PERMISSION_START_ORDER = "jobscheduler:joc:command:start:order";
    private static final String PERMISSION_START_JOB_PROMPT = "excecute start_job";
    private static final String PERMISSION_START_JOB = "jobscheduler:joc:command:start:job";
    private SchedulerXmlCommandExecutor xmlCommandExecutor;
    private static SOSShiroCurrentUserAnswer getSOSShiroCurrentUserAnswer=null;
    private static String user;
    private static String password;
    private static String resource;
     
    

    @Inject
    private SOSSecurityWebservice   (SchedulerXmlCommandExecutor xmlCommandExecutor_)   {
        xmlCommandExecutor = xmlCommandExecutor_;
     }
    
    @GET
    @Path("/test2")
    @Produces( {MediaType.TEXT_PLAIN})
    public String isAuthenticated() {

      

        return "hello world";

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

    private String checkAuthentication(String permission, String s1,String item ) throws MalformedURLException {
        if (getSOSShiroCurrentUserAnswer == null) {
            return "Please login";
        }

        if (!getSOSShiroCurrentUserAnswer.isAuthenticated()) {
            return String.format("User %s is not authenticated", user);
        }
         
        permission = permission + ":" + item;
        if (!isPermitted(permission)) {
            return String.format("User %s is not permitted to %s for %s. Missing permission: %s", user, s1,item,permission);
        }
         
       return "";
        
    }
    
    @GET
    @Path("/startjob")
    @Produces( {MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public String startJob(@QueryParam("job") String job) throws MalformedURLException {

        String s = checkAuthentication(PERMISSION_START_JOB,PERMISSION_START_JOB_PROMPT,job);
        if (s.equals("")) {
            xmlCommandExecutor.executeXml(String.format("<start_job job=\"%s\"/>",job));
            return String.format("job %s gestartet",job);
        }else {
            return s;
        }

    }
    
    
    @GET
    @Path("/startorder")
    @Produces( {MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public String startOrder(@QueryParam("job_chain") String jobChain, @QueryParam("order_id") String orderId) throws MalformedURLException {
        String order = String.format("%s(%s)",jobChain,orderId);
        String s = checkAuthentication(PERMISSION_START_ORDER,PERMISSION_START_ORDER_PROMPT,order);
        if (s.equals("")) {
            xmlCommandExecutor.executeXml(String.format("<add_order job_chain=\"%s\" id=\"%s\"/>",jobChain,orderId));
            return String.format("job chain %s gestartet with order %s",jobChain, orderId);
        }else {
            return s;
        }
    }
     
    @GET
    @Path("/login")
    @Produces( {MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public String login(@QueryParam("user") String user_,@QueryParam("password") String password_,@QueryParam("resource") String resource_)   {
        
        user = user_;
        password = password_;
        resource  = resource_;

        
        boolean authenticated;
        String sAuthenticated = String.format("user: %s, password: %s, resource: %s", user, password, resource);
        try {
            authenticated = authenticate();
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
            return sAuthenticated;
        }
        if (authenticated) {
            sAuthenticated = String.format("%s --> authenticated", sAuthenticated); 
        }else {
            sAuthenticated = String.format("%s --> Not authenticated", sAuthenticated); 
        }
        return  sAuthenticated;

    }

    @GET
    @Path("/logout")
    @Produces( {MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void login() {
        
        user = null;
        password = null;
        resource = null;
        getSOSShiroCurrentUserAnswer = null;

    }
 
    private boolean authenticate() throws MalformedURLException {
        SOSRestShiroClient sosRestShiroClient = new SOSRestShiroClient();
        String authenticateRessource = resource + "/authenticate" + "?user=%s&pwd=%s";
        getSOSShiroCurrentUserAnswer = sosRestShiroClient.getSOSShiroCurrentUserAnswer(user, password, "", authenticateRessource);
        return (getSOSShiroCurrentUserAnswer != null && getSOSShiroCurrentUserAnswer.isAuthenticated());
         
    }
     
    private boolean isPermitted(String permission) throws MalformedURLException {
        SOSRestShiroClient sosRestShiroClient = new SOSRestShiroClient();
        String permissionRessource = resource + "/permission" + "?user=%s&pwd=%s&permission=%s";
        getSOSShiroCurrentUserAnswer = sosRestShiroClient.getSOSShiroCurrentUserAnswer(user, password, permission, permissionRessource);
        return (getSOSShiroCurrentUserAnswer != null && getSOSShiroCurrentUserAnswer.isPermitted() && getSOSShiroCurrentUserAnswer.isAuthenticated());
         
    }

}