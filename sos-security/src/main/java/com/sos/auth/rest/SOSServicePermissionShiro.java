package com.sos.auth.rest;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.List;
import java.util.TimeZone;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
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
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.objects.Spooler;

import org.apache.log4j.Logger;
import org.apache.shiro.session.Session;

@Path("/security")
public class SOSServicePermissionShiro {
    private static final String ACCESS_TOKEN = "access_token";
    private static final String UTC = "UTC";
    private static final String UNKNOWN_USER = "*Unknown User*";
    private static final String EMPTY_STRING = "";
    private static final String USER_IS_NULL = "user is null";
    private static final String AUTHORIZATION_HEADER_WITH_BASIC_BASED64PART_EXPECTED = "Authorization Header with Basic based64part expected";
    private static final String ROLE_API_USER = "api_user";
    private static final String ROLE_BUSINESS_USER = "business_user";
    private static final String ROLE_INCIDENT_MANAGER = "incident_manager";
    private static final String ROLE_IT_OPERATOR = "it_operator";
    private static final String ROLE_APPLICATION_MANAGER = "application_manager";
    private static final String ROLE_ADMINISTRATOR = "administrator";
    private static final String ROLE_EVENTS = "events";
    private static final String ROLE_JOC = "joc";
    private static final String ROLE_JOE = "joe";
    private static final String ROLE_JID = "jid";
    private static final String ROLE_WORKINGPLAN = "workingplan";
    private static final String ROLE_CONTROLLER = "controller";
    private static final String ROLE_JOBEDITOR = "jobeditor";
    private static final String ROLE_JOC_ADMIN = "joc_admin";
    private static final String ROLE_ADMIN = "admin";
    private static final String ROLE_SUPER = "super";
    private static final Logger LOGGER = Logger.getLogger(SOSServicePermissionShiro.class);

    private SOSShiroCurrentUser currentUser;

    @GET
    @Path("/permissions")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSPermissionShiro getPermissions(@HeaderParam(ACCESS_TOKEN) String accessTokenFromHeader, @QueryParam(ACCESS_TOKEN) String accessTokenFromQuery,
            @QueryParam("user") String user, @QueryParam("pwd") String pwd) {

        String accessToken = this.getAccessToken(accessTokenFromHeader, accessTokenFromQuery);
        this.setCurrentUserfromAccessToken(accessToken, user, pwd);

        ObjectFactory o = new ObjectFactory();
        SOSPermissionShiro sosPermissionShiro = o.createSOSPermissionShiro();

        if (currentUser != null && currentUser.getCurrentSubject() != null) {

            sosPermissionShiro.setAuthenticated(currentUser.isAuthenticated());
            sosPermissionShiro.setAccessToken(currentUser.getAccessToken());
            sosPermissionShiro.setUser(currentUser.getUsername());

            SOSPermissionRoles roles = o.createSOSPermissionRoles();
            addRole(roles.getSOSPermissionRole(), ROLE_SUPER);
            addRole(roles.getSOSPermissionRole(), ROLE_ADMIN);
            addRole(roles.getSOSPermissionRole(), ROLE_JOC_ADMIN);
            addRole(roles.getSOSPermissionRole(), ROLE_JOBEDITOR);
            addRole(roles.getSOSPermissionRole(), ROLE_CONTROLLER);
            addRole(roles.getSOSPermissionRole(), ROLE_WORKINGPLAN);
            addRole(roles.getSOSPermissionRole(), ROLE_JID);
            addRole(roles.getSOSPermissionRole(), ROLE_JOE);
            addRole(roles.getSOSPermissionRole(), ROLE_JOC);
            addRole(roles.getSOSPermissionRole(), ROLE_EVENTS);
            addRole(roles.getSOSPermissionRole(), ROLE_ADMINISTRATOR);
            addRole(roles.getSOSPermissionRole(), ROLE_APPLICATION_MANAGER);
            addRole(roles.getSOSPermissionRole(), ROLE_IT_OPERATOR);
            addRole(roles.getSOSPermissionRole(), ROLE_INCIDENT_MANAGER);
            addRole(roles.getSOSPermissionRole(), ROLE_BUSINESS_USER);
            addRole(roles.getSOSPermissionRole(), ROLE_API_USER);

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
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_master:pause");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_master:continue");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_master:restart:terminate");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_master:restart:abort");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_master:terminate");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_master:abort");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_master:manage_categories");

            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_master_cluster:view:cluster_status");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_master_cluster:terminate_fail_safe");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_master_cluster:restart");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_master_cluster:terminate");

            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_universal_agent:view:status");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_universal_agent:terminate");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_universal_agent:abort");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_universal_agent:restart:terminate");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_universal_agent:restart:abort");

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
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:delete:temporary");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:delete:permanent");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:remove_setback");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:set_run_time");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:set_state");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:reset");

            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:view:status");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:view:configuration");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:view:history");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:view:order_log");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:stop");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:unstop");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:add_order");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:skip_jobchain_node");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:unskip_jobchain_node");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:stop_jobchain_node");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:unstop_jobchain_node");

            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:view:status");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:view:configuration");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:view:history");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:view:task_log");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:start:task");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:start:task_immediately");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:stop");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:unstop");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:terminate");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:kill");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:set_run_time");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:end_all_tasks");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:suspend_all_tasks");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:continue_all_tasks");

            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:process_class:view:status");
            addPermission(sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:process_class:view:configuration");

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
    public JOCDefaultResponse getJocCockpitPermissions(@HeaderParam(ACCESS_TOKEN) String accessTokenFromHeader, @QueryParam(ACCESS_TOKEN) String accessTokenFromQuery,
            @QueryParam("user") String user, @QueryParam("pwd") String pwd) {
        String accessToken = getAccessToken(accessTokenFromHeader, accessTokenFromQuery);

        this.setCurrentUserfromAccessToken(accessToken, user, pwd);
        return JOCDefaultResponse.responseStatus200(createPermissionObject(accessToken, user, pwd));
    }

    @POST
    @Path("/joc_cockpit_permissions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postJocCockpitPermissions(@HeaderParam(ACCESS_TOKEN) String accessTokenFromHeader) {

        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = new SOSWebserviceAuthenticationRecord();

        try {

            if (!EMPTY_STRING.equals(accessTokenFromHeader)) {
                sosWebserviceAuthenticationRecord.setAccessToken(accessTokenFromHeader);
            }

            setCurrentUserfromAccessToken(sosWebserviceAuthenticationRecord.getAccessToken(), sosWebserviceAuthenticationRecord.getUser(), sosWebserviceAuthenticationRecord
                    .getPassword());

            if (currentUser == null) {
                LOGGER.debug(USER_IS_NULL + " " + AUTHORIZATION_HEADER_WITH_BASIC_BASED64PART_EXPECTED);
                return JOCDefaultResponse.responseStatusJSError(USER_IS_NULL + " " + AUTHORIZATION_HEADER_WITH_BASIC_BASED64PART_EXPECTED);
            }

            return JOCDefaultResponse.responseStatus200(currentUser.getSosPermissionJocCockpit());
        } catch (org.apache.shiro.session.ExpiredSessionException e) {
            LOGGER.error(e.getMessage());
            SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = createSOSShiroCurrentUserAnswer(accessTokenFromHeader, sosWebserviceAuthenticationRecord.getUser(), e
                    .getMessage());
            return JOCDefaultResponse.responseStatus440(sosShiroCurrentUserAnswer);
        }

        catch (Exception ee) {
            LOGGER.error(ee.getMessage());
            return JOCDefaultResponse.responseStatusJSError(ee.getMessage());
        }
    }

    @GET
    @Path("/login")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse loginGet(@HeaderParam("Authorization") String basicAuthorization, @QueryParam("user") String user, @QueryParam("pwd") String pwd) {
        return login(basicAuthorization, user, pwd);
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse loginPost(@HeaderParam("Authorization") String basicAuthorization, @QueryParam("user") String user, @QueryParam("pwd") String pwd) {
        return login(basicAuthorization, user, pwd);
    }

    private SOSShiroCurrentUserAnswer logout(String accessTokenFromHeader, String accessTokenFromQuery) {

        String accessToken = this.getAccessToken(accessTokenFromHeader, accessTokenFromQuery);

        currentUser = Globals.currentUsersList.getUser(accessToken);

        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = new SOSShiroCurrentUserAnswer(EMPTY_STRING);
        if (currentUser != null) {
            sosShiroCurrentUserAnswer.setUser(UNKNOWN_USER);
            sosShiroCurrentUserAnswer = new SOSShiroCurrentUserAnswer(currentUser.getUsername());
        }
        sosShiroCurrentUserAnswer.setIsAuthenticated(false);
        sosShiroCurrentUserAnswer.setHasRole(false);
        sosShiroCurrentUserAnswer.setIsPermitted(false);
        sosShiroCurrentUserAnswer.setAccessToken(EMPTY_STRING);
        Globals.currentUsersList.removeUser(accessToken);

        if (Globals.currentUsersList.size() == 0 && Globals.sosHibernateConnection != null) {
            Globals.sosHibernateConnection.disconnect();
        }
        if (Globals.sosSchedulerHibernateConnections != null) {
            for (SOSHibernateConnection sosHibernateConnection : Globals.sosSchedulerHibernateConnections.values()) {
                sosHibernateConnection.disconnect();
            }
        }

        return sosShiroCurrentUserAnswer;
    }

    @POST
    @Path("/logout")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSShiroCurrentUserAnswer logoutPost(@HeaderParam(ACCESS_TOKEN) String accessTokenFromHeader) {
        return logout(accessTokenFromHeader, EMPTY_STRING);
    }

    @POST
    @Path("/db_refresh")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse dbRefresh() {
        try {
            if (Globals.sosHibernateConnection != null) {
                Globals.sosHibernateConnection.disconnect();
                Globals.sosHibernateConnection.connect();

            }
            if (Globals.sosSchedulerHibernateConnections != null) {
                for (SOSHibernateConnection sosHibernateConnection : Globals.sosSchedulerHibernateConnections.values()) {
                    sosHibernateConnection.disconnect();
                    sosHibernateConnection.connect();
                }
            }

            return JOCDefaultResponse.responseStatus200("Db connections reconnected");
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e);
        }
    }

    @GET
    @Path("/logout")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSShiroCurrentUserAnswer logoutGet(@HeaderParam(ACCESS_TOKEN) String accessTokenFromHeader, @QueryParam(ACCESS_TOKEN) String accessTokenFromQuery) {
        return logout(accessTokenFromHeader, accessTokenFromQuery);
    }

    @GET
    @Path("/role")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSShiroCurrentUserAnswer hasRole(@HeaderParam(ACCESS_TOKEN) String accessTokenFromHeader, @QueryParam(ACCESS_TOKEN) String accessTokenFromQuery,
            @QueryParam("user") String user, @QueryParam("pwd") String pwd, @QueryParam("role") String role) {

        String accessToken = getAccessToken(accessTokenFromHeader, accessTokenFromQuery);

        setCurrentUserfromAccessToken(accessToken, user, pwd);

        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = new SOSShiroCurrentUserAnswer(currentUser.getUsername());
        sosShiroCurrentUserAnswer.setRole(role);
        sosShiroCurrentUserAnswer.setIsAuthenticated(currentUser.isAuthenticated());
        sosShiroCurrentUserAnswer.setHasRole(currentUser.hasRole(role));
        sosShiroCurrentUserAnswer.setAccessToken(currentUser.getAccessToken());

        return sosShiroCurrentUserAnswer;

    }

    @GET
    @Path("/permission")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSShiroCurrentUserAnswer isPermitted(@HeaderParam(ACCESS_TOKEN) String accessTokenFromHeader, @QueryParam(ACCESS_TOKEN) String accessTokenFromQuery,
            @QueryParam("user") String user, @QueryParam("pwd") String pwd, @QueryParam("permission") String permission) {

        String accessToken = getAccessToken(accessTokenFromHeader, accessTokenFromQuery);
        setCurrentUserfromAccessToken(accessToken, user, pwd);

        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = new SOSShiroCurrentUserAnswer(currentUser.getUsername());
        sosShiroCurrentUserAnswer.setPermission(permission);
        sosShiroCurrentUserAnswer.setIsAuthenticated(currentUser.isAuthenticated());
        sosShiroCurrentUserAnswer.setIsPermitted(currentUser.isPermitted(permission));

        sosShiroCurrentUserAnswer.setAccessToken(currentUser.getAccessToken());

        return sosShiroCurrentUserAnswer;

    }

    private void setCurrentUserfromAccessToken(String accessToken, String user, String pwd) {
        if (Globals.currentUsersList != null && accessToken != null && accessToken.length() > 0) {
            currentUser = Globals.currentUsersList.getUser(accessToken);
        } else {
            if (user != null && user.length() > 0 && pwd != null && pwd.length() > 0) {
                currentUser = new SOSShiroCurrentUser(user, pwd);
                try {
                    createUser();
                } catch (Exception e) {
                    LOGGER.error(e);
                }
            }
        }
        resetTimeOut();
    }

    private String getAccessToken(String accessTokenFromHeader, String accessTokenFromQuery) {
        if (accessTokenFromHeader != null && !EMPTY_STRING.equals(accessTokenFromHeader)) {
            accessTokenFromQuery = accessTokenFromHeader;
        }
        return accessTokenFromQuery;
    }

    private SOSPermissionJocCockpit createPermissionObject(String accessToken, String user, String pwd) {

        this.setCurrentUserfromAccessToken(accessToken, user, pwd);

        ObjectFactory o = new ObjectFactory();
        SOSPermissionJocCockpit sosPermissionJocCockpit = o.createSOSPermissionJocCockpit();

        if (currentUser != null && currentUser.getCurrentSubject() != null) {

            sosPermissionJocCockpit.setIsAuthenticated(currentUser.isAuthenticated());
            sosPermissionJocCockpit.setAccessToken(currentUser.getAccessToken());
            sosPermissionJocCockpit.setUser(currentUser.getUsername());

            SOSPermissionRoles roles = o.createSOSPermissionRoles();
            addRole(roles.getSOSPermissionRole(), ROLE_SUPER);
            addRole(roles.getSOSPermissionRole(), ROLE_ADMIN);
            addRole(roles.getSOSPermissionRole(), ROLE_JOC_ADMIN);
            addRole(roles.getSOSPermissionRole(), ROLE_JOBEDITOR);
            addRole(roles.getSOSPermissionRole(), ROLE_CONTROLLER);
            addRole(roles.getSOSPermissionRole(), ROLE_WORKINGPLAN);
            addRole(roles.getSOSPermissionRole(), ROLE_JID);
            addRole(roles.getSOSPermissionRole(), ROLE_JOE);
            addRole(roles.getSOSPermissionRole(), ROLE_JOC);
            addRole(roles.getSOSPermissionRole(), ROLE_EVENTS);
            addRole(roles.getSOSPermissionRole(), ROLE_ADMINISTRATOR);
            addRole(roles.getSOSPermissionRole(), ROLE_APPLICATION_MANAGER);
            addRole(roles.getSOSPermissionRole(), ROLE_IT_OPERATOR);
            addRole(roles.getSOSPermissionRole(), ROLE_INCIDENT_MANAGER);
            addRole(roles.getSOSPermissionRole(), ROLE_BUSINESS_USER);
            addRole(roles.getSOSPermissionRole(), ROLE_API_USER);

            sosPermissionJocCockpit.setJobschedulerMaster(o.createSOSPermissionJocCockpitJobschedulerMaster());
            sosPermissionJocCockpit.setJobschedulerMasterCluster(o.createSOSPermissionJocCockpitJobschedulerMasterCluster());
            sosPermissionJocCockpit.setJobschedulerUniversalAgent(o.createSOSPermissionJocCockpitJobschedulerUniversalAgent());
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
            sosPermissionJocCockpit.getJobschedulerMaster().setRestart(o.createSOSPermissionJocCockpitJobschedulerMasterRestart());
            sosPermissionJocCockpit.getJobschedulerMasterCluster().setView(o.createSOSPermissionJocCockpitJobschedulerMasterClusterView());
            sosPermissionJocCockpit.getJobschedulerUniversalAgent().setView(o.createSOSPermissionJocCockpitJobschedulerUniversalAgentView());
            sosPermissionJocCockpit.getJobschedulerUniversalAgent().setRestart(o.createSOSPermissionJocCockpitJobschedulerUniversalAgentRestart());

            sosPermissionJocCockpit.getDailyPlan().setView(o.createSOSPermissionJocCockpitDailyPlanView());
            sosPermissionJocCockpit.getOrder().setView(o.createSOSPermissionJocCockpitOrderView());
            sosPermissionJocCockpit.getOrder().setChange(o.createSOSPermissionJocCockpitOrderChange());
            sosPermissionJocCockpit.getOrder().setDelete(o.createSOSPermissionJocCockpitOrderDelete());
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
            sosPermissionJocCockpit.getJobschedulerMaster().getRestart().setAbort(haveRight("sos:products:joc_cockpit:jobscheduler_master:restart:terminate"));
            sosPermissionJocCockpit.getJobschedulerMaster().getRestart().setTerminate(haveRight("sos:products:joc_cockpit:jobscheduler_master:restart:abort"));
            sosPermissionJocCockpit.getJobschedulerMaster().setPause(haveRight("sos:products:joc_cockpit:jobscheduler_master:pause"));
            sosPermissionJocCockpit.getJobschedulerMaster().setContinue(haveRight("sos:products:joc_cockpit:jobscheduler_master:continue"));
            sosPermissionJocCockpit.getJobschedulerMaster().setTerminate(haveRight("sos:products:joc_cockpit:jobscheduler_master:terminate"));
            sosPermissionJocCockpit.getJobschedulerMaster().setAbort(haveRight("sos:products:joc_cockpit:jobscheduler_master:abort"));
            sosPermissionJocCockpit.getJobschedulerMaster().setManageCategories(haveRight("sos:products:joc_cockpit:jobscheduler_master:manage_categories"));

            sosPermissionJocCockpit.getJobschedulerMasterCluster().getView().setClusterStatus(haveRight(
                    "sos:products:joc_cockpit:jobscheduler_master_cluster:view:cluster_status"));
            sosPermissionJocCockpit.getJobschedulerMasterCluster().setTerminateFailSafe(haveRight("sos:products:joc_cockpit:jobscheduler_master_cluster:terminate_fail_safe"));
            sosPermissionJocCockpit.getJobschedulerMasterCluster().setRestart(haveRight("sos:products:joc_cockpit:jobscheduler_master_cluster:restart"));
            sosPermissionJocCockpit.getJobschedulerMasterCluster().setTerminate(haveRight("sos:products:joc_cockpit:jobscheduler_master_cluster:terminate"));

            sosPermissionJocCockpit.getJobschedulerUniversalAgent().getView().setStatus(haveRight("sos:products:joc_cockpit:jobscheduler_universal_agent:view:status"));
            sosPermissionJocCockpit.getJobschedulerUniversalAgent().getRestart().setAbort(haveRight("sos:products:joc_cockpit:jobscheduler_universal_agent:restart:abort"));
            sosPermissionJocCockpit.getJobschedulerUniversalAgent().getRestart().setTerminate(haveRight("sos:products:joc_cockpit:jobscheduler_universal_agent:restart:terminate"));
            sosPermissionJocCockpit.getJobschedulerUniversalAgent().setAbort(haveRight("sos:products:joc_cockpit:jobscheduler_universal_agent:abort"));
            sosPermissionJocCockpit.getJobschedulerUniversalAgent().setTerminate(haveRight("sos:products:joc_cockpit:jobscheduler_universal_agent:terminate"));

            sosPermissionJocCockpit.getDailyPlan().getView().setStatus(haveRight("sos:products:joc_cockpit:daily_plan:view_status"));

            sosPermissionJocCockpit.getHistory().setView(haveRight("sos:products:joc_cockpit:history:view"));

            sosPermissionJocCockpit.getOrder().getView().setStatus(haveRight("sos:products:joc_cockpit:order:view:status"));
            sosPermissionJocCockpit.getOrder().getView().setConfiguration(haveRight("sos:products:joc_cockpit:order:view:configuration"));
            sosPermissionJocCockpit.getOrder().getView().setOrderLog(haveRight("sos:products:joc_cockpit:order:view:order_log"));
            sosPermissionJocCockpit.getOrder().getChange().setStartAndEndNode(haveRight("sos:products:joc_cockpit:order:change:start_and_end_node"));
            sosPermissionJocCockpit.getOrder().getChange().setTimeForAdhocOrder(haveRight("sos:products:joc_cockpit:order:change:time_for_adhoc_orders"));
            sosPermissionJocCockpit.getOrder().getChange().setParameter(haveRight("sos:products:joc_cockpit:order:change:parameter"));
            sosPermissionJocCockpit.getOrder().setStart(haveRight("sos:products:joc_cockpit:order:start"));
            sosPermissionJocCockpit.getOrder().setUpdate(haveRight("sos:products:joc_cockpit:order:update"));
            sosPermissionJocCockpit.getOrder().setSuspend(haveRight("sos:products:joc_cockpit:order:suspend"));
            sosPermissionJocCockpit.getOrder().setResume(haveRight("sos:products:joc_cockpit:order:resume"));
            sosPermissionJocCockpit.getOrder().getDelete().setPermanent(haveRight("sos:products:joc_cockpit:order:delete:permanent"));
            sosPermissionJocCockpit.getOrder().getDelete().setTemporary(haveRight("sos:products:joc_cockpit:order:delete:temporary"));
            sosPermissionJocCockpit.getOrder().setRemoveSetback(haveRight("sos:products:joc_cockpit:order:remove_setback"));
            sosPermissionJocCockpit.getOrder().setReset(haveRight("sos:products:joc_cockpit:order:reset"));
            sosPermissionJocCockpit.getOrder().setSetRunTime(haveRight("sos:products:joc_cockpit:order:set_run_time"));
            sosPermissionJocCockpit.getOrder().setSetState(haveRight("sos:products:joc_cockpit:order:set_state"));

            sosPermissionJocCockpit.getJobChain().getView().setConfiguration(haveRight("sos:products:joc_cockpit:job_chain:view:configuration"));
            sosPermissionJocCockpit.getJobChain().getView().setHistory(haveRight("sos:products:joc_cockpit:job_chain:view:history"));
            sosPermissionJocCockpit.getJobChain().getView().setStatus(haveRight("sos:products:joc_cockpit:job_chain:view:status"));
            sosPermissionJocCockpit.getJobChain().setStop(haveRight("sos:products:joc_cockpit:job_chain:stop"));
            sosPermissionJocCockpit.getJobChain().setUnstop(haveRight("sos:products:joc_cockpit:job_chain:unstop"));
            sosPermissionJocCockpit.getJobChain().setAddOrder(haveRight("sos:products:joc_cockpit:job_chain:add_order"));
            sosPermissionJocCockpit.getJobChain().setSkipJobChainNode(haveRight("sos:products:joc_cockpit:job_chain:skip_jobchain_node"));
            sosPermissionJocCockpit.getJobChain().setUnskipJobChainNode(haveRight("sos:products:joc_cockpit:job_chain:unskip_jobchain_node"));
            sosPermissionJocCockpit.getJobChain().setStopJobChainNode(haveRight("sos:products:joc_cockpit:job_chain:stop_job"));
            sosPermissionJocCockpit.getJobChain().setUnstopJobChainNode(haveRight("sos:products:joc_cockpit:job_chain:unstop_job"));

            sosPermissionJocCockpit.getJob().getView().setStatus(haveRight("sos:products:joc_cockpit:job:view_status"));
            sosPermissionJocCockpit.getJob().getView().setTaskLog(haveRight("sos:products:joc_cockpit:job:view:task_log"));
            sosPermissionJocCockpit.getJob().getView().setConfiguration(haveRight("sos:products:joc_cockpit:job:view:configuration"));
            sosPermissionJocCockpit.getJob().getView().setHistory(haveRight("sos:products:joc_cockpit:job:view:history"));
            sosPermissionJocCockpit.getJob().getStart().setTask(haveRight("sos:products:joc_cockpit:job:start:task"));
            sosPermissionJocCockpit.getJob().getStart().setTaskImmediately(haveRight("sos:products:joc_cockpit:job:start:task_immediately"));
            sosPermissionJocCockpit.getJob().setStop(haveRight("sos:products:joc_cockpit:job:stop"));
            sosPermissionJocCockpit.getJob().setUnstop(haveRight("sos:products:joc_cockpit:job:unstop"));
            sosPermissionJocCockpit.getJob().setTerminate(haveRight("sos:products:joc_cockpit:job:terminate"));
            sosPermissionJocCockpit.getJob().setKill(haveRight("sos:products:joc_cockpit:job:unstop"));
            sosPermissionJocCockpit.getJob().setUnstop(haveRight("sos:products:joc_cockpit:job:kill"));
            sosPermissionJocCockpit.getJob().setSetRunTime(haveRight("sos:products:joc_cockpit:job:set_run_time"));
            sosPermissionJocCockpit.getJob().setEndAllTasks(haveRight("sos:products:joc_cockpit:job:end_all_tasks"));
            sosPermissionJocCockpit.getJob().setSuspendAllTasks(haveRight("sos:products:joc_cockpit:job:suspend_all_tasks"));
            sosPermissionJocCockpit.getJob().setContinueAllTasks(haveRight("sos:products:joc_cockpit:job:continue_all_tasks"));

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
            sosPermissionJocCockpit.getMaintenanceWindow().setEnableDisableMaintenanceWindow(haveRight(
                    "sos:products:joc_cockpit:maintenance_window:enable_disable_mainenance_window"));

        }
        return sosPermissionJocCockpit;
    }

    private boolean haveRight(String permission) {
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

    private void createUser() throws Exception {
        if (Globals.currentUsersList == null) {
            Globals.currentUsersList = new SOSShiroCurrentUsersList();
        }

        SOSlogin sosLogin = new SOSlogin();
        sosLogin.login(currentUser.getUsername(), currentUser.getPassword());

        currentUser.setCurrentSubject(sosLogin.getCurrentUser());

        Session session = sosLogin.getCurrentUser().getSession();
        String accessToken = session.getId().toString();

        currentUser.setAccessToken(accessToken);
        Globals.currentUsersList.addUser(currentUser);

        SOSPermissionJocCockpit sosPermissionJocCockpit = createPermissionObject(accessToken, EMPTY_STRING, EMPTY_STRING);
        currentUser.setSosPermissionJocCockpit(sosPermissionJocCockpit);

        JocCockpitProperties sosShiroProperties = new JocCockpitProperties();

        if (Globals.sosHibernateConnection == null) {
            Globals.sosHibernateConnection = new SOSHibernateConnection(sosShiroProperties.getProperty("hibernate_configuration_file"));
            Globals.sosHibernateConnection.addClassMapping(DBLayer.getInventoryClassMapping());
            Globals.sosHibernateConnection.addClassMapping(DBLayer.getSchedulerClassMapping());
            Globals.sosHibernateConnection.connect();
        }
        
        if (Globals.sosHibernateConnection.getCurrentSession() == null){
            Globals.sosHibernateConnection.connect();
        }
        
        if (Globals.sosSchedulerHibernateConnections != null){
            for (SOSHibernateConnection sosHibernateConnection : Globals.sosSchedulerHibernateConnections.values()) {
                if (sosHibernateConnection.getCurrentSession() == null){
                    sosHibernateConnection.connect();
                }
             }
        }
    }

    private SOSShiroCurrentUser getUserPwdFromHeaderOrQuery(String basicAuthorization, String user, String pwd) {
        String authorization = EMPTY_STRING;
        try {
            if (basicAuthorization != null) {
                String[] authorizationParts = basicAuthorization.split(" ");
                if (authorizationParts.length > 1) {
                    authorization = new String(Base64.getDecoder().decode(authorizationParts[1].getBytes("UTF-8")), "UTF-8");
                }
            }
        } catch (UnsupportedEncodingException e) {
            return null;
        }

        String[] authorizationParts = authorization.split(":");
        if (authorizationParts.length == 2) {
            user = authorizationParts[0];
            pwd = authorizationParts[1];
        }
        return new SOSShiroCurrentUser(user, pwd);
    }

    private SOSShiroCurrentUserAnswer authenticate() throws Exception {

        createUser();

        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = new SOSShiroCurrentUserAnswer(currentUser.getUsername());
        sosShiroCurrentUserAnswer.setIsAuthenticated(currentUser.getCurrentSubject().isAuthenticated());
        sosShiroCurrentUserAnswer.setAccessToken(currentUser.getAccessToken());
        sosShiroCurrentUserAnswer.setUser(currentUser.getUsername());

        return sosShiroCurrentUserAnswer;

    }

    private JOCDefaultResponse login(String basicAuthorization, String user, String pwd) {

        TimeZone.setDefault(TimeZone.getTimeZone(UTC));

        Globals.schedulerObjectFactory = new SchedulerObjectFactory();
        Globals.schedulerObjectFactory.initMarshaller(Spooler.class);
        Globals.schedulerObjectFactory.setOmmitXmlDeclaration(true);

        currentUser = getUserPwdFromHeaderOrQuery(basicAuthorization, user, pwd);
        currentUser.setAuthorization(basicAuthorization);

        if (currentUser == null) {
            return JOCDefaultResponse.responseStatusJSError(USER_IS_NULL + " " + AUTHORIZATION_HEADER_WITH_BASIC_BASED64PART_EXPECTED);
        }

        try {
            SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = authenticate();

            if (!sosShiroCurrentUserAnswer.isAuthenticated()) {
                return JOCDefaultResponse.responseStatus401(sosShiroCurrentUserAnswer);
            } else {
                return JOCDefaultResponse.responseStatus200WithHeaders(sosShiroCurrentUserAnswer, sosShiroCurrentUserAnswer.getAccessToken(), currentUser.getCurrentSubject()
                        .getSession().getTimeout());
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return JOCDefaultResponse.responseStatusJSError(e.getMessage());
        }
    }

    private void resetTimeOut() {
        if (currentUser != null) {
            currentUser.getCurrentSubject().getSession().touch();
        } else {
            LOGGER.warn(USER_IS_NULL);
        }
    }

    private SOSShiroCurrentUserAnswer createSOSShiroCurrentUserAnswer(String accessToken, String user, String message) {
        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = new SOSShiroCurrentUserAnswer();
        sosShiroCurrentUserAnswer.setAccessToken(accessToken);
        sosShiroCurrentUserAnswer.setUser(user);
        sosShiroCurrentUserAnswer.setHasRole(false);
        sosShiroCurrentUserAnswer.setIsAuthenticated(false);
        sosShiroCurrentUserAnswer.setIsPermitted(false);
        sosShiroCurrentUserAnswer.setMessage(message);
        return sosShiroCurrentUserAnswer;
    }

}