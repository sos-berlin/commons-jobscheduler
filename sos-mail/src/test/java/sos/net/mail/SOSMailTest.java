package sos.net.mail;

import org.junit.Test;

import sos.net.SOSMail;
import sos.net.mail.options.SOSSmtpMailOptions;

public class SOSMailTest {

    @Test
    public void testSendMail() throws RuntimeException, Exception {
        SOSSmtpMailOptions objO = new SOSSmtpMailOptions();
        objO.host.Value("smtp.sos");
        objO.port.value(25);
        objO.from.Value("JUnit-Test@sos-berlin.com");
        objO.body.Value("bodobodododo");
        objO.subject.Value("mail from JUnit-Test");
        objO.to.Value("scheduler_test@sos-berlin.com");
        objO.cc.Value("scheduler_test@sos-berlin.com;info@sos-berlin.com");
        objO.bcc.Value("scheduler_test@sos-berlin.com;scheduler_test@sos-berlin.com");
        SOSMail objMail = new SOSMail(objO.host.Value());
        objMail.sendMail(objO);
    }

}