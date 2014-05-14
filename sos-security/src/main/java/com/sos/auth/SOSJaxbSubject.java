package com.sos.auth;

import com.sos.auth.rest.permission.model.SOSPermissionShiro;
 
public class SOSJaxbSubject {

    private SOSPermissionShiro sosPermissionShiro;
    
       public SOSJaxbSubject(SOSPermissionShiro sosPermissionShiro) {
        super();
        this.sosPermissionShiro = sosPermissionShiro;
    }

    public boolean hasRole(String role) {
         return (sosPermissionShiro != null && sosPermissionShiro.getSOSPermissionRoles().getSOSPermissionRole().contains(role));
    }
    
    public boolean isPermitted(String permission) {
        if (permission.startsWith("jobscheduler:jid:")) {
            return (sosPermissionShiro != null && sosPermissionShiro.getSOSPermissions().getSOSPermissionJid().getSOSPermission().contains(permission));
         }
        
        if (permission.startsWith("jobscheduler:joc:")) {
            return (sosPermissionShiro != null && sosPermissionShiro.getSOSPermissions().getSOSPermissionJoc().getSOSPermission().contains(permission));
         }
        
        if (permission.startsWith("jobscheduler:joe:")) {
            return (sosPermissionShiro != null && sosPermissionShiro.getSOSPermissions().getSOSPermissionJoe().getSOSPermission().contains(permission));
         }
        
        return false;
    }
    
  
    
    public boolean isAuthenticated() {
        return (sosPermissionShiro != null && sosPermissionShiro.isAuthenticated() != null && sosPermissionShiro.isAuthenticated());
    }
    
    

}
