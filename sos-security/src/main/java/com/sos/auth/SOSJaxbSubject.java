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
        return (sosPermissionShiro != null && sosPermissionShiro.getSOSPermissions().getSOSPermissionJid().getSOSPermission().contains(permission));
    }
    
  
    
    public boolean isAuthenticated() {
        return (sosPermissionShiro != null && sosPermissionShiro.isAuthenticated());
    }
    
    

}
