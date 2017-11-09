package com.sos.auth.shiro;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

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

import org.apache.log4j.Logger;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.Ini.Section;
import org.apache.shiro.realm.ldap.JndiLdapContextFactory;
import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.apache.shiro.realm.ldap.LdapUtils;
import org.apache.shiro.subject.PrincipalCollection;
import com.sos.joc.Globals;

public class SOSLdapAuthorizing {

    private static final Logger LOGGER = Logger.getLogger(SOSLdapAuthorizing.class);
    private static final String ROLE_NAMES_DELIMETER = "\\|";
    private SimpleAuthorizationInfo authorizationInfo = null;
    private LdapContextFactory ldapContextFactory;
    private LdapContext ldapContext;
    private SOSLdapAuthorizingRealm sosLdapAuthorizingRealm;
    private AuthenticationToken authcToken;

    public SimpleAuthorizationInfo setRoles(SimpleAuthorizationInfo authorizationInfo_, PrincipalCollection principalCollection) {
        if (authorizationInfo_ == null) {
            SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
            authorizationInfo = simpleAuthorizationInfo;
        } else {
            authorizationInfo = authorizationInfo_;
        }
        try {
            queryForAuthorizationInfo(principalCollection);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return authorizationInfo;
    }

    protected void queryForAuthorizationInfo(PrincipalCollection principals) throws Exception {
        if (ldapContext != null) {

            String userName = (String) principals.getPrimaryPrincipal();
            try {
                getRoleNamesForUser(userName);
            } finally {
                LdapUtils.closeContext(ldapContext);
            }
        }
    }

    private Collection<String> getGroupNamesByGroup(String username) throws NamingException {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        String searchFilter = String.format(sosLdapAuthorizingRealm.getGroupSearchFilter(), username);
        LOGGER.debug(String.format("getting groups from ldap using search filter %s with search base %s", sosLdapAuthorizingRealm.getSearchBase(),
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

    private Attributes getUserAttributes(String username) throws NamingException {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        Attributes user = null;

        String searchFilter = "(" + sosLdapAuthorizingRealm.getUserNameAttribute() + "=%s";
        if (sosLdapAuthorizingRealm.getUserSearchFilter() == null) {
            LOGGER.warn(String.format(
                    "You have specified a value for userNameAttribute but you have not defined the userSearchFilter. The default filter %s will be used",
                    searchFilter));
        } else {
            searchFilter = String.format(sosLdapAuthorizingRealm.getUserSearchFilter(), username);
        }

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
            LOGGER.warn(String.format("Cannot find user: %s with search filter %s and search base: %s ", username, searchFilter, searchBase));
        } else {
            SearchResult result = answer.nextElement();
            user = result.getAttributes();
            LOGGER.debug("user found: " + user.toString());
        }
        return user;

    }

    private Collection<String> getGroupNamesByUser(String username) throws NamingException {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        Collection<String> groupNames = null;

        String searchFilter = String.format(sosLdapAuthorizingRealm.getUserSearchFilter(), username);
        LOGGER.debug(String.format("getting groups from ldap using search filter %s with search base %s", sosLdapAuthorizingRealm.getSearchBase(),
                searchFilter));

        NamingEnumeration<SearchResult> answer = ldapContext.search(sosLdapAuthorizingRealm.getSearchBase(), searchFilter, searchCtls);

        if (!answer.hasMore()) {
            LOGGER.warn(String.format("Cannot find roles for user: %s with search filter %s and  search base: %s ", username, searchFilter,
                    sosLdapAuthorizingRealm.getSearchBase()));
        } else {
            SearchResult result = answer.nextElement();
            String groupNameAttribute = sosLdapAuthorizingRealm.getGroupNameAttribute();
            Attribute memberOf = result.getAttributes().get(groupNameAttribute);

            if (memberOf != null) {
                LOGGER.debug("getting all attribute values using attribute " + memberOf);
                groupNames = LdapUtils.getAllAttributeValues(memberOf);
            } else {
                LOGGER.info(String.format("User: %s is not member of any group", username));
            }
        }
        return groupNames;
    }

    private static String normalizeUser(String username) {
        String[] s = username.split("@");
        if (s.length > 1) {
            username = s[0];
        } else {

            s = username.split("\\\\");
            if (s.length > 1) {
                username = s[1];
            }
        }
        return username;

    }

    private void getRoleNamesForUser(String username) throws Exception {
        LOGGER.debug(String.format("Getting roles for user %s", username));
        Ini ini = Globals.getIniFromSecurityManagerFactory();
        Section s = ini.getSection("users");
        if (s != null) {
            LOGGER.debug("reading roles from section [users]");
            String searchUsername = username.replaceAll(" ", "%20");

            String roles = s.get(searchUsername);

            if (roles != null) {
                String[] listOfRoles = roles.split(",");
                if (listOfRoles.length > 1) {
                    for (int i = 1; i < listOfRoles.length; i++) {
                        LOGGER.debug("add role" + listOfRoles[i].trim());
                        authorizationInfo.addRole(listOfRoles[i].trim());
                    }
                }
            }
        }

        String userPrincipalName = normalizeUser(username);
        if (sosLdapAuthorizingRealm.getUserNameAttribute() != null && !sosLdapAuthorizingRealm.getUserNameAttribute().isEmpty()) {
            LOGGER.debug("get userPrincipalName for substitution from user record. Using username from login.");
            Attributes user = getUserAttributes(username);
            if (user != null) {
                if (user.get(sosLdapAuthorizingRealm.getUserNameAttribute()) == null) {
                    LOGGER.error(String.format(
                            "can not find the attribute %s in the user record. Please check the setting .userNameAttribute in the shiro.ini configuration file",
                            sosLdapAuthorizingRealm.getUserNameAttribute()));
                } else {
                    userPrincipalName = user.get(sosLdapAuthorizingRealm.getUserNameAttribute()).get().toString();
                }
            } else {
                LOGGER.info("using the username from login: " + userPrincipalName);
            }
        }
        LOGGER.debug("userPrincipalName: " + userPrincipalName);

        if ("true".equalsIgnoreCase(sosLdapAuthorizingRealm.getGetRolesFromLdap())) {
            if ((sosLdapAuthorizingRealm.getSearchBase() != null || sosLdapAuthorizingRealm.getGroupSearchBase() != null) && (sosLdapAuthorizingRealm
                    .getGroupSearchFilter() != null || sosLdapAuthorizingRealm.getUserSearchFilter() != null)) {
                LOGGER.debug(String.format("getting groups from ldap using search filter %s with search base %s", sosLdapAuthorizingRealm
                        .getSearchBase(), sosLdapAuthorizingRealm.getUserSearchFilter()));

                Collection<String> groupNames;

                if (sosLdapAuthorizingRealm.getGroupSearchFilter() != null && !sosLdapAuthorizingRealm.getGroupSearchFilter().isEmpty()) {
                    groupNames = getGroupNamesByGroup(userPrincipalName);
                } else {
                    groupNames = getGroupNamesByUser(userPrincipalName);
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
        String ldapAdServer = "ldap://ldap.itd.umich.edu:389";
        Hashtable<String, Object> env = new Hashtable<String, Object>();
        env.put(Context.PROVIDER_URL, ldapAdServer);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");

        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapAdServer);
        env.put("java.naming.ldap.attributes.binary", "objectSID");

        InitialDirContext ldapContext = new InitialDirContext(env);

        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        String userPrincipalName = username;

        if (sosLdapAuthorizingRealm.getSearchBase() != null && sosLdapAuthorizingRealm.getUserSearchFilter() != null) {
            LOGGER.debug(String.format("getting groups from ldap using search filter %s with search base %s", sosLdapAuthorizingRealm.getSearchBase(),
                    sosLdapAuthorizingRealm.getUserSearchFilter()));

            String searchFilter = String.format(sosLdapAuthorizingRealm.getUserSearchFilter(), userPrincipalName);
            LOGGER.debug(String.format("getting groups from ldap using search filter %s with search base %s", sosLdapAuthorizingRealm.getSearchBase(),
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
            for (String groupName : groupNames) {
                String strRoleNames = sosLdapAuthorizingRealm.getGroupRolesMap().get(groupName);
                if (strRoleNames != null) {
                    LOGGER.debug(String.format("roles for group %s: %s", groupName, strRoleNames));
                    for (String roleName : strRoleNames.split(ROLE_NAMES_DELIMETER)) {
                        authorizationInfo.addRole(roleName);
                    }
                }
            }
        }
    }

    public void setSosLdapAuthorizingRealm(SOSLdapAuthorizingRealm sosLdapAuthorizingRealm) throws IOException, NamingException {
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

                boolean globalHostNameVerification = "true".equalsIgnoreCase(Globals.jocConfigurationProperties.getProperties().getProperty(
                        "https_with_hostname_verification"));
                if (globalHostNameVerification) {
                    if ("false".equalsIgnoreCase(sosLdapAuthorizingRealm.getHostNameVerification()) || "off".equalsIgnoreCase(sosLdapAuthorizingRealm
                            .getHostNameVerification())) {
                        tls.setHostnameVerifier(new DummyVerifier());
                    }
                } else {
                    if ("true".equalsIgnoreCase(sosLdapAuthorizingRealm.getHostNameVerification()) || "on".equalsIgnoreCase(sosLdapAuthorizingRealm
                            .getHostNameVerification())) {
                    } else {
                        tls.setHostnameVerifier(new DummyVerifier());
                    }
                }
                tls.negotiate();
                LOGGER.debug("negotiation succeeded");
                if (jndiLdapContextFactory.getAuthenticationMechanism() != null) {
                    ldapContext.addToEnvironment(Context.SECURITY_AUTHENTICATION, jndiLdapContextFactory.getAuthenticationMechanism());
                }
                ldapContext.addToEnvironment(Context.SECURITY_PRINCIPAL, principal);
                ldapContext.addToEnvironment(Context.SECURITY_CREDENTIALS, credentials);
                ldapContext.reconnect(ldapContext.getConnectControls());
                LOGGER.debug("reconnection succeeded");
            }
        } catch (IOException e) {
            LdapUtils.closeContext(ldapContext);
            LOGGER.error("Failed to negotiate TLS connection': ", e);
            throw e;
        } catch (NamingException e) {
            LOGGER.warn(e);
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
