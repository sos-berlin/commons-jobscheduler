package sos.net.ssh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.CredentialStore.Options.SOSCredentialStoreOptions;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.io.Files.JSXMLFile;

/**
 * @author KB
 *
 */
public class SOSSSHJobOptionsTest {

	private static final String	SOS_USER	= "sos-user";
	private static final String	USER	= "user";
	private static final String	AUTH_FILE	= "auth_file";
	private final String	conClassName	= "SOSSSHJobOptionsTest";

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

	@Test
	public void TestToXml() throws Exception {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::TestToXml";

		String strParameterName = USER;
		String strParameterValue = "JunitTestUser";
		String strCmdLineArgs[] = { "-" + strParameterName, strParameterValue };
		SOSSSHJobOptions objOptions = new SOSSSHJobOptions();

		objOptions.CommandLineArgs(strCmdLineArgs);
		assertEquals(strParameterName, strParameterValue, objOptions.user.Value());

		// String strTempFileName = JSFile.createTempFile("JSTest", ".xml").getAbsolutePath();
		String strTempFileName = "C:/temp/" + conClassName + ".xml";
		JSXMLFile objXF = objOptions.toXMLFile(strTempFileName);

		SOSSSHJobOptions objO2 = new SOSSSHJobOptions();
		objO2.LoadXML(objXF);
		assertEquals(strParameterName, strParameterValue, objO2.user.Value());

	} // private void TestToXml
	@Test
	public void testCommand_Script () {

		SOSSSHJobOptions objOptions = new SOSSSHJobOptions();
		objOptions.command_script.Value();
		objOptions.command.Value();
	}

	@Test
  @Ignore("Test set to Ignore for later examination")
	public void TestSerialize() throws Exception {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::TestSerialize";

		String strParameterName = USER;
		String strCmdLineArgs[] = { "-" + strParameterName, "JunitTestUser" };
		SOSSSHJobOptions objOptions = new SOSSSHJobOptions();

		objOptions.CommandLineArgs(strCmdLineArgs);
		assertEquals(strParameterName, "JunitTestUser", objOptions.user.Value());

		String strSerializedFileName = "c:/temp/test.object";
		objOptions.putObject(strSerializedFileName);

		System.setProperty(strParameterName, "sos-user2");
		objOptions.LoadSystemProperties();
		assertEquals(strParameterName, "sos-user2", objOptions.user.Value());

		SOSSSHJobOptions objO2 = new SOSSSHJobOptions();
		objO2 = (SOSSSHJobOptions) JSOptionsClass.getObject(strSerializedFileName);
		assertEquals(strParameterName, "JunitTestUser", objO2.user.Value());

	} // private void TestSerialize

	@Test
	public void TestSystemProperties() throws Exception {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::TestSystemProperties";

		String strCmdLineArgs[] = { "-user", "JunitTestUser" };
		SOSSSHJobOptions objOptions = new SOSSSHJobOptions();

		System.setProperty("SOSSSHJobOptions.user", SOS_USER);
		objOptions.LoadSystemProperties();
		assertEquals(USER, System.getProperty("SOSSSHJobOptions.user"), objOptions.user.Value());
		System.setProperty("SOSSSHJobOptions.user", "");

		objOptions.setAllOptions(new HashMap<String, String>());

		System.setProperty(USER, SOS_USER);
		objOptions.LoadSystemProperties();
		assertEquals(USER, SOS_USER, objOptions.user.Value());
		System.setProperty("SOSSSHJobOptions.user", "");

		objOptions.CommandLineArgs(strCmdLineArgs);
		assertEquals(USER, "JunitTestUser", objOptions.user.Value());

		System.setProperty(USER, "sos-user2");
		objOptions.LoadSystemProperties();
		assertEquals(USER, "sos-user2", objOptions.user.Value());

	} // private void TestSystemProperties

	@Test
	public void SetHashMap() throws Exception {

		SOSSSHJobOptions objOptions = new SOSSSHJobOptions();
		objOptions.setAllOptions(this.SetJobSchedulerSSHJobOptions(new HashMap<String, String>()));
//		objOptions.auth_method.Value(enuAuthenticationMethods.password);
//		objOptions.password.Value("pw");
//		objOptions.CheckMandatory();

		assertEquals(AUTH_FILE, "test", objOptions.auth_file.Value());
		assertEquals(USER, "test", objOptions.user.Value());

		objOptions.CurrentNodeName("step1");
		objOptions.setAllOptions(this.SetJobSchedulerSSHJobOptions(new HashMap<String, String>()));
		assertEquals(USER, "step1user", objOptions.user.Value());

		objOptions.CurrentNodeName("step2");
		objOptions.setAllOptions(this.SetJobSchedulerSSHJobOptions(new HashMap<String, String>()));
		assertEquals(USER, "userofstep2", objOptions.user.Value());

		System.out.println(objOptions.toString());
	}

	@Test
	public void SetCmdArgs() throws Exception {

		SOSSSHJobOptions objOptions = new SOSSSHJobOptions();

		String strArgs[] = new String[] { "-command", "ls", "-auth_method", "password", "-host", "8of9.sos", "-auth_file", "test", "-user", "kb", "-password",
				"huhu" };
		objOptions.CommandLineArgs(strArgs);

		objOptions.CheckMandatory();
		assertEquals(AUTH_FILE, objOptions.auth_file.Value(), "test");
		assertEquals(USER, objOptions.user.Value(), "kb");

		objOptions.CommandLineArgs(new String[] { "-user", "testtest" });
		assertEquals(USER, "testtest", objOptions.user.Value());

		System.out.println(objOptions.toString());
	}

	@Test
	public void SetCmdArgs2() throws Exception {

		SOSSSHJobOptions objOptions = new SOSSSHJobOptions();

		String strArgs[] = new String[] { "-command=ls", "-auth_method=password", "-host=8of9.sos", "-AuthFile=test", "-user=kb", "-password=huhu" };
		objOptions.CommandLineArgs(strArgs);

		objOptions.CheckMandatory();
		assertEquals(AUTH_FILE, objOptions.auth_file.Value(), "test");
		assertEquals(USER, objOptions.user.Value(), "kb");

		System.out.println(objOptions.toString());
	}
	
	@Test
	public void SetCmdArgsString() throws Exception {

		SOSSSHJobOptions objOptions = new SOSSSHJobOptions();

		String strArgs = new String ( "-command=ls -auth_method=password -host=8of9.sos -AuthFile=test -user=kb -password=huhu" );
		objOptions.CommandLineArgs(strArgs);

		objOptions.CheckMandatory();
		assertEquals(AUTH_FILE, objOptions.auth_file.Value(), "test");
		assertEquals(USER, objOptions.user.Value(), "kb");

		System.out.println(objOptions.toString());
	}
	
	
	
	/*
	 
	Test ueberfluessig, da CheckMandatory keine Exception mehr schmeisst, wenn command leer ist
	
	if (command.IsEmpty() && command_script.IsEmpty() && command_script_file.IsEmpty()) {
		throw new JSExceptionMandatoryOptionMissing("ErrSSH060 no command, command_script or command_script_file has been specified");
	}

	@Test(expected = com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing.class)
	public void SetEmptyCmd() throws Exception {

		SOSSSHJobOptions objOptions = new SOSSSHJobOptions();

		String strArgs[] = new String[] { "-auth_method=password", "-host=ftphost", "-auth_file=test", "-user=kb", "-password=huhu" };
		objOptions.CommandLineArgs(strArgs);

		objOptions.CheckMandatory();
		assertEquals(AUTH_FILE, objOptions.auth_file.Value(), "test");
		assertEquals(USER, objOptions.user.Value(), "kb");

		System.out.println(objOptions.toString());
	}
	*/
	
	@Test(expected = com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing.class)
	public void SetEmptyPassw() throws Exception {

		SOSSSHJobOptions objOptions = new SOSSSHJobOptions();

		String strArgs[] = new String[] { "-auth_method=password", "-host=ftphost", "-auth_file=test", "-user=kb", "-password=" };
		objOptions.CommandLineArgs(strArgs);

		objOptions.CheckMandatory();
		assertEquals(AUTH_FILE, objOptions.auth_file.Value(), "test");
		assertEquals(USER, objOptions.user.Value(), "kb");

		System.out.println(objOptions.toString());
	}

	private HashMap<String, String> SetJobSchedulerSSHJobOptions(final HashMap<String, String> pobjHM) {
		pobjHM.put("step1/user", "step1user"); // This parameter specifies the user account to be used when
		pobjHM.put("step2/user", "userofstep2"); // This parameter specifies the user account to be used when
		pobjHM.put("SOSSSHJobOptions.authfile", "test"); // This parameter specifies the path and name of a user's pr
		pobjHM.put(AUTH_FILE, "test"); // This parameter specifies the path and name of a user's pr
		pobjHM.put("SOSSSHJobOptions.auth_file", "test"); // This parameter specifies the path and name of a user's pr
		// pobjHM.put ("SOSSSHJobOptions.auth_method", "publickey"); // This parameter specifies the authorization method for the
		pobjHM.put("SOSSSHJobOptions.auth_method", "password"); // This parameter specifies the authorization method for the
		pobjHM.put("SOSSSHJobOptions.command", "test"); // This parameter specifies a command that is to be executed
		pobjHM.put("SOSSSHJobOptions.command_delimiter", "%%"); // Command delimiter characters are specified using this par
		pobjHM.put("SOSSSHJobOptions.command_script", "test"); // This parameter can be used as an alternative to command,
		pobjHM.put("SOSSSHJobOptions.command_script_file", "test"); // This parameter can be used as an alternative to command,
		pobjHM.put("SOSSSHJobOptions.command_script_param", "test"); // This parameter contains a parameterstring, which will be
		pobjHM.put("SOSSSHJobOptions.host", "wilma.sos"); // This parameter specifies the hostname or IP address of th
		pobjHM.put("SOSSSHJobOptions.ignore_error", "false"); // Should the value true be specified, then execution errors
		pobjHM.put("SOSSSHJobOptions.ignore_exit_code", "12,33-47"); // This parameter configures one or more exit codes which wi
		pobjHM.put("SOSSSHJobOptions.ignore_signal", "false"); // Should the value true be specified, then on
		pobjHM.put("SOSSSHJobOptions.ignore_stderr", "false"); // This job checks if any output to stderr has been created
//		pobjHM.put("SOSSSHJobOptions.password", "test"); // This parameter specifies the user account password for au
		pobjHM.put("SOSSSHJobOptions.port", "22"); // This parameter specifies the port number of the SSH serve
		pobjHM.put("SOSSSHJobOptions.proxy_host", "test"); // The value of this parameter is the host name or the IP ad
		pobjHM.put("SOSSSHJobOptions.proxy_password", "test"); // This parameter specifies the password for the proxy serve
		pobjHM.put("SOSSSHJobOptions.proxy_port", "22"); // This parameter specifies the port number of the proxy,
		pobjHM.put("SOSSSHJobOptions.proxy_user", "test"); // The value of this parameter specifies the user account fo
		pobjHM.put("SOSSSHJobOptions.simulate_shell", "false"); // Should the value true be specified for this parameter,
		pobjHM.put("SOSSSHJobOptions.simulate_shell_inactivity_timeout", "22"); // If no new characters are written to stdout or stderr afte
		pobjHM.put("SOSSSHJobOptions.simulate_shell_login_timeout", "22"); // If no new characters are written to stdout or stderr afte
		pobjHM.put("SOSSSHJobOptions.simulate_shell_prompt_trigger", "test"); // The expected comman line prompt. Using this prompt the jo
		pobjHM.put("SOSSSHJobOptions.user", "test"); // This parameter specifies the user account to be used when
		pobjHM.put("SOSSSHJobOptions.user", "test"); // This parameter specifies the user account to be used when
		pobjHM.put(USER, "test"); // This parameter specifies the user account to be used when
		pobjHM.put("step1/SOSSSHJobOptions.user", "step1user"); // This parameter specifies the user account to be used when
		pobjHM.put("step2/SOSSSHJobOptions.user", "userofstep2"); // This parameter specifies the user account to be used when

		pobjHM.put("UseCredentialStore", "true"); // This parameter specifies the user account to be used when
		pobjHM.put("CredentialStoreFileName", strKeePassDBFileName); // This parameter specifies the user account to be used when
		pobjHM.put("CredentialStorePassword", "testing"); // This parameter specifies the user account to be used when
		pobjHM.put("CredentialStoreKeyPath", "sos/server/wilma.sos"); // This parameter specifies the user account to be used when

		return pobjHM;
	} // private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)

	private HashMap<String, String> SetSSHJobOptionsUsingCredentialStore(final HashMap<String, String> pobjHM) {
//		pobjHM.put("SOSSSHJobOptions.command", "test"); // This parameter specifies a command that is to be executed
		pobjHM.put("SOSSSHJobOptions.command_delimiter", "%%"); // Command delimiter characters are specified using this par
		pobjHM.put("SOSSSHJobOptions.command_script", "test"); // This parameter can be used as an alternative to command,
		pobjHM.put("SOSSSHJobOptions.command_script_file", "test"); // This parameter can be used as an alternative to command,
		pobjHM.put("SOSSSHJobOptions.command_script_param", "test"); // This parameter contains a parameterstring, which will be
		pobjHM.put("SOSSSHJobOptions.ignore_error", "false"); // Should the value true be specified, then execution errors
		pobjHM.put("SOSSSHJobOptions.ignore_exit_code", "12,33-47"); // This parameter configures one or more exit codes which wi
		pobjHM.put("SOSSSHJobOptions.ignore_signal", "false"); // Should the value true be specified, then on
		pobjHM.put("SOSSSHJobOptions.ignore_stderr", "false"); // This job checks if any output to stderr has been created

		pobjHM.put("UseCredentialStore", "true"); // This parameter specifies the user account to be used when
		pobjHM.put("CredentialStore_FileName", strKeePassDBFileName); // This parameter specifies the user account to be used when
		pobjHM.put("CredentialStore_Password", "testing"); // This parameter specifies the user account to be used when
		pobjHM.put("CredentialStore_KeyPath", "/sos/server/wilma.sos"); // This parameter specifies the user account to be used when
		pobjHM.put("CredentialStore_ProcessNotesParams", "true"); // This parameter specifies the user account to be used when

		return pobjHM;
	} // private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)

	@Test (expected=java.lang.RuntimeException.class)
	public void testCredentialStore1 () {
		SOSSSHJobOptions objOptions = new SOSSSHJobOptions();
		SOSCredentialStoreOptions objCSO = objOptions.getCredentialStore().Options();
		assertTrue("not null", objCSO != null);
		objCSO.use_credential_Store.setTrue();
		objCSO.CredentialStore_FileName.Value("c:/temp/t.1");
		objOptions.getCredentialStore().checkCredentialStoreOptions();
	}

	// "C:/Users/KB/workspace-kepler/credentialstore/src/test/resources/keepassX-test.kdb"
//	private final String strKeePassDBFileName = "R:/java.sources/trunk/products/jobscheduler/virtual-file-system/src/test/resources/keepassX-test.kdb";
	private final String strKeePassDBFileName = "R:/backup/sos/java/junittests/testdata/keepassX-test.kdb";
	
	@Test 
	@Ignore("Test set to Ignore for later examination")
	//Entry in used credentialStore is expired since 17.07.14, therfore an exception occurs [SP]
	public void testCredentialStore2 () {
		SOSSSHJobOptions objOptions = new SOSSSHJobOptions();
		SOSCredentialStoreOptions objCSO = objOptions.getCredentialStore().Options();
		assertTrue("not null", objCSO != null);
		objCSO.use_credential_Store.setTrue();
		objCSO.CredentialStore_FileName.Value(strKeePassDBFileName);
		objCSO.CredentialStore_password.Value("testing");
		objCSO.CredentialStore_KeyPath.Value("/sos/server/wilma.sos");
		objOptions.getCredentialStore().checkCredentialStoreOptions();
		assertEquals("userid", "test", objOptions.user.Value());
		assertEquals("password", "12345", objOptions.password.Value());
	}

	@Test 
	@Ignore("Test set to Ignore for later examination")
	//Entry in used credentialStore is expired since 17.07.14, therfore an exception occurs [SP]
	public void testCredentialStore3 () throws Exception {
		SOSSSHJobOptions objOptions = new SOSSSHJobOptions(SetSSHJobOptionsUsingCredentialStore(new HashMap <String, String>()));
		assertTrue("not null", objOptions != null);
		assertEquals("userid", "test", objOptions.user.Value());
		assertEquals("password", "12345", objOptions.password.Value());
		assertEquals("command", "test.bsh", objOptions.command.Value());
	}



}
