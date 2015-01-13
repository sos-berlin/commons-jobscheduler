package com.sos.VirtualFileSystem.common;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.localization.SOSMsg;

public class SOSMsgVfsTest {
	
	private String				strExpectedMessage	= "";
	private String				strExpectedMessageFull	= "";
	
	@Before
	public void setUp() throws Exception {
		strExpectedMessage = "SOSVfs_W_140: Probleme während des Logout-Vorgangs.";
		strExpectedMessageFull = strExpectedMessage;
		strExpectedMessageFull += "\n\n" + "DESCRIPTION" + "\n";
		strExpectedMessageFull += "the logout process was not finished as expected. No detailed reason was reported by the server.";
		strExpectedMessageFull += "\n\n" + "REASON" + "\n";
		strExpectedMessageFull += "logout failed due to unknown error";
	}

	@Test
	@Ignore("Test set to Ignore for later examination")
	// Test ignores the locale of the system where the test runs, presumes the system has the locale set to german [SP]
	public void testGet() {
		SOSMsg.flgShowFullMessageText = false;
		SOSMsgVfs objT = new SOSMsgVfs("SOSVfs_W_136");
		String strT = objT.get().trim();
		strExpectedMessage = "SOSVfs_W_136: Problem beim Abbau der Verbindung.";
		assertEquals("Msg not as expected", strExpectedMessage, strT);
	}

	@Test
  @Ignore("Test set to Ignore for later examination")
	// Test ignores the locale of the system where the test runs, presumes the system has the locale set to german [SP]
	public void testGetWithParams() {
		SOSMsg.flgShowFullMessageText = false;
		SOSMsgVfs SOSVfs_D_133 = new SOSMsgVfs("SOSVfs_D_133");
		String strT = SOSVfs_D_133.params("kb").trim();
		assertEquals("Msg not as expected", "SOSVfs_D_133: Benutzer 'kb' eingeloggt.", strT);
	}

	@Test(expected = com.sos.JSHelper.Exceptions.JobSchedulerException.class)
	public void testGetException() {
		throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_177);
	}

	@Test
//  @Ignore("Test set to Ignore for later examination")
	public void testGetFull() {
		SOSMsg.flgShowFullMessageText = true;
		SOSMsgVfs objT = new SOSMsgVfs("SOSVfs_W_140");
		String strT = objT.getFullMessage();
		assertEquals("Msg not as expected", strExpectedMessageFull, strT);
	}

	@Test
//  @Ignore("Test set to Ignore for later examination")
	public void testGetFull2() {
		SOSMsg.flgShowFullMessageText = false;
		SOSMsgVfs objT = new SOSMsgVfs("SOSVfs_W_140");
		String str  = "";
		String strT = objT.getFullMessage();
		System.out.println(strT);
		str = strT;
		
		SOSMsg.flgShowFullMessageText = true;
		objT = new SOSMsgVfs("SOSVfs_W_140");
		strT = objT.getFullMessage();
		System.out.println(strT);
		str += strT;
		assertEquals("Msg not as expected", strExpectedMessage + strExpectedMessageFull, str);
	}

	@Test
//  @Ignore("Test set to Ignore for later examination")
	public void testGetFullRepeated() {
		SOSMsg.flgShowFullMessageText = true;
		SOSMsgVfs objT = new SOSMsgVfs("SOSVfs_W_140");
		String str  = "";
		String strT = objT.get();
		System.out.println(strT);
		str = strT;
		strT = objT.get();
		System.out.println(strT);
		str += strT;
		strT = objT.get();
		System.out.println(strT);
		str += strT;
		assertEquals("Msg not as expected", strExpectedMessageFull + strExpectedMessage + strExpectedMessage, str);
	}

}
