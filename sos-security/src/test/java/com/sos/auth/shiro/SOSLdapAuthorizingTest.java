package com.sos.auth.shiro;

import static org.junit.Assert.*;
import org.junit.Test;

public class SOSLdapAuthorizingTest {

    @Test
    public void testSetRoles() throws Exception {
        //public SimpleAuthorizationInfo setRoles(SimpleAuthorizationInfo authorizationInfo_, PrincipalCollection principalCollection) {
        SOSLdapAuthorizingRealm sosLdapAuthorizingRealm = new SOSLdapAuthorizingRealm();
        sosLdapAuthorizingRealm.setSearchBase("dc=umich,dc=edu");
        sosLdapAuthorizingRealm.setUserSearchFilter("(&(objectClass=rfc822MailGroup) (member=uid=jnorton,ou=People,dc=umich,dc=edu))");
        sosLdapAuthorizingRealm.setGroupNameAttribute("cn");
        SOSLdapAuthorizing sosLdapAuthorizing = new SOSLdapAuthorizing();
        sosLdapAuthorizing.getRoleNamesForUserTest(sosLdapAuthorizingRealm, "00000000-0000-1000-7362-0800207F02E6");

    }
}
