package com.sos.JSHelper.Options;

import org.junit.Test;

public class SOSOptionUrlTest {
	
	private SOSOptionUrl objU = new SOSOptionUrl(null, "url", "descr", "", "", false);
	
	@Test public void testValueString() {
		objU.Value("ftp://kb:kb@homer.sos/home/test/test.txt");
//		ISOSFtpOptions objSF = new SOSFtpOptions();
		
	}

	@Test public void testSOSOptionUrl() {
		objU = new SOSOptionUrl(null, "url", "descr", "", "", false);
	}

	@Test public void testGetOptions() {
//		fail("Not yet implemented");
	}
}
