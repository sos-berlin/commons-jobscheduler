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
import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.apache.shiro.realm.ldap.LdapUtils;
import org.apache.shiro.subject.PrincipalCollection;

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
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        String userPrincipalName = username;
        String searchFilter = String.format(sosLdapAuthorizingRealm.getUserSearchFilter(), userPrincipalName);
        NamingEnumeration<SearchResult> answer = ldapContext.search(sosLdapAuthorizingRealm.getSearchBase(), searchFilter, searchCtls);

        if (!answer.hasMore()) {
            throw new Exception("Cannot locate user information for " + username);
        }
        SearchResult result = answer.nextElement();

        Attribute memberOf = result.getAttributes().get(sosLdapAuthorizingRealm.getGroupNameAttribute());
        if (memberOf != null) {
            Collection<String> groupNames = LdapUtils.getAllAttributeValues(memberOf);
            getRoleNamesForGroups(groupNames);
            // groups.add(new GrantedAuthorityImpl(att.get().toString()));
        }

        LdapUtils.closeContext(ldapContext);
    }

    protected void getRoleNamesForGroups(Collection<String> groupNames) {
        if (sosLdapAuthorizingRealm.getGroupRolesMap() != null) {
            for (String groupName : groupNames) {
                String strRoleNames = sosLdapAuthorizingRealm.getGroupRolesMap().get(groupName);
                if (strRoleNames != null) {
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
