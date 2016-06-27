package com.sos.jobscheduler.tools.webservices;

import java.util.HashMap;

public class SOSCommandSecurityWebserviceCurrentUsersList {

    private HashMap<String, SOSCommandSecurityWebserviceCurrentUser> currentUsers;

    public SOSCommandSecurityWebserviceCurrentUsersList() {
        super();
        this.currentUsers = new HashMap<String, SOSCommandSecurityWebserviceCurrentUser>();
    }

    public void addUser(SOSCommandSecurityWebserviceCurrentUser user) {
        this.currentUsers.put(user.getAccessToken(), user);
    }

    public SOSCommandSecurityWebserviceCurrentUser getUser(String sessionId) {
        return (SOSCommandSecurityWebserviceCurrentUser) this.currentUsers.get(sessionId);
    }

    public void removeUser(String sessionId) {
        currentUsers.remove(sessionId);
    }

}
