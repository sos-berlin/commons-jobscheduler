package com.sos.auth.rest;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.sos.auth.rest.permission.model.ObjectFactory;
import com.sos.auth.rest.permission.model.SOSPermissionDashboard;
import com.sos.auth.rest.permission.model.SOSPermissionEvents;
import com.sos.auth.rest.permission.model.SOSPermissionJid;
import com.sos.auth.rest.permission.model.SOSPermissionJoc;
import com.sos.auth.rest.permission.model.SOSPermissionJocCockpit;
import com.sos.auth.rest.permission.model.SOSPermissionJoe;
import com.sos.auth.rest.permission.model.SOSPermissionRoles;
import com.sos.auth.rest.permission.model.SOSPermissionShiro;
import com.sos.auth.rest.permission.model.SOSPermissionWorkingplan;
import com.sos.auth.rest.permission.model.SOSPermissions;
import com.sos.auth.shiro.SOSlogin;
 

import org.apache.shiro.session.Session;

@Path("/sosPermission")
public class SOSServicePermissionShiro {

    private SOSShiroCurrentUser currentUser;
    public static SOSShiroCurrentUsersList currentUsersList;

    
    
    private SOSPermissionJocCockpit createPermissionObject(String accessToken, String user, String pwd) {
        
        if (currentUsersList != null && accessToken != null && accessToken.length() > 0) {
            currentUser = currentUsersList.getUser(accessToken);
        } else {
            if (user != null && user.length() > 0 && pwd != null && pwd.length() > 0) {
                currentUser = new SOSShiroCurrentUser(user, pwd);
                createUser(currentUser);
            }
        }

        ObjectFactory o = new ObjectFactory();
        SOSPermissionJocCockpit sosPermissionJocCockpit = o.createSOSPermissionJocCockpit();

        if (currentUser != null && currentUser.getCurrentSubject() != null) {

            sosPermissionJocCockpit.setAuthenticated(currentUser.isAuthenticated());
            sosPermissionJocCockpit.setAccessToken(currentUser.getAccessToken());
            sosPermissionJocCockpit.setUser(currentUser.getUsername());

            SOSPermissionRoles roles = o.createSOSPermissionRoles();
            addRole(roles.getSOSPermissionRole(), "super");
            addRole(roles.getSOSPermissionRole(), "admin");
            addRole(roles.getSOSPermissionRole(), "joc_admin");
            addRole(roles.getSOSPermissionRole(), "jobeditor");
            addRole(roles.getSOSPermissionRole(), "controller");
            addRole(roles.getSOSPermissionRole(), "workingplan");
            addRole(roles.getSOSPermissionRole(), "jid");
            addRole(roles.getSOSPermissionRole(), "joe");
            addRole(roles.getSOSPermissionRole(), "joc");
            addRole(roles.getSOSPermissionRole(), "events");
            addRole(roles.getSOSPermissionRole(), "application_manager");
            addRole(roles.getSOSPermissionRole(), "it_operator");
            addRole(roles.getSOSPermissionRole(), "incident_manager");
            addRole(roles.getSOSPermissionRole(), "business_user");
            addRole(roles.getSOSPermissionRole(), "api_user");
            addRole(roles.getSOSPermissionRole(), "events");

           
            
            sosPermissionJocCockpit.setJobschedulerMaster(o.createSOSPermissionJocCockpitJobschedulerMaster() );
            sosPermissionJocCockpit.setJobschedulerMasterCluster(o.createSOSPermissionJocCockpitJobschedulerMasterCluster() );
            sosPermissionJocCockpit.setJobschedulerUniversalAgent(o.createSOSPermissionJocCockpitJobschedulerUniversalAgent() );
            sosPermissionJocCockpit.setDailyPlan(o.createSOSPermissionJocCockpitDailyPlan());
            sosPermissionJocCockpit.setHistory(o.createSOSPermissionJocCockpitHistory());
            sosPermissionJocCockpit.setOrder(o.createSOSPermissionJocCockpitOrder());
            sosPermissionJocCockpit.setJobChain(o.createSOSPermissionJocCockpitJobChain());
            sosPermissionJocCockpit.setJob(o.createSOSPermissionJocCockpitJob());
            sosPermissionJocCockpit.setProcessClass(o.createSOSPermissionJocCockpitProcessClass());
            sosPermissionJocCockpit.setSchedule(o.createSOSPermissionJocCockpitSchedule());
            sosPermissionJocCockpit.setLock(o.createSOSPermissionJocCockpitLock());
            sosPermissionJocCockpit.setEvent(o.createSOSPermissionJocCockpitEvent());
            sosPermissionJocCockpit.setEventAction(o.createSOSPermissionJocCockpitEventAction());
            sosPermissionJocCockpit.setHolidayCalendar(o.createSOSPermissionJocCockpitHolidayCalendar());
            sosPermissionJocCockpit.setMaintenanceWindow(o.createSOSPermissionJocCockpitMaintenanceWindow());
            sosPermissionJocCockpit.setSOSPermissionRoles(roles);
            
            
            sosPermissionJocCockpit.getJobschedulerMaster().setView(o.createSOSPermissionJocCockpitJobschedulerMasterView());
            sosPermissionJocCockpit.getJobschedulerMasterCluster().setView(o.createSOSPermissionJocCockpitJobschedulerMasterClusterView());
            sosPermissionJocCockpit.getJobschedulerUniversalAgent().setView(o.createSOSPermissionJocCockpitJobschedulerUniversalAgentView());
            sosPermissionJocCockpit.getDailyPlan().setView(o.createSOSPermissionJocCockpitDailyPlanView());
            sosPermissionJocCockpit.getOrder().setView(o.createSOSPermissionJocCockpitOrderView());
            sosPermissionJocCockpit.getOrder().setChange(o.createSOSPermissionJocCockpitOrderChange());
            sosPermissionJocCockpit.getJobChain().setView(o.createSOSPermissionJocCockpitJobChainView());
            sosPermissionJocCockpit.getJob().setView(o.createSOSPermissionJocCockpitJobView());
            sosPermissionJocCockpit.getJob().setStart(o.createSOSPermissionJocCockpitJobStart());
            sosPermissionJocCockpit.getProcessClass().setView(o.createSOSPermissionJocCockpitProcessClassView());
            sosPermissionJocCockpit.getSchedule().setView(o.createSOSPermissionJocCockpitScheduleView());
            sosPermissionJocCockpit.getLock().setView(o.createSOSPermissionJocCockpitLockView());
            sosPermissionJocCockpit.getEvent().setView(o.createSOSPermissionJocCockpitEventView());
            sosPermissionJocCockpit.getEventAction().setView(o.createSOSPermissionJocCockpitEventActionView());
            sosPermissionJocCockpit.getHolidayCalendar().setView(o.createSOSPermissionJocCockpitHolidayCalendarView());
            sosPermissionJocCockpit.getMaintenanceWindow().setView(o.createSOSPermissionJocCockpitMaintenanceWindowView());

            sosPermissionJocCockpit.getJobschedulerMaster().getView().setStatus(haveRight("sos:products:joc_cockpit:jobscheduler_master:view:status"));
            sosPermissionJocCockpit.getJobschedulerMaster().getView().setMainlog(haveRight("sos:products:joc_cockpit:jobscheduler_master:view:mainlog"));
            sosPermissionJocCockpit.getJobschedulerMaster().setPauseContinue(haveRight("sos:products:joc_cockpit:jobscheduler_master:pause_continue"));
            sosPermissionJocCockpit.getJobschedulerMaster().setRestart(haveRight("sos:products:joc_cockpit:jobscheduler_master:restart"));
            sosPermissionJocCockpit.getJobschedulerMaster().setTerminateRestart(haveRight("sos:products:joc_cockpit:jobscheduler_master:terminate"));
            sosPermissionJocCockpit.getJobschedulerMaster().setAbortRestart(haveRight("sos:products:joc_cockpit:jobscheduler_master:abort"));
            sosPermissionJocCockpit.getJobschedulerMaster().setManageCategories(haveRight("sos:products:joc_cockpit:jobscheduler_master:pause_continue"));
            sosPermissionJocCockpit.getJobschedulerMaster().setPauseContinue(haveRight("sos:products:joc_cockpit:jobscheduler_master:pause_continue"));
            sosPermissionJocCockpit.getJobschedulerMaster().setPauseContinue(haveRight("sos:products:joc_cockpit:jobscheduler_master:manage_categories"));
                      
            
            sosPermissionJocCockpit.getJobschedulerMasterCluster().getView().setClusterStatus(haveRight("sos:products:joc_cockpit:jobscheduler_master_cluster:view:cluster_status"));
            sosPermissionJocCockpit.getJobschedulerMasterCluster().setTerminateClusterMember(haveRight("sos:products:joc_cockpit:jobscheduler_master_cluster:terminate_cluster_member"));
            sosPermissionJocCockpit.getJobschedulerMasterCluster().setRestartClusterMember(haveRight("sos:products:joc_cockpit:jobscheduler_master_cluster:restart_cluster_member"));
            
            sosPermissionJocCockpit.getJobschedulerUniversalAgent().getView().setStatus(haveRight("sos:products:joc_cockpit:jobscheduler_universal_agent:view:status"));
            sosPermissionJocCockpit.getJobschedulerUniversalAgent().setStop(haveRight("sos:products:joc_cockpit:jobscheduler_universal_agent:stop"));
            sosPermissionJocCockpit.getJobschedulerUniversalAgent().setAbort(haveRight("sos:products:joc_cockpit:jobscheduler_universal_agent:abort"));
            sosPermissionJocCockpit.getJobschedulerUniversalAgent().setRestart(haveRight("sos:products:joc_cockpit:jobscheduler_universal_agent:restart"));

            sosPermissionJocCockpit.getDailyPlan().getView().setStatus(haveRight("sos:products:joc_cockpit:daily_plan:view_status"));

            sosPermissionJocCockpit.getHistory().setView(haveRight("sos:products:joc_cockpit:history:view"));
            
            
            sosPermissionJocCockpit.getOrder().getView().setStatus(haveRight("sos:products:joc_cockpit:order:view:status"));
            sosPermissionJocCockpit.getOrder().getView().setConfiguration(haveRight("sos:products:joc_cockpit:order:view:configuration"));
            sosPermissionJocCockpit.getOrder().getView().setOrderLog(haveRight("sos:products:joc_cockpit:order:view:order_log"));
            sosPermissionJocCockpit.getOrder().getView().setHistory(haveRight("sos:products:joc_cockpit:order:view:history"));
            sosPermissionJocCockpit.getOrder().getChange().setStartAndEndNode(haveRight("sos:products:joc_cockpit:order:change:start_and_end_node"));
            sosPermissionJocCockpit.getOrder().getChange().setTimeForAdhocOrder(haveRight("sos:products:joc_cockpit:order:change:time_for_adhoc_orders"));
            sosPermissionJocCockpit.getOrder().getChange().setParameter(haveRight("sos:products:joc_cockpit:order:change:parameter"));
            sosPermissionJocCockpit.getOrder().setStart(haveRight("sos:products:joc_cockpit:order:start"));
            sosPermissionJocCockpit.getOrder().setUpdate(haveRight("sos:products:joc_cockpit:order:update"));
            sosPermissionJocCockpit.getOrder().setSuspend(haveRight("sos:products:joc_cockpit:order:suspend"));
            sosPermissionJocCockpit.getOrder().setResume(haveRight("sos:products:joc_cockpit:order:resume"));
            sosPermissionJocCockpit.getOrder().setStart(haveRight("sos:products:joc_cockpit:order:start"));
            sosPermissionJocCockpit.getOrder().setDeleteAdhocOrdersBlacklistOrders(haveRight("sos:products:joc_cockpit:order:delete_order"));
            sosPermissionJocCockpit.getOrder().setRemoveSetback(haveRight("sos:products:joc_cockpit:order:remove_setback"));
            
            sosPermissionJocCockpit.getJobChain().getView().setOrderLog(haveRight("sos:products:joc_cockpit:job_chain:view:order_log"));
            sosPermissionJocCockpit.getJobChain().getView().setConfiguration(haveRight("sos:products:joc_cockpit:job_chain:view:configuration"));
            sosPermissionJocCockpit.getJobChain().getView().setHistory(haveRight("sos:products:joc_cockpit:job_chain:view:history"));
            sosPermissionJocCockpit.getJobChain().getView().setStatus(haveRight("sos:products:joc_cockpit:job_chain:view:status"));
            sosPermissionJocCockpit.getJobChain().setStop(haveRight("sos:products:joc_cockpit:job_chain:stop"));
            sosPermissionJocCockpit.getJobChain().setUnstop(haveRight("sos:products:joc_cockpit:job_chain:unstop"));
            sosPermissionJocCockpit.getJobChain().setAddOrder(haveRight("sos:products:joc_cockpit:job_chain:add_order"));
            sosPermissionJocCockpit.getJobChain().setDeleteTemporayOders(haveRight("sos:products:joc_cockpit:job_chain:delete_temporary_orders"));
            sosPermissionJocCockpit.getJobChain().setSkipOrUnskipJobNodes(haveRight("sos:products:joc_cockpit:job_chain:skip_or_unskip_nodes"));
            sosPermissionJocCockpit.getJobChain().setStopJob(haveRight("sos:products:joc_cockpit:job_chain:stop_job"));
            sosPermissionJocCockpit.getJobChain().setUnstopJob(haveRight("sos:products:joc_cockpit:job_chain:unstop_job"));
            sosPermissionJocCockpit.getJobChain().setStopJobNode(haveRight("sos:products:joc_cockpit:job_chain:stop_node"));
            sosPermissionJocCockpit.getJobChain().setUnstopJobNode(haveRight("sos:products:joc_cockpit:job_chain:unstop_unstop_node"));
          
            sosPermissionJocCockpit.getJob().getView().setStatus(haveRight("sos:products:joc_cockpit:job:view_status"));
            sosPermissionJocCockpit.getJob().getView().setTaskLog(haveRight("sos:products:joc_cockpit:job:view:task_log"));
            sosPermissionJocCockpit.getJob().getView().setConfiguration(haveRight("sos:products:joc_cockpit:job:view:configuration"));
            sosPermissionJocCockpit.getJob().getView().setHistory(haveRight("sos:products:joc_cockpit:job:view:history"));
            sosPermissionJocCockpit.getJob().getStart().setTask(haveRight("sos:products:joc_cockpit:job:start:task"));
            sosPermissionJocCockpit.getJob().getStart().setTaskImmediate(haveRight("sos:products:joc_cockpit:job:start:task_immediately"));
            sosPermissionJocCockpit.getJob().setStop(haveRight("sos:products:joc_cockpit:job:stop"));
            sosPermissionJocCockpit.getJob().setUnstop(haveRight("sos:products:joc_cockpit:job:unstop"));
            sosPermissionJocCockpit.getJob().setTerminate(haveRight("sos:products:joc_cockpit:job:terminate"));
            sosPermissionJocCockpit.getJob().setKill(haveRight("sos:products:joc_cockpit:job:unstop"));
            sosPermissionJocCockpit.getJob().setUnstop(haveRight("sos:products:joc_cockpit:job:kill"));
             
            sosPermissionJocCockpit.getProcessClass().getView().setStatus(haveRight("sos:products:joc_cockpit:process_class:view:status"));
            sosPermissionJocCockpit.getProcessClass().getView().setConfiguration(haveRight("sos:products:joc_cockpit:process_class:view:configuration"));
            
            
            sosPermissionJocCockpit.getSchedule().getView().setConfiguration(haveRight("sos:products:joc_cockpit:schedule:view:configuration"));
            sosPermissionJocCockpit.getSchedule().getView().setStatus(haveRight("sos:products:joc_cockpit:schedule:view:status"));
            sosPermissionJocCockpit.getSchedule().setEdit(haveRight("sos:products:joc_cockpit:schedule:edit"));            
            sosPermissionJocCockpit.getSchedule().setAddSubstitute(haveRight("sos:products:joc_cockpit:schedule:add_substitute"));            

            sosPermissionJocCockpit.getLock().getView().setConfiguration(haveRight("sos:products:joc_cockpit:lock:view:configuration"));          
            sosPermissionJocCockpit.getLock().getView().setStatus(haveRight("sos:products:joc_cockpit:lock:view:status"));          

            sosPermissionJocCockpit.getEvent().getView().setStatus(haveRight("sos:products:joc_cockpit:event:view:status"));          
            sosPermissionJocCockpit.getEvent().setDelete(haveRight("sos:products:joc_cockpit:event:delete"));          


            sosPermissionJocCockpit.getEventAction().getView().setStatus(haveRight("sos:products:joc_cockpit:event_action:view:status"));
            sosPermissionJocCockpit.getEventAction().setCreateEventsManually(haveRight("sos:products:joc_cockpit:event_action:create_event_manually"));
            
            sosPermissionJocCockpit.getHolidayCalendar().getView().setStatus(haveRight("sos:products:joc_cockpit:holiday_calendar:view:status"));

            sosPermissionJocCockpit.getMaintenanceWindow().getView().setStatus(haveRight("sos:products:joc_cockpit:maintenance_window:view:status"));
            sosPermissionJocCockpit.getMaintenanceWindow().setEnableDisableMaintenanceWindow(haveRight("sos:products:joc_cockpit:maintenance_window:enable_disable_mainenance_window"));
            
 
 
   
        }
        return sosPermissionJocCockpit;
    }

    @GET
    @Path("/permissions")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSPermissionShiro getPermissions(@QueryParam("access_token") String accessToken, @QueryParam("user") String user, @QueryParam("pwd") String pwd) {
 
        if (currentUsersList != null && accessToken != null && accessToken.length() > 0) {
            currentUser = currentUsersList.getUser(accessToken);
        } else {
            if (user != null && user.length() > 0 && pwd != null && pwd.length() > 0) {
                currentUser = new SOSShiroCurrentUser(user, pwd);
                createUser(currentUser);
            }
        }

        ObjectFactory o = new ObjectFactory();
        SOSPermissionShiro sosPermissionShiro = o.createSOSPermissionShiro();

        if (currentUser != null && currentUser.getCurrentSubject() != null) {

            sosPermissionShiro.setAuthenticated(currentUser.isAuthenticated());
            sosPermissionShiro.setAccessToken(currentUser.getAccessToken());
            sosPermissionShiro.setUser(currentUser.getUsername());

            SOSPermissionRoles roles = o.createSOSPermissionRoles();
            addRole(roles.getSOSPermissionRole(), "super");
            addRole(roles.getSOSPermissionRole(), "admin");
            addRole(roles.getSOSPermissionRole(), "joc_admin");
            addRole(roles.getSOSPermissionRole(), "jobeditor");
            addRole(roles.getSOSPermissionRole(), "controller");
            addRole(roles.getSOSPermissionRole(), "workingplan");
            addRole(roles.getSOSPermissionRole(), "jid");
            addRole(roles.getSOSPermissionRole(), "joe");
            addRole(roles.getSOSPermissionRole(), "joc");
            addRole(roles.getSOSPermissionRole(), "events");
            addRole(roles.getSOSPermissionRole(), "application_manager");
            addRole(roles.getSOSPermissionRole(), "it_operator");
            addRole(roles.getSOSPermissionRole(), "incident_manager");
            addRole(roles.getSOSPermissionRole(), "business_user");
            addRole(roles.getSOSPermissionRole(), "api_user");
            addRole(roles.getSOSPermissionRole(), "events");

            SOSPermissions sosPermissions = o.createSOSPermissions();

            SOSPermissionJoe sosPermissionJoe = o.createSOSPermissionJoe();
            addPermission(sosPermissionJoe.getSOSPermission(), "sos:products:joe:execute");
            addPermission(sosPermissionJoe.getSOSPermission(), "sos:products:joe:edit");
            addPermission(sosPermissionJoe.getSOSPermission(), "sos:products:joe:write");
            addPermission(sosPermissionJoe.getSOSPermission(), "sos:products:joe:delete");
            sosPermissions.setSOSPermissionJoe(sosPermissionJoe);

            SOSPermissionJoc sosPermissionJoc = o.createSOSPermissionJoc();
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc:execute");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc:command:start:job");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc:command:add:order");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc:command:add:process_class");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc:command:modify:spooler");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc:command:modify:job");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc:command:modify:job");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc:command:modify:job_chain");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc:command:modify:job_chain_node");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc:command:kill_task");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc:command:add:lock");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc:command:remove:lock");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc:command:remove:process_class");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc:command:remove:job_chain");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc:command:remove:order");

            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc:command:terminate");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc:view_configuration");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc:add_parameter");
            
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_master:view:status");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_master:view:mainlog");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_master:pause_continue");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_master:restart");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_master:terminate");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_master:abort");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_master:manage_categories");

            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_master_cluster:view:cluster_status");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_master_cluster:terminate_cluster_member");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_master_cluster:restart_cluster_member");
            
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_universal_agent:view:status");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_universal_agent:stop");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_universal_agent:abort");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_universal_agent:restart");

            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:daily_plan:view_status");

            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:history:view");

            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:view:status");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:view:configuration");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:view:order_log");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:start");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:update");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:change:time_for_adhoc_orders");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:change:parameter");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:change:start_and_end_node");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:suspend");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:resume");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:delete_order");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:remove_setback");


            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:view:status");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:view:configuration");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:view:history");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:view:order_log");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:stop");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:unstop");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:add_order");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:delete_temporary_orders");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:skip_or_unskip_nodes");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:stop_job");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:unstop_job");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:stop_node");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:unstop_unstop_node");

            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:view_status");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:view:configuration");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:view:history");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:view:task_log");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:start:task");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:start:task_immediately");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:stop");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:unstop");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:terminate");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:kill");

            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:process_class:view:status");

            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:schedule:view:status");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:schedule:view:configuration");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:schedule:edit");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:schedule:add_substitute");

            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:lock:view:status");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:lock:view:configuration");

            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:event:view:status");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:event:delete");

            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:event_action:view:status");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:event_action:create_event_manually");
            
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:holiday_calendar:view:status");

            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:maintenance_window:view:status");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:maintenance_window:enable_disable_mainenance_window");

 
            
            sosPermissions.setSOSPermissionJoc(sosPermissionJoc);

            SOSPermissionDashboard sosPermissionDashboard = o.createSOSPermissionDashboard();
            addPermission(sosPermissionDashboard.getSOSPermission(), "sos:products:jid:jobstart");

            SOSPermissionEvents sosPermissionEvents = o.createSOSPermissionEvents();

            SOSPermissionWorkingplan sosPermissionWorkingplan = o.createSOSPermissionWorkingplan();

            SOSPermissionJid sosPermissionJid = o.createSOSPermissionJid();
            sosPermissionJid.setSOSPermissionJoc(sosPermissionJoc);
            sosPermissionJid.setSOSPermissionJoe(sosPermissionJoe);

            sosPermissionJid.setSOSPermissionJoe(sosPermissionJoe);
            sosPermissionJid.setSOSPermissionJoc(sosPermissionJoc);
            sosPermissionJid.setSOSPermissionDashboard(sosPermissionDashboard);
            sosPermissionJid.setSOSPermissionEvents(sosPermissionEvents);
            sosPermissionJid.setSOSPermissionWorkingplan(sosPermissionWorkingplan);

            addPermission(sosPermissionJid.getSOSPermission(), "sos:products:jid:execute");
            addPermission(sosPermissionJid.getSOSPermission(), "sos:products:jid:joetab:show");
            addPermission(sosPermissionJid.getSOSPermission(), "sos:products:jid:joctab:show");
            addPermission(sosPermissionJid.getSOSPermission(), "sos:products:jid:reportstab:show");
            addPermission(sosPermissionJid.getSOSPermission(), "sos:products:jid:eventtab:show");
            addPermission(sosPermissionJid.getSOSPermission(), "sos:products:jid:instances:show");
            addPermission(sosPermissionJid.getSOSPermission(), "sos:products:jid:workingplantab:show");

            sosPermissions.setSOSPermissionJid(sosPermissionJid);

            sosPermissionShiro.setSOSPermissionRoles(roles);
            sosPermissionShiro.setSOSPermissions(sosPermissions);
        }
        return sosPermissionShiro;

    }

    @GET
    @Path("/joc_cockpit_permissions")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSPermissionJocCockpit getJocCockpitPermissions(@QueryParam("access_token") String accessToken, @QueryParam("user") String user, @QueryParam("pwd") String pwd) {
       return createPermissionObject(accessToken,user,pwd); 
    }    
    
   
    @POST
    @Path("/joc_cockpit_permissions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSPermissionJocCockpit postJocCockpitPermissions(SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord) {
        return createPermissionObject(sosWebserviceAuthenticationRecord.getAccessToken(),sosWebserviceAuthenticationRecord.getUser(),sosWebserviceAuthenticationRecord.getPassword()); 
    }    
    
    
    
    private void createUser(SOSShiroCurrentUser sosShiroCurrentUser) {
        if (currentUsersList == null) {
            currentUsersList = new SOSShiroCurrentUsersList();
        }

        SOSlogin sosLogin = new SOSlogin();
        sosLogin.login(sosShiroCurrentUser.getUsername(), sosShiroCurrentUser.getPassword());

        currentUser.setCurrentSubject(sosLogin.getCurrentUser());

        Session session = sosLogin.getCurrentUser().getSession();
        String accessToken = session.getId().toString();

        currentUser.setAccessToken(accessToken);
        currentUsersList.addUser(currentUser);

    }

    @GET
    @Path("/authenticate")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSShiroCurrentUserAnswer authenticate(@QueryParam("user") String user, @QueryParam("pwd") String pwd) {

        currentUser = new SOSShiroCurrentUser(user, pwd);
        createUser(currentUser);

        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = new SOSShiroCurrentUserAnswer(user);
        sosShiroCurrentUserAnswer.setIsAuthenticated(currentUser.getCurrentSubject().isAuthenticated());
        sosShiroCurrentUserAnswer.setAccessToken(currentUser.getAccessToken());
        sosShiroCurrentUserAnswer.setUser(user);

        return sosShiroCurrentUserAnswer;

    }

    @GET
    @Path("/login")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSShiroCurrentUserAnswer login(@QueryParam("user") String user, @QueryParam("pwd") String pwd) {
        return authenticate(user, pwd);
    }

    @GET
    @Path("/logout")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSShiroCurrentUserAnswer logout(@QueryParam("access_token") String accessToken) {

        currentUser = currentUsersList.getUser(accessToken);

        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = new SOSShiroCurrentUserAnswer(currentUser.getUsername());
        sosShiroCurrentUserAnswer.setIsAuthenticated(currentUser.isAuthenticated());
        sosShiroCurrentUserAnswer.setAccessToken(currentUser.getAccessToken());

        currentUsersList.removeUser(accessToken);

        return sosShiroCurrentUserAnswer;

    }

    @GET
    @Path("/role")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSShiroCurrentUserAnswer hasRole(@QueryParam("access_token") String accessToken, @QueryParam("user") String user, @QueryParam("pwd") String pwd,
            @QueryParam("role") String role) {

        if (currentUsersList != null && accessToken != null && accessToken.length() > 0) {
            currentUser = currentUsersList.getUser(accessToken);
        } else {
            if (user != null && user.length() > 0 && pwd != null && pwd.length() > 0) {
                currentUser = new SOSShiroCurrentUser(user, pwd);
                createUser(currentUser);
            }
        }

        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = new SOSShiroCurrentUserAnswer(user);
        sosShiroCurrentUserAnswer.setRole(role);
        sosShiroCurrentUserAnswer.setIsAuthenticated(currentUser.isAuthenticated());
        sosShiroCurrentUserAnswer.setHasRole(currentUser.hasRole(role));

        sosShiroCurrentUserAnswer.setAccessToken(currentUser.getAccessToken());

        return sosShiroCurrentUserAnswer;

    }

    @GET
    @Path("/permission")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSShiroCurrentUserAnswer isPermitted(@QueryParam("access_token") String accessToken, @QueryParam("user") String user, @QueryParam("pwd") String pwd,
            @QueryParam("permission") String permission) {

        if (currentUsersList != null && accessToken != null && accessToken.length() > 0) {
            currentUser = currentUsersList.getUser(accessToken);
        } else {
            if (user != null && user.length() > 0 && pwd != null && pwd.length() > 0) {
                currentUser = new SOSShiroCurrentUser(user, pwd);
                createUser(currentUser);
            }
        }

        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = new SOSShiroCurrentUserAnswer(user);
        sosShiroCurrentUserAnswer.setPermission(permission);
        sosShiroCurrentUserAnswer.setIsAuthenticated(currentUser.isAuthenticated());
        sosShiroCurrentUserAnswer.setIsPermitted(currentUser.isPermitted(permission));

        sosShiroCurrentUserAnswer.setAccessToken(currentUser.getAccessToken());

        return sosShiroCurrentUserAnswer;

    }

    private boolean haveRight(String permission){
        return (currentUser != null && currentUser.isPermitted(permission) && currentUser.isAuthenticated());
    }
    
    private void addPermission(List<String> sosPermission, String permission) {
        if (currentUser != null && currentUser.isPermitted(permission) && currentUser.isAuthenticated()) {
            sosPermission.add(permission);
        }

    }

    private void addRole(List<String> sosRoles, String role) {
        if (currentUser != null && currentUser.hasRole(role) && currentUser.isAuthenticated()) {
            sosRoles.add(role);
        }

    }

}