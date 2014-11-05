package com.sos.VirtualFileSystem.Filter;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

public class SOSBase64DecodeFilterTest extends SOSNullFilterBase <SOSBase64DecodeFilter> {

	@SuppressWarnings("unused")
	private final String conClassName = this.getClass().getSimpleName();
	@SuppressWarnings("unused")
	private static final String conSVNVersion = "$Id$";
	private final Logger logger = Logger.getLogger(this.getClass());
	
	
	public SOSBase64DecodeFilterTest() {
		super (new SOSBase64DecodeFilter());
	}

	@Test
	public void testWriteByteArray() {
		String strT = "SGFsbG8sIFdlbHQh";
		String strT2 = "Hallo, Welt!";
		objF.write(strT.getBytes());
		
		bteBuffer = objF.read();
		String strX = new String(bteBuffer);
		logger.debug(strX);
		Assert.assertEquals(strT2, strX);
	}

}
