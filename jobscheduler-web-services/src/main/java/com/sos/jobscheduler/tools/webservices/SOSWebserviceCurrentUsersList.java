package com.sos.jobscheduler.tools.webservices;

import java.util.HashMap;

public class SOSWebserviceCurrentUsersList {

    private HashMap<String, SOSWebserviceCurrentUser> currentUsers;

    public SOSWebserviceCurrentUsersList() {
        super();
        this.currentUsers = new HashMap<String, SOSWebserviceCurrentUser>();
    }
    
    public void addUser(SOSWebserviceCurrentUser user) {
        this.currentUsers.put(user.getSessionId(), user);   
       }
       
    public SOSWebserviceCurrentUser getUser(String sessionId) {
        return (SOSWebserviceCurrentUser) this.currentUsers.get(sessionId);   
    }
       
    public void removeUser(String sessionId){
        currentUsers.remove(sessionId);   
    }



}
