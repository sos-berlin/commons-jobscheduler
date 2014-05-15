package com.sos.VirtualFileSystem.Filter;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SOSNullFilterTest<T> {

	@SuppressWarnings("unused")
	private final String		conClassName	= this.getClass().getSimpleName();
	@SuppressWarnings("unused")
	private static final String	conSVNVersion	= "$Id$";
	private final Logger		logger			= Logger.getLogger(this.getClass());

	protected SOSNullFilter		objF			= null;
	protected byte[]			bteBuffer		= null;

	public SOSNullFilterTest(final T pobjT) {
		objF = (SOSNullFilter) pobjT;
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testOptions() {
	}

	@Test
	public void testOptionsSOSFTPOptions() {
	}

	@Test
	public void testWriteByteArrayIntInt() {
	}

	@Test
	public void testWriteByteArray() {
		String strT = "Hallo, Welt!";
		objF.write(strT.getBytes());

		bteBuffer = objF.read();
		String strX = new String(bteBuffer);
		logger.debug(strX);
		Assert.assertEquals(strT, strX);
	}

	@Test
	public void testReadByteArray() {
	}

	@Test
	public void testReadByteArrayIntInt() {
	}

	@Test
	public void testReadBufferByteArray() {
	}

	@Test
	public void testReadBufferByteArrayIntInt() {
	}

	@Test
	public void testClose() {
	}

}
