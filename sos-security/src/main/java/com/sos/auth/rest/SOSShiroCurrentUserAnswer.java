package com.sos.auth.rest;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

    @XmlRootElement(name = "sosshiro_current_user")
    public class SOSShiroCurrentUserAnswer {
    private String user;
    private String role;
    private String permission;
    private boolean isPermittet;
    private boolean hasRole;
    private boolean isAuthenticated;
    
    public SOSShiroCurrentUserAnswer() {
    }
    
    public SOSShiroCurrentUserAnswer(String user) {
    this.user = user;
    }
     
    @XmlAttribute
    public void setRole(String role) {
    this.role = role;
    }
     
    public String getRole() {
    return this.role;
    }

    @XmlAttribute
    public void setPermission(String permission) {
    this.permission = permission;
    }
     
    public String getPermission() {
    return this.permission;
    }
     
    @XmlAttribute
    public void setUser(String user) {
    this.user = user;
    }
     
    public String getUser() {
    return this.user;
    }
    
    @XmlElement
    public void setIsPermitted(boolean isPermitted) {
    this.isPermittet = isPermitted;
    }
     
    public boolean getIsPermitted() {
    return this.isPermittet;
    }
     
    public boolean isPermitted() {
    return getIsPermitted();
    }
     
     
    @XmlElement
    public void setHasRole(boolean hasRole) {
    this.hasRole = hasRole;
    }
     
    public boolean getHasRole() {
    return this.hasRole;
    }
    
    public boolean hasRole() {
    return getHasRole();
    }
     
    @XmlElement
    public void setIsAuthenticated(boolean isAuthenticated) {
    this.isAuthenticated = isAuthenticated;
    }
     
    public boolean getIsAuthenticated() {
    return this.isAuthenticated;
    }

    public boolean isAuthenticated() {
    return getIsAuthenticated();
    }
    
    @Override
    public String toString() {
    return new StringBuffer(" User: ").append(this.user)
    .append(" Role: ").append(this.role).append(this.hasRole)
    .append(" Permission : ").append(this.permission).append(this.isPermittet).toString();
    }
    }