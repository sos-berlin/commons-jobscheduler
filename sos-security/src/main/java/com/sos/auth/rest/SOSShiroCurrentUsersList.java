package com.sos.auth.rest;

import java.util.HashMap;

public class SOSShiroCurrentUsersList {

    private HashMap<String, SOSShiroCurrentUser> currentUsers;

    public SOSShiroCurrentUsersList() {
        super();
        this.currentUsers = new HashMap<String, SOSShiroCurrentUser>();
    }
    
    public void addUser(SOSShiroCurrentUser user) {
        this.currentUsers.put(user.getSessionId(), user);   
       }
       
    public SOSShiroCurrentUser getUser(String sessionId) {
        return (SOSShiroCurrentUser) this.currentUsers.get(sessionId);   
    }
       
    public void removeUser(String sessionId){
        currentUsers.remove(sessionId);   
    }



}
