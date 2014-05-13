package sos.net.mail;


import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import sos.net.SOSMailReceiver;

public class SOSMailReceiverTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void connect() throws Exception {
		
		String host = "smtp.sos";
	    String user = "oh";
	    String password = "ahxei2Eixeir";
	    String protocol = "IMAP";
	    String port = "143";
	    
	    connect(host,port,user,password,protocol);
	}
	
	public void connect(final String host, final String port, final String user, final String password, final String protocol) throws Exception {
		
		SOSMailReceiver receiver = new SOSMailReceiver(host, port, user, password);
		receiver.getSession().setDebug(true);
		receiver.connect(protocol);
		receiver.disconnect();
	}

}
