package com.sos.VirtualFileSystem.Filter;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

public class SOSBase64EncodeFilterTest extends SOSNullFilterTest <SOSBase64EncodeFilter> {

	@SuppressWarnings("unused")
	private final String conClassName = this.getClass().getSimpleName();
	@SuppressWarnings("unused")
	private static final String conSVNVersion = "$Id$";
	private final Logger logger = Logger.getLogger(this.getClass());
	
	
	public SOSBase64EncodeFilterTest() {
		super (new SOSBase64EncodeFilter());
	}

	@Override
	@Test
	public void testWriteByteArray() {
		String strT = "Hallo, Welt!";
		String strT2 = "SGFsbG8sIFdlbHQh";
		objF.write(strT.getBytes());
		
		bteBuffer = objF.read();
		String strX = new String(bteBuffer);
		logger.debug(strX);
		Assert.assertEquals(strT2, strX);
	}

}
