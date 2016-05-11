package sos.net.mail;

import org.junit.Ignore;
import org.junit.Test;

import sos.net.SOSMailReceiver;

public class SOSMailReceiverTest {

    @Test
    @Ignore
    public void connect() throws Exception {
        String host = "ADD_HOST_HERE";
        String user = "ADD_USER_HERE";
        String password = "ADD_PASSWORD_HERE";
        String protocol = "IMAP";
        String port = "ADD_PORT_HERE";
        connect(host, port, user, password, protocol);
    }

    public void connect(final String host, final String port, final String user, final String password, final String protocol) throws Exception {
        SOSMailReceiver receiver = new SOSMailReceiver(host, port, user, password);
        receiver.getSession().setDebug(true);
        receiver.connect(protocol);
        receiver.disconnect();
    }

}