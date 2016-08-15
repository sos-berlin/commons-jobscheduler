package com.sos.auth.rest;

import java.util.HashMap;

import org.apache.shiro.subject.Subject;

import com.sos.auth.classes.JobSchedulerIdentifier;
import com.sos.auth.rest.permission.model.SOSPermissionJocCockpit;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.reporting.db.DBItemInventoryInstance;

public class SOSShiroCurrentUser {

    private Subject currentSubject;
    private String username;
    private String password;
    private String accessToken;
    private SOSPermissionJocCockpit sosPermissionJocCockpit;
    private HashMap<String, DBItemInventoryInstance> listOfSchedulerInstances;
    private SOSHibernateConnection sosHibernateConnection;

    public SOSShiroCurrentUser(String username, String password) {
        super();
        this.listOfSchedulerInstances = new HashMap<String, DBItemInventoryInstance>();
        this.username = username;
        this.password = password;
    }

    public SOSPermissionJocCockpit getSosPermissionJocCockpit() {
        return sosPermissionJocCockpit;
    }

    public void setSosPermissionJocCockpit(SOSPermissionJocCockpit sosPermissionJocCockpit) {
        this.sosPermissionJocCockpit = sosPermissionJocCockpit;
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

    public DBItemInventoryInstance getSchedulerInstanceDBItem(JobSchedulerIdentifier jobSchedulerIdentifier) {
        return listOfSchedulerInstances.get(jobSchedulerIdentifier.getId());
    }

    public void addSchedulerInstanceDBItem(JobSchedulerIdentifier jobSchedulerIdentifier, DBItemInventoryInstance schedulerInstancesDBItem) {
        listOfSchedulerInstances.put(jobSchedulerIdentifier.getId(), schedulerInstancesDBItem);
    }

    public SOSHibernateConnection getSosHibernateConnection() {
        return sosHibernateConnection;
    }

    public void setSosHibernateConnection(SOSHibernateConnection sosHibernateConnection) {
        this.sosHibernateConnection = sosHibernateConnection;
    }

}
