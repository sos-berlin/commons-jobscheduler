package com.sos.auth.rest;

 

    public class SOSWebserviceAuthenticationRecord {
    private String sessionId="";
    private String user="";
    private String password="";
    private String permission="";
    private String resource="";
    
    public SOSWebserviceAuthenticationRecord() {
    }
    
   
    
 
    public void setPassword(String password) {
    this.password = password;
    }
     
    public String getPassword() {
    return this.password;
    }
 
    public void setUser(String user) {
    this.user = user;
    }
     
    public String getUser() {
    return this.user;
    }

 
    public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
    }
     
    public String getSessionId() {
    return this.sessionId;
    }

     
  
    public void setResource(String resource) {
    this.resource = resource;
    }
     
    public String getResource() {
    return this.resource;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }
    
    
    
    }