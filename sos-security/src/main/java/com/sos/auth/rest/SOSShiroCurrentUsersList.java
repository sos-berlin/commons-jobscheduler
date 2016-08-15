package com.sos.auth.rest;

import java.util.HashMap;

public class SOSShiroCurrentUsersList {

    private HashMap<String, SOSShiroCurrentUser> currentUsers;

    public SOSShiroCurrentUsersList() {
        super();
        this.currentUsers = new HashMap<String, SOSShiroCurrentUser>();
    }

    public void addUser(SOSShiroCurrentUser user) {
        this.currentUsers.put(user.getAccessToken(), user);
    }

    public SOSShiroCurrentUser getUser(String accessToken) {
        return (SOSShiroCurrentUser) this.currentUsers.get(accessToken);
    }

    public void removeUser(String accessToken) {
        currentUsers.remove(accessToken);
    }
    
    public int size(){
        return currentUsers.size();
    }
   

}
