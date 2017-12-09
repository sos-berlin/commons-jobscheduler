package com.sos.auth.shiro;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;

public class SOSlogin {

    private static final Logger LOGGER = Logger.getLogger(SOSlogin.class);

    private Subject currentUser;
    private String msg;
    private IniSecurityManagerFactory factory = null;

    public SOSlogin(IniSecurityManagerFactory factory) {
        this.factory = factory;
    }

    private void clearCache(String user) {
        RealmSecurityManager mgr = (RealmSecurityManager) SecurityUtils.getSecurityManager();

        Collection<Realm> realmCollection = mgr.getRealms();
        for (Realm realm : realmCollection) {
            if (realm instanceof AuthorizingRealm) {
                SimplePrincipalCollection spc = new SimplePrincipalCollection();
                spc.add(user, realm.getName());

                AuthorizingRealm authRealm = (AuthorizingRealm) realm;
                if (authRealm.getAuthenticationCache() != null) {
                    authRealm.getAuthenticationCache().remove(spc);
                }
                if (authRealm.getAuthorizationCache() != null) {
                    authRealm.getAuthorizationCache().remove(spc);
                }
            }
        }

    }

    public void createSubject(String user, String pwd) {
        clearCache(user);
        UsernamePasswordToken token = new UsernamePasswordToken(user, pwd);
        if (currentUser != null) {
            try {
                currentUser.login(token);
            } catch (UnknownAccountException uae) {
                setMsg("There is no user with username/password combination of " + token.getPrincipal());
                currentUser = null;
            } catch (IncorrectCredentialsException ice) {
                setMsg("There is no user with username / password combination of " + token.getPrincipal());
                currentUser = null;
            } catch (LockedAccountException lae) {
                setMsg("The account for username " + token.getPrincipal() + " is locked.  " + "Please contact your administrator to unlock it.");
                currentUser = null;
            } catch (Exception ee) {
                String cause = "";
                if (ee.getCause() != null) {
                    cause = ee.getCause().toString();
                }
                setMsg("Exception while logging in " + token.getPrincipal() + " " + ee.toString() + ": " + cause);
                currentUser = null;
            }
        }
    }

    public void login(String user, String pwd) {
        if (user == null) {
            currentUser = null;
        } else {
            if (currentUser != null && currentUser.isAuthenticated()) {
                logout();
            }
            this.init();

            createSubject(user, pwd);
        }
    }

    public void logout() {
        if (currentUser != null) {
            currentUser.logout();
        }
    }

    private void init() {

        if (factory != null) {
            SecurityManager securityManager = factory.getInstance();
            SecurityUtils.setSecurityManager(securityManager);
        } else {
            LOGGER.error("Shiro init: SecurityManagerFactory is not defined");
        }

        currentUser = new Subject.Builder().buildSubject();

        try {
            logout();
        } catch (InvalidSessionException e) {
            // ignore this.
        } catch (Exception e) {
            LOGGER.info(String.format("Shiro init: %1$s: %2$s", e.getClass().getSimpleName(), e.getMessage()));
        }
    }

    public Subject getCurrentUser() {
        return currentUser;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
