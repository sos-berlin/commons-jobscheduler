package com.sos.jobscheduler.tools.webservices;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.inject.Inject;

import org.w3c.dom.Node;

import sos.xml.SOSXMLXPath;

import com.sos.auth.rest.SOSShiroCurrentUserAnswer;
import com.sos.auth.rest.SOSWebserviceAuthenticationRecord;
import com.sos.auth.rest.client.SOSRestShiroClient;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.commands.JSCmdAddOrder;
import com.sos.scheduler.model.commands.JSCmdJobChainModify;
import com.sos.scheduler.model.commands.JSCmdJobChainNodeModify;
import com.sos.scheduler.model.commands.JSCmdKillTask;
import com.sos.scheduler.model.commands.JSCmdLockRemove;
import com.sos.scheduler.model.commands.JSCmdModifyJob;
import com.sos.scheduler.model.commands.JSCmdModifyOrder;
import com.sos.scheduler.model.commands.JSCmdModifySpooler;
import com.sos.scheduler.model.commands.JSCmdProcessClassRemove;
import com.sos.scheduler.model.commands.JSCmdRemoveOrder;
import com.sos.scheduler.model.commands.JSCmdStartJob;
import com.sos.scheduler.model.commands.JSCmdTerminate;
import com.sos.scheduler.model.objects.JSObjLock;
import com.sos.scheduler.model.objects.JSObjProcessClass;
import com.sos.scheduler.model.objects.JSObjRunTime;
import com.sos.scheduler.model.objects.JobChainNodeAction;
import com.sos.scheduler.model.objects.Spooler;
import com.sun.jersey.guice.JerseyServletModule;

@Path("/plugin/security")
public class SOSSecurityWebservice {

    private static final String         PERMISSION_ADD_ORDER            = "jobscheduler:joc:command:add:order";
    private static final String         PERMISSION_ADD_PROCESS_CLASS    = "jobscheduler:joc:command:add:process_class";
    private static final String         PERMISSION_MODIFY_ORDER         = "jobscheduler:joc:command:modify:order";
    private static final String         PERMISSION_MODIFY_JOB           = "jobscheduler:joc:command:modify:job";
    private static final String         PERMISSION_MODIFY_SPOOLER       = "jobscheduler:joc:command:modify:spooler";
    private static final String         PERMISSION_MODIFY_JOBCHAIN      = "jobscheduler:joc:command:modify:job_chain";
    private static final String         PERMISSION_MODIFY_JOBCHAIN_NODE = "jobscheduler:joc:command:modify:job_chain_node";
    private static final String         PERMISSION_START_JOB            = "jobscheduler:joc:command:start:job";
    private static final String         PERMISSION_KILL_TASK            = "jobscheduler:joc:command:kill_task";
    private static final String         PERMISSION_LOCK                 = "jobscheduler:joc:command:lock";
    private static final String         PERMISSION_REMOVE_LOCK          = "jobscheduler:joc:command:remove:lock";
    private static final String         PERMISSION_REMOVE_PROCESS_CLASS = "jobscheduler:joc:command:remove:process_class";
    private static final String         PERMISSION_REMOVE_JOB_CHAIN     = "jobscheduler:joc:command:remove:job_chain";
    private static final String         PERMISSION_REMOVE_ORDER         = "jobscheduler:joc:command:remove:order";
    private static final String         PERMISSION_TERMINATE            = "jobscheduler:joc:command:terminate";
    private SchedulerXmlCommandExecutor xmlCommandExecutor;

    private static SOSWebserviceCurrentUsersList               currentUserList;

    @Inject
    private SOSSecurityWebservice(SchedulerXmlCommandExecutor xmlCommandExecutor_) {
        xmlCommandExecutor = xmlCommandExecutor_;
    }

    public SOSSecurityWebservice() {
        //For Junit Tests only
        xmlCommandExecutor = null;
    }

    public String executeXml(String cmd) {
        if (xmlCommandExecutor != null) {
           return  xmlCommandExecutor.executeXml(cmd);
        }
        return "xmlCommandExecutor is null";
    }
    
    private String getResource(String sessionId) {
        if (currentUserList != null) {
        SOSWebserviceCurrentUser c = currentUserList.getUser(sessionId);
            if (c != null){
               return c.getResource();
            }else {
                return "";
            }
            
        }else {
            return "";
        }
    }
    
    private String getResourceFromJobScheduler() {
        //TODO using Scheduler.model here
        String answer = executeXml("<params.get name=\"security_server\"/>");
        SOSXMLXPath xPath = new SOSXMLXPath(new StringBuffer(answer));
        Node n = xPath.selectSingleNode("" );
        String v =  n.getAttributes().getNamedItem(attribute).getNodeValue();

        
    }

    @GET
    @Path("/test2")
    @Produces({ MediaType.TEXT_PLAIN })
    public String test2() {

        return "hello world";

    }

    @GET
    @Path("/test")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public MyWebserviceAnswer test(
            @QueryParam("name")  String name, 
            @QueryParam("email") String email) {

        MyWebserviceAnswer m = new MyWebserviceAnswer("Uwe Risse");
        m.setTelephone("03086479034");
        m.setName(name);
        m.setEmail(email);
        return m;

    }

    private SOSSecurityWebserviceAnswer createAnswer(String jobSchedulerAnswer, String message, SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord) {
        String sessionId = "-";
        if (sosWebserviceAuthenticationRecord != null) {
            sessionId = sosWebserviceAuthenticationRecord.getSessionId();
        }
        SOSSecurityWebserviceAnswer m = new SOSSecurityWebserviceAnswer();
        m.setUser(sosWebserviceAuthenticationRecord.getUser());
        m.setResource(getResource(sessionId));
        m.setSessionId(sessionId);
        m.setMessage(message);
        m.setJobSchedulerAnswer(jobSchedulerAnswer);
        return m;
    }

   
    private String checkAuthentication(SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord, String item) throws MalformedURLException {
        
        sosWebserviceAuthenticationRecord.setResource(this.getResource(sosWebserviceAuthenticationRecord.getSessionId()));

        String permission = sosWebserviceAuthenticationRecord.getPermission() + ":" + item;
        permission = permission.replace("/", ":");
        sosWebserviceAuthenticationRecord.setPermission(permission);

        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = isPermitted(sosWebserviceAuthenticationRecord);

        if (sosShiroCurrentUserAnswer == null) {
            return "Please login";
        }

        if (!sosShiroCurrentUserAnswer.isAuthenticated()) {
            return String.format("User %s is not authenticated", sosWebserviceAuthenticationRecord.getUser());
        }

        boolean isPermitted = (sosShiroCurrentUserAnswer != null && sosShiroCurrentUserAnswer.isPermitted() && sosShiroCurrentUserAnswer.isAuthenticated());
        if (!isPermitted) {
            return String.format("User %s is not permitted. Missing permission: %s", sosWebserviceAuthenticationRecord.getUser(), permission);
        }

        return "";

    }

    private String[] getParams(String params) {
        if (params != null && params.length() > 0) {
            params = params.replaceAll("=", "|");
            String[] jobParams = params.split("\\|");
            return jobParams;
        }
        return null;
    }

    @POST
    @Path("/start_job")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSSecurityWebserviceAnswer startJob(
            @QueryParam("session_id")    String sessionId, 
            @QueryParam("job")           String job, 
            @QueryParam("at")            String at, 
            @QueryParam("force")         String force, 
            @QueryParam("name")          String name, 
            @QueryParam("params")        String params) 
                    throws MalformedURLException {

        SchedulerObjectFactory objFactory = new SchedulerObjectFactory();
        objFactory.initMarshaller(Spooler.class);
        JSCmdStartJob objStartJob = new JSCmdStartJob(objFactory);

        if (job != null) {
            objStartJob.setJob(job);
        }
        if (force != null && force.length() > 0) {
            objStartJob.setForce(force);
        }
        if (at != null && at.length() > 0) {
            objStartJob.setAt(at);
        }

        if (name != null && name.length() > 0) {
            objStartJob.setName(name);
        }

        String[] jobParams = getParams(params);
        if (jobParams != null) {
            objStartJob.setParams(jobParams);
        }

        String xml = objFactory.toXMLString(objStartJob);

        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = new SOSWebserviceAuthenticationRecord();
        sosWebserviceAuthenticationRecord.setResource(getResource(sessionId));
        sosWebserviceAuthenticationRecord.setSessionId(sessionId);
        sosWebserviceAuthenticationRecord.setPermission(PERMISSION_START_JOB);

        String s = checkAuthentication(sosWebserviceAuthenticationRecord, job);
        if (s.equals("")) {
            String answer = executeXml(xml);
            return createAnswer(answer,String.format("job %s gestartet", job), sosWebserviceAuthenticationRecord);
        }
        else {
            return createAnswer("",s, sosWebserviceAuthenticationRecord);
        }

    }

    @POST
    @Path("/modify_order")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSSecurityWebserviceAnswer modifyOrder(
            @QueryParam("session_id")   String sessionId, 
            @QueryParam("job_chain")    String jobChain, 
            @QueryParam("order")        String order, 
            @QueryParam("action")       String action, 
            @QueryParam("at")           String at, 
            @QueryParam("end_state")    String endState, 
            @QueryParam("priority")     String priority, 
            @QueryParam("setback")      String setback, 
            @QueryParam("state")        String state, 
            @QueryParam("suspended")    String suspended, 
            @QueryParam("title")        String title, 
            @QueryParam("params")       String params, 
            @QueryParam("runtime")      String runtime)

    throws MalformedURLException {

        String orderS = String.format("%s:%s", jobChain, order);

        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = new SOSWebserviceAuthenticationRecord();
        sosWebserviceAuthenticationRecord.setSessionId(sessionId);
        sosWebserviceAuthenticationRecord.setPermission(PERMISSION_MODIFY_ORDER);

        String s = checkAuthentication(sosWebserviceAuthenticationRecord, orderS);

        if (s.equals("")) {

            SchedulerObjectFactory objFactory = new SchedulerObjectFactory();
            objFactory.initMarshaller(Spooler.class);

            JSCmdModifyOrder objOrder = objFactory.createModifyOrder();

            if (jobChain != null && jobChain.length() > 0) {
                objOrder.setJobChain(jobChain);
            }
            if (order != null && order.length() > 0) {
                objOrder.setOrder(order);
            }
            if (action != null && action.length() > 0) {
                objOrder.setAction(action);
            }
            if (at != null && at.length() > 0) {
                objOrder.setAt(at);
            }
            if (endState != null && endState.length() > 0) {
                objOrder.setEndState(endState);
            }
            if (priority != null && priority.length() > 0) {
                BigInteger p = new BigInteger(priority);
                objOrder.setPriority(p);
            }
            if (setback != null && setback.length() > 0) {
                objOrder.setSetback(setback);
            }
            if (state != null && state.length() > 0) {
                objOrder.setState(state);
            }
            if (suspended != null && suspended.length() > 0) {
                objOrder.setSuspended(suspended);
            }
            if (title != null && title.length() > 0) {
                objOrder.setTitle(title);
            }

            String[] jobParams = getParams(params);
            if (jobParams != null) {
                // objOrder.setParams(jobParams);
            }

            if (runtime != null && runtime.length() > 0) {
                JSObjRunTime objRuntime = new JSObjRunTime(objFactory, runtime);
                objOrder.setRunTime(objRuntime);
            }

            String xml = objFactory.toXMLString(objOrder);
            String answer = executeXml(xml);

            return createAnswer(answer,String.format("job chain %s modfied with order %s", jobChain, order), sosWebserviceAuthenticationRecord);
        }
        else {
            return createAnswer("",s,sosWebserviceAuthenticationRecord);
        }
    }

    @POST
    @Path("/add_order")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSSecurityWebserviceAnswer addOrder(
            @QueryParam("session_id")   String sessionId, 
            @QueryParam("job_chain")    String jobChain, 
            @QueryParam("order")        String order, 
            @QueryParam("at")           String at, 
            @QueryParam("end_state")    String endState, 
            @QueryParam("priority")     String priority, 
            @QueryParam("replace")      String replace, 
            @QueryParam("state")        String state, 
            @QueryParam("title")        String title, 
            @QueryParam("web_service")  String webService, 
            @QueryParam("params")       String params, 
            @QueryParam("runtime")      String runtime) 
                    throws MalformedURLException {

        String orderS = String.format("%s:%s", jobChain, order);

        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = new SOSWebserviceAuthenticationRecord();
        sosWebserviceAuthenticationRecord.setSessionId(sessionId);
        sosWebserviceAuthenticationRecord.setPermission(PERMISSION_ADD_ORDER);

        String s = checkAuthentication(sosWebserviceAuthenticationRecord, orderS);
        if (s.equals("")) {

            SchedulerObjectFactory objFactory = new SchedulerObjectFactory();
            objFactory.initMarshaller(Spooler.class);
            JSCmdAddOrder objOrder = objFactory.createAddOrder();

            if (jobChain != null && jobChain.length() > 0) {
                objOrder.setJobChain(jobChain);
            }
            if (order != null && order.length() > 0) {
                objOrder.setId(order);
            }
            if (webService != null && webService.length() > 0) {
                objOrder.setWebService(webService);
            }
            if (at != null && at.length() > 0) {
                objOrder.setAt(at);
            }
            if (endState != null && endState.length() > 0) {
                objOrder.setEndState(endState);
            }
            if (priority != null && priority.length() > 0) {
                BigInteger p = new BigInteger(priority);
                objOrder.setPriority(p);
            }

            if (state != null && state.length() > 0) {
                objOrder.setState(state);
            }

            if (title != null && title.length() > 0) {
                objOrder.setTitle(title);
            }

            String[] orderParams = getParams(params);
            if (orderParams != null) {
                //objOrder.setParams(orderParams);
            }

            if (runtime != null && runtime.length() > 0) {
                JSObjRunTime objRuntime = new JSObjRunTime(objFactory, runtime);
                objOrder.setRunTime(objRuntime);
            }

            String xml = objFactory.toXMLString(objOrder);
            String answer = executeXml(xml);

            return createAnswer(answer,String.format("order %s added to job chain %s", order, jobChain),  sosWebserviceAuthenticationRecord);
        }
        else {
            return createAnswer("",s,  sosWebserviceAuthenticationRecord);
        }
    }

    @POST
    @Path("/job_chain_modify")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSSecurityWebserviceAnswer jobChainModify(
            @QueryParam("session_id") String sessionId, 
            @QueryParam("job_chain")  String jobChain, 
            @QueryParam("state")      String state) 
                    throws MalformedURLException {

        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = new SOSWebserviceAuthenticationRecord();
        sosWebserviceAuthenticationRecord.setSessionId(sessionId);
        sosWebserviceAuthenticationRecord.setPermission(PERMISSION_MODIFY_JOBCHAIN);

        String s = checkAuthentication(sosWebserviceAuthenticationRecord, jobChain);

        if (s.equals("")) {

            SchedulerObjectFactory objFactory = new SchedulerObjectFactory();
            objFactory.initMarshaller(Spooler.class);
            JSCmdJobChainModify objModifyJobChain = objFactory.createJobChainModify();

            if (jobChain != null && jobChain.length() > 0) {
                objModifyJobChain.setJobChain(jobChain);
            }

            if (state != null && state.length() > 0) {
                objModifyJobChain.setState(state);
            }

            String xml = objFactory.toXMLString(objModifyJobChain);
            String answer = executeXml(xml);

            return createAnswer(answer,String.format("job chain %s modified with state %s", jobChain, state), sosWebserviceAuthenticationRecord);
        }
        else {
            return createAnswer("",s, sosWebserviceAuthenticationRecord);
        }
    }

    @POST
    @Path("/job_chain_node_modify")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSSecurityWebserviceAnswer jobChainNodeModify(
            @QueryParam("session_id")   String sessionId, 
            @QueryParam("job_chain")    String jobChain, 
            @QueryParam("action")       String action, 
            @QueryParam("state")        String state) 
                    throws MalformedURLException {

        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = new SOSWebserviceAuthenticationRecord();
        sosWebserviceAuthenticationRecord.setSessionId(sessionId);
        sosWebserviceAuthenticationRecord.setPermission(PERMISSION_MODIFY_JOBCHAIN_NODE);

        String s = checkAuthentication(sosWebserviceAuthenticationRecord, jobChain);

        if (s.equals("")) {

            SchedulerObjectFactory objFactory = new SchedulerObjectFactory();
            objFactory.initMarshaller(Spooler.class);
            JSCmdJobChainNodeModify objModifyJobChainNode = objFactory.createJobChainNodeModify();

            JobChainNodeAction jobChainNodeAction = JobChainNodeAction.fromValue(action);

            if (jobChain != null && jobChain.length() > 0) {
                objModifyJobChainNode.setJobChain(jobChain);
            }

            if (state != null && state.length() > 0) {
                objModifyJobChainNode.setState(state);
            }

            if (action != null && action.length() > 0) {
                objModifyJobChainNode.setAction(jobChainNodeAction);
            }

            String xml = objFactory.toXMLString(objModifyJobChainNode);
            String answer = executeXml(xml);

            return createAnswer(answer, String.format("job chain %s modified with state %s", jobChain, state), sosWebserviceAuthenticationRecord);
        }
        else {
            return createAnswer("",s,  sosWebserviceAuthenticationRecord);
        }
    }

    @POST
    @Path("/kill_task")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSSecurityWebserviceAnswer killTask(
            @QueryParam("session_id")    String sessionId, 
            @QueryParam("id")            String id, 
            @QueryParam("immediately")   String immediately, 
            @QueryParam("job")           String job) 
                    throws MalformedURLException {

        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = new SOSWebserviceAuthenticationRecord();
        sosWebserviceAuthenticationRecord.setSessionId(sessionId);
        sosWebserviceAuthenticationRecord.setPermission(PERMISSION_KILL_TASK);

        String s = checkAuthentication(sosWebserviceAuthenticationRecord, job);

        if (s.equals("")) {

            SchedulerObjectFactory objFactory = new SchedulerObjectFactory();
            objFactory.initMarshaller(Spooler.class);
            JSCmdKillTask objKillTask = objFactory.createKillTask();

            if (id != null && id.length() > 0) {
                BigInteger b = new BigInteger(id);
                objKillTask.setId(b);
            }

            if (job != null && job.length() > 0) {
                objKillTask.setJob(job);
            }

            if (immediately != null && immediately.length() > 0) {
                objKillTask.setImmediately(immediately);
            }

            String xml = objFactory.toXMLString(objKillTask);
            String answer = executeXml(xml);

            return createAnswer(answer, String.format("job %s with id: %s killed", job, id), sosWebserviceAuthenticationRecord);
        }
        else {
            return createAnswer("",s, sosWebserviceAuthenticationRecord);
        }

    }

    @POST
    @Path("/modify_job")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSSecurityWebserviceAnswer modifyJob(
            @QueryParam("session_id")    String sessionId, 
            @QueryParam("job")           String job, 
            @QueryParam("cmd")           String cmd) 
                    throws MalformedURLException {

        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = new SOSWebserviceAuthenticationRecord();
        sosWebserviceAuthenticationRecord.setSessionId(sessionId);
        sosWebserviceAuthenticationRecord.setPermission(PERMISSION_MODIFY_JOB);

        String s = checkAuthentication(sosWebserviceAuthenticationRecord, cmd + ":" + job);

        if (s.equals("")) {

            SchedulerObjectFactory objFactory = new SchedulerObjectFactory();
            objFactory.initMarshaller(Spooler.class);
            JSCmdModifyJob objModifyJob = objFactory.createModifyJob();

            if (cmd != null && cmd.length() > 0) {
                objModifyJob.setCmd(cmd);
            }

            if (job != null && job.length() > 0) {
                objModifyJob.setJob(job);
            }

            String xml = objFactory.toXMLString(objModifyJob);

            String answer = executeXml(xml);
            return createAnswer(answer,String.format("job %s modified with cmd: %s", job, cmd), sosWebserviceAuthenticationRecord);
        }
        else {
            return createAnswer("",s, sosWebserviceAuthenticationRecord);
        }

    }

    @POST
    @Path("/modify_spooler")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSSecurityWebserviceAnswer modifySpooler(
            @QueryParam("session_id")    String sessionId, 
            @QueryParam("cmd")           String cmd, 
            @QueryParam("timeout")       String timeout) 
                    throws MalformedURLException {

        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = new SOSWebserviceAuthenticationRecord();
        sosWebserviceAuthenticationRecord.setSessionId(sessionId);
        sosWebserviceAuthenticationRecord.setPermission(PERMISSION_MODIFY_SPOOLER);

        String s = checkAuthentication(sosWebserviceAuthenticationRecord, cmd);

        if (s.equals("")) {

            SchedulerObjectFactory objFactory = new SchedulerObjectFactory();
            objFactory.initMarshaller(Spooler.class);
            JSCmdModifySpooler objModifySpooler = objFactory.createModifySpooler();

            if (cmd != null && cmd.length() > 0) {
                objModifySpooler.setCmd(cmd);
            }

            if (timeout != null && timeout.length() > 0) {
                BigInteger b = new BigInteger(timeout);
                objModifySpooler.setTimeout(b);
            }

            String xml = objFactory.toXMLString(objModifySpooler);

            String answer = executeXml(xml);
            return createAnswer(answer,String.format("JobScheduler modified with cmd: %s", cmd), sosWebserviceAuthenticationRecord);
        }
        else {
            return createAnswer("",s, sosWebserviceAuthenticationRecord);
        }

    }

    @POST
    @Path("/process_class")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSSecurityWebserviceAnswer processClass(
            @QueryParam("session_id")          String sessionId, 
            @QueryParam("scheduler_id")        String schedulerId, 
            @QueryParam("name")                String name, 
            @QueryParam("remote_scheduler")    String remoteScheduler, 
            @QueryParam("replace")             String replace, 
            @QueryParam("max_processes")       String maxProcesses) 
                    throws MalformedURLException {

        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = new SOSWebserviceAuthenticationRecord();
        sosWebserviceAuthenticationRecord.setSessionId(sessionId);
        sosWebserviceAuthenticationRecord.setPermission(PERMISSION_ADD_PROCESS_CLASS);

        String s = checkAuthentication(sosWebserviceAuthenticationRecord, name);

        if (s.equals("")) {

            SchedulerObjectFactory objFactory = new SchedulerObjectFactory();
            objFactory.initMarshaller(Spooler.class);
            JSObjProcessClass objProcessClass = objFactory.createProcessClass();

            if (schedulerId != null && schedulerId.length() > 0) {
                objProcessClass.setSpoolerId(schedulerId);
            }

            if (name != null && name.length() > 0) {
                objProcessClass.setName(name);
            }

            if (remoteScheduler != null && remoteScheduler.length() > 0) {
                objProcessClass.setRemoteScheduler(remoteScheduler);
            }

            if (replace != null && replace.length() > 0) {
                objProcessClass.setReplace(replace);
            }

            if (maxProcesses != null && maxProcesses.length() > 0) {
                try {
                    objProcessClass.setMaxProcesses(Integer.valueOf(maxProcesses));
                }
                catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            String xml = objFactory.toXMLString(objProcessClass);
            String answer = executeXml(xml);
            return createAnswer(answer,String.format("Process %s class modified", name), sosWebserviceAuthenticationRecord);
        }
        else {
            return createAnswer("",s, sosWebserviceAuthenticationRecord);
        }
    }

    @POST
    @Path("/process_class_remove")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSSecurityWebserviceAnswer processClassRemove(
            @QueryParam("session_id")       String sessionId, 
            @QueryParam("process_class")    String processClass) 
                    throws MalformedURLException {

        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = new SOSWebserviceAuthenticationRecord();
        sosWebserviceAuthenticationRecord.setSessionId(sessionId);
        sosWebserviceAuthenticationRecord.setPermission(PERMISSION_REMOVE_PROCESS_CLASS);

        String s = checkAuthentication(sosWebserviceAuthenticationRecord, processClass);

        if (s.equals("")) {

            SchedulerObjectFactory objFactory = new SchedulerObjectFactory();
            objFactory.initMarshaller(Spooler.class);
            JSCmdProcessClassRemove objProcessClass = objFactory.createProcessClassRemove();

            if (processClass != null && processClass.length() > 0) {
                objProcessClass.setProcessClass(processClass);
            }

            //String xml = objFactory.toXMLString(objProcessClass);
            //TODO Scheduler Modell
            String xml = String.format("<process_class.remove process_class=\"%s\"/>", processClass);

            String answer = executeXml(xml);
            return createAnswer(answer,String.format("Process class %s removed", processClass), sosWebserviceAuthenticationRecord);
        }
        else {
            return createAnswer("",s, sosWebserviceAuthenticationRecord);
        }

    }

    @POST
    @Path("/remove_job_chain")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSSecurityWebserviceAnswer jobChainRemove(
            @QueryParam("session_id")    String sessionId, 
            @QueryParam("job_chain")     String jobChain) 
                    throws MalformedURLException {
        //TODO adding command to scheduler_model

        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = new SOSWebserviceAuthenticationRecord();
        sosWebserviceAuthenticationRecord.setSessionId(sessionId);
        sosWebserviceAuthenticationRecord.setPermission(PERMISSION_REMOVE_JOB_CHAIN);

        String s = checkAuthentication(sosWebserviceAuthenticationRecord, jobChain);

        if (s.equals("")) {

            String xml = String.format("remove_job_chain job_chain=\"%s\"", jobChain);

            String answer = executeXml(xml);
            return createAnswer(answer,String.format("Job chain %s removed", jobChain), sosWebserviceAuthenticationRecord);
        }
        else {
            return createAnswer("",s, sosWebserviceAuthenticationRecord);
        }

    }

    @POST
    @Path("/remove_order")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSSecurityWebserviceAnswer orderRemove(
            @QueryParam("session_id")    String sessionId, 
            @QueryParam("job_chain")     String jobChain, 
            @QueryParam("order")         String order) 
                    throws MalformedURLException {

        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = new SOSWebserviceAuthenticationRecord();
        sosWebserviceAuthenticationRecord.setSessionId(sessionId);
        sosWebserviceAuthenticationRecord.setPermission(PERMISSION_REMOVE_ORDER);

        String s = checkAuthentication(sosWebserviceAuthenticationRecord, jobChain + ":" + order);

        if (s.equals("")) {

            SchedulerObjectFactory objFactory = new SchedulerObjectFactory();
            objFactory.initMarshaller(Spooler.class);
            JSCmdRemoveOrder objRemoveOrder = objFactory.createRemoveOrder();

            if (jobChain != null && jobChain.length() > 0) {
                objRemoveOrder.setJobChain(jobChain);
            }

            if (order != null && order.length() > 0) {
                objRemoveOrder.setOrder(order);
            }

            String xml = objFactory.toXMLString(objRemoveOrder);
            executeXml(xml);

            String answer = executeXml(xml);
            return createAnswer(answer,String.format("Order %s removed from job chain %s", order, jobChain), sosWebserviceAuthenticationRecord);
        }
        else {
            return createAnswer("",s, sosWebserviceAuthenticationRecord);
        }

    }

    @POST
    @Path("/lock")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSSecurityWebserviceAnswer lock(
            @QueryParam("session_id")           String sessionId, 
            @QueryParam("max_non_exclusive")    String maxNonExclusive, 
            @QueryParam("name")                 String name) 
                    throws MalformedURLException {

        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = new SOSWebserviceAuthenticationRecord();
        sosWebserviceAuthenticationRecord.setSessionId(sessionId);
        sosWebserviceAuthenticationRecord.setPermission(PERMISSION_LOCK);

        String s = checkAuthentication(sosWebserviceAuthenticationRecord, name);

        if (s.equals("")) {

            SchedulerObjectFactory objFactory = new SchedulerObjectFactory();
            objFactory.initMarshaller(Spooler.class);
            JSObjLock objLock = objFactory.createLock();

            if (maxNonExclusive != null && maxNonExclusive.length() > 0) {
                objLock.setMaxNonExclusive(Integer.valueOf(maxNonExclusive));
            }

            if (name != null && name.length() > 0) {
                objLock.setName(name);
            }

            String xml = objFactory.toXMLString(objLock);

            String answer = executeXml(xml);
            return createAnswer(answer,String.format("lock %s created", name), sosWebserviceAuthenticationRecord);
        }
        else {
            return createAnswer("",s, sosWebserviceAuthenticationRecord);
        }

    }

    @POST
    @Path("/lock_remove")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSSecurityWebserviceAnswer lockRemove(
            @QueryParam("session_id")    String sessionId, 
            @QueryParam("lock")          String lock) 
                    throws MalformedURLException {

        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = new SOSWebserviceAuthenticationRecord();
        sosWebserviceAuthenticationRecord.setSessionId(sessionId);
        sosWebserviceAuthenticationRecord.setPermission(PERMISSION_REMOVE_LOCK);

        String s = checkAuthentication(sosWebserviceAuthenticationRecord, lock);

        if (s.equals("")) {

            SchedulerObjectFactory objFactory = new SchedulerObjectFactory();
            objFactory.initMarshaller(Spooler.class);
            JSCmdLockRemove objLock = objFactory.createLockRemove();

            if (lock != null && lock.length() > 0) {
                objLock.setLock(lock);
            }

            //String xml = objFactory.toXMLString(objLock);
            // TODO: toXMLString does not work
            String xml = String.format("lock.remove lock=\"%s\"", lock);
            String answer = executeXml(xml);
            return createAnswer(answer,String.format("lock %s removed", lock), sosWebserviceAuthenticationRecord);
        }
        else {
            return createAnswer("",s, sosWebserviceAuthenticationRecord);
        }

    }

    @POST
    @Path("/terminate")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSSecurityWebserviceAnswer terminate(
            @QueryParam("session_id")                   String sessionId, 
            @QueryParam("all_schedulers")               String allSchedulers, 
            @QueryParam("continue_exclusive_operation") String continueExclusiveOperation, 
            @QueryParam("restart")                      String restart, 
            @QueryParam("timeout")                      String timeout) 
                    throws MalformedURLException {

        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = new SOSWebserviceAuthenticationRecord();
        sosWebserviceAuthenticationRecord.setSessionId(sessionId);
        sosWebserviceAuthenticationRecord.setPermission(PERMISSION_TERMINATE);

        String s = checkAuthentication(sosWebserviceAuthenticationRecord, "");

        if (s.equals("")) {
            SchedulerObjectFactory objFactory = new SchedulerObjectFactory();
            objFactory.initMarshaller(Spooler.class);
            JSCmdTerminate objTerminate = new JSCmdTerminate(objFactory);

            if (allSchedulers != null && allSchedulers.length() > 0) {
                objTerminate.setAllSchedulers(allSchedulers);
            }
            if (continueExclusiveOperation != null && continueExclusiveOperation.length() > 0) {
                objTerminate.setContinueExclusiveOperation(continueExclusiveOperation);
            }
            if (restart != null && restart.length() > 0) {
                objTerminate.setRestart(restart);
            }
            if (timeout != null && timeout.length() > 0) {
                BigInteger t = new BigInteger(timeout);
                objTerminate.setTimeout(t);
            }

            String xml = objFactory.toXMLString(objTerminate);

            String answer = executeXml(xml);
            return createAnswer(answer,"JoBScheduler terminated", sosWebserviceAuthenticationRecord);
        }
        else {
            return createAnswer("",s, sosWebserviceAuthenticationRecord);
        }

    }

    @POST
    @Path("/login")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSSecurityWebserviceAnswer login(
            @QueryParam("user")      String user, 
            @QueryParam("password")  String password, 
 
        if (currentUserList == null) {
            currentUserList = new SOSWebserviceCurrentUsersList();
        }
        
        String resource = getResourceFromJobScheduler();
        String sAuthenticated = String.format("user: %s, password: %s, resource: %s", user, password, resource);
        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = null;
        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = new SOSWebserviceAuthenticationRecord();
        try {

            sosWebserviceAuthenticationRecord.setUser(user);
            sosWebserviceAuthenticationRecord.setPassword(password);
            sosWebserviceAuthenticationRecord.setResource(resource);

            sosShiroCurrentUserAnswer = authenticate(sosWebserviceAuthenticationRecord);
            sosWebserviceAuthenticationRecord.setSessionId(sosShiroCurrentUserAnswer.getSessionId());

            SOSWebserviceCurrentUser c = new SOSWebserviceCurrentUser();
            c.setSessionId(sosShiroCurrentUserAnswer.getSessionId());
            c.setPassword(password);
            c.setUsername(user);
            c.setResource(resource);
            currentUserList.addUser(c);

        
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
            return createAnswer("",sAuthenticated, sosWebserviceAuthenticationRecord);
        }
        if (sosShiroCurrentUserAnswer != null && sosShiroCurrentUserAnswer.getIsAuthenticated()) {
            sAuthenticated = String.format("%s --> authenticated", sAuthenticated);
        }
        else {
            sAuthenticated = String.format("%s --> Not authenticated", sAuthenticated);
        }

        return createAnswer("",sAuthenticated, sosWebserviceAuthenticationRecord);

    }

    @POST
    @Path("/logout")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSSecurityWebserviceAnswer logout(
            @QueryParam("session_id")  String sessionId             
                    ) throws MalformedURLException {
 

        
        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = new SOSWebserviceAuthenticationRecord();
        if (currentUserList != null) {
            currentUserList.removeUser(sessionId);
        }

        SOSRestShiroClient sosRestShiroClient = new SOSRestShiroClient();
        String resource = sosWebserviceAuthenticationRecord.getResource() + "/logout" + "?session_id=%s";
        sosWebserviceAuthenticationRecord.setResource(resource);

        sosRestShiroClient.getSOSShiroCurrentUserAnswer(new URL(String.format(sosWebserviceAuthenticationRecord.getResource(),sosWebserviceAuthenticationRecord.getSessionId())));
        SOSSecurityWebserviceAnswer message = createAnswer("",String.format("%s --> Abgemeldet", sosWebserviceAuthenticationRecord.getSessionId()), sosWebserviceAuthenticationRecord);
 
        return message;
    }

    private SOSShiroCurrentUserAnswer authenticate(SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord) throws MalformedURLException {
        SOSRestShiroClient sosRestShiroClient = new SOSRestShiroClient();
        String authenticateRessource = sosWebserviceAuthenticationRecord.getResource() + "/authenticate" + "?user=%s&pwd=%s";
        sosWebserviceAuthenticationRecord.setResource(authenticateRessource);

        return sosRestShiroClient.getSOSShiroCurrentUserAnswer(sosWebserviceAuthenticationRecord);
    }

    private SOSShiroCurrentUserAnswer isPermitted(SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord) throws MalformedURLException {
        SOSRestShiroClient sosRestShiroClient = new SOSRestShiroClient();
        String permissionRessource = sosWebserviceAuthenticationRecord.getResource() + "/permission" + "?user=%s&pwd=%s&permission=%s&session_id=%s";
        sosWebserviceAuthenticationRecord.setResource(permissionRessource);

        SOSShiroCurrentUserAnswer getSOSShiroCurrentUserAnswer = sosRestShiroClient.getSOSShiroCurrentUserAnswer(sosWebserviceAuthenticationRecord);
        return getSOSShiroCurrentUserAnswer;

    }

}