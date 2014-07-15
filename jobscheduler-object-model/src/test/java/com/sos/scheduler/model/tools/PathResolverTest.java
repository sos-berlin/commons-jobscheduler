package com.sos.scheduler.model.tools;

import org.apache.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PathResolverTest {

	private final static Logger logger = Logger.getLogger(PathResolverTest.class);

	@Test
	public void test() throws Exception {
		assertEquals("/folder", PathResolver.getRelativePath("", "", "/folder"));
		assertEquals("/folder", PathResolver.getRelativePath(getResourceFolder(), getResourceFolder(), "/folder"));
		assertEquals("/folder", PathResolver.getRelativePath("", "", "folder"));
		assertEquals("/folder", PathResolver.getRelativePath(getResourceFolder(), getResourceFolder(), "/folder"));
		assertEquals("/folder2", PathResolver.getRelativePath(getResourceFolder(), getResourceFolder() + "folder", "../folder2"));
		assertEquals("", PathResolver.getRelativePath(getResourceFolder(), getResourceFolder() + "folder", ".."));
		assertEquals("/folder", PathResolver.resolvePath("/folder/../folder"));
		assertEquals("/folder/folder2", PathResolver.resolvePath("/folder/folder2"));
		assertEquals("/folder/folder2", PathResolver.resolvePath("/folder/./folder2"));
		assertEquals("/folder", PathResolver.getRelativePath("", "", "./folder/.././folder/./"));
		logger.debug( PathResolver.getRelativePath(
				"C:/scheduler/ncfast/com.sos.jobnet/testdata", 
				"C:/scheduler/ncfast/com.sos.jobnet/testdata/subfolder/folder1/..",
				"") + "/job_chain_multiple_inheritance_sample,H");
		String result = PathResolver.getRelativePath(
				"C:/scheduler/ncfast/com.sos.jobnet/testdata", 
				"C:/scheduler/ncfast/com.sos.jobnet/testdata", 
				"//CloseofBusinessIndicator");
		logger.debug(result);
		logger.debug( PathResolver.getRelativePath(
				"C:/Users/eqcpn/java/ncfast/com.sos.jobnet/testdata", 
				"C:/Users/eqcpn/java/ncfast/com.sos.jobnet/testdata/subfolder/folder1",
				"../../myInclude.txt")
		);
		logger.debug( PathResolver.getAbsolutePath(
				"C:/Users/eqcpn/java/ncfast/com.sos.jobnet/testdata", 
				"C:/Users/eqcpn/java/ncfast/com.sos.jobnet/testdata/subfolder/folder1",
				"../../myInclude.txt")
		);
	}
	
	
	@Test
	public void testNormalize() throws Exception {
		logger.debug(PathResolver.resolvePath("c:\\temp\\..\\test"));
	}
		
	public static String getResourceFolder() {
		StringBuffer sb = new StringBuffer();
		sb.append(System.getProperty("user.dir"));
		sb.append("/testdata/");
		return sb.toString().replace("\\", "/");
	}

}
