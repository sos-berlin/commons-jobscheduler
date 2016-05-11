package com.sos.auth.shiro;

import java.util.Map;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.ldap.JndiLdapRealm;
import org.apache.shiro.subject.PrincipalCollection;

public class SOSLdapAuthorizingRealm extends JndiLdapRealm {

    private SOSLdapAuthorizing authorizing;
    private String searchBase;
    private Map<String, String> groupRolesMap;
    private Map<String, String> permissions;
    private String groupNameAttribute;
    private String userNameAttribute;

    public boolean supports(AuthenticationToken token) {
        SOSLdapAuthorizing authorizing = new SOSLdapAuthorizing();
        setAuthorizing(authorizing);
        return true;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        SimpleAuthorizationInfo authzInfo = null;
        if (authorizing != null) {
            authorizing.setSosLdapAuthorizingRealm(this);
            authzInfo = authorizing.setRoles(authzInfo, principalCollection);
        }
        return authzInfo;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) throws AuthenticationException {
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

    public SOSLdapAuthorizing getAuthorizing() {
        return authorizing;
    }

    public String getSearchBase() {
        return searchBase;
    }

    public Map<String, String> getGroupRolesMap() {
        return groupRolesMap;
    }

    public String getGroupNameAttribute() {
        return groupNameAttribute;
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

}