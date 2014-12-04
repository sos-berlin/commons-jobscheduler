package com.sos.VirtualFileSystem.DataElements;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import com.sos.VirtualFileSystem.Options.SOSConnection2Options;

import org.apache.log4j.Logger;
import org.junit.*;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * \class 		SOSConnection2OptionsJUnitTest - Options for a connection to an uri (server, site, e.g.)
 *
 * \brief
 *
 *

 *
 * see \see j:\e\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\SOSConnection2.xml for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse\xsl\JSJobDoc2JSJUnitOptionSuperClass.xsl from http://www.sos-berlin.com at 20100917112411
 * \endverbatim
 *
 * \section TestData Eine Hilfe zum Erzeugen einer HashMap mit Testdaten
 *
 * Die folgenden Methode kann verwendet werden, um für einen Test eine HashMap
 * mit sinnvollen Werten für die einzelnen Optionen zu erzeugen.
 *
 * \verbatim
 private HashMap <String, String> SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM) {
	pobjHM.put ("		SOSConnection2OptionsJUnitTest.auth_file", "test");  // This parameter specifies the path and name of a user's pr
		return pobjHM;
  }  //  private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim
 */
public class SOSConnection2OptionsJUnitTest extends JSToolBox {
	private final String			conClassName	= "SOSConnection2OptionsJUnitTest";						//$NON-NLS-1$
	@SuppressWarnings("unused")
	private static Logger			logger			= Logger.getLogger(SOSConnection2OptionsJUnitTest.class);
	@SuppressWarnings("unused")

	protected SOSConnection2Options	objOptions		= null;

	public SOSConnection2OptionsJUnitTest() {
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
		// objE = new SOSConnection2();
		// objE.registerMessageListener(this);
		objOptions = new SOSConnection2Options();
		// objOptions.registerMessageListener(this);

		JSListenerClass.bolLogDebugInformation = true;
		JSListenerClass.intMaxDebugLevel = 2;
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test public void testPreCommand () {
		String strCmd = "chmod $TargetfileName 777";
		objOptions.Source().Pre_Command.Value(strCmd);
		assertEquals("chmod ", strCmd, objOptions.Source().Pre_Command.Value());
		objOptions.Target().Pre_Command.Value(strCmd);
		assertEquals("chmod ", strCmd, objOptions.Target().Pre_Command.Value());
	}
	/**
	 * \brief testhost : Host-Name This parameter specifies th
	 *
	 * \details
	 * This parameter specifies the hostname or IP address of the server to which a connection has to be made.
	 *
	 */
	@Test
	public void testhost() { // SOSOptionHostName
		objOptions.host.Value("++----++");
		assertEquals("Host-Name This parameter specifies th", objOptions.host.Value(), "++----++");

	}

	/**
	 * \brief testpassive_mode : passive_mode Passive mode for FTP is often used wit
	 *
	 * \details
	 * Passive mode for FTP is often used with firewalls. Valid values are 0 or 1.
	 *
	 */
	@Test
	public void testpassive_mode() { // SOSOptionBoolean
		objOptions.passive_mode.Value("true");
		assertTrue("passive_mode Passive mode for FTP is often used wit", objOptions.passive_mode.value());
		objOptions.passive_mode.Value("false");
		assertFalse("passive_mode Passive mode for FTP is often used wit", objOptions.passive_mode.value());

	}

	/**
	 * \brief testport : Port-Number to be used for Data-Transfer
	 *
	 * \details
	 * Port by which files should be transferred. For FTP this is usually port 21, for SFTP this is usually port 22.
	 *
	 */
	@Test
	public void testport() { // SOSOptionPortNumber
		objOptions.port.Value("21");
		assertEquals("Port-Number to be used for Data-Transfer", objOptions.port.Value(), "21");

	}

	/**
	 * \brief testprotocol : Type of requested Datatransfer The values ftp, sftp
	 *
	 * \details
	 * The values ftp, sftp or ftps are valid for this parameter. If sftp is used, then the ssh_* parameters will be applied.
	 *
	 */
	@Test
	public void testprotocol() { // SOSOptionStringValueList
		objOptions.protocol.Value("++ftp++");
		assertEquals("Type of requested Datatransfer The values ftp, sftp", objOptions.protocol.Value(), "++ftp++");

	}

	/**
	 * \brief testtransfer_mode : Type of Character-Encoding Transfe
	 *
	 * \details
	 * Transfer mode is used for FTP exclusively and can be either ascii or binary.
	 *
	 */
	@Test
	public void testtransfer_mode() { // SOSOptionTransferMode
		objOptions.transfer_mode.Value("++binary++");
		assertEquals("Type of Character-Encoding Transfe", objOptions.transfer_mode.Value(), "++binary++");

	}

	@Test
  @Ignore("Test set to Ignore for later examination")
	public void testKeyWithPrefix () throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::testKeyWithPrefix";

		objOptions = new SOSConnection2Options(SetJobSchedulerSSHJobOptions(new HashMap <String, String>()));
		assertEquals("host failed", "test1", objOptions.host.Value());
		assertEquals("alternative_host failed", "test2", objOptions.Alternatives().getHost().Value());
		assertEquals("source_host failed", "test3", objOptions.Source().host.Value());
		assertEquals("target_host failed", "test5", objOptions.Target().host.Value());
		assertEquals("source_alternative_host failed", "test4", objOptions.Source().Alternatives().host.Value());
//		assertEquals("jump_host failed", "test6", objOptions.JumpServer().host.Value());
	}

	@Test
  @Ignore("Test set to Ignore for later examination")
	public void testKeyWithAlias () throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::testKeyWithAlias";

		objOptions = new SOSConnection2Options(SetJobSchedulerSSHJobOptions(new HashMap <String, String>()));
		assertEquals("host failed", "test1", objOptions.host.Value());
		assertEquals("alternative_host failed", "test2", objOptions.Alternatives().host.Value());
		assertEquals("source_host failed", "test3", objOptions.Source().host.Value());
		assertEquals("target_host failed", "test5", objOptions.Target().host.Value());
		assertEquals("source_alternative_host failed", "test4", objOptions.Source().Alternatives().host.Value());
//		assertEquals("jump_host failed", "test6", objOptions.JumpServer().host.Value());
	}

	@Test
  @Ignore("Test set to Ignore for later examination")
	public void testAlternativeOptions () throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::testAlternativeOptions";
		HashMap <String, String> objT = new HashMap <String, String>();
		objT.put("IsAnUnknownOption", "ValueOfUnknownOption");
		objT.put("host", "test1");
		objT.put("alternative_host", "test2");
		objT.put("alternative_port", "22");
		objT.put("alternative_protocol", "test2");

		objOptions = new SOSConnection2Options(objT);
		assertEquals("alternative_host failed", "test2", objOptions.Alternatives().host.Value());
	}

	@Test
	public void testStrictHostKeyChecking () {

		objOptions = new SOSConnection2Options();
		assertEquals("StrictHostKeyChecking", "no", objOptions.StrictHostKeyChecking.Value());

		objOptions.StrictHostKeyChecking.Value("yes");
		assertEquals("StrictHostKeyChecking", "yes", objOptions.StrictHostKeyChecking.Value());

	}

	@Test
	public void testForUnknownOptions () throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::testForUnknownOptions";

		HashMap <String, String> objT = SetJobSchedulerSSHJobOptionsAlias(new HashMap <String, String>());
		objT.put("IsAnUnknownOption", "ValueOfUnknownOption");
		objOptions = new SOSConnection2Options(objT);
		boolean flgAllOptionsProcessed = objOptions.ReportNotProcessedOptions();

		assertFalse("Unknown Option found", flgAllOptionsProcessed);
	}  // testForUnknownOptions

	private HashMap<String, String> SetJobSchedulerSSHJobOptions(final HashMap<String, String> pobjHM) {
		pobjHM.put("host", "test1");
		pobjHM.put("port", "21");
		pobjHM.put("protocol", "test1");

		pobjHM.put("alternative_host", "test2");
		pobjHM.put("alternative_port", "22");
		pobjHM.put("alternative_protocol", "test2");

		pobjHM.put("source_host", "test3");
		pobjHM.put("source_port", "23");
		pobjHM.put("source_protocol", "test3");

		pobjHM.put("alternative_source_host", "test4");
		pobjHM.put("alternative_source_port", "24");
		pobjHM.put("alternative_source_protocol", "test4");

		pobjHM.put("target_host", "test5");
		pobjHM.put("target_port", "255");
		pobjHM.put("target_host", "test5");

		pobjHM.put("jump_port", "26");
		pobjHM.put("jump_protocol", "ftp");
        pobjHM.put("jump_host", "test6");

		return pobjHM;
	} // private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)

	private HashMap<String, String> SetJobSchedulerSSHJobOptionsAlias(final HashMap<String, String> pobjHM) {
		pobjHM.put("ftp_host", "wilma.sos");
		pobjHM.put("ftp_port", "21");
		pobjHM.put("ftp_protocol", "test1");

		pobjHM.put("source_host", "wilma.sos");
		pobjHM.put("source_port", "23");
		pobjHM.put("source_protocol", "test3");

		pobjHM.put("alternative_source_host", "wilma.sos");
		pobjHM.put("alternative_source_port", "24");
		pobjHM.put("alternative_source_protocol", "test4");

		pobjHM.put("target_host", "wilma.sos");
		pobjHM.put("target_port", "25");
		pobjHM.put("target_host", "wilma.sos");

		pobjHM.put("jump_port", "26");
		pobjHM.put("jump_protocol", "26");
        pobjHM.put("jump_host", "wilma.sos");

		return pobjHM;
	} // private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)

} // public class SOSConnection2OptionsJUnitTest