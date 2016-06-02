package sos.net.mail;

import org.junit.Test;

import sos.net.SOSMail;
import sos.net.mail.options.SOSSmtpMailOptions;

public class SOSMailTest {

    @Test
    public void testSendMail() throws RuntimeException, Exception {
        SOSSmtpMailOptions objO = new SOSSmtpMailOptions();
        objO.host.setValue("smtp.sos");
        objO.port.value(25);
        objO.from.setValue("JUnit-Test@sos-berlin.com");
        objO.body.setValue("bodobodododo");
        objO.subject.setValue("mail from JUnit-Test");
        objO.to.setValue("scheduler_test@sos-berlin.com");
        objO.cc.setValue("scheduler_test@sos-berlin.com;info@sos-berlin.com");
        objO.bcc.setValue("scheduler_test@sos-berlin.com;scheduler_test@sos-berlin.com");
        SOSMail objMail = new SOSMail(objO.host.getValue());
        objMail.sendMail(objO);
    }

}