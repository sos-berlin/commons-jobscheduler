package com.sos.auth.rest;

import java.util.HashMap;

import org.apache.shiro.subject.Subject;

import com.sos.scheduler.db.SchedulerInstancesDBItem;

public class SOSShiroCurrentUser {

    private Subject currentSubject;
    private String username;
    private String password;
    private String accessToken;
    private HashMap <String, SchedulerInstancesDBItem> listOfSchedulerInstances;
  
  
    public SOSShiroCurrentUser(String username, String password) {
        super();
        this.listOfSchedulerInstances = new HashMap<String, SchedulerInstancesDBItem>();
        this.username = username;
        this.password = password;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
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
        } else {
            return false;
        }
    }

    public boolean isPermitted(String permission) {
        if (currentSubject != null) {
            return currentSubject.isPermitted(permission) && !currentSubject.isPermitted("-" + permission);
        } else {
            return false;
        }
    }

    public boolean isAuthenticated() {
        if (currentSubject != null) {
            return currentSubject.isAuthenticated();
        } else {
            return false;
        }
    }
    
    public SchedulerInstancesDBItem getSchedulerInstanceDBItem(String schedulerId){
        return listOfSchedulerInstances.get(schedulerId);
    }
    
    public void addSchedulerInstanceDBItem(String schedulerId, SchedulerInstancesDBItem schedulerInstancesDBItem){
        listOfSchedulerInstances.put(schedulerId, schedulerInstancesDBItem);
    }

}
