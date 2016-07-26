package com.sos.auth.rest;

import static org.junit.Assert.*;

 
import org.junit.Test;

import com.sos.auth.classes.SOSShiroProperties;
import com.sos.scheduler.db.SchedulerInstancesDBItem;
import com.sos.scheduler.db.SchedulerInstancesDBLayer;

public class SOSShiroCurrentUserTest {
    private static final String LDAP_PASSWORD = "sos01";
    private static final String LDAP_USER = "SOS01";

    @Test

    public void getJobSchedulerInstance() {
        SOSServicePermissionShiro sosServicePermissionShiro = new SOSServicePermissionShiro();
        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = (SOSShiroCurrentUserAnswer) sosServicePermissionShiro.loginGet("", LDAP_USER, LDAP_PASSWORD).getEntity();

        SOSShiroCurrentUser sosShiroCurrentUser = SOSServicePermissionShiro.currentUsersList.getUser(sosShiroCurrentUserAnswer.getAccessToken());

        SOSShiroProperties sosShiroProperties = new SOSShiroProperties();
        SchedulerInstancesDBLayer schedulerInstancesDBLayer = new SchedulerInstancesDBLayer(sosShiroProperties.getProperty("hibernate_configuration_file"));
        sosShiroCurrentUser.addSchedulerInstanceDBItem ("scheduler_current",schedulerInstancesDBLayer.getFirstInstanceById("scheduler_current"));
        SchedulerInstancesDBItem schedulerInstancesDBItem = sosShiroCurrentUser.getSchedulerInstanceDBItem("scheduler_current");
        
        assertEquals("getJobSchedulerInstance", "http://localhost:4000", schedulerInstancesDBItem.getUrl());

    }

}
