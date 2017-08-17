package com.sos.auth.shiro;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;

public class SOSlogin {

    private static final Logger LOGGER = Logger.getLogger(SOSlogin.class);

    private static final String DEFAULT_INI_FILE = "classpath:shiro.ini";
    private String inifile;
    private Subject currentUser;
    private String msg;

    public SOSlogin() {
        this.inifile = DEFAULT_INI_FILE;
    }

    public SOSlogin(String inifile) {
        this.inifile = inifile;
    }

    public void setInifile(String inifile) {
        this.inifile = inifile;
    }

    public String getInifile() {
        return inifile;
    }

    public void createSubject(String user, String pwd) {
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
                setMsg("Exception while logging in " + token.getPrincipal() + " " + ee.getMessage().toString());
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
        Factory<SecurityManager> factory = new IniSecurityManagerFactory(inifile);
        SecurityManager securityManager = factory.getInstance();
        SecurityUtils.setSecurityManager(securityManager);
        
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
