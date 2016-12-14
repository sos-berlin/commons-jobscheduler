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

        if (permission.startsWith("sos:products:joc_cockpit:")) {
            return (sosPermissionShiro != null && sosPermissionShiro.getSOSPermissions().getSOSPermissionListJoc().getSOSPermission().contains(permission));
        }
        if (permission.startsWith("sos:products:commands:")) {
            return (sosPermissionShiro != null && sosPermissionShiro.getSOSPermissions().getSOSPermissionListCommands().getSOSPermission().contains(permission));
        }
        return false;
    }

    public boolean isAuthenticated() {
        return sosPermissionShiro != null && sosPermissionShiro.isAuthenticated() != null && sosPermissionShiro.isAuthenticated();
    }

    public String getSessionId() {
        if (sosPermissionShiro == null) {
            return "";
        } else {
            return sosPermissionShiro.getAccessToken();
        }
    }

}