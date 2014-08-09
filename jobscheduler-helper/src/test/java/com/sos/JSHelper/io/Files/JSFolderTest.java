/**
 * 
 */
package com.sos.JSHelper.io.Files;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author KB
 *
 */
public class JSFolderTest {
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link com.sos.JSHelper.io.Files.JSFolder#getFilelist(java.lang.String, int)}.
	 */
	@Test public void testGetFilelist() {
		JSFolder objF = JSFolder.getTempDir();
		objF.getFilelist("\\.txt", 0);
	}

	/**
	 * Test method for {@link com.sos.JSHelper.io.Files.JSFolder#newFile(java.lang.String)}.
	 * @throws IOException 
	 */
	@Test public void testNewFile() throws IOException {
		JSFolder objF = JSFolder.getTempDir();
		JSFile objFle = objF.newFile("testNewFile.tmp");
		objFle.WriteLine("testNewFile.tmp");
		assertTrue("File must exist", objFle.exists());
		objFle.delete();
	}
}
