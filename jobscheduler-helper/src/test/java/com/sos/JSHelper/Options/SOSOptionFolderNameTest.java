package com.sos.JSHelper.Options;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SOSOptionFolderNameTest {

	@SuppressWarnings("unused")
	private final String		conClassName	= "SOSOptionFolderNameTest";
	@SuppressWarnings("unused")
	private static final String	conSVNVersion	= "$Id$";
	private SOSOptionFolderName	objCS			= null;

	public SOSOptionFolderNameTest() {
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		objCS = new SOSOptionFolderName(null, "test", "Description", null, null, false);

	}

	@After
	public void tearDown() throws Exception {
	}

//	@Test
//	public void testValueString() throws Exception {
//		JSOptionsClass objO = new JSOptionsClass();
//		String strF = objO.TempDir() + "testSOSOptionFolderName.txt";
//		JSFile objF = new JSFile(strF);
//		objF.deleteOnExit();
//		String strT = "Select * from table;";
//		objF.Write(strT);
//		objF.close();
//
//		objCS.Value(strF); // the filename is the value
//		System.out.println(objCS.Value());
//		assertEquals("select", strT, objCS.Value());
//	}
//
	@Test
	public void testRelativeFolderNames() {
		objCS.Value(".");
		assertEquals("relative .", ".", objCS.Value());
	}

	@Test
	public void testisDotFolder() {
		objCS.Value(".");
		assertTrue("relative .", objCS.isDotFolder());
		objCS.Value("..");
		assertTrue("relative ..", objCS.isDotFolder());
		objCS.Value("/tmp/test");
		assertFalse("absolute .", objCS.isDotFolder());

	}

	@Test
	public void getSubFolderArrayTest () {
		objCS.Value("/a/b/c/d");
		String[] strT = objCS.getSubFolderArray();
		assertTrue("wrong number of folders", strT.length == 4);
		
		assertEquals("ungleich", "/a", strT[0]);
		assertEquals("ungleich", "/a/b", strT[1]);
		assertEquals("ungleich", "/a/b/c", strT[2]);
		assertEquals("ungleich", "/a/b/c/d", strT[3]);
//		assertEquals("ungleich", "", strT[4]);

	}
}
