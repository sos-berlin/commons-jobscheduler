package com.sos.jobscheduler.tools.webservices;

public class SOSCommandSecurityWebserviceCurrentUser {

    private String resource;
    private String username;
    private String password;
    private String accessToken;

    public SOSCommandSecurityWebserviceCurrentUser() {
        super();

    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
