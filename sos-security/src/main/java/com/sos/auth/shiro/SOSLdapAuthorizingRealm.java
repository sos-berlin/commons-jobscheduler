package com.sos.auth.shiro;

import java.io.IOException;
import java.util.Map;

import javax.naming.NamingException;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.ldap.JndiLdapRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.log4j.Logger;

public class SOSLdapAuthorizingRealm extends JndiLdapRealm {

    private static final Logger LOGGER = Logger.getLogger(SOSLdapAuthorizingRealm.class);

    private static final String DEFAULT_GROUP_NAME_ATTRIBUTE = "memberOf";
    private SOSLdapAuthorizing authorizing;
    private String searchBase;
    private String groupSearchBase;
    private Map<String, String> groupRolesMap;
    private Map<String, String> permissions;
    private String groupNameAttribute;
    private String userNameAttribute;
    private String getRolesFromLdap;
    private String useStartTls;
    private String groupSearchFilter;
    private String userSearchFilter;
    private String hostNameVerification;
    private AuthenticationToken authcToken;

    public boolean supports(AuthenticationToken token) {
        setAuthorizing(new SOSLdapAuthorizing());
        return true;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        SimpleAuthorizationInfo authzInfo = null;
        if (authorizing != null) {

            authorizing.setAuthcToken(authcToken);
            try {
                authorizing.setSosLdapAuthorizingRealm(this);
                authzInfo = authorizing.setRoles(authzInfo, principalCollection);
                
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (NamingException e) {
                throw new RuntimeException(e);
            }
            
            if (this.getCacheManager() == null && this.isCachingEnabled()) {
                throw new RuntimeException("LDAP configuration is not valid: Missing setting 'cacheManager'");
            }
            if (this.getSearchBase() == null && this.getUserSearchFilter() != null) {
                throw new RuntimeException("LDAP configuration is not valid: Missing setting 'ldapRealm.searchBase'");
            }
            if (this.getGroupSearchBase() == null && this.getGroupSearchFilter() != null) {
                throw new RuntimeException("LDAP configuration is not valid: Missing setting 'ldapRealm.groupSearchBase'");
            }
            if (this.getUserDnTemplate() == null) {
                throw new RuntimeException("LDAP configuration is not valid: Missing setting 'ldapRealm.userDnTemplate'");
            }
            if (this.getContextFactory() == null) {
                throw new RuntimeException("LDAP configuration is not valid: Missing setting 'ldapRealm.contextFactory.url'");
            }
            if (this.getRolePermissionResolver() == null) {
                throw new RuntimeException("LDAP configuration is not valid: Missing setting 'ldapRealm.rolePermissionResolver'");
            }
            if (this.getPermissionResolver() == null) {
                throw new RuntimeException("LDAP configuration is not valid: Missing setting rolePermissionResolver'");
            }

        }
        return authzInfo;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) throws AuthenticationException {
        this.authcToken = authcToken;

        return super.doGetAuthenticationInfo(authcToken);
    }

    public void setAuthorizing(SOSLdapAuthorizing authorizing) {
        this.authorizing = authorizing;
    }

    public void setSearchBase(String searchBase) {
        this.searchBase = searchBase;
    }

    public void setGroupRolesMap(Map<String, String> groupRolesMap) {
        this.groupRolesMap = groupRolesMap;
    }

    public String getSearchBase() {
        return searchBase;
    }

    public Map<String, String> getGroupRolesMap() {
        return groupRolesMap;
    }

    public String getGroupNameAttribute() {
        if (groupNameAttribute == null) {
            return DEFAULT_GROUP_NAME_ATTRIBUTE;
        } else {
            return groupNameAttribute;
        }
    }

    public void setGroupNameAttribute(String groupNameAttribute) {
        this.groupNameAttribute = groupNameAttribute;
    }

    public String getUserNameAttribute() {
        return userNameAttribute;
    }

    public void setUserNameAttribute(String userNameAttribute) {
        this.userNameAttribute = userNameAttribute;
    }

    public Map<String, String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String, String> permissions) {
        this.permissions = permissions;
    }

    public Object getLdapPrincipal(AuthenticationToken token) {
        return super.getLdapPrincipal(token);
    }

    public String getUserSearchFilter() {
        return userSearchFilter;
    }

    public void setUserSearchFilter(String userSearchFilter) {
        this.userSearchFilter = userSearchFilter;
    }

    public void setGetRolesFromLdap(String getRolesFromLdap) {
        this.getRolesFromLdap = getRolesFromLdap;
    }

    public String getGetRolesFromLdap() {
        if (getRolesFromLdap == null) {
            getRolesFromLdap = "true";
        }
        return getRolesFromLdap;
    }

    public void setUseStartTls(String useStartTls) {
        this.useStartTls = useStartTls;
    }

    public String getUseStartTls() {
        return useStartTls;
    }

    public String getGroupSearchFilter() {
        return groupSearchFilter;
    }

    public void setGroupSearchFilter(String groupSearchFilter) {
        this.groupSearchFilter = groupSearchFilter;
    }

    public String getGroupSearchBase() {
        return groupSearchBase;
    }

    public void setGroupSearchBase(String groupSearchBase) {
        this.groupSearchBase = groupSearchBase;
    }

    public String getHostNameVerification() {
        return hostNameVerification;
    }

    public void setHostNameVerification(String hostNameVerification) {
        this.hostNameVerification = hostNameVerification;
    }

}