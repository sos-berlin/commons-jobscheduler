package com.sos.auth.shiro;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;


import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.apache.shiro.realm.ldap.LdapUtils;
import org.apache.shiro.subject.PrincipalCollection;

public class SOSLdapAuthorizing{

    private static final String     ROLE_NAMES_DELIMETER = "\\|";
    private SimpleAuthorizationInfo authorizationInfo    = null;
    private LdapContextFactory ldapContextFactory;
    private SOSLdapAuthorizingRealm sosLdapAuthorizingRealm;        
 
    public SimpleAuthorizationInfo setRoles(SimpleAuthorizationInfo authorizationInfo_, PrincipalCollection principalCollection) {

        if (authorizationInfo_ == null) {
            SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
            authorizationInfo = simpleAuthorizationInfo;
        }
        else {
            authorizationInfo = authorizationInfo_;
        }

        try {
            queryForAuthorizationInfo(principalCollection,  ldapContextFactory);
        }
        catch (NamingException e) {
            e.printStackTrace();
        }    
        return authorizationInfo;
    }
 
    protected void queryForAuthorizationInfo(PrincipalCollection principals, LdapContextFactory ldapContextFactory) throws NamingException {

        String userName = (String) principals.getPrimaryPrincipal();
        LdapContext ldapContext = ldapContextFactory.getSystemLdapContext();

        try {
            getRoleNamesForUser(userName, ldapContext);
        } finally {
            LdapUtils.closeContext(ldapContext);
        }

     }
 

    private void getRoleNamesForUser(String username, LdapContext ldapContext) throws NamingException {

        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        String userPrincipalName = username;
       

        String searchFilter = String.format("(&(objectClass=*)(%s={0}))",sosLdapAuthorizingRealm.getUserNameAttribute());
        
        Object[] searchArguments = new Object[]{userPrincipalName};
     
        try {

        NamingEnumeration answer = ldapContext.search(sosLdapAuthorizingRealm.getSearchBase(), searchFilter, searchArguments, searchCtls);
      
        while (answer.hasMoreElements()) {
            SearchResult sr = (SearchResult) answer.next();

            Attributes attrs = sr.getAttributes();

            if (attrs != null) {
                NamingEnumeration ae = attrs.getAll();
                while (ae.hasMore()) {
                    Attribute attr = (Attribute) ae.next();

                    if (attr.getID().equals(sosLdapAuthorizingRealm.getGroupNameAttribute())) {
                        Collection<String> groupNames = LdapUtils.getAllAttributeValues(attr);
                        getRoleNamesForGroups(groupNames);
                     }
                }
            }
        }
        }catch (Exception e) {
            e.printStackTrace();
        }

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

    protected void queryForPermission(PrincipalCollection principals, LdapContextFactory ldapContextFactory) throws NamingException {

        String userName = (String) principals.getPrimaryPrincipal();
        LdapContext ldapContext = ldapContextFactory.getSystemLdapContext();
        Set<String> roleNames;

        try {
             getPermissionsForUser(userName, ldapContext);
        } finally {
            LdapUtils.closeContext(ldapContext);
        }

    }
    
    private void getPermissionsForUser(String username, LdapContext ldapContext) throws NamingException {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        String userPrincipalName = username;

        String searchFilter = String.format("(&(objectClass=*)(%s={0}))",sosLdapAuthorizingRealm.getUserNameAttribute());
        
        Object[] searchArguments = new Object[]{userPrincipalName};
     
        try {

        NamingEnumeration answer = ldapContext.search(sosLdapAuthorizingRealm.getSearchBase(), searchFilter, searchArguments, searchCtls);
      
        while (answer.hasMoreElements()) {
            SearchResult sr = (SearchResult) answer.next();

            Attributes attrs = sr.getAttributes();

            if (attrs != null) {
                NamingEnumeration ae = attrs.getAll();
                while (ae.hasMore()) {
                    Attribute attr = (Attribute) ae.next();

                    if (attr.getID().equals(sosLdapAuthorizingRealm.getGroupNameAttribute())) {
                        Collection<String> groupNames = LdapUtils.getAllAttributeValues(attr);
                        getPermissionsForRole(groupNames);
                     }
                }
            }
        }
        }catch (Exception e) {
            e.printStackTrace();
        }

     }

    
    protected void getPermissionsForRole(Collection<String> groupNames) {
 
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

    public SimpleAuthorizationInfo setPermittions(SimpleAuthorizationInfo authorizationInfo_, PrincipalCollection principalCollection) {
        if (authorizationInfo == null) {
            SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
            authorizationInfo = simpleAuthorizationInfo;
        }
        else {
            authorizationInfo = authorizationInfo_;
        }


        try {
            queryForPermission(principalCollection,  ldapContextFactory);
        }
        catch (NamingException e) {
            e.printStackTrace();
        }    
        
        return authorizationInfo;
    }

    
 
   
    public void setSosLdapAuthorizingRealm(SOSLdapAuthorizingRealm sosLdapAuthorizingRealm) {
        this.sosLdapAuthorizingRealm = sosLdapAuthorizingRealm;
        ldapContextFactory = sosLdapAuthorizingRealm.getContextFactory();
    }

}
