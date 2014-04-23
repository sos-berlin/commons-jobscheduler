package  com.sos.auth.shiro;


import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;

import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;



public class SOSlogin {
    
    private String inifile;
    private Subject currentUser;
    private Session session;
    private String msg;
    private static Logger logger = Logger.getLogger(SOSlogin.class);

    
    public SOSlogin() {
        super();
        this.inifile = "classpath:shiro.ini";
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
            setMsg("The account for username " + token.getPrincipal() + " is locked.  " +
                    "Please contact your administrator to unlock it.");
        } catch (Exception ee) {
            setMsg("Exception while logging in " + token.getPrincipal() + ee.getMessage());
    }
    }
    
    public void login(String user, String pwd) {
        
        if (user == null) {
            currentUser = null;
        }else {
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
      //SOSAuthorizingRealm  realm = new SOSAuthorizingRealm();
      //SecurityManager securityManager2 = new DefaultSecurityManager(realm);

      //SecurityUtils.setSecurityManager(securityManager2);
                        
        
        
        Factory<SecurityManager> factory = new IniSecurityManagerFactory(inifile);
        SecurityManager securityManager = factory.getInstance();
        SecurityUtils.setSecurityManager(securityManager);
        currentUser = SecurityUtils.getSubject();

        session = currentUser.getSession();    
        session.setAttribute("someKey", "aValue");
      
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
