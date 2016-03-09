package com.sos.auth.shiro;

import java.io.File;
import java.util.List;

import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.apache.shiro.subject.PrincipalCollection;

import com.sos.auth.shiro.db.SOSUser2RoleDBItem;
import com.sos.auth.shiro.db.SOSUserDBItem;
import com.sos.auth.shiro.db.SOSUserDBLayer;
import com.sos.auth.shiro.db.SOSUserRightDBItem;

public class SOSHibernateAuthorizing implements ISOSAuthorizing {

    private SimpleAuthorizationInfo authorizationInfo = null;
    private File hibernateConfigurationFile = null;

    public void setHibernateConfigurationFile(String filename) {
        hibernateConfigurationFile = new File(filename);

    }

    @Override
    public SimpleAuthorizationInfo setRoles(SimpleAuthorizationInfo authorizationInfo_, PrincipalCollection principalCollection) {

        if (authorizationInfo_ == null) {
            SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
            authorizationInfo = simpleAuthorizationInfo;
        } else {
            authorizationInfo = authorizationInfo_;
        }

        String userName = (String) principalCollection.getPrimaryPrincipal();

        SOSUserDBLayer sosUserDBLayer = new SOSUserDBLayer(hibernateConfigurationFile);
        sosUserDBLayer.getFilter().setUserName(userName);
        List<SOSUserDBItem> sosUserList = sosUserDBLayer.getSOSUserList(0);
        for (SOSUserDBItem sosUserDBItem : sosUserList) {

            for (SOSUser2RoleDBItem sosUser2RoleDBItem : sosUserDBItem.getSOSUserRoleDBItems()) {
                if (sosUser2RoleDBItem.getSosUserRoleDBItem() != null) {
                    System.out.println(sosUser2RoleDBItem.getSosUserRoleDBItem().getSosUserRole());
                    authorizationInfo.addRole(sosUser2RoleDBItem.getSosUserRoleDBItem().getSosUserRole());
                }
            }
        }

        return authorizationInfo;
    }

    @Override
    public SimpleAuthorizationInfo setPermittions(SimpleAuthorizationInfo authorizationInfo_, PrincipalCollection principalCollection) {
        if (authorizationInfo == null) {
            SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
            authorizationInfo = simpleAuthorizationInfo;
        } else {
            authorizationInfo = authorizationInfo_;
        }

        String userName = (String) principalCollection.getPrimaryPrincipal();
        SOSUserDBLayer sosUserDBLayer = new SOSUserDBLayer(new File("c:/temp/hibernate.cfg.xml"));
        sosUserDBLayer.getFilter().setUserName(userName);

        List<SOSUserDBItem> sosUserList = sosUserDBLayer.getSOSUserList(0);
        for (SOSUserDBItem sosUserDBItem : sosUserList) {

            // Die direkt zugewiesenen Rechte.
            for (SOSUserRightDBItem sosUserRightDBItem : sosUserDBItem.getSOSUserRightDBItems()) {
                authorizationInfo.addStringPermission(sosUserRightDBItem.getSosUserRight());
            }

            // Die über die Rollen zugewiesenen Rechte

            for (SOSUser2RoleDBItem sosUser2RoleDBItem : sosUserDBItem.getSOSUserRoleDBItems()) {
                for (SOSUserRightDBItem sosUserRightDBItemFromRole : sosUser2RoleDBItem.getSosUserRoleDBItem().getSOSUserRightDBItems()) {
                    authorizationInfo.addStringPermission(sosUserRightDBItemFromRole.getSosUserRight());
                }
            }
        }
        return authorizationInfo;
    }

}
