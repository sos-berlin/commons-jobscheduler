package com.sos.auth.rest;

import org.apache.shiro.subject.Subject;

public class SOSShiroCurrentUser {
    
    private Subject currentSubject;
    private String username;
    private String password;
    private String sessionId;
  
    public SOSShiroCurrentUser(String username, String password) {
        super();
        this.username = username;
        this.password = password;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Subject getCurrentSubject() {
        return currentSubject;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setCurrentSubject(Subject currentSubject) {
        this.currentSubject = currentSubject;
    }
    
    public boolean hasRole(String role) {
        if (currentSubject != null) {
            return currentSubject.hasRole(role);
        }else {
            return false;
        }
    }
    
    public boolean isPermitted(String permission) {
        if (currentSubject != null) {
            return currentSubject.isPermitted(permission);
        }else {
            return false;
        }
    }
    
    public boolean isAuthenticated() {
        if (currentSubject != null) {
            return currentSubject.isAuthenticated();
        }else {
            return false;
        }
    }
    
       

    

}
