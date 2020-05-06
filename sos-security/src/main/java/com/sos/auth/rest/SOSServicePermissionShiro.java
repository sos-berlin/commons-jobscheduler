package com.sos.auth.rest;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
/*
 * import javax.servlet.http.HttpServletRequest;
 */
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.config.Ini.Section;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;

import org.apache.shiro.session.ExpiredSessionException;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.session.mgt.SessionKey;

import com.sos.auth.rest.permission.model.ObjectFactory;
import com.sos.auth.rest.permission.model.SOSPermissionCommandsMasters;
import com.sos.auth.rest.permission.model.SOSPermissionJocCockpitMasters;
import com.sos.auth.rest.permission.model.SOSPermissionRoles;
import com.sos.auth.rest.permission.model.SOSPermissionShiro;
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

import sos.util.SOSSerializerUtil;

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

    @Context
    UriInfo uriInfo;

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

            SOSPermissionsCreator sosPermissionsCreator = new SOSPermissionsCreator(null);
            sosPermissionsCreator.loginFromAccessToken(accessToken);

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

            SOSPermissionsCreator sosPermissionsCreator = new SOSPermissionsCreator(null);
            sosPermissionsCreator.loginFromAccessToken(accessToken);

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
    public JOCDefaultResponse loginPost(@Context HttpServletRequest request, @HeaderParam("Authorization") String basicAuthorization,
            @QueryParam("user") String user, @QueryParam("pwd") String pwd) throws JocException, SOSHibernateException {
        return login(request, basicAuthorization, user, pwd);
    }

    /*
     * @POST
     * @Path("/login")
     * @Consumes(MediaType.APPLICATION_JSON)
     * @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON }) public JOCDefaultResponse loginPost( @HeaderParam("Authorization") String
     * basicAuthorization, @QueryParam("user") String user,
     * @QueryParam("pwd") String pwd) throws JocException, SOSHibernateException { return login(basicAuthorization, user, pwd); }
     */

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
                try {
                    Globals.sosShiroProperties = new JocCockpitProperties();
                    Globals.setProperties();
                    IniSecurityManagerFactory factory = Globals.getShiroIniSecurityManagerFactory();
                    SecurityManager securityManager = factory.getInstance();
                    SecurityUtils.setSecurityManager(securityManager);
                    SessionKey s = new DefaultSessionKey(accessToken);
                    Session session = SecurityUtils.getSecurityManager().getSession(s);
                    session.stop();
                } catch (Exception e) {
                    throw new SessionNotExistException("Session doesn't exist");
                }
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

        JocAuditLog jocAuditLog = new JocAuditLog(user, "./logout");
        SecurityAudit s = new SecurityAudit(comment);
        jocAuditLog.logAuditMessage(s);
        try {
            if (currentUser != null && currentUser.getCurrentSubject() != null) {
                Globals.forceClosingHttpClients(currentUser, accessToken);
                sosShiroSession.getTimeout();
                sosShiroSession.stop();
            }

        } catch (Exception e) {
        }

        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = new SOSShiroCurrentUserAnswer(EMPTY_STRING);
        if (currentUser != null) {
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
        SOSListOfPermissions sosListOfPermissions = new SOSListOfPermissions(currentUser, forUser);
        return sosListOfPermissions.getSosPermissionShiro();

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
        currentUser.getCurrentSubject().getSession().setAttribute("username_joc_permissions", SOSSerializerUtil.object2toString(sosPermissionJocCockpitMasters));

        currentUser.initFolders();

        Section s = sosPermissionsCreator.getIni().getSection("folders");
        if (s != null) {
            for (String role : s.keySet()) {
                currentUser.addFolder(role, s.get(role));
            }
        }

        SOSPermissionCommandsMasters sosPermissionCommandsMasters = sosPermissionsCreator.createCommandsPermissionMasterObjectList(accessToken);
        currentUser.setSosPermissionCommandsMasters(sosPermissionCommandsMasters);
        currentUser.getCurrentSubject().getSession().setAttribute("username_command_permissions", SOSSerializerUtil.object2toString(sosPermissionCommandsMasters));

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
        sosShiroCurrentUserAnswer.setCallerHostName(currentUser.getCallerHostName());
        sosShiroCurrentUserAnswer.setCallerIpAddress(currentUser.getCallerIpAddress());

        LOGGER.info("CallerIpAddress=" + currentUser.getCallerIpAddress());

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

    private JOCDefaultResponse login(HttpServletRequest request, String basicAuthorization, String user, String pwd) throws JocException,
            SOSHibernateException {
        Globals.setServletBaseUri(uriInfo);

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
            currentUser.setHttpServletRequest(request);

            SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = authenticate();

            if (request != null) {
                sosShiroCurrentUserAnswer.setCallerIpAddress(request.getRemoteAddr());
                sosShiroCurrentUserAnswer.setCallerHostName(request.getRemoteHost());
            }

            LOGGER.debug(String.format("Method: %s, User: %s, access_token: %s", "login", currentUser.getUsername(), currentUser.getAccessToken()));

            Globals.jocWebserviceDataContainer.getCurrentUsersList().removeTimedOutUser(currentUser.getUsername());

            JocAuditLog jocAuditLog = new JocAuditLog(currentUser.getUsername(), "./login");
            SecurityAudit s = new SecurityAudit(getRolesAsString(true));
            jocAuditLog.logAuditMessage(s);

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