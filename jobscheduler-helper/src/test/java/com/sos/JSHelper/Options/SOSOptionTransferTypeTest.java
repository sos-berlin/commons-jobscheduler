/**
 * 
 */
package com.sos.JSHelper.Options;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author KB
 *
 */
public class SOSOptionTransferTypeTest {
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link com.sos.JSHelper.Options.SOSOptionTransferType#Value(com.sos.JSHelper.Options.SOSOptionTransferType.enuTransferTypes)}.
	 */
//	@Test
	public void testValueEnuTransferTypes() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.sos.JSHelper.Options.SOSOptionTransferType#isSFtp()}.
	 */
	@Test
	public void testIsSFtp() {
		SOSOptionTransferType objTT = new SOSOptionTransferType("sftp");
		assertTrue("is FTPs", objTT.isSFtp());
	}

	/**
	 * Test method for {@link com.sos.JSHelper.Options.SOSOptionTransferType#isFtpS()}.
	 */
	@Test
	public void testIsFtpS() {
		SOSOptionTransferType objTT = new SOSOptionTransferType("ftps");
		assertTrue("is FTPs", objTT.isFtpS());
		objTT.Value("ftps");
		assertEquals("ftps is expected", "ftps", objTT.Value());
	}

	/**
	 * Test method for {@link com.sos.JSHelper.Options.SOSOptionTransferType#needPortNumber()}.
	 */
	@Test
	public void testNeedPortNumber() {
		SOSOptionTransferType objTT = new SOSOptionTransferType("ftps");
		assertTrue("port number must be mandatory for ftps", objTT.needPortNumber());
		objTT.Value("ftps");
		assertTrue("port number must be mandatory for ftps", objTT.needPortNumber());
	}

	@Test  (expected=com.sos.JSHelper.Exceptions.JobSchedulerException.class)
	public void testCheckMandatory() {
		try {
			SOSOptionTransferType objTT = new SOSOptionTransferType("ftps");
			objTT.isMandatory(true);
			objTT.CheckMandatory();
			
			objTT.Value("xxxx");
			objTT.CheckMandatory();
		}
		catch (Exception e) {
			
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Test method for {@link com.sos.JSHelper.Options.SOSOptionTransferType#getEnum()}.
	 */
	@Test
	public void testIsTypeValid() {
		SOSOptionTransferType objTT = new SOSOptionTransferType("ftps");
		assertTrue("Type is valid", objTT.validate());

		objTT = new SOSOptionTransferType("sftps");
		assertFalse("Type is valid", objTT.validate());

		objTT = new SOSOptionTransferType("sfpt");
		assertFalse("Type is valid", objTT.validate());

	}
}
