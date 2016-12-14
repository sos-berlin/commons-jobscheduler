package com.sos.auth.rest.client;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;

import org.junit.Ignore;
import org.junit.Test;

import com.sos.auth.SOSJaxbSubject;
import com.sos.auth.rest.SOSWebserviceAuthenticationRecord;
import com.sos.auth.rest.permission.model.SOSPermissionShiro;

/** @author Uwe Risse */
public class TestSOSRestShiroClient {

    private static final String LDAP_PASSWORD = "sos01";
    private static final String LDAP_USER = "SOS01";
    private static final String JETTY_URL = "http://8of9:40002/jobscheduler/rest/sosPermission/permissions?user=%s&pwd=%s";

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testClientJaxb() throws MalformedURLException {
        SOSRestShiroClient sosRestShiroClient = new SOSRestShiroClient();
        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = new SOSWebserviceAuthenticationRecord();
        sosWebserviceAuthenticationRecord.setUser(LDAP_USER);
        sosWebserviceAuthenticationRecord.setPassword(LDAP_PASSWORD);
        sosWebserviceAuthenticationRecord.setResource(JETTY_URL);
        SOSPermissionShiro shiro = sosRestShiroClient.getPermissions(sosWebserviceAuthenticationRecord);
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testClientJaxbSubject() throws MalformedURLException {
        SOSRestShiroClient sosRestShiroClient = new SOSRestShiroClient();
        SOSWebserviceAuthenticationRecord sosWebserviceAuthenticationRecord = new SOSWebserviceAuthenticationRecord();
        sosWebserviceAuthenticationRecord.setUser(LDAP_USER);
        sosWebserviceAuthenticationRecord.setPassword(LDAP_PASSWORD);
        sosWebserviceAuthenticationRecord.setResource(JETTY_URL);
        SOSPermissionShiro sosPermissionShiro = sosRestShiroClient.getPermissions(sosWebserviceAuthenticationRecord);
        SOSJaxbSubject currentUser = new SOSJaxbSubject(sosPermissionShiro);
        assertEquals("testClientWithJaxb is authenticated", true, currentUser.isAuthenticated());
        assertEquals("testClientWithJaxb has role joe", true, currentUser.hasRole("joe"));
        assertEquals("testClientWithJaxb is permitted sos:products:jid:execute", true, currentUser.isPermitted("sos:products:jid:execute"));
    }

}