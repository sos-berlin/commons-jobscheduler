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

import org.apache.log4j.Logger;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.Ini.Section;
import org.apache.shiro.session.ExpiredSessionException;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;

import com.sos.auth.rest.permission.model.ObjectFactory;
import com.sos.auth.rest.permission.model.SOSPermissionCommands;
import com.sos.auth.rest.permission.model.SOSPermissionJocCockpit;
import com.sos.auth.rest.permission.model.SOSPermissionListCommands;
import com.sos.auth.rest.permission.model.SOSPermissionListJoc;
import com.sos.auth.rest.permission.model.SOSPermissionRoles;
import com.sos.auth.rest.permission.model.SOSPermissionShiro;
import com.sos.auth.rest.permission.model.SOSPermissions;
import com.sos.auth.shiro.SOSlogin;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.classes.audit.SecurityAudit;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.SessionNotExistException;

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
    private static final Logger LOGGER = Logger.getLogger(SOSServicePermissionShiro.class);

    private SOSShiroCurrentUser currentUser;
    private SOSlogin sosLogin;
    private SOSPermissionRoles roles;
    private Ini ini;

    public JOCDefaultResponse getJocCockpitPermissions(String accessToken, String user, String pwd) {
        this.setCurrentUserfromAccessToken(accessToken, user, pwd);
        SOSPermissionJocCockpit sosPermissionJocCockpit = createJocCockpitPermissionObject(accessToken, user, pwd);
        if (currentUser.getSelectedInstance() != null) {
            sosPermissionJocCockpit.setJobschedulerId(currentUser.getSelectedInstance().getSchedulerId());
            if (currentUser.getSelectedInstance().getPrecedence() == null) {
                sosPermissionJocCockpit.setPrecedence(-1);
            } else {
                sosPermissionJocCockpit.setPrecedence(currentUser.getSelectedInstance().getPrecedence());
            }
        }
        return JOCDefaultResponse.responseStatus200(sosPermissionJocCockpit);
    }

    private JOCDefaultResponse getCommandPermissions(String accessToken, String user, String pwd) {
        this.setCurrentUserfromAccessToken(accessToken, user, pwd);
        return JOCDefaultResponse.responseStatus200(createCommandsPermissionObject(accessToken, user, pwd));
    }

    @GET
    @Path("/joc_cockpit_permissions")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public JOCDefaultResponse getJocCockpitPermissions(@HeaderParam(ACCESS_TOKEN) String accessTokenFromHeader,
            @HeaderParam(X_ACCESS_TOKEN) String xAccessTokenFromHeader, @QueryParam(ACCESS_TOKEN) String accessTokenFromQuery,
            @QueryParam("user") String user, @QueryParam("pwd") String pwd) {
        String accessToken = getAccessToken(accessTokenFromHeader, xAccessTokenFromHeader, accessTokenFromQuery);
        return getJocCockpitPermissions(accessToken, user, pwd);
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

            return JOCDefaultResponse.responseStatus200(currentUser.getSosPermissionJocCockpit());
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
            @QueryParam("user") String user, @QueryParam("pwd") String pwd) {
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

            return JOCDefaultResponse.responseStatus200(currentUser.getSosPermissionCommands());
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
        if (Globals.jocWebserviceDataContainer.getCurrentUsersList() != null) {
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
            @QueryParam("pwd") String pwd) {
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
        try {

            if (currentUser == null || currentUser.getCurrentSubject() == null) {
                comment = "Session time out";
                throw new SessionNotExistException("Session doesn't exist");
            }

            currentUser.getCurrentSubject().getSession().getTimeout();

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
            jocAuditLog.storeAuditLogEntry(s);
            try {
                Globals.forceClosingHttpClients(currentUser.getCurrentSubject().getSession(false));
                currentUser.getCurrentSubject().getSession().getTimeout();
                currentUser.getCurrentSubject().getSession().stop();

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
            @QueryParam("user") String user, @QueryParam("pwd") String pwd, @QueryParam("role") String role) {

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
            @QueryParam("user") String user, @QueryParam("pwd") String pwd, @QueryParam("permission") String permission) {

        String accessToken = getAccessToken(accessTokenFromHeader, xAccessTokenFromHeader, accessTokenFromQuery);
        setCurrentUserfromAccessToken(accessToken, user, pwd);

        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = new SOSShiroCurrentUserAnswer(currentUser.getUsername());
        sosShiroCurrentUserAnswer.setPermission(permission);
        sosShiroCurrentUserAnswer.setIsAuthenticated(currentUser.isAuthenticated());
        sosShiroCurrentUserAnswer.setIsPermitted(currentUser.isPermitted(permission));

        sosShiroCurrentUserAnswer.setAccessToken(currentUser.getAccessToken());

        return sosShiroCurrentUserAnswer;

    }

    private void setCurrentUserfromAccessToken(String accessToken, String user, String pwd) {
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
                    LOGGER.error(e);
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
            @QueryParam("forUser") Boolean forUser, @QueryParam("user") String user, @QueryParam("pwd") String pwd) {

        String accessToken = this.getAccessToken(accessTokenFromHeader, xAccessTokenFromHeader, accessTokenFromQuery);
        this.setCurrentUserfromAccessToken(accessToken, user, pwd);

        if (forUser == null) {
            forUser = false;
        }

        ObjectFactory o = new ObjectFactory();
        SOSPermissionShiro sosPermissionShiro = o.createSOSPermissionShiro();

        if (currentUser != null && currentUser.getCurrentSubject() != null) {

            sosPermissionShiro.setAuthenticated(currentUser.isAuthenticated());
            sosPermissionShiro.setAccessToken(currentUser.getAccessToken());
            sosPermissionShiro.setUser(currentUser.getUsername());

            SOSPermissionRoles roles = getRoles(forUser);

            SOSPermissions sosPermissions = o.createSOSPermissions();

            SOSPermissionListJoc sosPermissionJoc = o.createSOSPermissionListJoc();

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
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:view:status");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:view:configuration");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:order:view:order_log");
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
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:view:configuration");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:view:history");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:view:status");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:execute:stop");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:execute:unstop");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:execute:add_order");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:execute:skip_jobchain_node");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:execute:process_jobchain_node");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:execute:stop_jobchain_node");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job_chain:change:hot_folder");

            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:view:status");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:view:task_log");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:view:configuration");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:job:view:history");
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

            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:process_class:view:status");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:process_class:view:configuration");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:process_class:change:hot_folder");

            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:schedule:view:configuration");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:schedule:view:status");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:schedule:remove");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:schedule:change:add_substitute");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:schedule:change:edit_content");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:schedule:change:hot_folder");

            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:lock:view:configuration");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:lock:view:status");
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:lock:change:hot_folder");

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

            sosPermissions.setSOSPermissionListJoc(sosPermissionJoc);
            SOSPermissionListCommands sosPermissionCommands = o.createSOSPermissionListCommands();
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:jobscheduler_master:view:status");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:jobscheduler_master:view:calendar");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:jobscheduler_master:view:parameter");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:jobscheduler_master:execute:restart:terminate");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:jobscheduler_master:execute:restart:abort");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:jobscheduler_master:execute:pause");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:jobscheduler_master:execute:continue");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:jobscheduler_master:execute:terminate");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:jobscheduler_master:execute:abort");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:jobscheduler_master:execute:stop");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:jobscheduler_master:administration:manage_categories");

            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:jobscheduler_master_cluster:execute:terminate_fail_safe");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:jobscheduler_master_cluster:execute:restart");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:jobscheduler_master_cluster:execute:terminate");

            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:history:view");

            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:order:view:status");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:order:execute:start");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:order:execute:update");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:order:execute:suspend");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:order:execute:resume");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:order:execute:reset");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:order:execute:remove_setback");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:order:delete");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:order:change:start_and_end_node");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:order:change:time_for_adhoc_orders");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:order:change:parameter");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:order:change:run_time");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:order:change:state");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:order:change:other");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:order:change:hot_folder");

            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:job_chain:view:status");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:job_chain:execute:stop");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:job_chain:execute:unstop");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:job_chain:execute:add_order");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:job_chain:execute:skip_jobchain_node");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:job_chain:execute:process_jobchain_node");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:job_chain:execute:stop_jobchain_node");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:job_chain:change:hot_folder");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:job_chain:remove");

            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:job:view:status");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:job:execute:start");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:job:execute:stop");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:job:execute:unstop");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:job:execute:terminate");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:job:execute:kill");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:job:execute:end_all_tasks");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:job:execute:suspend_all_tasks");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:job:execute:continue_all_tasks");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:job:change:run_time");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:job:change:hot_folder");

            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:process_class:view:status");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:process_class:change:edit_content");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:process_class:remove");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:process_class:change:hot_folder");

            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:schedule:view:status");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:schedule:change:add_substitute");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:schedule:change:hot_folder");

            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:lock:view:status");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:lock:remove");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:lock:change:edit_content");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:commands:lock:change:hot_folder");
            
            
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:joc_cockpit:yade:view:status");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:joc_cockpit:yade:view:transfers");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:joc_cockpit:yade:view:files");
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:joc_cockpit:yade:execute:transfer_start");
            
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:joc_cockpit:calendar:view:status");            
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:joc_cockpit:calendar:edit:change");            
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:joc_cockpit:calendar:edit:delete");            
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:joc_cockpit:calendar:edit:create");            
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:joc_cockpit:calendar:assign:change");            
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:joc_cockpit:calendar:assign:nonworking");            
            addPermission(forUser,sosPermissionCommands.getSOSPermission(), "sos:products:joc_cockpit:calendar:assign:runtime");            
            
            addPermission(forUser, sosPermissionJoc.getSOSPermission(), "sos:products:joc_cockpit:runtime:execute:edit_xml");

            
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

    private SOSPermissionJocCockpit getSosPermissionJocCockpit() {

        ObjectFactory o = new ObjectFactory();

        SOSPermissionJocCockpit sosPermissionJocCockpit = o.createSOSPermissionJocCockpit();

        if (currentUser != null && currentUser.getCurrentSubject() != null) {

            sosPermissionJocCockpit.setIsAuthenticated(currentUser.isAuthenticated());
            sosPermissionJocCockpit.setAccessToken(currentUser.getAccessToken());
            sosPermissionJocCockpit.setUser(currentUser.getUsername());

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
            sosPermissionJocCockpit.setHolidayCalendar(o.createSOSPermissionJocCockpitHolidayCalendar());
            sosPermissionJocCockpit.setAuditLog(o.createSOSPermissionJocCockpitAuditLog());
            sosPermissionJocCockpit.setMaintenanceWindow(o.createSOSPermissionJocCockpitMaintenanceWindow());
            sosPermissionJocCockpit.setYADE(o.createSOSPermissionJocCockpitYADE());
            sosPermissionJocCockpit.setRuntime(o.createSOSPermissionJocCockpitRuntime());
            sosPermissionJocCockpit.getRuntime().setExecute(o.createSOSPermissionJocCockpitRuntimeExecute());

            sosPermissionJocCockpit.setCalendar(o.createSOSPermissionJocCockpitCalendar());
            sosPermissionJocCockpit.getCalendar().setView(o.createSOSPermissionJocCockpitCalendarView());
            sosPermissionJocCockpit.getCalendar().setEdit(o.createSOSPermissionJocCockpitCalendarEdit());
            sosPermissionJocCockpit.getCalendar().getEdit().setAssign(o.createSOSPermissionJocCockpitCalendarEditAssign());

            sosPermissionJocCockpit.setJOCConfigurations(o.createSOSPermissionJocCockpitJOCConfigurations());
            sosPermissionJocCockpit.getJOCConfigurations().setShare(o.createSOSPermissionJocCockpitJOCConfigurationsShare());
            sosPermissionJocCockpit.getJOCConfigurations().getShare().setView(o.createSOSPermissionJocCockpitJOCConfigurationsShareView());
            sosPermissionJocCockpit.getJOCConfigurations().getShare().setChange(o.createSOSPermissionJocCockpitJOCConfigurationsShareChange());
            sosPermissionJocCockpit.getJOCConfigurations().getShare().getChange().setSharedStatus(o
                    .createSOSPermissionJocCockpitJOCConfigurationsShareChangeSharedStatus());

            sosPermissionJocCockpit.getJobschedulerMaster().setView(o.createSOSPermissionJocCockpitJobschedulerMasterView());
            sosPermissionJocCockpit.getJobschedulerMaster().setAdministration(o.createSOSPermissionJocCockpitJobschedulerMasterAdministration());
            sosPermissionJocCockpit.getJobschedulerMaster().setExecute(o.createSOSPermissionJocCockpitJobschedulerMasterExecute());
            sosPermissionJocCockpit.getJobschedulerMaster().getExecute().setRestart(o
                    .createSOSPermissionJocCockpitJobschedulerMasterExecuteRestart());

            sosPermissionJocCockpit.getJobschedulerMasterCluster().setView(o.createSOSPermissionJocCockpitJobschedulerMasterClusterView());
            sosPermissionJocCockpit.getJobschedulerMasterCluster().setExecute(o.createSOSPermissionJocCockpitJobschedulerMasterClusterExecute());

            sosPermissionJocCockpit.getJobschedulerUniversalAgent().setView(o.createSOSPermissionJocCockpitJobschedulerUniversalAgentView());
            sosPermissionJocCockpit.getJobschedulerUniversalAgent().setExecute(o.createSOSPermissionJocCockpitJobschedulerUniversalAgentExecute());
            sosPermissionJocCockpit.getJobschedulerUniversalAgent().getExecute().setRestart(o
                    .createSOSPermissionJocCockpitJobschedulerUniversalAgentExecuteRestart());

            sosPermissionJocCockpit.getDailyPlan().setView(o.createSOSPermissionJocCockpitDailyPlanView());
            sosPermissionJocCockpit.getOrder().setView(o.createSOSPermissionJocCockpitOrderView());
            sosPermissionJocCockpit.getOrder().setChange(o.createSOSPermissionJocCockpitOrderChange());
            sosPermissionJocCockpit.getOrder().setDelete(o.createSOSPermissionJocCockpitOrderDelete());
            sosPermissionJocCockpit.getOrder().setExecute(o.createSOSPermissionJocCockpitOrderExecute());

            sosPermissionJocCockpit.getJobChain().setView(o.createSOSPermissionJocCockpitJobChainView());
            sosPermissionJocCockpit.getJobChain().setChange(o.createSOSPermissionJocCockpitJobChainChange());
            sosPermissionJocCockpit.getJobChain().setExecute(o.createSOSPermissionJocCockpitJobChainExecute());

            sosPermissionJocCockpit.getJob().setView(o.createSOSPermissionJocCockpitJobView());
            sosPermissionJocCockpit.getJob().setChange(o.createSOSPermissionJocCockpitJobChange());
            sosPermissionJocCockpit.getJob().setExecute(o.createSOSPermissionJocCockpitJobExecute());

            sosPermissionJocCockpit.getProcessClass().setView(o.createSOSPermissionJocCockpitProcessClassView());
            sosPermissionJocCockpit.getProcessClass().setChange(o.createSOSPermissionJocCockpitProcessClassChange());

            sosPermissionJocCockpit.getSchedule().setView(o.createSOSPermissionJocCockpitScheduleView());
            sosPermissionJocCockpit.getSchedule().setChange(o.createSOSPermissionJocCockpitScheduleChange());

            sosPermissionJocCockpit.getLock().setView(o.createSOSPermissionJocCockpitLockView());
            sosPermissionJocCockpit.getLock().setChange(o.createSOSPermissionJocCockpitLockChange());

            sosPermissionJocCockpit.getEvent().setView(o.createSOSPermissionJocCockpitEventView());
            sosPermissionJocCockpit.getEvent().setExecute(o.createSOSPermissionJocCockpitEventExecute());

            sosPermissionJocCockpit.getHolidayCalendar().setView(o.createSOSPermissionJocCockpitHolidayCalendarView());
            sosPermissionJocCockpit.getAuditLog().setView(o.createSOSPermissionJocCockpitAuditLogView());
            sosPermissionJocCockpit.getMaintenanceWindow().setView(o.createSOSPermissionJocCockpitMaintenanceWindowView());
            sosPermissionJocCockpit.getHistory().setView(o.createSOSPermissionJocCockpitHistoryView());
             
            sosPermissionJocCockpit.getYADE().setView(o.createSOSPermissionJocCockpitYADEView());
            sosPermissionJocCockpit.getYADE().setExecute(o.createSOSPermissionJocCockpitYADEExecute());

            
            sosPermissionJocCockpit.getJobschedulerMaster().getView().setStatus(haveRight(
                    "sos:products:joc_cockpit:jobscheduler_master:view:status"));
            sosPermissionJocCockpit.getJobschedulerMaster().getView().setMainlog(haveRight(
                    "sos:products:joc_cockpit:jobscheduler_master:view:mainlog"));
            sosPermissionJocCockpit.getJobschedulerMaster().getView().setParameter(haveRight(
                    "sos:products:joc_cockpit:jobscheduler_master:view:parameter"));
            sosPermissionJocCockpit.getJobschedulerMaster().getExecute().getRestart().setAbort(haveRight(
                    "sos:products:joc_cockpit:jobscheduler_master:execute:restart:terminate"));
            sosPermissionJocCockpit.getJobschedulerMaster().getExecute().getRestart().setTerminate(haveRight(
                    "sos:products:joc_cockpit:jobscheduler_master:execute:restart:abort"));
            sosPermissionJocCockpit.getJobschedulerMaster().getExecute().setPause(haveRight(
                    "sos:products:joc_cockpit:jobscheduler_master:execute:pause"));
            sosPermissionJocCockpit.getJobschedulerMaster().getExecute().setContinue(haveRight(
                    "sos:products:joc_cockpit:jobscheduler_master:execute:continue"));
            sosPermissionJocCockpit.getJobschedulerMaster().getExecute().setTerminate(haveRight(
                    "sos:products:joc_cockpit:jobscheduler_master:execute:terminate"));
            sosPermissionJocCockpit.getJobschedulerMaster().getExecute().setAbort(haveRight(
                    "sos:products:joc_cockpit:jobscheduler_master:execute:abort"));
            sosPermissionJocCockpit.getJobschedulerMaster().getAdministration().setManageCategories(haveRight(
                    "sos:products:joc_cockpit:jobscheduler_master:administration:manage_categories"));
            sosPermissionJocCockpit.getJobschedulerMaster().getAdministration().setEditPermissions(haveRight(
                    "sos:products:joc_cockpit:jobscheduler_master:administration:edit_permissions"));
            sosPermissionJocCockpit.getJobschedulerMaster().getAdministration().setRemoveOldInstances(haveRight(
                    "sos:products:joc_cockpit:jobscheduler_master:administration:remove_old_instances"));

            sosPermissionJocCockpit.getJobschedulerMasterCluster().getView().setStatus(haveRight(
                    "sos:products:joc_cockpit:jobscheduler_master_cluster:view:status"));
            sosPermissionJocCockpit.getJobschedulerMasterCluster().getExecute().setTerminateFailSafe(haveRight(
                    "sos:products:joc_cockpit:jobscheduler_master_cluster:execute:terminate_fail_safe"));
            sosPermissionJocCockpit.getJobschedulerMasterCluster().getExecute().setRestart(haveRight(
                    "sos:products:joc_cockpit:jobscheduler_master_cluster:execute:restart"));
            sosPermissionJocCockpit.getJobschedulerMasterCluster().getExecute().setTerminate(haveRight(
                    "sos:products:joc_cockpit:jobscheduler_master_cluster:execute:terminate"));

            sosPermissionJocCockpit.getJobschedulerUniversalAgent().getView().setStatus(haveRight(
                    "sos:products:joc_cockpit:jobscheduler_universal_agent:view:status"));
            sosPermissionJocCockpit.getJobschedulerUniversalAgent().getExecute().getRestart().setAbort(haveRight(
                    "sos:products:joc_cockpit:jobscheduler_universal_agent:execute:restart:abort"));
            sosPermissionJocCockpit.getJobschedulerUniversalAgent().getExecute().getRestart().setTerminate(haveRight(
                    "sos:products:joc_cockpit:jobscheduler_universal_agent:execute:restart:terminate"));
            sosPermissionJocCockpit.getJobschedulerUniversalAgent().getExecute().setAbort(haveRight(
                    "sos:products:joc_cockpit:jobscheduler_universal_agent:execute:abort"));
            sosPermissionJocCockpit.getJobschedulerUniversalAgent().getExecute().setTerminate(haveRight(
                    "sos:products:joc_cockpit:jobscheduler_universal_agent:execute:terminate"));

            sosPermissionJocCockpit.getDailyPlan().getView().setStatus(haveRight("sos:products:joc_cockpit:daily_plan:view:status"));

            sosPermissionJocCockpit.getHistory().getView().setStatus(haveRight("sos:products:joc_cockpit:history:view:status"));

            sosPermissionJocCockpit.getOrder().getView().setStatus(haveRight("sos:products:joc_cockpit:order:view:status"));
            sosPermissionJocCockpit.getOrder().getView().setConfiguration(haveRight("sos:products:joc_cockpit:order:view:configuration"));
            sosPermissionJocCockpit.getOrder().getView().setOrderLog(haveRight("sos:products:joc_cockpit:order:view:order_log"));
            sosPermissionJocCockpit.getOrder().getChange().setStartAndEndNode(haveRight("sos:products:joc_cockpit:order:change:start_and_end_node"));
            sosPermissionJocCockpit.getOrder().getChange().setTimeForAdhocOrder(haveRight("sos:products:joc_cockpit:order:change:time_for_adhoc_orders"));
            sosPermissionJocCockpit.getOrder().getChange().setParameter(haveRight("sos:products:joc_cockpit:order:change:parameter"));
            sosPermissionJocCockpit.getOrder().getChange().setRunTime(haveRight("sos:products:joc_cockpit:order:change:run_time"));
            sosPermissionJocCockpit.getOrder().getChange().setState(haveRight("sos:products:joc_cockpit:order:change:state"));
            sosPermissionJocCockpit.getOrder().getChange().setHotFolder(haveRight("sos:products:joc_cockpit:order:change:hot_folder"));
            sosPermissionJocCockpit.getOrder().getExecute().setStart(haveRight("sos:products:joc_cockpit:order:execute:start"));
            sosPermissionJocCockpit.getOrder().getExecute().setUpdate(haveRight("sos:products:joc_cockpit:order:execute:update"));
            sosPermissionJocCockpit.getOrder().getExecute().setSuspend(haveRight("sos:products:joc_cockpit:order:execute:suspend"));
            sosPermissionJocCockpit.getOrder().getExecute().setResume(haveRight("sos:products:joc_cockpit:order:execute:resume"));
            sosPermissionJocCockpit.getOrder().getExecute().setReset(haveRight("sos:products:joc_cockpit:order::execute:reset"));
            sosPermissionJocCockpit.getOrder().getExecute().setRemoveSetback(haveRight("sos:products:joc_cockpit:order:execute:remove_setback"));
            sosPermissionJocCockpit.getOrder().getDelete().setPermanent(haveRight("sos:products:joc_cockpit:order:delete:permanent"));
            sosPermissionJocCockpit.getOrder().getDelete().setTemporary(haveRight("sos:products:joc_cockpit:order:delete:temporary"));

            sosPermissionJocCockpit.getJobChain().getView().setConfiguration(haveRight("sos:products:joc_cockpit:job_chain:view:configuration"));
            sosPermissionJocCockpit.getJobChain().getView().setHistory(haveRight("sos:products:joc_cockpit:job_chain:view:history"));
            sosPermissionJocCockpit.getJobChain().getView().setStatus(haveRight("sos:products:joc_cockpit:job_chain:view:status"));
            sosPermissionJocCockpit.getJobChain().getExecute().setStop(haveRight("sos:products:joc_cockpit:job_chain:execute:stop"));
            sosPermissionJocCockpit.getJobChain().getExecute().setUnstop(haveRight("sos:products:joc_cockpit:job_chain:execute:unstop"));
            sosPermissionJocCockpit.getJobChain().getExecute().setAddOrder(haveRight("sos:products:joc_cockpit:job_chain:execute:add_order"));
            sosPermissionJocCockpit.getJobChain().getExecute().setSkipJobChainNode(haveRight(
                    "sos:products:joc_cockpit:job_chain:execute:skip_jobchain_node"));
            sosPermissionJocCockpit.getJobChain().getExecute().setProcessJobChainNode(haveRight(
                    "sos:products:joc_cockpit:job_chain:execute:process_jobchain_node"));
            sosPermissionJocCockpit.getJobChain().getExecute().setStopJobChainNode(haveRight(
                    "sos:products:joc_cockpit:job_chain:execute:stop_jobchain_node"));

            sosPermissionJocCockpit.getJob().getView().setStatus(haveRight("sos:products:joc_cockpit:job:view:status"));
            sosPermissionJocCockpit.getJob().getView().setTaskLog(haveRight("sos:products:joc_cockpit:job:view:task_log"));
            sosPermissionJocCockpit.getJob().getView().setConfiguration(haveRight("sos:products:joc_cockpit:job:view:configuration"));
            sosPermissionJocCockpit.getJob().getView().setHistory(haveRight("sos:products:joc_cockpit:job:view:history"));
            sosPermissionJocCockpit.getJob().getChange().setRunTime(haveRight("sos:products:joc_cockpit:job:change:run_time"));
            sosPermissionJocCockpit.getJob().getExecute().setStart(haveRight("sos:products:joc_cockpit:job:execute:start"));
            sosPermissionJocCockpit.getJob().getExecute().setStop(haveRight("sos:products:joc_cockpit:job:execute:stop"));
            sosPermissionJocCockpit.getJob().getExecute().setUnstop(haveRight("sos:products:joc_cockpit:job:execute:unstop"));
            sosPermissionJocCockpit.getJob().getExecute().setTerminate(haveRight("sos:products:joc_cockpit:job:execute:terminate"));
            sosPermissionJocCockpit.getJob().getExecute().setKill(haveRight("sos:products:joc_cockpit:job:execute:kill"));
            sosPermissionJocCockpit.getJob().getExecute().setEndAllTasks(haveRight("sos:products:joc_cockpit:job:execute:end_all_tasks"));
            sosPermissionJocCockpit.getJob().getExecute().setSuspendAllTasks(haveRight("sos:products:joc_cockpit:job:execute:suspend_all_tasks"));
            sosPermissionJocCockpit.getJob().getExecute().setContinueAllTasks(haveRight("sos:products:joc_cockpit:job:execute:continue_all_tasks"));

            sosPermissionJocCockpit.getProcessClass().getView().setStatus(haveRight("sos:products:joc_cockpit:process_class:view:status"));
            sosPermissionJocCockpit.getProcessClass().getView().setConfiguration(haveRight(
                    "sos:products:joc_cockpit:process_class:view:configuration"));

            sosPermissionJocCockpit.getSchedule().getView().setConfiguration(haveRight("sos:products:joc_cockpit:schedule:view:configuration"));
            sosPermissionJocCockpit.getSchedule().getView().setStatus(haveRight("sos:products:joc_cockpit:schedule:view:status"));
            sosPermissionJocCockpit.getSchedule().getChange().setEditContent(haveRight("sos:products:joc_cockpit:schedule:change:edit_content"));
            sosPermissionJocCockpit.getSchedule().getChange().setAddSubstitute(haveRight("sos:products:joc_cockpit:schedule:change:add_substitute"));

            sosPermissionJocCockpit.getLock().getView().setConfiguration(haveRight("sos:products:joc_cockpit:lock:view:configuration"));
            sosPermissionJocCockpit.getLock().getView().setStatus(haveRight("sos:products:joc_cockpit:lock:view:status"));

            sosPermissionJocCockpit.getEvent().getView().setStatus(haveRight("sos:products:joc_cockpit:event:view:status"));
            sosPermissionJocCockpit.getEvent().getExecute().setDelete(haveRight("sos:products:joc_cockpit:event:execute:delete"));
            sosPermissionJocCockpit.getEvent().getExecute().setAdd(haveRight("sos:products:joc_cockpit:event:execute:add"));

            sosPermissionJocCockpit.getHolidayCalendar().getView().setStatus(haveRight("sos:products:joc_cockpit:holiday_calendar:view:status"));
            sosPermissionJocCockpit.getAuditLog().getView().setStatus(haveRight("sos:products:joc_cockpit:audit_log:view:status"));

            sosPermissionJocCockpit.getJOCConfigurations().getShare().getView().setStatus(haveRight("sos:products:joc_cockpit:customization:share:view:status"));
            sosPermissionJocCockpit.getJOCConfigurations().getShare().getChange().setDelete(haveRight(
                    "sos:products:joc_cockpit:customization:share:change:delete"));
            sosPermissionJocCockpit.getJOCConfigurations().getShare().getChange().setEditContent(haveRight(
                    "sos:products:joc_cockpit:customization:share:change:edit_content"));
            sosPermissionJocCockpit.getJOCConfigurations().getShare().getChange().getSharedStatus().setMakePrivate(haveRight(
                    "sos:products:joc_cockpit:customization:share:change:shared_status:make_private"));
            sosPermissionJocCockpit.getJOCConfigurations().getShare().getChange().getSharedStatus().setMakeShared(haveRight(
                    "sos:products:joc_cockpit:customization:share:change:shared_status:make_share"));

            sosPermissionJocCockpit.getMaintenanceWindow().getView().setStatus(haveRight("sos:products:joc_cockpit:maintenance_window:view:status"));
            sosPermissionJocCockpit.getMaintenanceWindow().setEnableDisableMaintenanceWindow(haveRight(
                    "sos:products:joc_cockpit:maintenance_window:enable_disable_maintenance_window"));


            sosPermissionJocCockpit.getYADE().getView().setStatus(haveRight("sos:products:joc_cockpit:yade:view:status"));
            sosPermissionJocCockpit.getYADE().getView().setTransfers(haveRight("sos:products:joc_cockpit:yade:view:transfers"));
            sosPermissionJocCockpit.getYADE().getView().setFiles(haveRight("sos:products:joc_cockpit:yade:view:files"));
            sosPermissionJocCockpit.getYADE().getExecute().setTransferStart(haveRight("sos:products:joc_cockpit:yade:execute:transfer_start"));
            
            sosPermissionJocCockpit.getCalendar().getView().setStatus(haveRight("sos:products:joc_cockpit:calendar:view:status"));
            
            sosPermissionJocCockpit.getCalendar().getEdit().setChange(haveRight("sos:products:joc_cockpit:calendar:edit:change"));
            sosPermissionJocCockpit.getCalendar().getEdit().setDelete(haveRight("sos:products:joc_cockpit:calendar:edit:delete"));
            sosPermissionJocCockpit.getCalendar().getEdit().setCreate(haveRight("sos:products:joc_cockpit:calendar:edit:create"));
            
            sosPermissionJocCockpit.getCalendar().getEdit().getAssign().setChange(haveRight("sos:products:joc_cockpit:calendar:assign:change"));
            sosPermissionJocCockpit.getCalendar().getEdit().getAssign().setNonworking(haveRight("sos:products:joc_cockpit:calendar:assign:nonworking"));
            sosPermissionJocCockpit.getCalendar().getEdit().getAssign().setRuntime(haveRight("sos:products:joc_cockpit:calendar:assign:runtime"));

            sosPermissionJocCockpit.getRuntime().getExecute().setEditXml(haveRight("sos:products:joc_cockpit:runtime:execute:edit_xml"));
            
        }
        return sosPermissionJocCockpit;
    }

    private SOSPermissionCommands getSosPermissionCommands() {

        ObjectFactory o = new ObjectFactory();

        SOSPermissionCommands sosPermissionCommands = o.createSOSPermissionCommands();

        if (currentUser != null && currentUser.getCurrentSubject() != null) {

            sosPermissionCommands.setIsAuthenticated(currentUser.isAuthenticated());
            sosPermissionCommands.setAccessToken(currentUser.getAccessToken());
            sosPermissionCommands.setUser(currentUser.getUsername());

            sosPermissionCommands.setDailyPlan(o.createSOSPermissionCommandsDailyPlan());
            sosPermissionCommands.getDailyPlan().setView(o.createSOSPermissionCommandsDailyPlanView());
            sosPermissionCommands.setJobschedulerMaster(o.createSOSPermissionCommandsJobschedulerMaster());
            sosPermissionCommands.setJobschedulerMasterCluster(o.createSOSPermissionCommandsJobschedulerMasterCluster());
            sosPermissionCommands.setHistory(o.createSOSPermissionCommandsHistory());
            sosPermissionCommands.setOrder(o.createSOSPermissionCommandsOrder());
            sosPermissionCommands.setJobChain(o.createSOSPermissionCommandsJobChain());
            sosPermissionCommands.setJob(o.createSOSPermissionCommandsJob());
            sosPermissionCommands.setProcessClass(o.createSOSPermissionCommandsProcessClass());
            sosPermissionCommands.setSchedule(o.createSOSPermissionCommandsSchedule());
            sosPermissionCommands.setLock(o.createSOSPermissionCommandsLock());

            sosPermissionCommands.getJobschedulerMaster().setView(o.createSOSPermissionCommandsJobschedulerMasterView());
            sosPermissionCommands.getJobschedulerMaster().setExecute(o.createSOSPermissionCommandsJobschedulerMasterExecute());
            sosPermissionCommands.getJobschedulerMaster().setAdministration(o.createSOSPermissionCommandsJobschedulerMasterAdministration());
            sosPermissionCommands.getJobschedulerMaster().getExecute().setRestart(o.createSOSPermissionCommandsJobschedulerMasterExecuteRestart());

            sosPermissionCommands.getJobschedulerMasterCluster().setExecute(o.createSOSPermissionCommandsJobschedulerMasterClusterExecute());

            sosPermissionCommands.getOrder().setView(o.createSOSPermissionCommandsOrderView());
            sosPermissionCommands.getOrder().setChange(o.createSOSPermissionCommandsOrderChange());
            sosPermissionCommands.getOrder().setExecute(o.createSOSPermissionCommandsOrderExecute());

            sosPermissionCommands.getJobChain().setView(o.createSOSPermissionCommandsJobChainView());
            sosPermissionCommands.getJobChain().setExecute(o.createSOSPermissionCommandsJobChainExecute());
            sosPermissionCommands.getJobChain().setChange(o.createSOSPermissionCommandsJobChainChange());

            sosPermissionCommands.getJob().setView(o.createSOSPermissionCommandsJobView());
            sosPermissionCommands.getJob().setExecute(o.createSOSPermissionCommandsJobExecute());
            sosPermissionCommands.getJob().setChange(o.createSOSPermissionCommandsJobChange());

            sosPermissionCommands.getProcessClass().setView(o.createSOSPermissionCommandsProcessClassView());
            sosPermissionCommands.getProcessClass().setChange(o.createSOSPermissionCommandsProcessClassChange());

            sosPermissionCommands.getSchedule().setView(o.createSOSPermissionCommandsScheduleView());
            sosPermissionCommands.getSchedule().setChange(o.createSOSPermissionCommandsScheduleChange());

            sosPermissionCommands.getLock().setView(o.createSOSPermissionCommandsLockView());
            sosPermissionCommands.getLock().setChange(o.createSOSPermissionCommandsLockChange());

            sosPermissionCommands.getJobschedulerMaster().getView().setStatus(haveRight("sos:products:commands:jobscheduler_master:view:status"));
            sosPermissionCommands.getJobschedulerMaster().getView().setParameter(haveRight(
                    "sos:products:commands:jobscheduler_master:view:parameter"));
            sosPermissionCommands.getJobschedulerMaster().getExecute().getRestart().setAbort(haveRight(
                    "sos:products:commands:jobscheduler_master:execute:restart:terminate"));
            sosPermissionCommands.getJobschedulerMaster().getExecute().getRestart().setTerminate(haveRight(
                    "sos:products:commands:jobscheduler_master:execute:restart:abort"));
            sosPermissionCommands.getJobschedulerMaster().getExecute().setPause(haveRight("sos:products:commands:jobscheduler_master:execute:pause"));
            sosPermissionCommands.getJobschedulerMaster().getExecute().setContinue(haveRight(
                    "sos:products:commands:jobscheduler_master:execute:continue"));
            sosPermissionCommands.getJobschedulerMaster().getExecute().setTerminate(haveRight(
                    "sos:products:commands:jobscheduler_master:execute:terminate"));
            sosPermissionCommands.getJobschedulerMaster().getExecute().setAbort(haveRight("sos:products:commands:jobscheduler_master:execute:abort"));
            sosPermissionCommands.getJobschedulerMaster().getExecute().setStop(haveRight("sos:products:commands:jobscheduler_master:execute:stop"));
            sosPermissionCommands.getJobschedulerMaster().getAdministration().setManageCategories(haveRight(
                    "sos:products:commands:jobscheduler_master:manage_categories"));

            sosPermissionCommands.getJobschedulerMasterCluster().getExecute().setTerminateFailSafe(haveRight(
                    "sos:products:commands:jobscheduler_master_cluster:execute:terminate_fail_safe"));
            sosPermissionCommands.getJobschedulerMasterCluster().getExecute().setRestart(haveRight(
                    "sos:products:commands:jobscheduler_master_cluster:execute:restart"));
            sosPermissionCommands.getJobschedulerMasterCluster().getExecute().setTerminate(haveRight(
                    "sos:products:commands:jobscheduler_master_cluster:execute:terminate"));

            sosPermissionCommands.getDailyPlan().getView().setStatus(haveRight("sos:products:commands:jobscheduler_master:view:calendar"));

            sosPermissionCommands.getHistory().setView(haveRight("sos:products:commands:history:view"));

            sosPermissionCommands.getOrder().getView().setStatus(haveRight("sos:products:commands:order:view:status"));
            sosPermissionCommands.getOrder().getChange().setStartAndEndNode(haveRight("sos:products:commands:order:change:start_and_end_node"));
            sosPermissionCommands.getOrder().getChange().setTimeForAdhocOrder(haveRight("sos:products:commands:order:change:time_for_adhoc_orders"));
            sosPermissionCommands.getOrder().getChange().setParameter(haveRight("sos:products:commands:order:change:parameter"));
            sosPermissionCommands.getOrder().getChange().setOther(haveRight("sos:products:joc_cockpit:order:change:other"));
            sosPermissionCommands.getOrder().getChange().setRunTime(haveRight("sos:products:commands:order:change:run_time"));
            sosPermissionCommands.getOrder().getChange().setState(haveRight("sos:products:commands:order:change:state"));
            sosPermissionCommands.getOrder().getChange().setHotFolder(haveRight("sos:products:commands:order:change:hot_folder"));
            sosPermissionCommands.getOrder().getExecute().setStart(haveRight("sos:products:commands:order:execute:start"));
            sosPermissionCommands.getOrder().getExecute().setUpdate(haveRight("sos:products:commands:order:execute:update"));
            sosPermissionCommands.getOrder().getExecute().setSuspend(haveRight("sos:products:commands:order:execute:suspend"));
            sosPermissionCommands.getOrder().getExecute().setResume(haveRight("sos:products:commands:order:execute:resume"));
            sosPermissionCommands.getOrder().getExecute().setReset(haveRight("sos:products:commands:order::execute:reset"));
            sosPermissionCommands.getOrder().getExecute().setRemoveSetback(haveRight("sos:products:commands:order:execute:remove_setback"));
            sosPermissionCommands.getOrder().setDelete(haveRight("sos:products:commands:order:delete"));

            sosPermissionCommands.getJobChain().getView().setStatus(haveRight("sos:products:commands:job_chain:view:status"));
            sosPermissionCommands.getJobChain().getExecute().setStop(haveRight("sos:products:commands:job_chain:execute:stop"));
            sosPermissionCommands.getJobChain().getExecute().setUnstop(haveRight("sos:products:commands:job_chain:execute:unstop"));
            sosPermissionCommands.getJobChain().getExecute().setAddOrder(haveRight("sos:products:commands:job_chain:execute:add_order"));
            sosPermissionCommands.getJobChain().getExecute().setSkipJobChainNode(haveRight(
                    "sos:products:commands:job_chain:execute:skip_jobchain_node"));
            sosPermissionCommands.getJobChain().getExecute().setProcessJobChainNode(haveRight(
                    "sos:products:commands:job_chain:execute:process_jobchain_node"));
            sosPermissionCommands.getJobChain().getExecute().setStopJobChainNode(haveRight(
                    "sos:products:commands:job_chain:execute:stop_jobchain_node"));
            sosPermissionCommands.getJobChain().getExecute().setRemove(haveRight("sos:products:commands:job_chain:remove"));
            sosPermissionCommands.getJobChain().getChange().setHotFolder(haveRight("sos:products:commands:job_chain:change:hot_folder"));

            sosPermissionCommands.getJob().getView().setStatus(haveRight("sos:products:commands:job:view:status"));
            sosPermissionCommands.getJob().getChange().setRunTime(haveRight("sos:products:commands:job:change:run_time"));
            sosPermissionCommands.getJob().getChange().setHotFolder(haveRight("sos:products:commands:job:change:hot_folder"));
            sosPermissionCommands.getJob().getExecute().setStart(haveRight("sos:products:commands:job:execute:start"));
            sosPermissionCommands.getJob().getExecute().setStop(haveRight("sos:products:commands:job:execute:stop"));
            sosPermissionCommands.getJob().getExecute().setUnstop(haveRight("sos:products:commands:job:execute:unstop"));
            sosPermissionCommands.getJob().getExecute().setTerminate(haveRight("sos:products:commands:job:execute:terminate"));
            sosPermissionCommands.getJob().getExecute().setKill(haveRight("sos:products:commands:job:execute:kill"));
            sosPermissionCommands.getJob().getExecute().setEndAllTasks(haveRight("sos:products:commands:job:execute:end_all_tasks"));
            sosPermissionCommands.getJob().getExecute().setSuspendAllTasks(haveRight("sos:products:commands:job:execute:suspend_all_tasks"));
            sosPermissionCommands.getJob().getExecute().setContinueAllTasks(haveRight("sos:products:commands:job:execute:continue_all_tasks"));

            sosPermissionCommands.getProcessClass().getView().setStatus(haveRight("sos:products:commands:process_class:view:status"));
            sosPermissionCommands.getProcessClass().setRemove(haveRight("sos:products:commands:process_class:remove"));
            sosPermissionCommands.getProcessClass().getChange().setEditContent(haveRight("sos:products:commands:process_class:change:edit_content"));
            sosPermissionCommands.getProcessClass().getChange().setHotFolder(haveRight("sos:products:commands:process_class:change:hot_folder"));

            sosPermissionCommands.getSchedule().getView().setStatus(haveRight("sos:products:commands:schedule:view:status"));
            sosPermissionCommands.getSchedule().getChange().setAddSubstitute(haveRight("sos:products:commands:schedule:change:add_substitute"));
            sosPermissionCommands.getSchedule().getChange().setHotFolder(haveRight("sos:products:commands:schedule:change:hot_folder"));

            sosPermissionCommands.getLock().getView().setStatus(haveRight("sos:products:commands:lock:view:status"));
            sosPermissionCommands.getLock().setRemove(haveRight("sos:products:commands:lock:remove"));
            sosPermissionCommands.getLock().getChange().setHotFolder(haveRight("sos:products:commands:lock:change:hot_folder"));
        }
        return sosPermissionCommands;
    }

    private SOSPermissionJocCockpit createJocCockpitPermissionObject(String accessToken, String user, String pwd) {

        SOSPermissionJocCockpit sosPermissionJocCockpit = getSosPermissionJocCockpit();
        sosPermissionJocCockpit.setSOSPermissionRoles(getRoles(true));

        currentUser.setSosPermissionJocCockpit(sosPermissionJocCockpit);
        Globals.jocWebserviceDataContainer.getCurrentUsersList().addUser(currentUser);
        return sosPermissionJocCockpit;
    }

    private SOSPermissionCommands createCommandsPermissionObject(String accessToken, String user, String pwd) {

        SOSPermissionJocCockpit sosPermissionJocCockpit = getSosPermissionJocCockpit();
        SOSPermissionCommands sosPermissionCommands = getSosPermissionCommands();
        sosPermissionJocCockpit.setSOSPermissionRoles(getRoles(true));

        currentUser.setSosPermissionJocCockpit(sosPermissionJocCockpit);
        currentUser.setSosPermissionCommands(sosPermissionCommands);
        Globals.jocWebserviceDataContainer.getCurrentUsersList().addUser(currentUser);
        return sosPermissionCommands;
    }

    private boolean isPermitted(String permission) {
        return (currentUser != null && currentUser.isPermitted(permission) && currentUser.isAuthenticated());
    }

    private boolean haveRight(String permission) {
        return isPermitted(permission);
    }

    private void addPermission(Boolean forUser, List<String> sosPermission, String permission) {
        if (!forUser || isPermitted(permission)) {
            sosPermission.add(permission);
        }
    }

    private void addRole(List<String> sosRoles, String role, boolean forUser) {
        if (currentUser != null && (!forUser || currentUser.hasRole(role)) && currentUser.isAuthenticated()) {
            if (!sosRoles.contains(role)) {
                sosRoles.add(role);
            }
        }
    }

    private void createUser() throws Exception {
        if (Globals.jocWebserviceDataContainer.getCurrentUsersList() == null) {
            Globals.jocWebserviceDataContainer.setCurrentUsersList(new SOSShiroCurrentUsersList());
        }

        sosLogin = new SOSlogin(Globals.getShiroIniSecurityManagerFactory());
        sosLogin.login(currentUser.getUsername(), currentUser.getPassword());

        currentUser.setCurrentSubject(sosLogin.getCurrentUser());

        if (sosLogin.getCurrentUser() == null) {
            JocError error = new JocError();
            error.setMessage(String.format("%s: Could not login with user: %s password:*******", sosLogin.getMsg(), currentUser.getUsername()));
            throw new JocException(error);
        }
        Session session = sosLogin.getCurrentUser().getSession();
        String accessToken = session.getId().toString();

        currentUser.setAccessToken(accessToken);
        Globals.jocWebserviceDataContainer.getCurrentUsersList().addUser(currentUser);

        SOSPermissionJocCockpit sosPermissionJocCockpit = createJocCockpitPermissionObject(accessToken, EMPTY_STRING, EMPTY_STRING);
        currentUser.setSosPermissionJocCockpit(sosPermissionJocCockpit);

        currentUser.initFolders();

        Section s = getIni().getSection("folders");
        if (s != null) {
            for (String role : s.keySet()) {
                currentUser.addFolder(role, s.get(role));
            }
        }

        SOSPermissionCommands sosPermissionCommands = createCommandsPermissionObject(accessToken, EMPTY_STRING, EMPTY_STRING);
        currentUser.setSosPermissionCommands(sosPermissionCommands);

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

        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = new SOSShiroCurrentUserAnswer(currentUser.getUsername());
        sosShiroCurrentUserAnswer.setIsAuthenticated(currentUser.getCurrentSubject().isAuthenticated());
        sosShiroCurrentUserAnswer.setAccessToken(currentUser.getAccessToken());
        sosShiroCurrentUserAnswer.setUser(currentUser.getUsername());
        sosShiroCurrentUserAnswer.setSessionTimeout(currentUser.getCurrentSubject().getSession().getTimeout());

        boolean enableTouch = "true".equals(Globals.sosShiroProperties.getProperty(WebserviceConstants.ENABLE_SESSION_TOUCH,
                WebserviceConstants.ENABLE_SESSION_TOUCH_DEFAULT));
        sosShiroCurrentUserAnswer.setEnableTouch(enableTouch);

        return sosShiroCurrentUserAnswer;

    }

    private String getRolesAsString(boolean forUser) {
        String roles = "  ";
        if (currentUser != null) {
            SOSPermissionRoles listOfRoles = getRoles(forUser);
            for (int i = 0; i < listOfRoles.getSOSPermissionRole().size(); i++) {
                roles = roles + listOfRoles.getSOSPermissionRole().get(i) + ",";
            }
            return roles.substring(0, roles.length() - 1).trim();
        } else {
            return EMPTY_STRING;
        }
    }

    public SOSPermissionRoles getRoles(boolean forUser) {

        if (roles == null || !forUser) {
            ObjectFactory o = new ObjectFactory();
            roles = o.createSOSPermissionRoles();

            ini = getIni();
            Section s = ini.getSection("roles");

            if (s != null) {
                for (String role : s.keySet()) {
                    addRole(roles.getSOSPermissionRole(), role, forUser);
                }
            }

            s = ini.getSection("folders");
            if (s != null) {
                for (String role : s.keySet()) {
                    String[] key = role.split("\\|");
                    if (key.length == 1) {
                        addRole(roles.getSOSPermissionRole(), role, forUser);
                    }
                    if (key.length == 2) {
                        addRole(roles.getSOSPermissionRole(), key[1], forUser);
                    }
                }
            }
        }
        return roles;
    }

    public Ini getIni() {

        if (ini == null) {
            return Globals.getIniFromSecurityManagerFactory();
        }
        return ini;
    }

    private JOCDefaultResponse login(String basicAuthorization, String user, String pwd) {

        try {
            Globals.sosShiroProperties = new JocCockpitProperties();
            Globals.setProperties();
            TimeZone.setDefault(TimeZone.getTimeZone(UTC));

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
            jocAuditLog.storeAuditLogEntry(s);

            if (!sosShiroCurrentUserAnswer.isAuthenticated()) {
                if (sosLogin != null) {
                    LOGGER.info(sosLogin.getMsg());
                }
                return JOCDefaultResponse.responseStatus401(sosShiroCurrentUserAnswer);
            } else {
                return JOCDefaultResponse.responseStatus200WithHeaders(sosShiroCurrentUserAnswer, sosShiroCurrentUserAnswer.getAccessToken(),
                        currentUser.getCurrentSubject().getSession().getTimeout());
            }

        } catch (UnsupportedEncodingException e) {
            return JOCDefaultResponse.responseStatusJSError(AUTHORIZATION_HEADER_WITH_BASIC_BASED64PART_EXPECTED);

        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e);
        }
    }

    private void resetTimeOut() {

        if (currentUser != null) {
            Session curSession = currentUser.getCurrentSubject().getSession(false);
            if (curSession != null) {
                curSession.touch();
            } else {
                throw new org.apache.shiro.session.InvalidSessionException("Session doesn't exist");
            }
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