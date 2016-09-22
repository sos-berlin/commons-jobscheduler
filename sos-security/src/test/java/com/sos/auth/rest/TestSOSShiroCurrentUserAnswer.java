package com.sos.auth.rest;

import static org.junit.Assert.assertEquals;
import java.net.MalformedURLException;
import org.junit.Test;


/** @author Uwe Risse */
public class TestSOSShiroCurrentUserAnswer {

    private SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer;

    @Test
    public void testSOSShiroCurrentUserAnswer() throws MalformedURLException {
        sosShiroCurrentUserAnswer = new SOSShiroCurrentUserAnswer();
        sosShiroCurrentUserAnswer.setHasRole(true);
        sosShiroCurrentUserAnswer.setIsAuthenticated(true);
        sosShiroCurrentUserAnswer.setIsPermitted(true);
        sosShiroCurrentUserAnswer.setPermission("permission");
        sosShiroCurrentUserAnswer.setRole("role");
        sosShiroCurrentUserAnswer.setUser("user");
        assertEquals("testSOSShiroCurrentUserAnswer getHasRole", true, sosShiroCurrentUserAnswer.getHasRole());
        assertEquals("testSOSShiroCurrentUserAnswer hasRole", true, sosShiroCurrentUserAnswer.hasRole());
        assertEquals("testSOSShiroCurrentUserAnswer getIsPermitted", true, sosShiroCurrentUserAnswer.getIsPermitted());
        assertEquals("testSOSShiroCurrentUserAnswer isPermitted", true, sosShiroCurrentUserAnswer.isPermitted());
        assertEquals("testSOSShiroCurrentUserAnswer getIsAuthenticated", true, sosShiroCurrentUserAnswer.getIsAuthenticated());
        assertEquals("testSOSShiroCurrentUserAnswer isAuthenticated", true, sosShiroCurrentUserAnswer.isAuthenticated());
        assertEquals("testSOSShiroCurrentUserAnswer getRole", "role", sosShiroCurrentUserAnswer.getRole());
        assertEquals("testSOSShiroCurrentUserAnswer getUser", "user", sosShiroCurrentUserAnswer.getUser());
        assertEquals("testSOSShiroCurrentUserAnswer getPermission", "permission", sosShiroCurrentUserAnswer.getPermission());
    }

}