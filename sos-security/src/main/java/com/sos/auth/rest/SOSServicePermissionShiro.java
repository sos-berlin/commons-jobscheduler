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

import org.apache.shiro.config.Ini.Section;
import org.apache.shiro.session.ExpiredSessionException;
import org.apache.shiro.session.UnknownSessionException;
import com.sos.auth.rest.permission.model.ObjectFactory;
import com.sos.auth.rest.permission.model.SOSPermissionCommandsMasters;
import com.sos.auth.rest.permission.model.SOSPermissionJocCockpitMasters;
import com.sos.auth.rest.permission.model.SOSPermissionListCommands;
import com.sos.auth.rest.permission.model.SOSPermissionListJoc;
import com.sos.auth.rest.permission.model.SOSPermissionRoles;
import com.sos.auth.rest.permission.model.SOSPermissionShiro;
import com.sos.auth.rest.permission.model.SOSPermissions;
import com.sos.auth.shiro.SOSlogin;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.classes.audit.SecurityAudit;
import com.sos.joc.exceptions.JocAuthenticationException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.SessionNotExistException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/security")
public class SOSServicePermissionShiro {

    private static final String ACCESS_TOKEN = "access_token";
    private static final String X_ACCESS_TOKEN = "X-Access-Token";
    private static final String UTC = "UTC";
    private static final String UNKNOWN_USER = "*Unknown User*";
    private static final String EMPTY_STRING = "";
    private static final String USER_IS_NULL = "user is null";
    private static final String AUTHORIZATION_HEADER_WITH_BASIC_BASED64PART_EXPECTED = "Authorization header with basic based64part expected";
    private static final String ACCESS_TOKEN_EXPECTED = "Access token header expected";
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSPermissionsCreator.class);

    private SOSShiroCurrentUser currentUser;
    private SOSlogin sosLogin;

    public JOCDefaultResponse getJocCockpitMasterPermissions(String accessToken, String user, String pwd) throws JocException {
        this.setCurrentUserfromAccessToken(accessToken, user, pwd);
        SOSPermissionsCreator sosPermissionsCreator = new SOSPermissionsCreator(currentUser);

        SOSPermissionJocCockpitMasters sosPermissionMasters = sosPermissionsCreator.createJocCockpitPermissionMasterObjectList(accessToken);
        return JOCDefaultResponse.responseStatus200(sosPermissionMasters);
    }

    private JOCDefaultResponse getCommandPermissions(String accessToken, String user, String pwd) throws JocException {
        this.setCurrentUserfromAccessToken(accessToken, user, pwd);
        SOSPermissionsCreator sosPermissionsCreator = new SOSPermissionsCreator(currentUser);

        SOSPermissionCommandsMasters sosPermissionCommandsMasters = sosPermissionsCreator.createCommandsPermissionMasterObjectList(accessToken);
        return JOCDefaultResponse.responseStatus200(sosPermissionCommandsMasters);
    }

    @GET
    @Path("/joc_cockpit_permissions")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse getJocCockpitPermissions(@HeaderParam(ACCESS_TOKEN) String accessTokenFromHeader,
            @HeaderParam(X_ACCESS_TOKEN) String xAccessTokenFromHeader, @QueryParam(ACCESS_TOKEN) String accessTokenFromQuery,
            @QueryParam("user") String user, @QueryParam("pwd") String pwd) throws JocException {
        String accessToken = getAccessToken(accessTokenFromHeader, xAccessTokenFromHeader, accessTokenFromQuery);
        return getJocCockpitMasterPermissions(accessToken, user, pwd);
    }

    @POST
    @Path("/joc_cockpit_permissions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postJocCockpitPermissions(@HeaderParam(ACCESS_TOKEN) String accessTokenFromHeader,
            @HeaderParam(X_ACCESS_TOKEN) String xAccessTokenFromHeader) {

        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = new SOSWebserviceAuthenticationRecord();

        try {

            String accessToken = getAccessToken(accessTokenFromHeader, xAccessTokenFromHeader, EMPTY_STRING);
            sosWebserviceAuthenticationRecord.setAccessToken(accessToken);

            setCurrentUserfromAccessToken(sosWebserviceAuthenticationRecord.getAccessToken(), sosWebserviceAuthenticationRecord.getUser(),
                    sosWebserviceAuthenticationRecord.getPassword());

            if (currentUser == null) {
                LOGGER.debug(USER_IS_NULL + " " + AUTHORIZATION_HEADER_WITH_BASIC_BASED64PART_EXPECTED);
                return JOCDefaultResponse.responseStatusJSError(USER_IS_NULL + " " + AUTHORIZATION_HEADER_WITH_BASIC_BASED64PART_EXPECTED);
            }

            return JOCDefaultResponse.responseStatus200(currentUser.getSosPermissionJocCockpitMasters());
        } catch (org.apache.shiro.session.ExpiredSessionException e) {
            LOGGER.error(e.getMessage());
            SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = createSOSShiroCurrentUserAnswer(accessTokenFromHeader,
                    sosWebserviceAuthenticationRecord.getUser(), e.getMessage());
            return JOCDefaultResponse.responseStatus440(sosShiroCurrentUserAnswer);
        }

        catch (Exception ee) {
            LOGGER.error(ee.getMessage());
            return JOCDefaultResponse.responseStatusJSError(ee.getMessage());
        }
    }

    @GET
    @Path("/command_permissions")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse getCommandPermissions(@HeaderParam(ACCESS_TOKEN) String accessTokenFromHeader,
            @HeaderParam(X_ACCESS_TOKEN) String xAccessTokenFromHeader, @QueryParam(ACCESS_TOKEN) String accessTokenFromQuery,
            @QueryParam("user") String user, @QueryParam("pwd") String pwd) throws JocException {
        String accessToken = getAccessToken(accessTokenFromHeader, xAccessTokenFromHeader, accessTokenFromQuery);
        return getCommandPermissions(accessToken, user, pwd);
    }

    @POST
    @Path("/command_permissions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse postCommandPermissions(@HeaderParam(ACCESS_TOKEN) String accessTokenFromHeader,
            @HeaderParam(X_ACCESS_TOKEN) String xAccessTokenFromHeader) {

        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = new SOSWebserviceAuthenticationRecord();

        try {

            String accessToken = getAccessToken(accessTokenFromHeader, xAccessTokenFromHeader, EMPTY_STRING);
            sosWebserviceAuthenticationRecord.setAccessToken(accessToken);

            setCurrentUserfromAccessToken(sosWebserviceAuthenticationRecord.getAccessToken(), sosWebserviceAuthenticationRecord.getUser(),
                    sosWebserviceAuthenticationRecord.getPassword());

            if (currentUser == null) {
                LOGGER.debug(USER_IS_NULL + " " + AUTHORIZATION_HEADER_WITH_BASIC_BASED64PART_EXPECTED);
                return JOCDefaultResponse.responseStatusJSError(USER_IS_NULL + " " + AUTHORIZATION_HEADER_WITH_BASIC_BASED64PART_EXPECTED);
            }

            return JOCDefaultResponse.responseStatus200(currentUser.getSosPermissionCommandsMasters());
        } catch (org.apache.shiro.session.ExpiredSessionException e) {
            LOGGER.error(e.getMessage());
            SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = createSOSShiroCurrentUserAnswer(accessTokenFromHeader,
                    sosWebserviceAuthenticationRecord.getUser(), e.getMessage());
            return JOCDefaultResponse.responseStatus440(sosShiroCurrentUserAnswer);
        }

        catch (Exception ee) {
            LOGGER.error(ee.getMessage());
            return JOCDefaultResponse.responseStatusJSError(ee.getMessage());
        }
    }

    @POST
    @Path("/size")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse getSize() {
        if (Globals.jocWebserviceDataContainer.getCurrentUsersList() == null) {
            return JOCDefaultResponse.responseStatus200(-1);
        } else {
            return JOCDefaultResponse.responseStatus200(Globals.jocWebserviceDataContainer.getCurrentUsersList().size());
        }
    }

    @POST
    @Path("/userbyname")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse getAccessToken(String user) {
        if (Globals.jocWebserviceDataContainer.getCurrentUsersList() != null) {
            SOSShiroCurrentUserAnswer s = Globals.jocWebserviceDataContainer.getCurrentUsersList().getUserByName(user);
            return JOCDefaultResponse.responseStatus200(s);
        } else {
            SOSShiroCurrentUserAnswer s = new SOSShiroCurrentUserAnswer();
            s.setAccessToken("not-valid");
            s.setUser(user);
            return JOCDefaultResponse.responseStatus200(s);
        }

    }

    @POST
    @Path("/userbytoken")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse getAccessToken(@HeaderParam(ACCESS_TOKEN) String accessTokenFromHeader,
            @HeaderParam(X_ACCESS_TOKEN) String xAccessTokenFromHeader) {
        String token = this.getAccessToken(accessTokenFromHeader, xAccessTokenFromHeader, "");
        if (Globals.jocWebserviceDataContainer.getCurrentUsersList() != null) {

            SOSShiroCurrentUserAnswer s = Globals.jocWebserviceDataContainer.getCurrentUsersList().getUserByToken(token);
            return JOCDefaultResponse.responseStatus200(s);
        } else {
            SOSShiroCurrentUserAnswer s = new SOSShiroCurrentUserAnswer();
            s.setAccessToken("not-valid");
            return JOCDefaultResponse.responseStatus200(s);
        }
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse loginPost(@HeaderParam("Authorization") String basicAuthorization, @QueryParam("user") String user,
            @QueryParam("pwd") String pwd) throws JocException, SOSHibernateException {
        return login(basicAuthorization, user, pwd);
    }

    private JOCDefaultResponse logout(String accessToken) {

        if (accessToken == null || accessToken.isEmpty()) {
            return JOCDefaultResponse.responseStatusJSError(ACCESS_TOKEN_EXPECTED);
        }

        if (Globals.jocWebserviceDataContainer.getCurrentUsersList() != null) {
            currentUser = Globals.jocWebserviceDataContainer.getCurrentUsersList().getUser(accessToken);
        }
        String user = "";
        String comment = "";
        if (currentUser != null) {
            user = currentUser.getUsername();
        }
        LOGGER.debug(String.format("Method: %s, User: %s, access_token: %s", "logout", user, accessToken));
        SOSShiroSession sosShiroSession = new SOSShiroSession(currentUser);
        try {

            if (currentUser == null || currentUser.getCurrentSubject() == null) {
                comment = "Session time out";
                throw new SessionNotExistException("Session doesn't exist");
            }

            sosShiroSession.getTimeout();

        } catch (ExpiredSessionException ex) {
            comment = "Session time out: " + ex.getMessage();
        } catch (SessionNotExistException e) {
            comment = "Session time out: " + e.getMessage();
        } catch (UnknownSessionException u) {
            comment = "Session time out: " + u.getMessage();
        }

        if (currentUser != null && currentUser.getCurrentSubject() != null) {
            JocAuditLog jocAuditLog = new JocAuditLog(user, "./logout");
            SecurityAudit s = new SecurityAudit(comment);
            jocAuditLog.logAuditMessage(s);
            // jocAuditLog.storeAuditLogEntry(s);
            try {

                Globals.forceClosingHttpClients(currentUser, accessToken);
                sosShiroSession.getTimeout();
                sosShiroSession.stop();

            } catch (Exception e) {
            }
        } else {
            LOGGER.warn(String.format("Unknown User --> Method: %s, access_token: %s", "logout", accessToken));
        }

        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = new SOSShiroCurrentUserAnswer(EMPTY_STRING);
        if (currentUser != null) {
            sosShiroCurrentUserAnswer.setUser(UNKNOWN_USER);
            sosShiroCurrentUserAnswer = new SOSShiroCurrentUserAnswer(currentUser.getUsername());
        }
        sosShiroCurrentUserAnswer.setIsAuthenticated(false);
        sosShiroCurrentUserAnswer.setHasRole(false);
        sosShiroCurrentUserAnswer.setIsPermitted(false);
        sosShiroCurrentUserAnswer.setAccessToken(EMPTY_STRING);
        if (Globals.jocWebserviceDataContainer.getCurrentUsersList() != null) {
            Globals.jocWebserviceDataContainer.getCurrentUsersList().removeUser(accessToken);
        }

        return JOCDefaultResponse.responseStatus200(sosShiroCurrentUserAnswer);
    }

    @POST
    @Path("/logout")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse logoutPost(@HeaderParam(ACCESS_TOKEN) String accessTokenFromHeader,
            @HeaderParam(X_ACCESS_TOKEN) String xAccessTokenFromHeader) {
        String accessToken = getAccessToken(accessTokenFromHeader, xAccessTokenFromHeader, EMPTY_STRING);
        return logout(accessToken);
    }

    @POST
    @Path("/db_refresh")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse dbRefresh() {
        try {
            if (Globals.sosHibernateFactory != null) {
                Globals.sosHibernateFactory.close();
                Globals.sosHibernateFactory.build();

            }
            if (Globals.sosSchedulerHibernateFactories != null) {
                for (SOSHibernateFactory sosHibernateFactory : Globals.sosSchedulerHibernateFactories.values()) {
                    sosHibernateFactory.close();
                    sosHibernateFactory.build();
                }
            }

            return JOCDefaultResponse.responseStatus200("Db connections reconnected");
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e);
        }
    }

    @GET
    @Path("/role")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSShiroCurrentUserAnswer hasRole(@HeaderParam(ACCESS_TOKEN) String accessTokenFromHeader,
            @HeaderParam(X_ACCESS_TOKEN) String xAccessTokenFromHeader, @QueryParam(ACCESS_TOKEN) String accessTokenFromQuery,
            @QueryParam("user") String user, @QueryParam("pwd") String pwd, @QueryParam("role") String role) throws SessionNotExistException {

        String accessToken = getAccessToken(accessTokenFromHeader, xAccessTokenFromHeader, accessTokenFromQuery);

        setCurrentUserfromAccessToken(accessToken, user, pwd);

        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = new SOSShiroCurrentUserAnswer(currentUser.getUsername());
        sosShiroCurrentUserAnswer.setRole(role);
        sosShiroCurrentUserAnswer.setIsAuthenticated(currentUser.isAuthenticated());
        sosShiroCurrentUserAnswer.setHasRole(currentUser.hasRole(role));
        sosShiroCurrentUserAnswer.setAccessToken(currentUser.getAccessToken());

        return sosShiroCurrentUserAnswer;

    }

    @POST
    @Path("/permission")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSShiroCurrentUserAnswer isPermitted(@HeaderParam(ACCESS_TOKEN) String accessTokenFromHeader,
            @HeaderParam(X_ACCESS_TOKEN) String xAccessTokenFromHeader, @QueryParam(ACCESS_TOKEN) String accessTokenFromQuery,
            @QueryParam("user") String user, @QueryParam("pwd") String pwd, @QueryParam("permission") String permission)
            throws SessionNotExistException {

        String accessToken = getAccessToken(accessTokenFromHeader, xAccessTokenFromHeader, accessTokenFromQuery);
        setCurrentUserfromAccessToken(accessToken, user, pwd);

        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = new SOSShiroCurrentUserAnswer(currentUser.getUsername());
        sosShiroCurrentUserAnswer.setPermission(permission);
        sosShiroCurrentUserAnswer.setIsAuthenticated(currentUser.isAuthenticated());
        sosShiroCurrentUserAnswer.setIsPermitted(currentUser.isPermitted(permission));

        sosShiroCurrentUserAnswer.setAccessToken(currentUser.getAccessToken());

        return sosShiroCurrentUserAnswer;

    }

    private void setCurrentUserfromAccessToken(String accessToken, String user, String pwd) throws SessionNotExistException {
        if (Globals.jocWebserviceDataContainer.getCurrentUsersList() != null && accessToken != null && accessToken.length() > 0) {
            currentUser = Globals.jocWebserviceDataContainer.getCurrentUsersList().getUser(accessToken);
            LOGGER.debug(String.format("Method: %s, access_token: %s", "setCurrentUserfromAccessToken", accessToken));
        } else {
            if (user != null && user.length() > 0 && pwd != null && pwd.length() > 0) {
                LOGGER.debug(String.format("Method: %s, User: %s, access_token: %s", "setCurrentUserfromAccessToken", user, accessToken));
                currentUser = new SOSShiroCurrentUser(user, pwd);
                try {
                    createUser();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
        resetTimeOut();
    }

    @POST
    @Path("/permissions")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SOSPermissionShiro getPermissions(@HeaderParam(ACCESS_TOKEN) String accessTokenFromHeader,
            @HeaderParam(X_ACCESS_TOKEN) String xAccessTokenFromHeader, @QueryParam(ACCESS_TOKEN) String accessTokenFromQuery,
            @QueryParam("forUser") Boolean forUser, @QueryParam("user") String user, @QueryParam("pwd") String pwd) throws SessionNotExistException {

        String accessToken = this.getAccessToken(accessTokenFromHeader, xAccessTokenFromHeader, accessTokenFromQuery);
        this.setCurrentUserfromAccessToken(accessToken, user, pwd);
        SOSPermissionsCreator sosPermissionsCreator = new SOSPermissionsCreator(currentUser);

        if (forUser == null) {
            forUser = false;
        }

        ObjectFactory o = new ObjectFactory();
        SOSPermissionShiro sosPermissionShiro = o.createSOSPermissionShiro();

        if (currentUser != null && currentUser.getCurrentSubject() != null) {

            sosPermissionShiro.setAuthenticated(currentUser.isAuthenticated());
            sosPermissionShiro.setAccessToken(currentUser.getAccessToken());
            sosPermissionShiro.setUser(currentUser.getUsername());

            SOSPermissionRoles roles = sosPermissionsCreator.getRoles(forUser);

            SOSPermissions sosPermissions = o.createSOSPermissions();

            SOSPermissionListJoc sosPermissionJoc = o.createSOSPermissionListJoc();

            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:joc:view:log");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_master:view:status");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_master:view:parameter");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_master:view:mainlog");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_master:execute:restart:terminate");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_master:execute:restart:abort");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_master:execute:pause");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_master:execute:continue");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_master:execute:terminate");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_master:execute:abort");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_master:execute:stop");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(),
                    "sos:products:joc_cockpit:jobscheduler_master:administration:manage_categories");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(),
                    "sos:products:joc_cockpit:jobscheduler_master:administration:edit_permissions");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(),
                    "sos:products:joc_cockpit:jobscheduler_master:administration:remove_old_instances");

            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_master_cluster:view:status");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(),
                    "sos:products:joc_cockpit:jobscheduler_master_cluster:execute:terminate_fail_safe");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_master_cluster:execute:restart");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_master_cluster:execute:terminate");

            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_universal_agent:view:status");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(),
                    "sos:products:joc_cockpit:jobscheduler_universal_agent:execute:restart:abort");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(),
                    "sos:products:joc_cockpit:jobscheduler_universal_agent:execute:restart:terminate");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_universal_agent:execute:abort");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:jobscheduler_universal_agent:execute:terminate");

            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:daily_plan:view:status");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:history:view:status");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:documentation:view");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:documentation:import");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:documentation:export");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:documentation:delete");

            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:view:status");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:view:configuration");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:view:order_log");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:view:documentation");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:change:start_and_end_node");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:change:time_for_adhoc_orders");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:change:parameter");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:change:run_time");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:change:state");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:change:hot_folder");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:execute:start");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:execute:update");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:execute:suspend");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:execute:resume");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:execute:reset");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:execute:remove_setback");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:delete:permanent");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:delete:temporary");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:assign_documentation");
            
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:view:configuration");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:view:history");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:view:status");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:view:documentation");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:execute:stop");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:execute:unstop");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:execute:add_order");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:execute:skip_jobchain_node");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:execute:process_jobchain_node");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:execute:stop_jobchain_node");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:change:hot_folder");            
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:assign_documentation");

            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:view:status");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:view:task_log");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:view:configuration");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:view:history");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:view:documentation");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:change:run_time");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:change:hot_folder");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:execute:start");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:execute:stop");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:execute:unstop");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:execute:kill");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:execute:terminate");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:execute:end_all_tasks");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:execute:suspend_all_tasks");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:execute:continue_all_tasks");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:assign_documentation");
            
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:process_class:view:status");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:process_class:view:configuration");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:process_class:view:documentation");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:process_class:change:hot_folder");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:process_class:assign_documentation");
            
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:schedule:view:configuration");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:schedule:view:status");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:schedule:view:documentation");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:schedule:remove");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:schedule:change:add_substitute");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:schedule:change:edit_content");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:schedule:change:hot_folder");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:schedule:assign_documentation");

            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:lock:view:configuration");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:lock:view:status");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:lock:view:documentation");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:lock:change:hot_folder");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:lock:assign_documentation");

            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:event:view:status");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:event:execute:delete");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:event:execute:add");

            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:holiday_calendar:view:status");

            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:maintenance_window:view:status");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(),
                    "sos:products:joc_cockpit:maintenance_window:enable_disable_maintenance_window");

            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:audit_log:view:status");

            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:customization:share:view:status");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:customization:share:change:delete");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:customization:share:change:edit_content");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(),
                    "sos:products:joc_cockpit:customization:share:change:shared_status:make_private");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(),
                    "sos:products:joc_cockpit:customization:share:change:shared_status:make_share");

            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:yade:view:status");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:yade:view:files");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:yade:execute:transfer_start");

            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:calendar:view:status");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:calendar:view:documentation");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:calendar:edit:change");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:calendar:edit:delete");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:calendar:edit:create");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:calendar:assign:change");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:calendar:assign:nonworking");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:calendar:assign:runtime");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:calendar:assign_documentation");

            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:runtime:execute:edit_xml");

            sosPermissions.setSOSPermissionListJoc(sosPermissionJoc);
            SOSPermissionListCommands sosPermissionCommands = o.createSOSPermissionListCommands();
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:jobscheduler_master:view:status");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:jobscheduler_master:view:calendar");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:jobscheduler_master:view:parameter");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:jobscheduler_master:execute:restart:terminate");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:jobscheduler_master:execute:restart:abort");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:jobscheduler_master:execute:pause");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:jobscheduler_master:execute:continue");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:jobscheduler_master:execute:terminate");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:jobscheduler_master:execute:abort");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:jobscheduler_master:execute:stop");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(),
                    "sos:products:commands:jobscheduler_master:administration:manage_categories");

            addPermission(forUser, sosPermissionCommands.getSOSPermission(),
                    "sos:products:commands:jobscheduler_master_cluster:execute:terminate_fail_safe");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:jobscheduler_master_cluster:execute:restart");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:jobscheduler_master_cluster:execute:terminate");

            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:history:view");

            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:order:view:status");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:order:execute:start");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:order:execute:update");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:order:execute:suspend");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:order:execute:resume");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:order:execute:reset");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:order:execute:remove_setback");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:order:delete");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:order:change:start_and_end_node");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:order:change:time_for_adhoc_orders");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:order:change:parameter");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:order:change:run_time");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:order:change:state");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:order:change:other");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:order:change:hot_folder");

            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:job_chain:view:status");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:job_chain:execute:stop");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:job_chain:execute:unstop");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:job_chain:execute:add_order");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:job_chain:execute:skip_jobchain_node");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:job_chain:execute:process_jobchain_node");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:job_chain:execute:stop_jobchain_node");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:job_chain:change:hot_folder");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:job_chain:remove");

            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:job:view:status");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:job:execute:start");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:job:execute:stop");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:job:execute:unstop");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:job:execute:terminate");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:job:execute:kill");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:job:execute:end_all_tasks");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:job:execute:suspend_all_tasks");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:job:execute:continue_all_tasks");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:job:change:run_time");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:job:change:hot_folder");

            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:process_class:view:status");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:process_class:change:edit_content");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:process_class:remove");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:process_class:change:hot_folder");

            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:schedule:view:status");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:schedule:change:add_substitute");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:schedule:change:hot_folder");

            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:lock:view:status");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:lock:remove");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:lock:change:edit_content");
            addPermission(forUser, sosPermissionCommands.getSOSPermission(), "sos:products:commands:lock:change:hot_folder");

            sosPermissions.setSOSPermissionListCommands(sosPermissionCommands);

            sosPermissionShiro.setSOSPermissionRoles(roles);
            sosPermissionShiro.setSOSPermissions(sosPermissions);
        }
        return sosPermissionShiro;

    }

    private String getAccessToken(String accessTokenFromHeader, String xAccessTokenFromHeader, String accessTokenFromQuery) {
        if (xAccessTokenFromHeader != null && !EMPTY_STRING.equals(xAccessTokenFromHeader)) {
            accessTokenFromQuery = xAccessTokenFromHeader;
        } else {
            if (accessTokenFromHeader != null && !EMPTY_STRING.equals(accessTokenFromHeader)) {
                accessTokenFromQuery = accessTokenFromHeader;
            }
        }
        return accessTokenFromQuery;
    }

    private boolean isPermitted(String permission) {
        return (currentUser != null && currentUser.isPermitted(permission) && currentUser.isAuthenticated());
    }

    private void addPermission(Boolean forUser, List<String> sosPermission, String permission) {
        if (!forUser || isPermitted(permission)) {
            sosPermission.add(permission);
        }
    }

    private void createUser() throws Exception {
        if (Globals.jocWebserviceDataContainer.getCurrentUsersList() == null) {
            Globals.jocWebserviceDataContainer.setCurrentUsersList(new SOSShiroCurrentUsersList());
        }
        SOSPermissionsCreator sosPermissionsCreator = new SOSPermissionsCreator(currentUser);
        sosLogin = new SOSlogin(Globals.getShiroIniSecurityManagerFactory());
        sosLogin.login(currentUser.getUsername(), currentUser.getPassword());

        currentUser.setCurrentSubject(sosLogin.getCurrentUser());

        if (sosLogin.getCurrentUser() == null) {
            SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = new SOSShiroCurrentUserAnswer(currentUser.getUsername());
            sosShiroCurrentUserAnswer.setIsAuthenticated(false);
            sosShiroCurrentUserAnswer.setMessage(String.format("%s: Could not login with user: %s password:*******", sosLogin.getMsg(), currentUser
                    .getUsername()));
            throw new JocAuthenticationException(sosShiroCurrentUserAnswer);
        }

        SOSShiroSession sosShiroSession = new SOSShiroSession(currentUser);
        String accessToken = sosShiroSession.getId().toString();

        currentUser.setAccessToken(accessToken);
        Globals.jocWebserviceDataContainer.getCurrentUsersList().addUser(currentUser);

        SOSPermissionJocCockpitMasters sosPermissionJocCockpitMasters = sosPermissionsCreator.createJocCockpitPermissionMasterObjectList(accessToken);
        currentUser.setSosPermissionJocCockpitMasters(sosPermissionJocCockpitMasters);

        currentUser.initFolders();

        Section s = sosPermissionsCreator.getIni().getSection("folders");
        if (s != null) {
            for (String role : s.keySet()) {
                currentUser.addFolder(role, s.get(role));
            }
        }

        SOSPermissionCommandsMasters sosPermissionCommandsMasters = sosPermissionsCreator.createCommandsPermissionMasterObjectList(accessToken);
        currentUser.setSosPermissionCommandsMasters(sosPermissionCommandsMasters);

        if (Globals.sosShiroProperties == null) {
            Globals.sosShiroProperties = new JocCockpitProperties();
        }

    }

    private SOSShiroCurrentUser getUserPwdFromHeaderOrQuery(String basicAuthorization, String user, String pwd) throws UnsupportedEncodingException,
            JocException {
        String authorization = EMPTY_STRING;

        if (basicAuthorization != null) {
            String[] authorizationParts = basicAuthorization.split(" ");
            if (authorizationParts.length > 1) {
                authorization = new String(Base64.getDecoder().decode(authorizationParts[1].getBytes("UTF-8")), "UTF-8");
            }
        } else {
            JocError error = new JocError();
            error.setMessage("The Header Authorization with the Base64 encoded authorization string is missing");
            throw new JocException(error);
        }

        int idx = authorization.indexOf(':');
        if (idx == -1) {
            if (!basicAuthorization.isEmpty()) {
                user = authorization;
            }
        } else {
            user = authorization.substring(0, idx);
            pwd = authorization.substring(idx + 1);
        }

        return new SOSShiroCurrentUser(user, pwd, authorization);
    }

    private SOSShiroCurrentUserAnswer authenticate() throws Exception {

        createUser();
        SOSShiroSession sosShiroSession = new SOSShiroSession(currentUser);

        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = new SOSShiroCurrentUserAnswer(currentUser.getUsername());
        sosShiroCurrentUserAnswer.setIsAuthenticated(currentUser.getCurrentSubject().isAuthenticated());
        sosShiroCurrentUserAnswer.setAccessToken(currentUser.getAccessToken());
        sosShiroCurrentUserAnswer.setUser(currentUser.getUsername());
        sosShiroCurrentUserAnswer.setSessionTimeout(sosShiroSession.getTimeout());

        boolean enableTouch = "true".equals(Globals.sosShiroProperties.getProperty(WebserviceConstants.ENABLE_SESSION_TOUCH,
                WebserviceConstants.ENABLE_SESSION_TOUCH_DEFAULT));
        sosShiroCurrentUserAnswer.setEnableTouch(enableTouch);

        return sosShiroCurrentUserAnswer;

    }

    private String getRolesAsString(boolean forUser) {
        SOSPermissionsCreator sosPermissionsCreator = new SOSPermissionsCreator(currentUser);

        String roles = "  ";
        if (currentUser != null) {
            SOSPermissionRoles listOfRoles = sosPermissionsCreator.getRoles(forUser);
            for (int i = 0; i < listOfRoles.getSOSPermissionRole().size(); i++) {
                roles = roles + listOfRoles.getSOSPermissionRole().get(i) + ",";
            }
            return roles.substring(0, roles.length() - 1).trim();
        } else {
            return EMPTY_STRING;
        }
    }

    private JOCDefaultResponse login(String basicAuthorization, String user, String pwd) throws JocException, SOSHibernateException {

        Globals.sosShiroProperties = new JocCockpitProperties();
        Globals.jocTimeZone = TimeZone.getDefault();
        Globals.setProperties();
        SOSHibernateSession sosHibernateSession = null;

        if (basicAuthorization.isEmpty()) {
            String s = user + ":" + pwd;
            byte[] authEncBytes = org.apache.commons.codec.binary.Base64.encodeBase64(s.getBytes());
            String authStringEnc = new String(authEncBytes);
            basicAuthorization = "Basic " + authStringEnc;
        }

        try {

            sosHibernateSession = Globals.createSosHibernateStatelessConnection("JOC: Login");
            TimeZone.setDefault(TimeZone.getTimeZone(UTC));

            SOSShiroIniShare sosShiroIniShare = new SOSShiroIniShare(sosHibernateSession);
            sosShiroIniShare.provideIniFile();

            currentUser = getUserPwdFromHeaderOrQuery(basicAuthorization, user, pwd);

            if (currentUser == null || currentUser.getAuthorization() == null) {
                return JOCDefaultResponse.responseStatusJSError(USER_IS_NULL + " " + AUTHORIZATION_HEADER_WITH_BASIC_BASED64PART_EXPECTED);
            }

            currentUser.setAuthorization(basicAuthorization);
            SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = authenticate();
            LOGGER.debug(String.format("Method: %s, User: %s, access_token: %s", "login", currentUser.getUsername(), currentUser.getAccessToken()));

            Globals.jocWebserviceDataContainer.getCurrentUsersList().removeTimedOutUser(currentUser.getUsername());

            JocAuditLog jocAuditLog = new JocAuditLog(currentUser.getUsername(), "./login");
            SecurityAudit s = new SecurityAudit(getRolesAsString(true));
            jocAuditLog.logAuditMessage(s);
            // jocAuditLog.storeAuditLogEntry(s);

            if (!sosShiroCurrentUserAnswer.isAuthenticated()) {
                if (sosLogin != null) {
                    LOGGER.info(sosLogin.getMsg());
                }
                return JOCDefaultResponse.responseStatus401(sosShiroCurrentUserAnswer);
            } else {
                SOSShiroSession sosShiroSession = new SOSShiroSession(currentUser);
                return JOCDefaultResponse.responseStatus200WithHeaders(sosShiroCurrentUserAnswer, sosShiroCurrentUserAnswer.getAccessToken(),
                        sosShiroSession.getTimeout());
            }

        } catch (JocAuthenticationException e) {
            return JOCDefaultResponse.responseStatus401(e.getSosShiroCurrentUserAnswer());
        } catch (UnsupportedEncodingException e) {
            return JOCDefaultResponse.responseStatusJSError(AUTHORIZATION_HEADER_WITH_BASIC_BASED64PART_EXPECTED);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    private void resetTimeOut() throws SessionNotExistException {

        if (currentUser != null) {
            SOSShiroSession sosShiroSession = new SOSShiroSession(currentUser);
            sosShiroSession.touch();

        } else {
            LOGGER.error(USER_IS_NULL);
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