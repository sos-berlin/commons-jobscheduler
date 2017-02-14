package com.sos.auth.rest.client;

import java.net.MalformedURLException;
import java.net.URL;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sos.auth.rest.SOSShiroCurrentUserAnswer;
import com.sos.auth.rest.SOSWebserviceAuthenticationRecord;
import com.sos.auth.rest.permission.model.SOSPermissionShiro;
import com.sos.jobscheduler.tools.webservices.globals.SOSCommandSecurityWebserviceAnswer;
 

public class SOSRestShiroClient {

    public SOSRestShiroClient() {
        super();
    }

    public SOSPermissionShiro getSOSPermissionShiro(URL resource) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(resource.toExternalForm());
      
        Response response = target.request(MediaType.APPLICATION_XML).get();
        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
        }
        SOSPermissionShiro shiro = response.readEntity(SOSPermissionShiro.class);
        shiro.setSecurityServerUrl(String.format("%s://%s:%s", resource.getProtocol(), resource.getHost(), resource.getPort()));
        return shiro;

    }

    private SOSCommandSecurityWebserviceAnswer getSOSCommandSecurityPlugin(URL resource) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(resource.toExternalForm());

        Response response = target.request(MediaType.APPLICATION_XML).get();
        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
        }
        SOSCommandSecurityWebserviceAnswer answer = response.readEntity(SOSCommandSecurityWebserviceAnswer.class);
        return answer;

    }

    public SOSShiroCurrentUserAnswer getSOSShiroCurrentUserAnswer(URL resource) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(resource.toExternalForm());

        Response response = target.request(MediaType.APPLICATION_JSON).get();
        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
        }
        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = response.readEntity(SOSShiroCurrentUserAnswer.class);
        return sosShiroCurrentUserAnswer;

    }

    public SOSPermissionShiro getPermissions(SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord) throws MalformedURLException {
        return getSOSPermissionShiro(new URL(String.format(sosWebserviceAuthenticationRecord.getResource(), sosWebserviceAuthenticationRecord.getUser(),
                sosWebserviceAuthenticationRecord.getPassword(), sosWebserviceAuthenticationRecord.getPermission(),
                sosWebserviceAuthenticationRecord.getAccessToken())));
    }

    public boolean isEnabled(URL url) throws Exception {
        try {
            SOSCommandSecurityWebserviceAnswer m = getSOSCommandSecurityPlugin(url);
            return m.getIsEnabled();
        } catch (Exception e) {
            throw new Exception(String.format("Could not connect to security server at %s --> %s", url, e.getMessage()));
        }
    }

    public SOSShiroCurrentUserAnswer getSOSShiroCurrentUserAnswer(SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord) {
        String url = String.format(sosWebserviceAuthenticationRecord.getResource(), sosWebserviceAuthenticationRecord.getUser(),
                sosWebserviceAuthenticationRecord.getPassword(), sosWebserviceAuthenticationRecord.getPermission(),
                sosWebserviceAuthenticationRecord.getAccessToken());
        try {
            return getSOSShiroCurrentUserAnswer(new URL(url));
        } catch (MalformedURLException e) {
            SOSShiroCurrentUserAnswer a = new SOSShiroCurrentUserAnswer();
            a.setAccessToken(String.format("could not establish a session: MalformedUrlException %s", url));
            return a;
        }
    }

    public static void main(String[] args) throws MalformedURLException {

        SOSRestShiroClient sosRestShiroClient = new SOSRestShiroClient();
        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = new SOSWebserviceAuthenticationRecord();
        sosWebserviceAuthenticationRecord.setUser("SOS01");
        sosWebserviceAuthenticationRecord.setPassword("sos01");
        sosWebserviceAuthenticationRecord.setResource("http://localhost:40040/jobscheduler/rest/sosPermission/permissions?user=%s&pwd=%s");

        SOSPermissionShiro shiro = sosRestShiroClient.getPermissions(sosWebserviceAuthenticationRecord);
        System.out.println("Output xml client .... \n");
        System.out.println("Roles " + shiro.getSOSPermissionRoles().getSOSPermissionRole());
        System.out.println("JOC " + shiro.getSOSPermissions().getSOSPermissionListJoc().getSOSPermission());
        System.out.println("Commands " + shiro.getSOSPermissions().getSOSPermissionListCommands().getSOSPermission());

        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = sosRestShiroClient.getSOSShiroCurrentUserAnswer(sosWebserviceAuthenticationRecord);
        System.out.println("SOS01:" + sosShiroCurrentUserAnswer.getIsAuthenticated());

        sosShiroCurrentUserAnswer = sosRestShiroClient.getSOSShiroCurrentUserAnswer(sosWebserviceAuthenticationRecord);
        System.out.println("SOS01:" + sosShiroCurrentUserAnswer.getIsAuthenticated());
        System.out.println("SOS01:" + sosShiroCurrentUserAnswer.getHasRole());

        sosShiroCurrentUserAnswer = sosRestShiroClient.getSOSShiroCurrentUserAnswer(sosWebserviceAuthenticationRecord);
        System.out.println("SOS01:" + sosShiroCurrentUserAnswer.getIsAuthenticated());
        System.out.println("SOS01:" + sosShiroCurrentUserAnswer.getIsPermitted());

    }
}