package com.sos.auth.shiro;

import java.util.Collection;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import org.apache.log4j.Logger;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.Ini.Section;
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
        String userName = (String) principals.getPrimaryPrincipal();
        try {
            getRoleNamesForUser(userName);
        } finally {
            LdapUtils.closeContext(ldapContext);
        }
    }

    private void getRoleNamesForUser(String username) throws Exception {
        LOGGER.debug(String.format("Getting roles for user %s", username));
        Ini ini = Ini.fromResourcePath(Globals.getShiroIniInClassPath());
        Section s = ini.getSection("users");
        if (s != null) {
            LOGGER.debug("reading roles from section [users]");
            String searchUsername = username.replaceAll(" +", " ").replaceAll(" ", "%20");

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

            if (!answer.hasMore()) {
                LOGGER.warn(String.format("Cannot find roles for user: %s with search filter %s and  search base: %s ", username, searchFilter, sosLdapAuthorizingRealm.getSearchBase()));
            } else {
                SearchResult result = answer.nextElement();

                String groupNameAttribute;

                groupNameAttribute = sosLdapAuthorizingRealm.getGroupNameAttribute();

                Attribute memberOf = result.getAttributes().get(groupNameAttribute);
                if (memberOf != null) {
                    LOGGER.debug("getting all attribute values using attribute" + memberOf);
                    Collection<String> groupNames = LdapUtils.getAllAttributeValues(memberOf);
                    getRoleNamesForGroups(groupNames);
                }
            }
        }
        LdapUtils.closeContext(ldapContext);
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

    public void setSosLdapAuthorizingRealm(SOSLdapAuthorizingRealm sosLdapAuthorizingRealm) {
        this.sosLdapAuthorizingRealm = sosLdapAuthorizingRealm;

        ldapContextFactory = sosLdapAuthorizingRealm.getContextFactory();

        Object principal = sosLdapAuthorizingRealm.getLdapPrincipal(authcToken);
        Object credentials = authcToken.getCredentials();

        try {
            ldapContext = ldapContextFactory.getLdapContext(principal, credentials);
        } catch (NamingException e) {
            LOGGER.error(e);
        }
    }

    public void setAuthcToken(AuthenticationToken authcToken) {
        this.authcToken = authcToken;
    }

}
