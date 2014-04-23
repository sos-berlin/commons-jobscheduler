package com.sos.auth.rest;

import static org.junit.Assert.*;

import javax.ws.rs.QueryParam;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.auth.rest.permission.model.SOSPermissionShiro;

public class TestSOSServicePermissionShiro {

    private static final String SHIRO_PERMISSION = "jobscheduler:jid:execute";
    private static final String SHIRO_MAPPED_ROLE = "admin";
    private static final String LDAP_PASSWORD = "sos01";
    private static final String LDAP_USER = "SOS01";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    //Test predefined permissions and roles as an JAXB Object
    @Test
    public void testGetPermissions() {
        SOSServicePermissionShiro sosServicePermissionShiro = new SOSServicePermissionShiro();
        SOSPermissionShiro sosPermissionShiro = sosServicePermissionShiro.getPermissions(LDAP_USER,LDAP_PASSWORD);
        String permissisonsJid = sosPermissionShiro.getSOSPermissions().getSOSPermissionJid().getSOSPermission().get(0);
        String permissisonsJoe = sosPermissionShiro.getSOSPermissions().getSOSPermissionJid().getSOSPermissionJoe().getSOSPermission().get(0);
        String permissisonsJoc = sosPermissionShiro.getSOSPermissions().getSOSPermissionJid().getSOSPermissionJoc().getSOSPermission().get(0);
        String permissisonsDashboard = sosPermissionShiro.getSOSPermissions().getSOSPermissionJid().getSOSPermissionDashboard().getSOSPermission().get(0);
        
        
        assertEquals("testClient",SHIRO_PERMISSION,permissisonsJid);        
        assertEquals("testClient","jobscheduler:joe:execute",permissisonsJoe);        
        assertEquals("testClient","jobscheduler:joc:execute",permissisonsJoc);        
        assertEquals("testClient","jobscheduler:jid:dashboard:start_job",permissisonsDashboard);        
    }

    
    //Test whether user is authenticated. Return is a user object.
    @Test
    public void testIsAuthenticated() {
        SOSServicePermissionShiro sosServicePermissionShiro = new SOSServicePermissionShiro();
        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = sosServicePermissionShiro.isAuthenticated(LDAP_USER,LDAP_PASSWORD);
        assertEquals("testCurrentUserAnswer is authenticated",true,sosShiroCurrentUserAnswer.getIsAuthenticated());
    }

    //Test whether user has role Return is a user object.
    @Test
    public void testHasRole() {
        SOSServicePermissionShiro sosServicePermissionShiro = new SOSServicePermissionShiro();
        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = sosServicePermissionShiro.hasRole(LDAP_USER,LDAP_PASSWORD,SHIRO_MAPPED_ROLE);
        assertEquals("testCurrentUserAnswer is authenticated",true,sosShiroCurrentUserAnswer.getIsAuthenticated());
        assertEquals("testCurrentUserAnswer is has role " + SHIRO_MAPPED_ROLE,true, sosShiroCurrentUserAnswer.hasRole());
    }

    //Test whether user is permitted. Return is a user object.
    @Test
    public void testIsPermitted() {
        SOSServicePermissionShiro sosServicePermissionShiro = new SOSServicePermissionShiro();
        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = sosServicePermissionShiro.isPermitted(LDAP_USER,LDAP_PASSWORD,SHIRO_PERMISSION);
        assertEquals("testCurrentUserAnswer is authenticated",true,sosShiroCurrentUserAnswer.getIsAuthenticated());
        assertEquals("testCurrentUserAnswer is permitted  " + SHIRO_PERMISSION,true, sosShiroCurrentUserAnswer.isPermitted());
    }

}
