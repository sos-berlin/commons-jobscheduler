package com.sos.auth.rest;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.joc.exceptions.JocException;

public class TestSOSServicePermissionShiro {

    private static final String SHIRO_MAPPED_ROLE = "application_manager";


    private String accessToken;
    
    @Before
    public void setUp() throws Exception {
        accessToken = GlobalsTest.getAccessToken();
    }


    @Test
    public void testIsAuthenticated() throws JocException, SOSHibernateException {
        GlobalsTest.logout();
        assertEquals("testCurrentUserAnswer is authenticated", true, GlobalsTest.sosShiroCurrentUserAnswer.getIsAuthenticated());
    }

    @Test
    public void testHasRole() throws JocException, SOSHibernateException {
        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = GlobalsTest.sosServicePermissionShiro.hasRole("","",accessToken, "", "", SHIRO_MAPPED_ROLE);
        assertEquals("testCurrentUserAnswer is authenticated", true, sosShiroCurrentUserAnswer.getIsAuthenticated());
        assertEquals("testCurrentUserAnswer is has role " + SHIRO_MAPPED_ROLE, true, sosShiroCurrentUserAnswer.hasRole());
    }

    @Test
    public void testIsPermitted() throws JocException, SOSHibernateException {
        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = GlobalsTest.sosServicePermissionShiro.isPermitted("","",accessToken, "", "", "sos:products:joc_cockpit:jobscheduler_master:pause");
//        assertEquals("testCurrentUserAnswer is permitted  " + SHIRO_PERMISSION, true, sosShiroCurrentUserAnswer.isPermitted());
        assertEquals("testCurrentUserAnswer is authenticated", true, sosShiroCurrentUserAnswer.getIsAuthenticated());
        sosShiroCurrentUserAnswer =
                GlobalsTest.sosServicePermissionShiro.isPermitted("","",accessToken, "", "", "sos:products:joc_cockpit:jobscheduler_master:pause");
        assertEquals("testCurrentUserAnswer is permitted  sos:products:joc_cockpit:jobscheduler_master:pause", true, sosShiroCurrentUserAnswer.isPermitted());
        sosShiroCurrentUserAnswer =
                GlobalsTest.sosServicePermissionShiro.isPermitted("","",accessToken, "", "", "sos:products:joc_cockpit:jobscheduler_master:restart");
        assertEquals("testCurrentUserAnswer is permitted  sos:products:joc_cockpit:jobscheduler_master:restart", true, sosShiroCurrentUserAnswer.isPermitted());
        sosShiroCurrentUserAnswer =
                GlobalsTest.sosServicePermissionShiro.isPermitted("","",accessToken, "", "", "sos:products:joc_cockpit:jobscheduler_master:continue");
        assertEquals("testCurrentUserAnswer is permitted  sos:products:joc_cockpit:jobscheduler_master:continue", true, sosShiroCurrentUserAnswer.isPermitted());
        sosShiroCurrentUserAnswer =
                GlobalsTest.sosServicePermissionShiro.isPermitted("","",accessToken, "", "", "sos:products:joc_cockpit:job_chain:view:status");
        assertEquals("testCurrentUserAnswer is permitted  sos:products:joc_cockpit:job_chain:view:status", true, sosShiroCurrentUserAnswer.isPermitted());
        sosShiroCurrentUserAnswer =
                GlobalsTest.sosServicePermissionShiro.isPermitted("","",accessToken, "", "", "sos:products:joc_cockpit:job_chain:view:history");
        assertEquals("testCurrentUserAnswer is permitted  sos:products:joc_cockpit:job_chain:view:history", true, sosShiroCurrentUserAnswer.isPermitted());
    }

   


    

}