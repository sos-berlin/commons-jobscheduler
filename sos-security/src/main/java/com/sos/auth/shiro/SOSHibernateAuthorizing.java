package com.sos.auth.shiro;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;

import com.sos.auth.shiro.db.SOSUser2RoleDBItem;
import com.sos.auth.shiro.db.SOSUserDBItem;
import com.sos.auth.shiro.db.SOSUserDBLayer;
import com.sos.auth.shiro.db.SOSUserPermissionDBItem;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.joc.Globals;

public class SOSHibernateAuthorizing implements ISOSAuthorizing {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateAuthorizing.class);
    private SimpleAuthorizationInfo authorizationInfo = null;
 
 

    @Override
    public SimpleAuthorizationInfo setRoles(SimpleAuthorizationInfo authorizationInfo_, PrincipalCollection principalCollection)  {
        if (authorizationInfo_ == null) {
            SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
            authorizationInfo = simpleAuthorizationInfo;
        } else {
            authorizationInfo = authorizationInfo_;
        }
        String userName = (String) principalCollection.getPrimaryPrincipal();
        SOSUserDBLayer sosUserDBLayer;
        SOSHibernateSession sosHibernateSession = null;

        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SimpleAuthorizationInfo");
            sosUserDBLayer = new SOSUserDBLayer(sosHibernateSession);
        } catch (Exception e1) {
             e1.printStackTrace();
             return null;
        }finally {
            if (sosHibernateSession != null) {
                sosHibernateSession.close();
            }
        }
        sosUserDBLayer.getFilter().setUserName(userName);
        List<SOSUserDBItem> sosUserList = null;
        try {
            sosUserList = sosUserDBLayer.getSOSUserList(0);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        for (SOSUserDBItem sosUserDBItem : sosUserList) {
            for (SOSUser2RoleDBItem sosUser2RoleDBItem : sosUserDBItem.getSOSUserRoleDBItems()) {
                if (sosUser2RoleDBItem.getSosUserRoleDBItem() != null) {
                    authorizationInfo.addRole(sosUser2RoleDBItem.getSosUserRoleDBItem().getSosUserRole());
                }
            }
        }
        return authorizationInfo;
    }

    @Override
    public SimpleAuthorizationInfo setPermissions(SimpleAuthorizationInfo authorizationInfo_, PrincipalCollection principalCollection) {
        if (authorizationInfo == null) {
            SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
            authorizationInfo = simpleAuthorizationInfo;
        } else {
            authorizationInfo = authorizationInfo_;
        }
        String userName = (String) principalCollection.getPrimaryPrincipal();
        SOSUserDBLayer sosUserDBLayer;
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SimpleAuthorizationInfo");
            sosUserDBLayer = new SOSUserDBLayer(sosHibernateSession);
        } catch (Exception e1) {
            e1.printStackTrace();
            return null;
        }finally {
            if (sosHibernateSession != null) {
                sosHibernateSession.close();
            }
        }       sosUserDBLayer.getFilter().setUserName(userName);
        List<SOSUserDBItem> sosUserList = null;
        try {
            sosUserList = sosUserDBLayer.getSOSUserList(0);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        for (SOSUserDBItem sosUserDBItem : sosUserList) {
            // Die direkt zugewiesenen Rechte.
            for (SOSUserPermissionDBItem sosUserPermissionDBItem : sosUserDBItem.getSOSUserPermissionDBItems()) {
                authorizationInfo.addStringPermission(sosUserPermissionDBItem.getSosUserPermission());
            }
            // Die über die Rollen zugewiesenen Rechte
            for (SOSUser2RoleDBItem sosUser2RoleDBItem : sosUserDBItem.getSOSUserRoleDBItems()) {
                for (SOSUserPermissionDBItem sosUserPermissionDBItemFromRole : sosUser2RoleDBItem.getSosUserRoleDBItem().getSOSUserPermissionDBItems()) {
                    authorizationInfo.addStringPermission(sosUserPermissionDBItemFromRole.getSosUserPermission());
                }
            }
        }
        return authorizationInfo;
    }

}
