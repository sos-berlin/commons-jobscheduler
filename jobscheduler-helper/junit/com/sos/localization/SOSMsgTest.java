package com.sos.localization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Locale;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class SOSMsgTest {
	private Messages		Messages		= null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		Messages = new Messages("com/sos/localization/messages", Locale.getDefault());
	}

	@After
	public void tearDown() throws Exception {
	}

//	@Test
	public void testSOSMsg() {
		fail("Not yet implemented");
	}

//	@Test
	public void testLabel() {
		fail("Not yet implemented");
	}

//	@Test
	public void testTooltip() {
		fail("Not yet implemented");
	}

//	@Test
	public void testGetF1() {
		fail("Not yet implemented");
	}

//	@Test
	public void testGet() {
		fail("Not yet implemented");
	}

//	@Test
	public void testGetException() {
		fail("Not yet implemented");
	}

//	@Test
	public void testGetFullMessage() {
		fail("Not yet implemented");
	}

//	@Test
	public void testGetObjectArray() {
		fail("Not yet implemented");
	}

	@Test
	public void testParams() {
	     SOSMsg JOE_M_JobAssistent_Execute  = new SOSMsg("JOE_test");
	     JOE_M_JobAssistent_Execute.Messages = Messages;
	     String strM = JOE_M_JobAssistent_Execute.params("irgendwas");
		assertEquals("testParams", "JOE_test irgendwas", strM);
		
	     JOE_M_JobAssistent_Execute  = new SOSMsg("JOE_G_JobAssistent_Execute");
	     JOE_M_JobAssistent_Execute.Messages = Messages;
	     strM =JOE_M_JobAssistent_Execute.params("irgendwas");
		assertEquals("testParams", "Execute irgendwas", strM);
	}

//	@Test
	public void testParamsNoKey() {
		fail("Not yet implemented");
	}

//	@Test
	public void testSetMessageResource() {
		fail("Not yet implemented");
	}

}
