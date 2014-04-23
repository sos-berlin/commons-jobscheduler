package com.sos.auth.rest.client;


import java.net.MalformedURLException;
import java.net.URL;

import javax.ws.rs.core.MediaType;


import com.sos.auth.rest.SOSShiroCurrentUserAnswer;
import com.sos.auth.rest.permission.model.SOSPermissionShiro;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

  
public class SOSRestShiroClient {
    
public SOSRestShiroClient() {
    super();
 }

private SOSPermissionShiro getSOSPermissionShiro(URL resource) {
    Client client = Client.create();
    WebResource webResource = client.resource(resource.toExternalForm());
    ClientResponse response = webResource.accept(MediaType.APPLICATION_XML).get(ClientResponse.class);  
    if (response.getStatus() != 200) {
         throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
    }   
    SOSPermissionShiro shiro = response.getEntity(SOSPermissionShiro.class); 
    return shiro;
     
}


private SOSShiroCurrentUserAnswer getSOSShiroCurrentUserAnswer(URL resource) {
    Client client = Client.create();
    WebResource webResource = client.resource(resource.toExternalForm());
    ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);  
    if (response.getStatus() != 200) {
         throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
    }   
    SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = response.getEntity(SOSShiroCurrentUserAnswer.class); 
    return sosShiroCurrentUserAnswer;
     
}

public SOSPermissionShiro getPermissions(String user, String password, String resource) throws MalformedURLException {
    return getSOSPermissionShiro(new URL(String.format(resource,user,password)));
}

public SOSShiroCurrentUserAnswer getSOSShiroCurrentUserAnswer(String user, String password,String param, String resource) throws MalformedURLException {
    return getSOSShiroCurrentUserAnswer(new URL(String.format(resource,user,password,param)));
} 

public static void main(String[] args) throws MalformedURLException {
    
    SOSRestShiroClient sosRestShiroClient = new SOSRestShiroClient();
    SOSPermissionShiro shiro = sosRestShiroClient.getPermissions("SOS01","sos01","http://localhost:40040/jobscheduler/rest/sosPermission/permissions?user=%s&pwd=%s");
    System.out.println("Output xml client .... \n");
    System.out.println("Roles "+shiro.getSOSPermissionRoles().getSOSPermissionRole());
    System.out.println("Joe "+shiro.getSOSPermissions().getSOSPermissionJid().getSOSPermissionJoe().getSOSPermission());
    System.out.println("Joc "+shiro.getSOSPermissions().getSOSPermissionJid().getSOSPermissionJoc().getSOSPermission());
    System.out.println("Jid "+shiro.getSOSPermissions().getSOSPermissionJid().getSOSPermission());
    System.out.println("Jobnet"+shiro.getSOSPermissions().getSOSPermissionJid().getSOSPermissionJobnet().getSOSPermission());
    System.out.println("Events "+shiro.getSOSPermissions().getSOSPermissionJid().getSOSPermissionEvents().getSOSPermission());
    System.out.println("Dashboard "+shiro.getSOSPermissions().getSOSPermissionJid().getSOSPermissionDashboard().getSOSPermission());
    System.out.println("Workingplan "+shiro.getSOSPermissions().getSOSPermissionJid().getSOSPermissionWorkingplan().getSOSPermission());
    
    SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = sosRestShiroClient.getSOSShiroCurrentUserAnswer("SOS01","sos01","","http://localhost:40040/jobscheduler/rest/sosPermission/authenticate?user=%s&pwd=%s");  
    System.out.println("SOS01:" + sosShiroCurrentUserAnswer.getIsAuthenticated());

    sosShiroCurrentUserAnswer = sosRestShiroClient.getSOSShiroCurrentUserAnswer("SOS01","sos01","admin","http://localhost:40040/jobscheduler/rest/sosPermission/role?user=%s&pwd=%s&role=%s");  
    System.out.println("SOS01:" + sosShiroCurrentUserAnswer.getIsAuthenticated());
    System.out.println("SOS01:" + sosShiroCurrentUserAnswer.getHasRole());
 
    sosShiroCurrentUserAnswer = sosRestShiroClient.getSOSShiroCurrentUserAnswer("SOS01","sos01","jobscheduler:jid:joctab:show","http://localhost:40040/jobscheduler/rest/sosPermission/permission?user=%s&pwd=%s&permission=%s");  
    System.out.println("SOS01:" + sosShiroCurrentUserAnswer.getIsAuthenticated());
    System.out.println("SOS01:" + sosShiroCurrentUserAnswer.getIsPermitted());

    
    
 
    
    }   
}