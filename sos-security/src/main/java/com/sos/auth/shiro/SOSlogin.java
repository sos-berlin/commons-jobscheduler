package com.sos.auth.shiro;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
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

    public void createSubject(String user, String pwd) {
        UsernamePasswordToken token = new UsernamePasswordToken(user, pwd);
        try {
            currentUser.login(token);
          } catch (UnknownAccountException uae) {
            setMsg("There is no user with username/password combination of " + token.getPrincipal());
        } catch (IncorrectCredentialsException ice) {
            setMsg("Password for account " + token.getPrincipal() + " was incorrect!");
        } catch (LockedAccountException lae) {
            setMsg("The account for username " + token.getPrincipal() + " is locked.  " + "Please contact your administrator to unlock it.");
        } catch (Exception ee) {
            setMsg("Exception while logging in " + token.getPrincipal() + ee.getMessage());
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
        currentUser.logout();
    }

    private void init() {
        Factory<SecurityManager> factory = new IniSecurityManagerFactory(inifile);
        SecurityManager securityManager = factory.getInstance();
        SecurityUtils.setSecurityManager(securityManager);
        currentUser = SecurityUtils.getSubject();
        try {
            currentUser.logout();
        }catch (Exception e){
            LOGGER.warn("Shiro init: " + e.getMessage(),e);
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
