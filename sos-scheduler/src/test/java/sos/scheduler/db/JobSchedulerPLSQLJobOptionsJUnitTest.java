package sos.scheduler.db;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.CredentialStore.Options.SOSCredentialStoreOptions;
import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import com.sos.JSHelper.Logging.Log4JHelper;

/**
 * \class 		JobSchedulerPLSQLJobOptionsJUnitTest - Launch Database Statement
 *
 * \brief 
 *
 *

 *
 * see \see R:\backup\sos\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\JobSchedulerPLSQLJob.xml for (more) details.
 * 
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse\xsl\JSJobDoc2JSJUnitOptionSuperClass.xsl from http://www.sos-berlin.com at 20120905153725 
 * \endverbatim
 *
 * \section TestData Eine Hilfe zum Erzeugen einer HashMap mit Testdaten
 *
 * Die folgenden Methode kann verwendet werden, um für einen Test eine HashMap
 * mit sinnvollen Werten für die einzelnen Optionen zu erzeugen.
 *
 * \verbatim
 private HashMap <String, String> SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM) {
	pobjHM.put ("		JobSchedulerPLSQLJobOptionsJUnitTest.auth_file", "test");  // This parameter specifies the path and name of a user's pr
		return pobjHM;
  }  //  private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim
 */
public class JobSchedulerPLSQLJobOptionsJUnitTest extends JSToolBox {
	private final String					conClassName	= "JobSchedulerPLSQLJobOptionsJUnitTest";						//$NON-NLS-1$
	@SuppressWarnings("unused")
	private static Logger					logger			= Logger.getLogger(JobSchedulerPLSQLJobOptionsJUnitTest.class);
	@SuppressWarnings("unused")
	private static Log4JHelper				objLogger		= null;
	private JobSchedulerPLSQLJob			objE			= null;
	protected JobSchedulerPLSQLJobOptions	objOptions		= null;

	public JobSchedulerPLSQLJobOptionsJUnitTest() {
		//
	}

	@BeforeClass public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass public static void tearDownAfterClass() throws Exception {
	}

	@Before public void setUp() throws Exception {
		objLogger = new Log4JHelper("./log4j.properties"); //$NON-NLS-1$
		BasicConfigurator.configure();
		logger.setLevel(Level.DEBUG);
		objE = new JobSchedulerPLSQLJob();
		objE.registerMessageListener(this);
		objOptions = objE.Options();
		objOptions.registerMessageListener(this);
		JSListenerClass.bolLogDebugInformation = true;
		JSListenerClass.intMaxDebugLevel = 9;
	}

	@After public void tearDown() throws Exception {
	}
// C:\Users\KB\workspace-kepler\products\commons\credentialstore\src\test\resources\keepassX-test.kdb
// C:\Users\KB\workspace-kepler\products\jobscheduler\sos-scheduler\keepassX-test.kdb
	
	private final String strKeePathDBPathName = "../../commons/credentialstore/src/test/resources/keepassX-test.kdb";
	@Test public void testSOSCredentialStore() throws Exception {
		HashMap<String, String> objH = new HashMap<String, String>();
		objH.put("source_dir", "source_dir");
		objH.put("use_credential_Store", "true");
		objH.put("CredentialStore_FileName", strKeePathDBPathName);
//		objH.put("CredentialStore_KeyFileName", "./testing-key.key");
		objH.put("CredentialStore_password", "testing");
		objH.put("CredentialStore_ProcessNotesParams", "true");
		objH.put("CredentialStore_Key_Path", "testserver/db/db_url_test");

		try {
			JobSchedulerPLSQLJobOptions objO = new JobSchedulerPLSQLJobOptions(objH);
			SOSCredentialStoreOptions objCS = objO.getCredentialStore().Options();
			logger.debug("objSource.UserName.Value() = " + objO.user.Value());
			logger.debug("objSource.passwrod.Value() = " + objO.password.Value());
			logger.debug("testSOSCredentialStore1 " + objCS.dirtyString());
			logger.info("testSOSCredentialStore1 " + objO.dirtyString());
			assertEquals("source_CredentialStore_use_credential_Store", strKeePathDBPathName, objCS.CredentialStore_FileName.Value());
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}


	/**
	 * \brief testcommand : Database Commands for the Job. It is possible to define m
	 * 
	 * \details
	 * Database Commands for the Job. It is possible to define more than one instruction in the COMMAND field. Such instructions are then carried out in the order in which they are written and must be separated by a semicolon and a subsequent new line. Parameters can be replaced in database commands. This is done by the addition of a variable in the form §{param} at any given point in a command. This variable is then given the value of the instruction parameter with the name [param] before execution.
	 *
	 */
	@Test public void testcommand() { // SOSOptionString
		objOptions.command.Value("++----++");
		assertEquals("Database Commands for the Job. It is possible to define m", objOptions.command.Value(), "++----++");
	}

	/**
	 * \brief testdb_password : database password
	 * 
	 * \details
	 * database password
	 *
	 */
	@Test public void testdb_password() { // SOSOptionString
		objOptions.db_password.Value("++----++");
		assertEquals("database password", objOptions.db_password.Value(), "++----++");
	}

	/**
	 * \brief testdb_url : jdbc url (e.g. jdbc:oracle:thin:@localhost:1521:XE)
	 * 
	 * \details
	 * jdbc url (e.g. jdbc:oracle:thin:@localhost:1521:XE)
	 *
	 */
	@Test public void testdb_url() { // SOSOptionString
		objOptions.db_url.Value("++----++");
		assertEquals("jdbc url (e.g. jdbc:oracle:thin:@localhost:1521:XE)", objOptions.db_url.Value(), "++----++");
	}

	/**
	 * \brief testdb_user : database user
	 * 
	 * \details
	 * database user
	 *
	 */
	@Test public void testdb_user() { // SOSOptionString
		objOptions.db_user.Value("++----++");
		assertEquals("database user", objOptions.db_user.Value(), "++----++");
	}

	/**
	 * \brief testexec_returns_resultset : If stored procedures are called which return a result set
	 * 
	 * \details
	 * If stored procedures are called which return a result set, this needs to be set to true in order to run the stored procedure as a query. This does not work with the SQL Server 2000 and 2005 jdbc drivers.
	 *
	 */
	@Test public void testexec_returns_resultset() { // SOSOptionString
		objOptions.exec_returns_resultset.Value("++false++");
		assertEquals("If stored procedures are called which return a result set", objOptions.exec_returns_resultset.Value(), "++false++");
	}

	/**
	 * \brief testresultset_as_parameters : false No output parameters are generated.
	 * 
	 * \details
	 * false No output parameters are generated. true If set to true, the first row of the resultset will be set as order parameters (using the column names as parameter names). Example: The query "SELECT first_name, last_name, age FROM persons" produces the following result set: first_name last_name age John Doe 30 Hans Mustermann 33 Jean Dupont 56 The following name/value pairs will be generated as order parameters: first_name=John last_name=Doe age=30 name_value If set to name_value, the first two columns of the resultset will be turned into order parameters. The values of the first column will become parameter names, the values of the second column will become parameter values. Example: From the above resultset, the following name/value pairs will be generated as order parameters: John=Doe Hans=Mustermann Jean=Dupont
	 *
	 */
	@Test public void testresultset_as_parameters() { // SOSOptionString
		objOptions.resultset_as_parameters.Value("++false++");
		assertEquals("false No output parameters are generated.", objOptions.resultset_as_parameters.Value(), "++false++");
	}

	/**
	 * \brief testresultset_as_warning : If set to true, a warning will be issued, if the statemen
	 * 
	 * \details
	 * If set to true, a warning will be issued, if the statements produce a result
	 *
	 */
	@Test public void testresultset_as_warning() { // SOSOptionString
		objOptions.resultset_as_warning.Value("++false++");
		assertEquals("If set to true, a warning will be issued, if the statemen", objOptions.resultset_as_warning.Value(), "++false++");
	}
} // public class JobSchedulerPLSQLJobOptionsJUnitTest