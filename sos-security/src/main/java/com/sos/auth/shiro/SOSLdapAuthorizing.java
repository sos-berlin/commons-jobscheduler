package com.sos.auth.shiro;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.Ini.Section;
import org.apache.shiro.realm.ldap.JndiLdapContextFactory;
import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.apache.shiro.realm.ldap.LdapUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;

public class SOSLdapAuthorizing {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSLdapAuthorizing.class);
    private static final String ROLE_NAMES_DELIMETER = "\\|";
    private SimpleAuthorizationInfo authorizationInfo = null;
    private LdapContextFactory ldapContextFactory;
    private LdapContext ldapContext;
    private SOSLdapAuthorizingRealm sosLdapAuthorizingRealm;
    private AuthenticationToken authcToken;
    private SOSLdapLoginUserName sosLdapLoginUserName;

    public SimpleAuthorizationInfo setRoles(SimpleAuthorizationInfo authorizationInfo_, PrincipalCollection principalCollection) {
        if (authorizationInfo_ == null) {
            SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
            authorizationInfo = simpleAuthorizationInfo;
        } else {
            authorizationInfo = authorizationInfo_;
        }
        try {
        	LOGGER.debug("setting roles with queryForAuthorizationInfo()");
            queryForAuthorizationInfo(principalCollection);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return authorizationInfo;
    }

    protected void queryForAuthorizationInfo(PrincipalCollection principals) throws Exception {
        if (ldapContext != null) {

            String userName = (String) principals.getPrimaryPrincipal();
            sosLdapLoginUserName = new SOSLdapLoginUserName(userName);
            try {
                getRoleNamesForUser();
            } finally {
                LdapUtils.closeContext(ldapContext);
            }
        }
    }

    private Collection<String> getGroupNamesByGroup() throws NamingException {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        String searchFilter = substituteUserName(sosLdapAuthorizingRealm.getGroupSearchFilter());
        LOGGER.debug(String.format("getting groups from ldap using group search filter %s with group search base %s", sosLdapAuthorizingRealm.getGroupSearchBase(),
                searchFilter));

        NamingEnumeration<SearchResult> answer = ldapContext.search(sosLdapAuthorizingRealm.getGroupSearchBase(), searchFilter, searchCtls);
        ArrayList<String> rolesForGroups = new ArrayList<String>();

        while (answer.hasMoreElements()) {
            SearchResult result = answer.next();
            String groupNameAttribute = sosLdapAuthorizingRealm.getGroupNameAttribute();
            Attribute g = result.getAttributes().get(groupNameAttribute);
            String group = g.get().toString();
            rolesForGroups.add(group);
            LOGGER.debug(String.format("Groupname %s found in attribute", group, groupNameAttribute));
        }
        return rolesForGroups;
    }

    private String substituteUserName(String source) {
        String s = String.format(source, sosLdapLoginUserName.getUserName());
        s = s.replaceAll("\\^s", "%s");
        s = String.format(s, sosLdapLoginUserName.getLogin());
        return s;
    }

    private Attributes getUserAttributes() throws NamingException {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        Attributes user = null;

        String searchFilter = "(" + sosLdapAuthorizingRealm.getUserNameAttribute() + "=%s)";
        if (sosLdapAuthorizingRealm.getUserSearchFilter() == null) {
            LOGGER.warn(String.format(
                    "You have specified a value for userNameAttribute but you have not defined the userSearchFilter. The default filter %s will be used",
                    searchFilter));
        } else {
            searchFilter = sosLdapAuthorizingRealm.getUserSearchFilter();
        }

        searchFilter = substituteUserName(searchFilter);

        LOGGER.debug(String.format("getting user from ldap using search filter %s with search base %s", sosLdapAuthorizingRealm.getSearchBase(),
                searchFilter));

        String searchBase = "";
        if (sosLdapAuthorizingRealm.getSearchBase() == null) {
            LOGGER.warn(String.format(
                    "You have specified a value for userNameAttribute but you have not defined the searchBase. The default empty search base will be used"));
        } else {
            searchBase = sosLdapAuthorizingRealm.getSearchBase();
        }

        NamingEnumeration<SearchResult> answer = ldapContext.search(searchBase, searchFilter, searchCtls);

        if (!answer.hasMore()) {
            LOGGER.warn(String.format("Cannot find user: %s with search filter %s and search base: %s ", sosLdapLoginUserName.getLogin(),
                    searchFilter, searchBase));
        } else {
            SearchResult result = answer.nextElement();
            user = result.getAttributes();
            LOGGER.debug("user found: " + user.toString());
        }
        return user;

    }

    private Collection<String> getGroupNamesByUser() throws NamingException {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        Collection<String> groupNames = null;

        String searchFilter = substituteUserName(sosLdapAuthorizingRealm.getUserSearchFilter());

        LOGGER.debug(String.format("getting groups from ldap using user search filter %s with search base %s", sosLdapAuthorizingRealm.getSearchBase(),
                searchFilter));

        NamingEnumeration<SearchResult> answer = ldapContext.search(sosLdapAuthorizingRealm.getSearchBase(), searchFilter, searchCtls);

        if (!answer.hasMore()) {
            LOGGER.warn(String.format("Cannot find roles for user: %s with search filter %s and  search base: %s ", sosLdapLoginUserName
                    .getUserName(), searchFilter, sosLdapAuthorizingRealm.getSearchBase()));
        } else {
            SearchResult result = answer.nextElement();
            String groupNameAttribute = sosLdapAuthorizingRealm.getGroupNameAttribute();
            Attribute memberOf = result.getAttributes().get(groupNameAttribute);

            if (memberOf != null) {
                LOGGER.debug("getting all attribute values using attribute " + memberOf);
                groupNames = LdapUtils.getAllAttributeValues(memberOf);
            } else {
                LOGGER.info(String.format("User: %s is not member of any group", sosLdapLoginUserName.getUserName()));
            }
        }
        return groupNames;
    }

    private boolean addRolesFromUserSection(Map<String, String> s, String searchUsername) {
        boolean found = false;
        if (searchUsername != null) {
            searchUsername = searchUsername.replaceAll(" ", "%20").toLowerCase();
            String roles = s.get(searchUsername);

            if (roles != null) {
                found = true;
                String[] listOfRoles = roles.split(",");
                if (listOfRoles.length > 1) {
                    for (int i = 1; i < listOfRoles.length; i++) {
                        LOGGER.debug("add role" + listOfRoles[i].trim());
                        authorizationInfo.addRole(listOfRoles[i].trim());
                    }
                }
            }
        }
        return found;

    }

    private void getRoleNamesForUser() throws Exception {
        LOGGER.debug(String.format("Getting roles for user %s", sosLdapLoginUserName.getLogin()));
        Ini ini = Globals.getIniFromSecurityManagerFactory();
        Section s = ini.getSection("users");
        HashMap<String, String> caseInsensitivUser = new HashMap<String, String>();

        if (s != null) {
            LOGGER.debug("reading roles for " + sosLdapLoginUserName.getLogin() + " from section [users]");
            for (Map.Entry<String, String> entry : s.entrySet()) {
                caseInsensitivUser.put(entry.getKey().toLowerCase(), entry.getValue());
            }
            LOGGER.debug("reading roles for " + sosLdapLoginUserName.getLogin() + " from section [users]");
            if (!addRolesFromUserSection(caseInsensitivUser, sosLdapLoginUserName.getLogin())) {
                LOGGER.debug("... not found: reading roles for " + sosLdapLoginUserName.getLogin() + " from section [users]");
                if (sosLdapLoginUserName.getAlternateLogin() != null) {
                    if (!addRolesFromUserSection(caseInsensitivUser, sosLdapLoginUserName.getAlternateLogin()))
                        LOGGER.debug("... not found: reading roles for " + sosLdapLoginUserName.getAlternateLogin() + " from section [users]");
                }
            }
        }

        if (sosLdapAuthorizingRealm.getUserNameAttribute() != null && !sosLdapAuthorizingRealm.getUserNameAttribute().isEmpty()) {
            LOGGER.debug("get userPrincipalName for substitution from user record.");
            Attributes user = getUserAttributes();
            if (user != null) {
                if (user.get(sosLdapAuthorizingRealm.getUserNameAttribute()) == null) {
                    LOGGER.error(String.format(
                            "can not find the attribute %s in the user record. Please check the setting .userNameAttribute in the shiro.ini configuration file",
                            sosLdapAuthorizingRealm.getUserNameAttribute()));
                } else {
                    sosLdapLoginUserName.setUserName(user.get(sosLdapAuthorizingRealm.getUserNameAttribute()).get().toString());
                }
            } else {
                LOGGER.info("using the username from login: " + sosLdapLoginUserName.getLogin());
            }
        }
        LOGGER.debug("userPrincipalName: " + sosLdapLoginUserName.getUserName());

        if ("true".equalsIgnoreCase(sosLdapAuthorizingRealm.getGetRolesFromLdap())) {
            if ((sosLdapAuthorizingRealm.getSearchBase() != null || sosLdapAuthorizingRealm.getGroupSearchBase() != null) && (sosLdapAuthorizingRealm
                    .getGroupSearchFilter() != null || sosLdapAuthorizingRealm.getUserSearchFilter() != null)) {
                
                Collection<String> groupNames;

                if (sosLdapAuthorizingRealm.getGroupSearchFilter() != null && !sosLdapAuthorizingRealm.getGroupSearchFilter().isEmpty()) {
                    groupNames = getGroupNamesByGroup();
                } else {
                    groupNames = getGroupNamesByUser();
                }

                if (groupNames != null) {
                    getRoleNamesForGroups(groupNames);
                }
            }
        }
        LdapUtils.closeContext(ldapContext);
    }

    public void getRoleNamesForUserTest(SOSLdapAuthorizingRealm sosLdapAuthorizingRealm, String username) throws Exception {

        // String ldapAdServer = "ldap://ldap.andrew.cmu.edu:389";
        String ldapAdServer = "ldap://localhost:389";
        Hashtable<String, Object> env = new Hashtable<String, Object>();
        env.put(Context.PROVIDER_URL, ldapAdServer);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");

        env.put(Context.SECURITY_PRINCIPAL, "CN=ur,CN=sos,DC=berlin,DC=com");
        env.put(Context.SECURITY_CREDENTIALS, "aplsos");

        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapAdServer);
        env.put("java.naming.ldap.attributes.binary", "objectSID");

        InitialDirContext ldapContext = new InitialDirContext(env);

        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        String userPrincipalName = username;

        if (sosLdapAuthorizingRealm.getSearchBase() != null && sosLdapAuthorizingRealm.getUserSearchFilter() != null) {
            LOGGER.debug("user_search_filter=" + sosLdapAuthorizingRealm.getUserSearchFilter());

            String searchFilter = substituteUserName(sosLdapAuthorizingRealm.getUserSearchFilter());
            LOGGER.debug(String.format("getting groups from ldap using user search filter %s with search base %s", sosLdapAuthorizingRealm.getSearchBase(),
                    searchFilter));

            NamingEnumeration<SearchResult> answer = ldapContext.search(sosLdapAuthorizingRealm.getSearchBase(), searchFilter, searchCtls);
            ArrayList<String> rolesForGroups = new ArrayList<String>();
            LOGGER.debug("Retrieving group names for user [" + userPrincipalName + "]");

            while (answer.hasMoreElements()) {
                SearchResult result = answer.next();
                String groupNameAttribute;
                groupNameAttribute = sosLdapAuthorizingRealm.getGroupNameAttribute();
                Attribute g = result.getAttributes().get(groupNameAttribute);
                rolesForGroups.add(g.get().toString());
            }
        }
    }

    protected void getRoleNamesForGroups(Collection<String> groupNames) {
        if (sosLdapAuthorizingRealm.getGroupRolesMap() != null) {
            LOGGER.debug(String.format("Analysing groupRolesMapping: %s", sosLdapAuthorizingRealm.getGroupRolesMap()));
            for (String groupName : groupNames) {
                LOGGER.debug(String.format("Looking for group: %s", groupName));
                String strRoleNames = sosLdapAuthorizingRealm.getGroupRolesMap().get(groupName);
                if (strRoleNames != null) {
                    LOGGER.debug(String.format("roles for group %s: %s", groupName, strRoleNames));
                    for (String roleName : strRoleNames.split(ROLE_NAMES_DELIMETER)) {
                        authorizationInfo.addRole(roleName);
                    }
                } else {
                    LOGGER.debug(String.format("Group %s not found in groupRolesMapping", groupName));
                }
            }
        }
    }

    public void setSosLdapAuthorizingRealm(SOSLdapAuthorizingRealm sosLdapAuthorizingRealm) throws IOException, NamingException {

        LOGGER.debug("...reading contextFactory. TLS=" + sosLdapAuthorizingRealm.getUseStartTls());

        this.sosLdapAuthorizingRealm = sosLdapAuthorizingRealm;

        ldapContextFactory = sosLdapAuthorizingRealm.getContextFactory();

        Object principal = sosLdapAuthorizingRealm.getLdapPrincipal(authcToken);
        Object credentials = authcToken.getCredentials();
        try {
            ldapContext = ldapContextFactory.getLdapContext(principal, credentials);
            JndiLdapContextFactory jndiLdapContextFactory = (JndiLdapContextFactory) ldapContextFactory;
            if ("true".equalsIgnoreCase(sosLdapAuthorizingRealm.getUseStartTls())) {
                LOGGER.debug("using StartTls for authentication");
                StartTlsRequest startTlsRequest = new StartTlsRequest();
                StartTlsResponse tls = (StartTlsResponse) ldapContext.extendedOperation(startTlsRequest);

                if (Globals.withHostnameVerification) {
                    if ("false".equalsIgnoreCase(sosLdapAuthorizingRealm.getHostNameVerification()) || "off".equalsIgnoreCase(sosLdapAuthorizingRealm
                            .getHostNameVerification())) {
                        LOGGER.debug("HostNameVerification=false");
                        tls.setHostnameVerifier(new DummyVerifier());
                    }else {
                        LOGGER.debug("HostNameVerification=true");
                    }
                } else {
                    if ("true".equalsIgnoreCase(sosLdapAuthorizingRealm.getHostNameVerification()) || "on".equalsIgnoreCase(sosLdapAuthorizingRealm
                            .getHostNameVerification())) {
                        LOGGER.debug("HostNameVerification=true");
                    } else {
                        LOGGER.debug("HostNameVerification=false");
                        tls.setHostnameVerifier(new DummyVerifier());
                    }
                }
                LOGGER.debug("negotiate ...");
                tls.negotiate();
                LOGGER.debug("...negotiation succeeded");
                if (jndiLdapContextFactory.getAuthenticationMechanism() != null) {
                    ldapContext.addToEnvironment(Context.SECURITY_AUTHENTICATION, jndiLdapContextFactory.getAuthenticationMechanism());
                }
                ldapContext.addToEnvironment(Context.SECURITY_PRINCIPAL, principal);
                ldapContext.addToEnvironment(Context.SECURITY_CREDENTIALS, credentials);
                LOGGER.debug("reconnect ...");
                ldapContext.reconnect(ldapContext.getConnectControls());
                LOGGER.debug("... reconnection succeeded");
            }
        } catch (IOException e) {
            LdapUtils.closeContext(ldapContext);
            LOGGER.error("Failed to negotiate TLS connection': ", e);
            throw e;
        } catch (NamingException e) {
            LOGGER.warn(e.getMessage(),e);
        } catch (Throwable t) {
            LdapUtils.closeContext(ldapContext);
            LOGGER.error("Unexpected failure to negotiate TLS connection", t);
            throw t;
        }
    }

    public void setAuthcToken(AuthenticationToken authcToken) {
        this.authcToken = authcToken;
    }

    /** The hostname verifier always return true */
    final static class DummyVerifier implements HostnameVerifier {

        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

}
