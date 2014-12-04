package com.sos.VirtualFileSystem.WebDAV;

import com.sos.JSHelper.Options.SOSOptionTransferType;
import com.sos.JSHelper.io.Files.JSTextFile;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.Options.SOSFTPOptions;

import org.apache.log4j.Logger;
import org.junit.*;

/**
* \class SOSVfsSFtpTest
*
* \brief SOSVfsSFtpTest -
*
* \details
*
* \code
*   .... code goes here ...
* \endcode
*
* <p style="text-align:center">
* <br />---------------------------------------------------------------------------
* <br /> APL/Software GmbH - Berlin
* <br />##### generated by ClaviusXPress (http://www.sos-berlin.com) #########
* <br />---------------------------------------------------------------------------
* </p>
* \author KB
* @version $Id$10.11.2010
* \see reference
*
* Created on 10.11.2010 15:34:42
 */

public class SOSVfsWebDAVTest {

	@SuppressWarnings("unused")
	private final String			conClassName			= "SOSVfsWebDAVTest";

	@SuppressWarnings("unused")
	protected static Logger			logger					= Logger.getLogger(SOSVfsWebDAVTest.class);
	protected SOSFTPOptions			objOptions				= null;

	protected ISOSVFSHandler		objVFS					= null;
	protected ISOSVfsFileTransfer	objVfsClient			= null;

	// siehe setUp
	protected String				dynamicClassNameSource	= null;
	protected String				dynamicClassNameTarget	= null;

	protected final String			LOCAL_BASE_PATH			= "R:/backup/sos/java/junittests/testdata/JADE/";
	protected final String			REMOTE_BASE_PATH		= "/home/kb/";

	protected final String			WEB_URI					= "https://mediacenter.gmx.net";
	protected final String			WEB_USER				= "sos.apl@gmx.de";
	protected final String			WEB_PASS				= "sosapl10629";
	
	protected final String			REMOTE_BASE_PATH2		= "/webdav/";

	protected final String			WEB_URI2				= "http://homer.sos/webdav";
	protected final String			WEB_USER2				= "test";
	protected final String			WEB_PASS2				= "12345";

	public SOSVfsWebDAVTest() {
		//
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		objOptions = new SOSFTPOptions(SOSOptionTransferType.enuTransferTypes.webdav);
		objOptions.protocol.Value(SOSOptionTransferType.enuTransferTypes.webdav);
		objVFS = VFSFactory.getHandler(objOptions.protocol.Value());
		objVfsClient = (ISOSVfsFileTransfer) objVFS;
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	@Test
	public void testConnect() throws Exception {

		objOptions.host.Value(WEB_URI);
		objOptions.port.value(0);

		SOSConnection2OptionsAlternate objSource = objOptions.getConnectionOptions().Source();
		objSource.host.Value(WEB_URI);
		objSource.user.Value(WEB_USER);

		objOptions.operation.Value("send");
		VFSFactory.setConnectionOptions(objSource);
		objVFS = VFSFactory.getHandler(objOptions.protocol.Value());
		objVfsClient = (ISOSVfsFileTransfer) objVFS;

		objVFS.Connect(objOptions.getConnectionOptions().Source());

		// ftpClient.disconnect();
	}*/

	/**
	@Test
	public void testAuthenticate() throws Exception {
		connect();
		authenticate();
		objVfsClient.disconnect();
	}*/

	private void connect() throws Exception {
		objOptions.host.Value(WEB_URI);
		objOptions.port.value(0);
		// objOptions.local_dir.Value("/temp");
		SOSConnection2OptionsAlternate objSource = objOptions.getConnectionOptions().Source();
		objSource.host.Value(WEB_URI);
		objSource.user.Value(WEB_USER);
		objSource.protocol.Value("webdav");
		objSource.port.value(443);
//		objSource.proxy_host.Value("proxy.sos");
//		objSource.proxy_port.value(3128);
		
		objOptions.operation.Value("send");
		VFSFactory.setConnectionOptions(objSource);
		objVFS = VFSFactory.getHandler(objOptions.protocol.Value());
		objVfsClient = (ISOSVfsFileTransfer) objVFS;
		objVFS.Connect(objOptions.getConnectionOptions().Source());
	}

	private void authenticate() throws Exception {
		SOSConnection2OptionsAlternate objSource = objOptions.getConnectionOptions().Source();
		objSource.host.Value(WEB_URI);
		objSource.user.Value(WEB_USER);
		objSource.port.value(443);
//		objSource.proxy_host.Value("proxy.sos");
//		objSource.proxy_port.value(3128);
		objSource.password.Value(WEB_PASS);
		objSource.protocol.Value("webdav");
		objSource.ssh_auth_method.isURL(true);

		objVFS.Authenticate(objSource);
	}
	
	private void connect2() throws Exception {
		objOptions.host.Value(WEB_URI2);
		objOptions.port.value(8080);
		// objOptions.local_dir.Value("/temp");
		SOSConnection2OptionsAlternate objSource = objOptions.getConnectionOptions().Source();
		objSource.host.Value(WEB_URI2);
		objSource.user.Value(WEB_USER2);
		objSource.port.value(8080);
		objSource.protocol.Value("webdav");
//		objSource.proxy_host.Value("proxy.sos");
//		objSource.proxy_port.value(3128);
		
		objOptions.operation.Value("send");
		VFSFactory.setConnectionOptions(objSource);
		objVFS = VFSFactory.getHandler(objOptions.protocol.Value());
		objVfsClient = (ISOSVfsFileTransfer) objVFS;
		objVFS.Connect(objOptions.getConnectionOptions().Source());
	}

	private void authenticate2() throws Exception {
		SOSConnection2OptionsAlternate objSource = objOptions.getConnectionOptions().Source();
		objSource.host.Value(WEB_URI2);
		objSource.user.Value(WEB_USER2);
//		objSource.proxy_host.Value("proxy.sos");
//		objSource.proxy_port.value(3128);
		objSource.password.Value(WEB_PASS2);
		objSource.protocol.Value("webdav");
		objSource.ssh_auth_method.isURL(true);

		objVFS.Authenticate(objSource);
	}

	
	@Test
	public void testMkdir() throws Exception {
		connect();
		authenticate();

		objVfsClient.rmdir(REMOTE_BASE_PATH + "test1");
		objVfsClient.mkdir(REMOTE_BASE_PATH + "test1");

		ISOSVirtualFile objVF = objVfsClient.getFileHandle(REMOTE_BASE_PATH + "test1");
		System.out.println(objVF.getFileSize());
		objVfsClient.disconnect();
	}
	
	@Test
	public void testMkdir2() throws Exception {
		connect2();
		authenticate2();

		objVfsClient.rmdir(REMOTE_BASE_PATH2 + "kb/test1");
		objVfsClient.mkdir(REMOTE_BASE_PATH2 + "kb/test1");

		ISOSVirtualFile objVF = objVfsClient.getFileHandle(REMOTE_BASE_PATH2 + "kb/test1");
		System.out.println(objVF.getFileSize());
		objVfsClient.disconnect();
	}

	
	@Test
	public void testMkdirMultiple() throws Exception {
		connect();
		authenticate();

		objVfsClient.mkdir(REMOTE_BASE_PATH + "test1/test2/test3/");
//		objVfsClient.rmdir(REMOTE_BASE_PATH + "test1/");

		objVfsClient.disconnect();

	}
	
	@Test
	public void testMkdirMultiple2() throws Exception {
		connect2();
		authenticate2();

		objVfsClient.mkdir(REMOTE_BASE_PATH2 + "kb/test1/test2/test3/");

		objVfsClient.disconnect();

	}

	@Test
	public void testRmdirString() throws Exception {
		connect();
		authenticate();

		objVfsClient.rmdir(REMOTE_BASE_PATH +"test1/test2/test3/");

		objVfsClient.disconnect();
	}
	
	@Test
  @Ignore("Test set to Ignore for later examination")
	public void testRmdir2() throws Exception {
		connect2();
		authenticate2();

		objVfsClient.rmdir(REMOTE_BASE_PATH2 +"kb/test1/test2/test3");

		objVfsClient.disconnect();
	}

	//@Test
	public void testNListString() {
		// fail("Not yet implemented");
	}

	//@Test
	public void testIsNotHiddenFile() {
		// fail("Not yet implemented");
	}

	//@Test
	public void testNListStringBoolean() {
		// fail("Not yet implemented");
	}

	//@Test
	public void testNList() {
		// fail("Not yet implemented");
	}

	//@Test
	public void testNListBoolean() {
		// fail("Not yet implemented");
	}

	//@Test
	public void testDirString() {
		// fail("Not yet implemented");
	}

	//@Test
	public void testDirStringInt() {
		// fail("Not yet implemented");
	}

	//@Test
	public void testListNames() {
		// fail("Not yet implemented");
	}

	//@Test
	public void testDir() {
		// fail("Not yet implemented");
	}

	//@Test
	public void testGetResponse() {
		// fail("Not yet implemented");
	}

	@Test
	public void testSize() throws Exception {
		connect();
		authenticate();

		System.out.println(objVfsClient.getFileSize(REMOTE_BASE_PATH + "sos-net-src.zip"));
		System.out.println(objVfsClient.getFileSize(REMOTE_BASE_PATH + "BVG.pdf"));
		objVfsClient.disconnect();
	}
	
	@Test
	public void testSize2() throws Exception {
		connect2();
		authenticate2();

		System.out.println(objVfsClient.getFileSize(REMOTE_BASE_PATH2 + "text.txt"));
		objVfsClient.disconnect();
	}

	//@Test
	public void testGetFileStringStringBoolean() {
		// fail("Not yet implemented");
	}

	//@Test
	public void testGetFileStringString() {
		// fail("Not yet implemented");
	}

	private String createTestFile() throws Exception {

		String strTestFileName = LOCAL_BASE_PATH + "webdav-test.dat";
		JSTextFile objF = new JSTextFile(strTestFileName);
		objF.WriteLine("Die Basis ist das Fundament der Grundlage");
		objF.deleteOnExit();
		objF.close();

		return strTestFileName;

	}
	
	@Test
  @Ignore("Test set to Ignore for later examination")
	public void testPut() throws Exception {
		connect();
		authenticate();

		String strTargetFileName = REMOTE_BASE_PATH + "sos-net-src.zip";
		objVfsClient.putFile(createTestFile(), strTargetFileName);
		ISOSVirtualFile objVF = objVfsClient.getFileHandle(strTargetFileName);
		long intFileSize = objVF.getFileSize();
		System.out.println("Size of Targetfile = " + intFileSize);

		objVfsClient.delete(strTargetFileName);
		objVfsClient.disconnect();
	}
	
	@Test
  @Ignore("Test set to Ignore for later examination")
	public void testPut2() throws Exception {
		connect2();
		authenticate2();

		String strTargetFileName = REMOTE_BASE_PATH2 + "sos-net-src.zip";
		objVfsClient.putFile(createTestFile(), strTargetFileName);
		ISOSVirtualFile objVF = objVfsClient.getFileHandle(strTargetFileName);
		long intFileSize = objVF.getFileSize();
		System.out.println("Size of Targetfile = " + intFileSize);

		objVfsClient.delete(strTargetFileName);
		objVfsClient.disconnect();
	}

	//@Test
	public void testPutFileStringString() {
		// fail("Not yet implemented");
	}

	@Test
  @Ignore("Test set to Ignore for later examination")
	public void testPutFileStringOutputStream() throws Exception {
		connect();
		authenticate();

		String strTargetFileName = REMOTE_BASE_PATH + "out.test.txt";
		objVfsClient.delete(strTargetFileName);
		ISOSVirtualFile objVF = objVfsClient.getFileHandle(strTargetFileName);
		objVF.write("hallo".getBytes());
		objVF.closeOutput();

		long intFileSize = objVF.getFileSize();
		System.out.println("Size of Targetfile = " + intFileSize);

		objVfsClient.delete(strTargetFileName);
		objVfsClient.disconnect();
	}
		

	//@Test
	public void testGetClient() {
		// fail("Not yet implemented");
	}

	//@Test
	public void testAppendFile() {
		// fail("Not yet implemented");
	}

	@Test
	public void testCd() throws Exception {
		connect();
		authenticate();

		objVfsClient.changeWorkingDirectory("/xxx/xxx");

		objVfsClient.disconnect();
	}
	
	@Test
	public void testCd2() throws Exception {
		connect2();
		authenticate2();

//		objVfsClient.changeWorkingDirectory("/xxx/xxx");
		objVfsClient.changeWorkingDirectory("/webdav/kb");

		objVfsClient.disconnect();
	}
	
	
//	@Test
	public void testCdSwisscom() throws Exception {
		SOSConnection2OptionsAlternate objSource = objOptions.getConnectionOptions().Source();
		objSource.host.Value("https://filestation.creditmaster.ch");
		objSource.port.value(6006);
		objSource.user.Value("SwissTest");
		objSource.protocol.Value("webdav");

		objOptions.operation.Value("send");
		VFSFactory.setConnectionOptions(objSource);
		objVFS = VFSFactory.getHandler(objOptions.protocol.Value());
		objVfsClient = (ISOSVfsFileTransfer) objVFS;
		objVFS.Connect(objOptions.getConnectionOptions().Source());
		                          
		objSource.password.Value("PE8UKgKKFxDGOnbp9CD");
		objSource.ssh_auth_method.isURL(true);

		objVFS.Authenticate(objSource);

		objVfsClient.changeWorkingDirectory("/Test");
		//objVfsClient.rmdir("/Test/sos");

		objVfsClient.disconnect();
	}
	
//	@Test
	public void testCdSwisscomWithProxy() throws Exception {
		SOSConnection2OptionsAlternate objSource = objOptions.getConnectionOptions().Source();
		objSource.host.Value("https://filestation.creditmaster.ch:6006/Test");
		objSource.port.value(6006);
		objSource.user.Value("SwissTest");
		objSource.protocol.Value("webdav");
		
		objSource.proxy_host.Value("proxy.sos");
		objSource.proxy_port.value(3128);
		
		objOptions.operation.Value("send");
		VFSFactory.setConnectionOptions(objSource);
		objVFS = VFSFactory.getHandler(objOptions.protocol.Value());
		objVfsClient = (ISOSVfsFileTransfer) objVFS;
		objVFS.Connect(objOptions.getConnectionOptions().Source());
		                          
		objSource.password.Value("PE8UKgKKFxDGOnbp9CD");
		objSource.ssh_auth_method.isURL(true);

		objVFS.Authenticate(objSource);

		objVfsClient.changeWorkingDirectory("/Test");
		//objVfsClient.rmdir("/Test/sos");

		objVfsClient.disconnect();
	}


	@Test
  @Ignore("Test set to Ignore for later examination")
	public void testDelete() throws Exception {
		connect();
		authenticate();

		objVfsClient.put(createTestFile(), REMOTE_BASE_PATH + "tmp123.zip");
		objVfsClient.delete(REMOTE_BASE_PATH + "tmp123.zip");

		objVfsClient.disconnect();
	}
	
	@Test
  @Ignore("Test set to Ignore for later examination")
	public void testDelete2() throws Exception {
		connect2();
		authenticate2();

		objVfsClient.put(createTestFile(), REMOTE_BASE_PATH2 + "tmp123.zip");
		objVfsClient.delete(REMOTE_BASE_PATH2 + "tmp123.zip");

		objVfsClient.disconnect();
	}

	@Test
	public void testLogin() throws Exception {
		connect();

		objVfsClient.login(WEB_USER, WEB_PASS);

		objVfsClient.disconnect();
	}

	@Test
	public void testDisconnect() throws Exception {
		connect();

		objVfsClient.login(WEB_USER, WEB_PASS);

		objVfsClient.disconnect();
	}

	@Test
	public void testGetReplyString() throws Exception {
		connect();

		objVfsClient.login(WEB_USER, WEB_PASS);

		logger.info("Replay = " + objVfsClient.getReplyString());

		objVfsClient.disconnect();
	}

	@Test
	public void testIsConnected() throws Exception {
		connect();

		//ftpClient.login(WEB_USER,WEB_PASS);

		logger.debug("IS CONNECTED = " + objVfsClient.isConnected());

		objVfsClient.disconnect();
	}

	@Test
	public void testLogout() throws Exception {
		connect();

		objVfsClient.login(WEB_USER, WEB_PASS);

		objVfsClient.logout();

		// ftpClient.disconnect();
	}

	@Test
  @Ignore("Test set to Ignore for later examination")
	public void testRename() throws Exception {
		connect();
		authenticate();

		String strTestFileName = createTestFile();

		objVfsClient.put(strTestFileName, REMOTE_BASE_PATH + "tmp123.zip");
		objVfsClient.rename(REMOTE_BASE_PATH + "tmp123.zip", REMOTE_BASE_PATH + "tmp1234.zip");
		objVfsClient.delete(REMOTE_BASE_PATH + "tmp1234.zip");

		objVfsClient.disconnect();
	}
	
	@Test
  @Ignore("Test set to Ignore for later examination")
	public void testRename2() throws Exception {
		connect2();
		authenticate2();

		String strTestFileName = createTestFile();

		objVfsClient.put(strTestFileName, REMOTE_BASE_PATH2 + "tmp123.zip");
		objVfsClient.rename(REMOTE_BASE_PATH2 + "tmp123.zip", REMOTE_BASE_PATH2 + "tmp1234.zip");
		objVfsClient.delete(REMOTE_BASE_PATH2 + "tmp1234.zip");

		objVfsClient.disconnect();
	}

	

	@Test
	public void testGetHandler() throws Exception {
		connect();
		authenticate();

		logger.debug("HANDLER = " + objVfsClient.getHandler());

		objVfsClient.disconnect();
	}

	@Test
	public void testExecuteCommand() throws Exception {
		connect();
		authenticate();

		String lineSeparator = "\n";

		objVFS.ExecuteCommand("cd /home/test" + lineSeparator + "cd /home/kb");

		objVfsClient.disconnect();
	}
	

	//@Test
	public void testCreateScriptFile() throws Exception {
		// fail("Not yet implemented");
	}

	//@Test
	public void testGetExitCode() throws Exception {
		// fail("Not yet implemented");
	}

	//@Test
	public void testGetExitSignal() throws Exception {
		// fail("Not yet implemented");
	}

	//@Test
	public void testCloseConnection() throws Exception {
		// fail("Not yet implemented");
	}

	//@Test
	public void testConnect1() throws Exception {
		// fail("Not yet implemented");
	}

	//@Test
	public void testConnectSOSConnection2OptionsAlternate() throws Exception {
		// fail("Not yet implemented");
	}

	//@Test
	public void testConnectISOSConnectionOptions() throws Exception {
		// fail("Not yet implemented");
	}

	//@Test
	public void testConnectStringInt() throws Exception {
		// fail("Not yet implemented");
	}

	//@Test
	public void testCloseSession() throws Exception {
		// fail("Not yet implemented");
	}

	//@Test
	public void testOpenSession() throws Exception {
		// fail("Not yet implemented");
	}

	//@Test
	public void testTransferMode() throws Exception {
		// fail("Not yet implemented");
	}

	//@Test
	public void testGetNewVirtualFile() throws Exception {
		// fail("Not yet implemented");
	}

	//@Test
	public void testMkdirSOSFolderName() throws Exception {
		// fail("Not yet implemented");
	}

	//@Test
	public void testRmdirSOSFolderName() throws Exception {
		// fail("Not yet implemented");
	}

	//@Test
	public void testGetConnection() throws Exception {
		// fail("Not yet implemented");
	}

	//@Test
	public void testGetSession() throws Exception {
		// fail("Not yet implemented");
	}

	//@Test
	public void testDirSOSFolderName() throws Exception {
		// fail("Not yet implemented");
	}

	//@Test
	public void testGetStdErr() throws Exception {
		// fail("Not yet implemented");
	}

	//@Test
	public void testGetStdOut() throws Exception {
		// fail("Not yet implemented");
	}

	//@Test
	public void testRemoteIsWindowsShell() throws Exception {
		// fail("Not yet implemented");
	}

	//@Test
	public void testSetJSJobUtilites() throws Exception {
		// fail("Not yet implemented");
	}

	@Test
	public void testGetFileHandle() throws Exception {
		connect();
		authenticate();

		System.out.println(objVfsClient.getModificationTime(REMOTE_BASE_PATH + "sos-net-src.zip"));

		objVfsClient.disconnect();
	}
	
	@Test
	public void testGetFileHandle2() throws Exception {
		connect2();
		authenticate2();

		System.out.println(objVfsClient.getModificationTime(REMOTE_BASE_PATH2 + "text.txt"));

		objVfsClient.disconnect();
	}

	@Test
	public void testGetFilelist() throws Exception {
		connect();
		authenticate();

		//String[] result = ftpClient.getFilelist(REMOTE_BASE_PATH, "", 0, true);
		String[] result = objVfsClient.getFilelist(REMOTE_BASE_PATH, "\\.pdf$", 0, true);
		for (String element : result) {
			logger.info(element);
		}

		objVfsClient.disconnect();
	}
	
	@Test
	public void testGetFilelist2() throws Exception {
		connect2();
		authenticate2();

		String[] result = objVfsClient.getFilelist(REMOTE_BASE_PATH2 + "kb", "", 0, true);
		for (String element : result) {
			logger.info(element);
		}

		objVfsClient.disconnect();
	}
}
