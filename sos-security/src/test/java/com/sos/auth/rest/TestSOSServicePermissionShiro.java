package com.sos.auth.rest;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.sos.auth.rest.permission.model.SOSPermissionShiro;

public class TestSOSServicePermissionShiro {

    private static final String SHIRO_PERMISSION = "sos:products:jid:execute";
    private static final String SHIRO_MAPPED_ROLE = "jid";
    private static final String LDAP_PASSWORD = "admin";
    private static final String LDAP_USER = "admin";

    @Test
    public void testGetPermissions() {
        SOSServicePermissionShiro sosServicePermissionShiro = new SOSServicePermissionShiro();
        SOSPermissionShiro sosPermissionShiro = sosServicePermissionShiro.getPermissions("", LDAP_USER, LDAP_PASSWORD);
        String permissisonsJid = sosPermissionShiro.getSOSPermissions().getSOSPermissionJid().getSOSPermission().get(0);
        String permissisonsJoe = sosPermissionShiro.getSOSPermissions().getSOSPermissionJid().getSOSPermissionJoe().getSOSPermission().get(0);
        String permissisonsJoc = sosPermissionShiro.getSOSPermissions().getSOSPermissionJid().getSOSPermissionJoc().getSOSPermission().get(0);
        String permissisonsDashboard =
                sosPermissionShiro.getSOSPermissions().getSOSPermissionJid().getSOSPermissionDashboard().getSOSPermission().get(0);
        assertEquals("testClient", SHIRO_PERMISSION, permissisonsJid);
        assertEquals("testClient", "sos:products:joe:execute", permissisonsJoe);
        assertEquals("testClient", "sos:products:joc:execute", permissisonsJoc);
        assertEquals("testClient", "sos:products:jid:jobstart", permissisonsDashboard);
    }

    @Test
    public void testIsAuthenticated() {
        SOSServicePermissionShiro sosServicePermissionShiro = new SOSServicePermissionShiro();
        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = sosServicePermissionShiro.authenticate(LDAP_USER, LDAP_PASSWORD);
        assertEquals("testCurrentUserAnswer is authenticated", true, sosShiroCurrentUserAnswer.getIsAuthenticated());
    }

    @Test
    public void testHasRole() {
        SOSServicePermissionShiro sosServicePermissionShiro = new SOSServicePermissionShiro();
        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = sosServicePermissionShiro.authenticate(LDAP_USER, LDAP_PASSWORD);
        sosShiroCurrentUserAnswer =
                sosServicePermissionShiro.hasRole(sosShiroCurrentUserAnswer.getAccessToken(), LDAP_USER, LDAP_PASSWORD, SHIRO_MAPPED_ROLE);
        assertEquals("testCurrentUserAnswer is authenticated", true, sosShiroCurrentUserAnswer.getIsAuthenticated());
        assertEquals("testCurrentUserAnswer is has role " + SHIRO_MAPPED_ROLE, true, sosShiroCurrentUserAnswer.hasRole());
    }

    @Test
    public void testIsPermitted() {
        SOSServicePermissionShiro sosServicePermissionShiro = new SOSServicePermissionShiro();
        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = sosServicePermissionShiro.authenticate(LDAP_USER, LDAP_PASSWORD);
        sosShiroCurrentUserAnswer =
                sosServicePermissionShiro.isPermitted(sosShiroCurrentUserAnswer.getAccessToken(), LDAP_USER, LDAP_PASSWORD, SHIRO_PERMISSION);
        assertEquals("testCurrentUserAnswer is authenticated", true, sosShiroCurrentUserAnswer.getIsAuthenticated());
        assertEquals("testCurrentUserAnswer is permitted  " + SHIRO_PERMISSION, true, sosShiroCurrentUserAnswer.isPermitted());
    }

}