package com.sos.localization;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Before;

public class SOSMsgTest {

    private Messages Messages = null;

    @Before
    public void setUp() throws Exception {
        Messages = new Messages("com/sos/localization/messages", Locale.getDefault());
    }

    public void testParams() {
        SOSMsg JOE_M_JobAssistent_Execute = new SOSMsg("JOE_test");
        JOE_M_JobAssistent_Execute.Messages = Messages;
        String strM = JOE_M_JobAssistent_Execute.params("irgendwas");
        assertEquals("testParams", "JOE_test irgendwas", strM);
        JOE_M_JobAssistent_Execute = new SOSMsg("JOE_G_JobAssistent_Execute");
        JOE_M_JobAssistent_Execute.Messages = Messages;
        strM = JOE_M_JobAssistent_Execute.params("irgendwas");
        assertEquals("testParams", "Execute irgendwas", strM);
    }

}