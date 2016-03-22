package com.sos.jobscheduler.tools.webservices;

import java.net.MalformedURLException;
import java.net.URL;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import sos.xml.SOSXMLXPath;

import com.sos.auth.rest.SOSShiroCurrentUserAnswer;
import com.sos.auth.rest.SOSWebserviceAuthenticationRecord;
import com.sos.auth.rest.client.SOSRestShiroClient;
import com.sos.jobscheduler.tools.webservices.globals.MyWebserviceAnswer;
import com.sos.jobscheduler.tools.webservices.globals.SOSCommandSecurityWebserviceAnswer;
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
import com.sos.scheduler.model.objects.JSObjJobChain;
import com.sos.scheduler.model.objects.JSObjLock;
import com.sos.scheduler.model.objects.JSObjProcessClass;
import com.sos.scheduler.model.objects.JSObjRunTime;
import com.sos.scheduler.model.objects.Spooler;

@Path("/plugin/security")
public class SOSCommandSecurityWebservice {

    private static final String PERMISSION_COMMAND = "/permission" + "?user=%s&pwd=%s&permission=%s&session_id=%s";
    private static final String AUTHENTICATE_COMMAND = "/authenticate" + "?user=%s&pwd=%s";
    private static final String JOBSCHEDULER_REST_SOS_PERMISSION = "/jobscheduler/rest/sosPermission";
    private static final String TRUE = "true";
    private static final String PERMISSION_LOGOUT = "sos:products";
    private static final String SECURITY_SERVER_ADDRESS = "security_server_address";
    private static final String SECURITY_SERVER_IS_ENABLED = "security_server_enabled";
    private static final String GET_PARAMETER = "<param.get name=\"%s\"/>";
    private static final String PERMISSION_ADD_ORDER = "sos:products:joc:command:add:order";
    private static final String PERMISSION_ADD_PROCESS_CLASS = "sos:products:joc:command:add:process_class";
    private static final String PERMISSION_ADD_JOB_CHAIN = "sos:products:joc:command:add:job_chain";
    private static final String PERMISSION_MODIFY_ORDER = "sos:products:joc:command:modify:order";
    private static final String PERMISSION_MODIFY_JOB = "sos:products:joc:command:modify:job";
    private static final String PERMISSION_MODIFY_SPOOLER = "sos:products:joc:command:modify:spooler";
    private static final String PERMISSION_MODIFY_JOBCHAIN = "sos:products:joc:command:modify:job_chain";
    private static final String PERMISSION_MODIFY_JOBCHAIN_NODE = "sos:products:joc:command:modify:job_chain_node";
    private static final String PERMISSION_START_JOB = "sos:products:joc:command:start:job";
    private static final String PERMISSION_KILL_TASK = "sos:products:joc:command:kill_task";
    private static final String PERMISSION_LOCK = "sos:products:joc:command:add:lock";
    private static final String PERMISSION_REMOVE_LOCK = "sos:products:joc:command:remove:lock";
    private static final String PERMISSION_REMOVE_PROCESS_CLASS = "sos:products:joc:command:remove:process_class";
    private static final String PERMISSION_REMOVE_JOB_CHAIN = "sos:products:joc:command:remove:job_chain";
    private static final String PERMISSION_REMOVE_ORDER = "sos:products:joc:command:remove:order";
    private static final String PERMISSION_TERMINATE = "sos:products:joc:command:terminate";
    private static final Logger LOGGER = Logger.getLogger(SOSCommandSecurityWebservice.class);
    private static SOSCommandSecurityWebserviceCurrentUsersList currentUserList;
    private SchedulerXmlCommandExecutor xmlCommandExecutor;

    @Inject
    private SOSCommandSecurityWebservice(SchedulerXmlCommandExecutor xmlCommandExecutor_) {
        xmlCommandExecutor = xmlCommandExecutor_;
    }

    public SOSCommandSecurityWebservice() {
        xmlCommandExecutor = null;
    }

    public String executeXml(String cmd) {
        if (xmlCommandExecutor != null) {
            return xmlCommandExecutor.executeXml(cmd);
        }
        return "xmlCommandExecutor is null";
    }

    private SOSCommandSecurityWebserviceCurrentUser getCurrentUser(String sessionId) {
        if (currentUserList != null) {
            return currentUserList.getUser(sessionId);
        } else {
            return null;
        }

    }

    private String getResource(String sessionId) {
        SOSCommandSecurityWebserviceCurrentUser c = getCurrentUser(sessionId);
        if (c != null) {
            return c.getResource();
        } else {
            return "";
        }
    }

    private String getParamFromJobScheduler(String param) {
        if (xmlCommandExecutor != null) {
            String answer = executeXml(String.format(GET_PARAMETER, param));
            SOSXMLXPath xPath;
            try {
                xPath = new SOSXMLXPath(new StringBuffer(answer));
                Node n = xPath.selectSingleNode("/spooler/answer/param");
                String v = n.getAttributes().getNamedItem("value").getNodeValue();
                return v;
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return "";
    }

    private String getResourceFromJobScheduler() {
        return getParamFromJobScheduler(SECURITY_SERVER_ADDRESS) + JOBSCHEDULER_REST_SOS_PERMISSION;
    }

    private boolean getIsEnabled() {
        String s = getParamFromJobScheduler(SECURITY_SERVER_IS_ENABLED);
        return (s.equalsIgnoreCase(TRUE));
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
    public MyWebserviceAnswer test(@QueryParam("name") String name, @QueryParam("email") String email) {
        MyWebserviceAnswer m = new MyWebserviceAnswer("Uwe Risse");
        m.setTelephone("03086479034");
        m.setName(name);
        m.setEmail(email);
        return m;
    }

    @GET
    @Path("/is_enabled")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSCommandSecurityWebserviceAnswer isEnabled() {
        SOSCommandSecurityWebserviceAnswer m = new SOSCommandSecurityWebserviceAnswer();
        m.setIsEnabled(getIsEnabled());
        return m;
    }

    private SOSCommandSecurityWebserviceAnswer createAnswer(String jobSchedulerAnswer, String message,
            SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord) {
        String sessionId = "-";
        if (sosWebserviceAuthenticationRecord != null) {
            sessionId = sosWebserviceAuthenticationRecord.getSessionId();
        }
        SOSCommandSecurityWebserviceAnswer m = new SOSCommandSecurityWebserviceAnswer();
        m.setIsEnabled(getIsEnabled());
        m.setUser(sosWebserviceAuthenticationRecord.getUser());
        m.setResource(getResource(sessionId));
        m.setSessionId(sessionId);
        m.setMessage(message);
        m.setJobSchedulerAnswer(jobSchedulerAnswer);
        return m;
    }

    private String checkAuthentication(SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord, String item) throws MalformedURLException {
        if (getIsEnabled()) {
            sosWebserviceAuthenticationRecord.setResource(this.getResource(sosWebserviceAuthenticationRecord.getSessionId()));
            String permission = sosWebserviceAuthenticationRecord.getPermission() + ":" + item;
            permission = permission.replace("/", ":");
            sosWebserviceAuthenticationRecord.setPermission(permission);
            SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = isPermitted(sosWebserviceAuthenticationRecord);
            if (sosShiroCurrentUserAnswer == null) {
                return "Please login";
            }
            if (!sosShiroCurrentUserAnswer.isAuthenticated()) {
                return String.format("%s:User %s is not authenticated", "SOSSEC001", sosWebserviceAuthenticationRecord.getUser());
            }
            boolean isPermitted = (sosShiroCurrentUserAnswer != null && sosShiroCurrentUserAnswer.isPermitted() && sosShiroCurrentUserAnswer.isAuthenticated());
            if (!isPermitted) {
                return String.format("%s,User %s is not permitted. Missing permission: %s", "SOSSEC002", sosWebserviceAuthenticationRecord.getUser(), permission);
            }
            return "";
        } else {
            return "";
        }

    }

    private String[] getParams(String params) {
        if (params != null && params.length() > 0) {
            params = params.replaceAll("=", "|");
            String[] jobParams = params.split("\\|");
            return jobParams;
        }
        return null;
    }

    private SOSWebserviceAuthenticationRecord getAuthencationRecord(String sessionId, String permission) {
        SOSCommandSecurityWebserviceCurrentUser currentUser = getCurrentUser(sessionId);
        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = new SOSWebserviceAuthenticationRecord();
        sosWebserviceAuthenticationRecord.setResource(currentUser.getResource());
        sosWebserviceAuthenticationRecord.setSessionId(sessionId);
        sosWebserviceAuthenticationRecord.setPermission(permission);
        sosWebserviceAuthenticationRecord.setUser(currentUser.getUsername());
        return sosWebserviceAuthenticationRecord;
    }

    @POST
    @Path("/start_job")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSCommandSecurityWebserviceAnswer startJob(@QueryParam("session_id") String sessionId, @QueryParam("job") String job, @QueryParam("at") String at,
            @QueryParam("force") String force, @QueryParam("name") String name, @QueryParam("params") String params) throws MalformedURLException {
        SchedulerObjectFactory objFactory = new SchedulerObjectFactory();
        objFactory.initMarshaller(Spooler.class);
        JSCmdStartJob objStartJob = new JSCmdStartJob(objFactory);
        objStartJob.setJobIfNotEmpty(job);
        objStartJob.setForceIfNotEmpty(force);
        objStartJob.setAtIfNotEmpty(at);
        objStartJob.setNameIfNotEmpty(name);
        String[] jobParams = getParams(params);
        if (jobParams != null) {
            objStartJob.setParams(jobParams);
        }
        String xml = objFactory.toXMLString(objStartJob);
        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = getAuthencationRecord(sessionId, PERMISSION_START_JOB);
        String s = checkAuthentication(sosWebserviceAuthenticationRecord, job);
        if ("".equals(s)) {
            String answer = executeXml(xml);
            return createAnswer(answer, String.format("%s:job %s gestartet", "SOSSEC003", job), sosWebserviceAuthenticationRecord);
        } else {
            return createAnswer("", s, sosWebserviceAuthenticationRecord);
        }
    }

    @POST
    @Path("/modify_order")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSCommandSecurityWebserviceAnswer modifyOrder(@QueryParam("session_id") String sessionId, @QueryParam("job_chain") String jobChain,
            @QueryParam("order") String order, @QueryParam("action") String action, @QueryParam("at") String at, @QueryParam("end_state") String endState,
            @QueryParam("priority") String priority, @QueryParam("setback") String setback, @QueryParam("state") String state,
            @QueryParam("suspended") String suspended, @QueryParam("title") String title, @QueryParam("params") String params,
            @QueryParam("runtime") String runtime) throws MalformedURLException {
        String orderS = String.format("%s:%s", jobChain, order);
        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = getAuthencationRecord(sessionId, PERMISSION_MODIFY_ORDER);
        String s = checkAuthentication(sosWebserviceAuthenticationRecord, orderS);
        if ("".equals(s)) {
            SchedulerObjectFactory objFactory = new SchedulerObjectFactory();
            objFactory.initMarshaller(Spooler.class);
            JSCmdModifyOrder objOrder = objFactory.createModifyOrder();
            objOrder.setJobChainIfNotEmpty(jobChain);
            objOrder.setOrderIfNotEmpty(order);
            objOrder.setActionIfNotEmpty(action);
            objOrder.setAtIfNotEmpty(at);
            objOrder.setEndStateIfNotEmpty(endState);
            objOrder.setPriorityIfNotEmpty(priority);
            objOrder.setSetbackIfNotEmpty(setback);
            objOrder.setStateIfNotEmpty(state);
            objOrder.setSuspendedIfNotEmpty(suspended);
            objOrder.setTitleIfNotEmpty(title);
            String[] jobParams = getParams(params);
            if (jobParams != null) {
                objOrder.setParams(jobParams);
            }
            if (runtime != null && !runtime.isEmpty()) {
                JSObjRunTime objRuntime = new JSObjRunTime(objFactory, runtime);
                objOrder.setRunTime(objRuntime);
            }
            String xml = objFactory.toXMLString(objOrder);
            String answer = executeXml(xml);
            return createAnswer(answer, String.format("%s:job chain %s modfied with order %s", "SOSSEC004", jobChain, order), sosWebserviceAuthenticationRecord);
        } else {
            return createAnswer("", s, sosWebserviceAuthenticationRecord);
        }
    }

    @POST
    @Path("/add_order")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSCommandSecurityWebserviceAnswer addOrder(@QueryParam("session_id") String sessionId, @QueryParam("job_chain") String jobChain,
            @QueryParam("order") String order, @QueryParam("at") String at, @QueryParam("end_state") String endState, @QueryParam("priority") String priority,
            @QueryParam("replace") String replace, @QueryParam("state") String state, @QueryParam("title") String title,
            @QueryParam("web_service") String webService, @QueryParam("params") String params, @QueryParam("runtime") String runtime)
            throws MalformedURLException {
        String orderId = order;
        if (order == null) {
            orderId = "";
        }
        String orderS = String.format("%s:%s", jobChain, orderId);
        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = getAuthencationRecord(sessionId, PERMISSION_ADD_ORDER);
        String s = checkAuthentication(sosWebserviceAuthenticationRecord, orderS);
        if ("".equals(s)) {
            SchedulerObjectFactory objFactory = new SchedulerObjectFactory();
            objFactory.initMarshaller(Spooler.class);
            JSCmdAddOrder objOrder = objFactory.createAddOrder();
            objOrder.setJobChainIfNotEmpty(jobChain);
            objOrder.setIdIfNotEmpty(order);
            objOrder.setWebService(webService);
            objOrder.setAtIfNotEmpty(at);
            objOrder.setEndState(endState);
            objOrder.setPriorityIfNotEmpty(priority);
            objOrder.setStateIfNotEmpty(state);
            objOrder.setTitleIfNotEmpty(title);
            String[] orderParams = getParams(params);
            if (orderParams != null) {
                objOrder.setParams(orderParams);
            }
            if (runtime != null && !runtime.isEmpty()) {
                JSObjRunTime objRuntime = new JSObjRunTime(objFactory, runtime);
                objOrder.setRunTime(objRuntime);
            }
            String xml = objOrder.toXMLString();
            String answer = executeXml(xml);
            return createAnswer(answer, String.format("%s:order %s added to job chain %s", "SOSSEC005", orderId, jobChain), sosWebserviceAuthenticationRecord);
        } else {
            return createAnswer("", s, sosWebserviceAuthenticationRecord);
        }
    }

    @POST
    @Path("/job_chain")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSCommandSecurityWebserviceAnswer jobChain(@QueryParam("session_id") String sessionId, @QueryParam("distributed") String distributed,
            @QueryParam("max_orders") String maxOrders, @QueryParam("name") String name, @QueryParam("orders_recoverable") String ordersRecoverable,
            @QueryParam("title") String title, @QueryParam("visible") String visible) throws MalformedURLException {
        String jobChainS = String.format("%s", name);
        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = getAuthencationRecord(sessionId, PERMISSION_ADD_JOB_CHAIN);
        String s = checkAuthentication(sosWebserviceAuthenticationRecord, jobChainS);
        if ("".equals(s)) {
            SchedulerObjectFactory objFactory = new SchedulerObjectFactory();
            objFactory.initMarshaller(Spooler.class);
            JSObjJobChain objJobChain = objFactory.createJobChain();
            objJobChain.setNameIfNotEmpty(name);
            objJobChain.setDistributedNotEmpty(distributed);
            objJobChain.setMaxOrdersIfNotEmpty(maxOrders);
            objJobChain.setOrdersRecoverableIfNotEmpty(ordersRecoverable);
            objJobChain.setVisibleIfNotEmpty(visible);
            objJobChain.setTitleIfNotEmpty(title);
            String xml = objJobChain.toXMLString();
            String answer = executeXml(xml);
            return createAnswer(answer, String.format("Jobchain %s added ", "SOSSEC005", name), sosWebserviceAuthenticationRecord);
        } else {
            return createAnswer("", s, sosWebserviceAuthenticationRecord);
        }
    }

    @POST
    @Path("/job_chain_modify")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSCommandSecurityWebserviceAnswer jobChainModify(@QueryParam("session_id") String sessionId, @QueryParam("job_chain") String jobChain,
            @QueryParam("state") String state) throws MalformedURLException {
        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = getAuthencationRecord(sessionId, PERMISSION_MODIFY_JOBCHAIN);
        String s = checkAuthentication(sosWebserviceAuthenticationRecord, jobChain);
        if ("".equals(s)) {
            SchedulerObjectFactory objFactory = new SchedulerObjectFactory();
            objFactory.initMarshaller(Spooler.class);
            JSCmdJobChainModify objModifyJobChain = objFactory.createJobChainModify();
            objModifyJobChain.setJobChainIfNotEmpty(jobChain);
            objModifyJobChain.setStateIfNotEmpte(state);
            String xml = objFactory.toXMLString(objModifyJobChain);
            String answer = executeXml(xml);
            return createAnswer(answer, String.format("%s:job chain %s modified with state %s", "SOSSEC006", jobChain, state), sosWebserviceAuthenticationRecord);
        } else {
            return createAnswer("", s, sosWebserviceAuthenticationRecord);
        }
    }

    @POST
    @Path("/job_chain_node_modify")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSCommandSecurityWebserviceAnswer jobChainNodeModify(@QueryParam("session_id") String sessionId, @QueryParam("job_chain") String jobChain,
            @QueryParam("action") String action, @QueryParam("state") String state) throws MalformedURLException {
        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = getAuthencationRecord(sessionId, PERMISSION_MODIFY_JOBCHAIN_NODE);
        String s = checkAuthentication(sosWebserviceAuthenticationRecord, jobChain);
        if ("".equals(s)) {
            SchedulerObjectFactory objFactory = new SchedulerObjectFactory();
            objFactory.initMarshaller(Spooler.class);
            JSCmdJobChainNodeModify objModifyJobChainNode = objFactory.createJobChainNodeModify();
            objModifyJobChainNode.setJobChainIfNotEmpty(jobChain);
            objModifyJobChainNode.setStateIfNotEmpty(state);
            objModifyJobChainNode.setActionIfNotEmpty(action);
            String xml = objFactory.toXMLString(objModifyJobChainNode);
            String answer = executeXml(xml);
            return createAnswer(answer, String.format("%s:job chain %s modified with state %s", "SOSSEC007", jobChain, state), sosWebserviceAuthenticationRecord);
        } else {
            return createAnswer("", s, sosWebserviceAuthenticationRecord);
        }
    }

    @POST
    @Path("/kill_task")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSCommandSecurityWebserviceAnswer killTask(@QueryParam("session_id") String sessionId, @QueryParam("id") String id,
            @QueryParam("immediately") String immediately, @QueryParam("job") String job) throws MalformedURLException {
        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = getAuthencationRecord(sessionId, PERMISSION_KILL_TASK);
        String s = checkAuthentication(sosWebserviceAuthenticationRecord, job);
        if ("".equals(s)) {
            SchedulerObjectFactory objFactory = new SchedulerObjectFactory();
            objFactory.initMarshaller(Spooler.class);
            JSCmdKillTask objKillTask = objFactory.createKillTask();
            objKillTask.setIdIfNotEmpty(id);
            objKillTask.setJobIfNotEmpty(job);
            objKillTask.setImmediatelyIfNotEmpty(immediately);
            String xml = objFactory.toXMLString(objKillTask);
            String answer = executeXml(xml);
            return createAnswer(answer, String.format("%s: job %s with id: %s killed", "SOSSEC008", job, id), sosWebserviceAuthenticationRecord);
        } else {
            return createAnswer("", s, sosWebserviceAuthenticationRecord);
        }
    }

    @POST
    @Path("/modify_job")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSCommandSecurityWebserviceAnswer modifyJob(@QueryParam("session_id") String sessionId, @QueryParam("job") String job, @QueryParam("cmd") String cmd)
            throws MalformedURLException {
        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = getAuthencationRecord(sessionId, PERMISSION_MODIFY_JOB);
        String s = checkAuthentication(sosWebserviceAuthenticationRecord, cmd + ":" + job);
        if ("".equals(s)) {
            SchedulerObjectFactory objFactory = new SchedulerObjectFactory();
            objFactory.initMarshaller(Spooler.class);
            JSCmdModifyJob objModifyJob = objFactory.createModifyJob();
            objModifyJob.setCmdIfNotEmpty(cmd);
            objModifyJob.setJobIfNotEmpty(job);
            String xml = objFactory.toXMLString(objModifyJob);
            String answer = executeXml(xml);
            return createAnswer(answer, String.format("%s: job %s modified with cmd: %s", "SOSSEC009", job, cmd), sosWebserviceAuthenticationRecord);
        } else {
            return createAnswer("", s, sosWebserviceAuthenticationRecord);
        }
    }

    @POST
    @Path("/modify_spooler")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSCommandSecurityWebserviceAnswer modifySpooler(@QueryParam("session_id") String sessionId, @QueryParam("cmd") String cmd,
            @QueryParam("timeout") String timeout) throws MalformedURLException {
        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = getAuthencationRecord(sessionId, PERMISSION_MODIFY_SPOOLER);
        String s = checkAuthentication(sosWebserviceAuthenticationRecord, cmd);
        if ("".equals(s)) {
            SchedulerObjectFactory objFactory = new SchedulerObjectFactory();
            objFactory.initMarshaller(Spooler.class);
            JSCmdModifySpooler objModifySpooler = objFactory.createModifySpooler();
            objModifySpooler.setCmdIfNotEmpty(cmd);
            objModifySpooler.setTimeoutIfNotEmpty(timeout);
            String xml = objFactory.toXMLString(objModifySpooler);
            String answer = executeXml(xml);
            return createAnswer(answer, String.format("%s: JobScheduler modified with cmd: %s", "SOSSEC010", cmd), sosWebserviceAuthenticationRecord);
        } else {
            return createAnswer("", s, sosWebserviceAuthenticationRecord);
        }
    }

    @POST
    @Path("/process_class")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSCommandSecurityWebserviceAnswer processClass(@QueryParam("session_id") String sessionId, @QueryParam("scheduler_id") String schedulerId,
            @QueryParam("name") String name, @QueryParam("remote_scheduler") String remoteScheduler, @QueryParam("replace") String replace,
            @QueryParam("max_processes") String maxProcesses) throws MalformedURLException {
        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = getAuthencationRecord(sessionId, PERMISSION_ADD_PROCESS_CLASS);
        String s = checkAuthentication(sosWebserviceAuthenticationRecord, name);
        if ("".equals(s)) {
            SchedulerObjectFactory objFactory = new SchedulerObjectFactory();
            objFactory.initMarshaller(Spooler.class);
            JSObjProcessClass objProcessClass = objFactory.createProcessClass();
            objProcessClass.setSpoolerIdIfNotEmpty(schedulerId);
            objProcessClass.setNameIfNotEmpty(name);
            objProcessClass.setRemoteSchedulerIfNotEmpty(remoteScheduler);
            objProcessClass.setReplaceIfNotEmpty(replace);
            objProcessClass.setMaxProcessesIfNotEmpty(maxProcesses);
            if (maxProcesses != null && !maxProcesses.isEmpty()) {
                try {
                    objProcessClass.setMaxProcesses(Integer.valueOf(maxProcesses));
                } catch (NumberFormatException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            String xml = objFactory.toXMLString(objProcessClass);
            String answer = executeXml(xml);
            return createAnswer(answer, String.format("%s: Process %s class modified", "SOSSEC011", name), sosWebserviceAuthenticationRecord);
        } else {
            return createAnswer("", s, sosWebserviceAuthenticationRecord);
        }
    }

    @POST
    @Path("/process_class_remove")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSCommandSecurityWebserviceAnswer processClassRemove(@QueryParam("session_id") String sessionId, @QueryParam("process_class") String processClass)
            throws MalformedURLException {
        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = getAuthencationRecord(sessionId, PERMISSION_REMOVE_PROCESS_CLASS);
        String s = checkAuthentication(sosWebserviceAuthenticationRecord, processClass);
        if ("".equals(s)) {
            SchedulerObjectFactory objFactory = new SchedulerObjectFactory();
            objFactory.initMarshaller(Spooler.class);
            JSCmdProcessClassRemove objProcessClass = objFactory.createProcessClassRemove();
            objProcessClass.setProcessClassIfNotEmpty(processClass);
            String xml = String.format("<process_class.remove process_class=\"%s\"/>", processClass);
            String answer = executeXml(xml);
            return createAnswer(answer, String.format("%s: Process class %s removed", "SOSSEC012", processClass), sosWebserviceAuthenticationRecord);
        } else {
            return createAnswer("", s, sosWebserviceAuthenticationRecord);
        }

    }

    @POST
    @Path("/consumes")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Consumes("text/plain")
    public SOSCommandSecurityWebserviceAnswer consume(@QueryParam("session_id") String sessionId, @QueryParam("job_chain") String jobChain, String xml)
            throws MalformedURLException {
        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = new SOSWebserviceAuthenticationRecord();
        return createAnswer("", xml, sosWebserviceAuthenticationRecord);
    }

    @POST
    @Path("/remove_job_chain")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSCommandSecurityWebserviceAnswer jobChainRemove(@QueryParam("session_id") String sessionId, @QueryParam("job_chain") String jobChain)
            throws MalformedURLException {
        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = getAuthencationRecord(sessionId, PERMISSION_REMOVE_JOB_CHAIN);
        String s = checkAuthentication(sosWebserviceAuthenticationRecord, jobChain);
        if ("".equals(s)) {
            String xml = String.format("<remove_job_chain job_chain=\"%s\"/>", jobChain);
            String answer = executeXml(xml);
            return createAnswer(answer, String.format("%s: Job chain %s removed", "SOSSEC013", jobChain), sosWebserviceAuthenticationRecord);
        } else {
            return createAnswer("", s, sosWebserviceAuthenticationRecord);
        }
    }

    @POST
    @Path("/remove_order")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSCommandSecurityWebserviceAnswer orderRemove(@QueryParam("session_id") String sessionId, @QueryParam("job_chain") String jobChain,
            @QueryParam("order") String order) throws MalformedURLException {
        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = getAuthencationRecord(sessionId, PERMISSION_REMOVE_ORDER);
        String s = checkAuthentication(sosWebserviceAuthenticationRecord, jobChain + ":" + order);
        if ("".equals(s)) {
            SchedulerObjectFactory objFactory = new SchedulerObjectFactory();
            objFactory.initMarshaller(Spooler.class);
            JSCmdRemoveOrder objRemoveOrder = objFactory.createRemoveOrder();
            objRemoveOrder.setJobChainIfNotEmpty(jobChain);
            objRemoveOrder.setOrderIfNotEmpty(order);
            String xml = objRemoveOrder.toXMLString();
            String answer = executeXml(xml);
            return createAnswer(answer, String.format("%s: Order %s removed from job chain %s", "SOSSEC014", order, jobChain), sosWebserviceAuthenticationRecord);
        } else {
            return createAnswer("", s, sosWebserviceAuthenticationRecord);
        }
    }

    @POST
    @Path("/lock")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSCommandSecurityWebserviceAnswer lock(@QueryParam("session_id") String sessionId, @QueryParam("max_non_exclusive") String maxNonExclusive,
            @QueryParam("name") String name) throws MalformedURLException {
        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = getAuthencationRecord(sessionId, PERMISSION_LOCK);
        String s = checkAuthentication(sosWebserviceAuthenticationRecord, name);
        if ("".equals(s)) {
            SchedulerObjectFactory objFactory = new SchedulerObjectFactory();
            objFactory.initMarshaller(Spooler.class);
            JSObjLock objLock = objFactory.createLock();
            objLock.setMaxNonExclusiveIfNotEmpty(maxNonExclusive);
            objLock.setNameIfNotEmpty(name);
            String xml = objFactory.toXMLString(objLock);
            String answer = executeXml(xml);
            return createAnswer(answer, String.format("%s: lock %s created", "SOSSEC015", name), sosWebserviceAuthenticationRecord);
        } else {
            return createAnswer("", s, sosWebserviceAuthenticationRecord);
        }
    }

    @POST
    @Path("/lock_remove")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSCommandSecurityWebserviceAnswer lockRemove(@QueryParam("session_id") String sessionId, @QueryParam("lock") String lock)
            throws MalformedURLException {
        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = getAuthencationRecord(sessionId, PERMISSION_REMOVE_LOCK);
        String s = checkAuthentication(sosWebserviceAuthenticationRecord, lock);
        if ("".equals(s)) {
            SchedulerObjectFactory objFactory = new SchedulerObjectFactory();
            objFactory.initMarshaller(Spooler.class);
            JSCmdLockRemove objLock = objFactory.createLockRemove();
            objLock.setLockIfNotEmpty(lock);
            String xml = String.format("lock.remove lock=\"%s\"", lock);
            String answer = executeXml(xml);
            return createAnswer(answer, String.format("%s: lock %s removed", "SOSSEC016", lock), sosWebserviceAuthenticationRecord);
        } else {
            return createAnswer("", s, sosWebserviceAuthenticationRecord);
        }
    }

    @POST
    @Path("/terminate")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSCommandSecurityWebserviceAnswer terminate(@QueryParam("session_id") String sessionId, @QueryParam("all_schedulers") String allSchedulers,
            @QueryParam("continue_exclusive_operation") String continueExclusiveOperation, @QueryParam("restart") String restart,
            @QueryParam("timeout") String timeout) throws MalformedURLException {
        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = getAuthencationRecord(sessionId, PERMISSION_TERMINATE);
        String s = checkAuthentication(sosWebserviceAuthenticationRecord, "");
        if ("".equals(s)) {
            SchedulerObjectFactory objFactory = new SchedulerObjectFactory();
            objFactory.initMarshaller(Spooler.class);
            JSCmdTerminate objTerminate = new JSCmdTerminate(objFactory);
            objTerminate.setAllSchedulersIfNotEmpty(allSchedulers);
            objTerminate.setContinueExclusiveOperationIfNotEmpty(continueExclusiveOperation);
            objTerminate.setRestartIfNotEmpty(restart);
            objTerminate.setTimeoutIfNotEmpty(timeout);
            String xml = objFactory.toXMLString(objTerminate);
            String answer = executeXml(xml);
            return createAnswer(answer, "SOSSEC017:JobScheduler terminated", sosWebserviceAuthenticationRecord);
        } else {
            return createAnswer("", s, sosWebserviceAuthenticationRecord);
        }
    }

    @POST
    @Path("/login")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSCommandSecurityWebserviceAnswer login(@QueryParam("user") String user, @QueryParam("password") String password) {
        if (currentUserList == null) {
            currentUserList = new SOSCommandSecurityWebserviceCurrentUsersList();
        }
        String resource = getResourceFromJobScheduler();
        String sAuthenticated = String.format("user: %s, password: %s, resource: %s", user, "********", resource);
        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = null;
        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = new SOSWebserviceAuthenticationRecord();
        try {
            sosWebserviceAuthenticationRecord.setUser(user);
            sosWebserviceAuthenticationRecord.setPassword(password);
            sosWebserviceAuthenticationRecord.setResource(resource);
            sosShiroCurrentUserAnswer = authenticate(sosWebserviceAuthenticationRecord);
            sosWebserviceAuthenticationRecord.setSessionId(sosShiroCurrentUserAnswer.getSessionId());
            SOSCommandSecurityWebserviceCurrentUser c = new SOSCommandSecurityWebserviceCurrentUser();
            c.setSessionId(sosShiroCurrentUserAnswer.getSessionId());
            c.setPassword(password);
            c.setUsername(user);
            c.setResource(resource);
            currentUserList.addUser(c);
        } catch (MalformedURLException e) {
            LOGGER.error(e.getMessage(), e);
            return createAnswer("", sAuthenticated, sosWebserviceAuthenticationRecord);
        }
        if (sosShiroCurrentUserAnswer != null && sosShiroCurrentUserAnswer.getIsAuthenticated()) {
            sAuthenticated = String.format("%s --> authenticated", sAuthenticated);
        } else {
            sAuthenticated = String.format("%s --> Not authenticated", sAuthenticated);
        }
        return createAnswer("", sAuthenticated, sosWebserviceAuthenticationRecord);
    }

    @POST
    @Path("/logout")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSCommandSecurityWebserviceAnswer logout(@QueryParam("session_id") String sessionId) throws MalformedURLException {
        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = getAuthencationRecord(sessionId, PERMISSION_LOGOUT);
        SOSRestShiroClient sosRestShiroClient = new SOSRestShiroClient();
        String resource = sosWebserviceAuthenticationRecord.getResource() + "/logout" + "?session_id=%s";
        sosWebserviceAuthenticationRecord.setResource(resource);
        sosRestShiroClient.getSOSShiroCurrentUserAnswer(new URL(String.format(sosWebserviceAuthenticationRecord.getResource(), sosWebserviceAuthenticationRecord.getSessionId())));
        SOSCommandSecurityWebserviceAnswer message = createAnswer("", String.format("%s --> Abgemeldet", sosWebserviceAuthenticationRecord.getSessionId()), sosWebserviceAuthenticationRecord);
        if (currentUserList != null) {
            currentUserList.removeUser(sessionId);
        }
        return message;
    }

    private SOSShiroCurrentUserAnswer callAuthenticationServer(SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord, String command)
            throws MalformedURLException {
        SOSRestShiroClient sosRestShiroClient = new SOSRestShiroClient();
        String authenticateRessource = sosWebserviceAuthenticationRecord.getResource() + command;
        sosWebserviceAuthenticationRecord.setResource(authenticateRessource);
        return sosRestShiroClient.getSOSShiroCurrentUserAnswer(sosWebserviceAuthenticationRecord);
    }

    private SOSShiroCurrentUserAnswer authenticate(SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord) throws MalformedURLException {
        return callAuthenticationServer(sosWebserviceAuthenticationRecord, AUTHENTICATE_COMMAND);
    }

    private SOSShiroCurrentUserAnswer isPermitted(SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord) throws MalformedURLException {
        return callAuthenticationServer(sosWebserviceAuthenticationRecord, PERMISSION_COMMAND);

    }

}