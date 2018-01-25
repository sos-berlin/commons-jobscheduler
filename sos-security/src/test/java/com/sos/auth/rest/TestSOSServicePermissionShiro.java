package com.sos.auth.rest;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.sos.auth.rest.permission.model.SOSPermissionShiro;
import com.sos.joc.Globals;
import com.sos.joc.exceptions.JocException;

public class TestSOSServicePermissionShiro {

    private static final String SHIRO_MAPPED_ROLE = "application_manager";
    private static final String LDAP_PASSWORD = "secret";
    private static final String LDAP_USER = "root";

    @Test
    public void testGetPermissions() {
        SOSServicePermissionShiro sosServicePermissionShiro = new SOSServicePermissionShiro();
        SOSPermissionShiro sosPermissionShiroAll = sosServicePermissionShiro.getPermissions("","","",false, LDAP_USER, LDAP_PASSWORD);
        Globals.sosShiroProperties = null;
        SOSPermissionShiro sosPermissionShiro = sosServicePermissionShiro.getPermissions("","","",true, LDAP_USER, LDAP_PASSWORD);
        for (int i=0;i< sosPermissionShiro.getSOSPermissions().getSOSPermissionListJoc().getSOSPermission().size();i++){
            System.out.println(sosPermissionShiro.getSOSPermissions().getSOSPermissionListJoc().getSOSPermission().get(i));
        }
        sosPermissionShiroAll.getSOSPermissions().getSOSPermissionListCommands().getSOSPermission().get(0);
        String permissisonsCommand = sosPermissionShiroAll.getSOSPermissions().getSOSPermissionListCommands().getSOSPermission().get(0);
        String permissisonsJoc = sosPermissionShiroAll.getSOSPermissions().getSOSPermissionListJoc().getSOSPermission().get(0);

    }

    @Test
    public void testIsAuthenticated() throws JocException {
        SOSServicePermissionShiro sosServicePermissionShiro = new SOSServicePermissionShiro();
        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = (SOSShiroCurrentUserAnswer)sosServicePermissionShiro.loginPost("",LDAP_USER, LDAP_PASSWORD).getEntity();
        sosServicePermissionShiro.logoutPost(sosShiroCurrentUserAnswer.getAccessToken(),"");
        assertEquals("testCurrentUserAnswer is authenticated", true, sosShiroCurrentUserAnswer.getIsAuthenticated());
    }

    @Test
    public void testHasRole() throws JocException {
        SOSServicePermissionShiro sosServicePermissionShiro = new SOSServicePermissionShiro();
        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = (SOSShiroCurrentUserAnswer)sosServicePermissionShiro.loginPost("",LDAP_USER, LDAP_PASSWORD).getEntity();
        sosShiroCurrentUserAnswer = sosServicePermissionShiro.hasRole("","", sosShiroCurrentUserAnswer.getAccessToken(), LDAP_USER, LDAP_PASSWORD, SHIRO_MAPPED_ROLE);
        assertEquals("testCurrentUserAnswer is authenticated", true, sosShiroCurrentUserAnswer.getIsAuthenticated());
        assertEquals("testCurrentUserAnswer is has role " + SHIRO_MAPPED_ROLE, true, sosShiroCurrentUserAnswer.hasRole());
    }

    @Test
    public void testIsPermitted() throws JocException {
        SOSServicePermissionShiro sosServicePermissionShiro = new SOSServicePermissionShiro();
        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = (SOSShiroCurrentUserAnswer)sosServicePermissionShiro.loginPost("",LDAP_USER, LDAP_PASSWORD).getEntity();
        sosShiroCurrentUserAnswer =
                sosServicePermissionShiro.isPermitted("","",sosShiroCurrentUserAnswer.getAccessToken(), LDAP_USER, LDAP_PASSWORD, "sos:products:joc_cockpit:jobscheduler_master:pause");
//        assertEquals("testCurrentUserAnswer is permitted  " + SHIRO_PERMISSION, true, sosShiroCurrentUserAnswer.isPermitted());
        assertEquals("testCurrentUserAnswer is authenticated", true, sosShiroCurrentUserAnswer.getIsAuthenticated());
        sosShiroCurrentUserAnswer =
                sosServicePermissionShiro.isPermitted("","",sosShiroCurrentUserAnswer.getAccessToken(), LDAP_USER, LDAP_PASSWORD, "sos:products:joc_cockpit:jobscheduler_master:pause");
        assertEquals("testCurrentUserAnswer is permitted  sos:products:joc_cockpit:jobscheduler_master:pause", false, sosShiroCurrentUserAnswer.isPermitted());
        sosShiroCurrentUserAnswer =
                sosServicePermissionShiro.isPermitted("","",sosShiroCurrentUserAnswer.getAccessToken(), LDAP_USER, LDAP_PASSWORD, "sos:products:joc_cockpit:jobscheduler_master:restart");
        assertEquals("testCurrentUserAnswer is permitted  sos:products:joc_cockpit:jobscheduler_master:restart", true, sosShiroCurrentUserAnswer.isPermitted());
        sosShiroCurrentUserAnswer =
                sosServicePermissionShiro.isPermitted("","",sosShiroCurrentUserAnswer.getAccessToken(), LDAP_USER, LDAP_PASSWORD, "sos:products:joc_cockpit:jobscheduler_master:continue");
        assertEquals("testCurrentUserAnswer is permitted  sos:products:joc_cockpit:jobscheduler_master:continue", false, sosShiroCurrentUserAnswer.isPermitted());
        sosShiroCurrentUserAnswer =
                sosServicePermissionShiro.isPermitted("","",sosShiroCurrentUserAnswer.getAccessToken(), LDAP_USER, LDAP_PASSWORD, "sos:products:joc_cockpit:job_chain:view:status");
        assertEquals("testCurrentUserAnswer is permitted  sos:products:joc_cockpit:job_chain:view:status", true, sosShiroCurrentUserAnswer.isPermitted());
        sosShiroCurrentUserAnswer =
                sosServicePermissionShiro.isPermitted("","",sosShiroCurrentUserAnswer.getAccessToken(), LDAP_USER, LDAP_PASSWORD, "sos:products:joc_cockpit:job_chain:view:history");
        assertEquals("testCurrentUserAnswer is permitted  sos:products:joc_cockpit:job_chain:view:history", false, sosShiroCurrentUserAnswer.isPermitted());
    }

   


    

}