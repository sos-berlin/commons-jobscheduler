package com.sos.VirtualFileSystem.Filter;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

public class SOSDos2UnixFilterTest extends SOSNullFilterTest <SOSDos2UnixFilter> {

	@SuppressWarnings("unused")
	private final String conClassName = this.getClass().getSimpleName();
	@SuppressWarnings("unused")
	private static final String conSVNVersion = "$Id$";
	private final Logger logger = Logger.getLogger(this.getClass());


	public SOSDos2UnixFilterTest() {
		super (new SOSDos2UnixFilter());
	}

	@Override
	@Test
	public void testWriteByteArray() {
		String strT = "abcdef\r\nabcdef";
		String strT2 = "abcdef\nabcdef";
		objF.write(strT.getBytes());

		bteBuffer = objF.read();
		String strX = new String(bteBuffer);
		logger.debug(strX);
		Assert.assertEquals(strT2, strX);
	}

}
