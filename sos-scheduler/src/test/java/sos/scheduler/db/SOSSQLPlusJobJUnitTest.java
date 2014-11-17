package sos.scheduler.db;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.JSHelper.Listener.JSListenerClass;
import com.sos.JSHelper.io.Files.JSTextFile;

/**
 * \class 		SOSSQLPlusJobJUnitTest - JUnit-Test for "Start SQL*Plus client and execute sql*plus programs"
 *
 * \brief MainClass to launch SOSSQLPlusJob as an executable command-line program
 *

 *
 * see \see R:\backup\sos\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\SOSSQLPlusJob.xml for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse - Kopie\xsl\JSJobDoc2JSJUnitClass.xsl from http://www.sos-berlin.com at 20120927164148
 * \endverbatim
 */
public class SOSSQLPlusJobJUnitTest extends JSJobUtilitiesClass <SOSSQLPlusJobOptions>{
	private final static String		conClassName	= "SOSSQLPlusJobJUnitTest";						//$NON-NLS-1$
	private static Logger			logger			= Logger.getLogger(SOSSQLPlusJobJUnitTest.class);

	protected SOSSQLPlusJobOptions	objOptions		= null;
	private SOSSQLPlusJob			objE			= null;

	public SOSSQLPlusJobJUnitTest() {
		super(new SOSSQLPlusJobOptions());
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		objE = new SOSSQLPlusJob();
		objE.setJSJobUtilites(this);
		objOptions = objE.Options();
		objOptions.registerMessageListener(this);

		JSListenerClass.bolLogDebugInformation = true;
		JSListenerClass.intMaxDebugLevel = 9;
		
	}

	@After
	public void tearDown() throws Exception {
	}

	private HashMap<String, String> createMap() {
		HashMap<String, String> objT = new HashMap<String, String>();
		objT.put("db_user", objOptions.db_user.Value());
		objT.put("hw", "Hello, world!");
		objT.put("count", "4711");
		objT.put("select", "4711");
        objT.put("ConfigurationBaseMonitor.conf_", "check");
 
		return objT;
	}

	final String	conNL	= System.getProperty("line.separator");

    @Ignore    // Test hängt
	// @Test(expected = com.sos.JSHelper.Exceptions.JobSchedulerException.class)
	public void testExecute() throws Exception {

		setOptions();
		String strCmdScript = "Set Echo on" + conNL + //
				"WHENEVER SQLERROR EXIT SQL.SQLCODE" + conNL + //
				"WHENEVER OSERROR EXIT FAILURE" + conNL + //
				"selct &SELECT from dual1;" + conNL + //
				"prompt fertig;" + conNL + //
				"prompt db_user = &DB_USER;" + conNL + //
				"prompt SET varname IS varWert;" + conNL + //
				"exit;" + conNL;
		setScript(strCmdScript);
		objOptions.setAllOptions(createMap());
		objE.Execute();

		String strT = paramMap.get("sql_Error");
		assertEquals("Variable as expected", "0", strT);
	}

	@Test
	public void testExecute2() throws Exception {

		setOptions();
		String strCmdScript = "-- initialize the varaible of out parameters" + conNL + //
				"WHENEVER SQLERROR EXIT SQL.SQLCODE" + conNL + //
				"WHENEVER OSERROR EXIT FAILURE" + conNL + //

				"column end_date new_value BCY_DATE" + conNL + //
				"column period new_value PN_YEAR_PERIOD" + conNL + //
				"column period_prev new_value PN_YEAR_PERIOD_PREV" + conNL + //

				"select '0' as end_date from dual;" + conNL + //
				"prompt SET end_date IS &BCY_DATE;" + conNL + //
				"/" + conNL + //
				"select '0' as period from dual;" + conNL + //
				"prompt SET period IS &PN_YEAR_PERIOD;" + conNL + //
				"/" + conNL + //
				"select '0' as period_prev from dual;" + conNL + //
				"prompt SET period_prev IS &PN_YEAR_PERIOD_PREV;" + conNL + //
				"/" + conNL + //
				"prompt SET end_date IS &BCY_DATE;" + conNL + //
				"exit;" + conNL;
		setScript(strCmdScript);
		objOptions.setAllOptions(createMap());
		objE.Execute();

		String strT = paramMap.get("period");
		assertEquals("Variable as expected", "0", strT);

	}

    //@Ignore    // Test hängt
    @Test
	public void testExecute3() throws Exception {

		setOptions();
		objOptions.ignore_sp2_messages.Value("0734");
		String strCmdScript = "Set Echo on" + conNL + //
				"WHENEVER SQLERROR EXIT SQL.SQLCODE" + conNL + //
				"WHENEVER OSERROR EXIT FAILURE" + conNL + //
				"select &SELECT from dual;" + conNL + //
				"prompt fertig;" + conNL + //
				"prompt set db_user is &DB_USER;" + conNL + //
				"prompt set huhu is &SELECT;" + conNL + //
				"prompt SET varname IS varWert;" + conNL + //
				"exit;" + conNL;
		setScript(strCmdScript);
		objOptions.setAllOptions(createMap());
		objE.Execute();

	}

    //@Ignore    // Test hängt
    @Test
	public void testExecute4() throws Exception {

		setOptions();
		objOptions.ignore_sp2_messages.Value("0734");
		String strCmdScript = "Set Echo on" + conNL + //
				"WHENEVER SQLERROR EXIT SQL.SQLCODE" + conNL + //
				"WHENEVER OSERROR EXIT FAILURE" + conNL + //
				"select &SELECT from dual;" + conNL + //
				"prompt fertig;" + conNL + //
				"prompt set db_user is &DB_USER;" + conNL + //
				"prompt set huhu is &SELECT;" + conNL + //
				"prompt huhu = &SELECT;" + conNL + //
				"prompt SET varname IS varWert;" + conNL + //
				"exit;" + conNL;
		setScript(strCmdScript);
		objOptions.variable_parser_reg_expr.Value("^\\s*([^=]+)\\s*=\\s*(.*)$");
		objOptions.setAllOptions(createMap());
		objE.Execute();

	}

	@Test
	public void testExecute5() throws Exception {

		setOptions();
		objOptions.ignore_ora_messages.Value("00942");
		String strCmdScript = "Set Echo on" + conNL + //
				"WHENEVER SQLERROR EXIT SQL.SQLCODE" + conNL + //
				"WHENEVER OSERROR EXIT FAILURE" + conNL + //
				"select &SELECT from dual1;" + conNL + //
				"exit;" + conNL;
		setScript(strCmdScript);
		objOptions.setAllOptions(createMap());
		objE.Execute();

	}

	@Test
	public void testExecute6() throws Exception {

		setOptions();
		objOptions.ignore_sp2_messages.Value("0734");
		String strCmdScript = "Set Echo on" + conNL + //
				"WHENEVER SQLERROR EXIT SQL.SQLCODE" + conNL + //
				"WHENEVER OSERROR EXIT FAILURE" + conNL + //
				"selct &SELECT from dual;" + conNL + //
				"exit;" + conNL;
		setScript(strCmdScript);
		objOptions.setAllOptions(createMap());
		objE.Execute();

	}

	@Test (expected=com.sos.JSHelper.Exceptions.JobSchedulerException.class)
	public void testExecute7() throws Exception {

		setOptions();
		objOptions.ignore_sp2_messages.Value("0734");
		String strCmdScript = "Set Echo on" + conNL + //
				"WHENEVER SQLERROR EXIT SQL.SQLCODE" + conNL + //
				"WHENEVER OSERROR EXIT FAILURE" + conNL + //
				"select &SELECT from dual;" + conNL + //
				"exit;" + conNL;
		setScript(strCmdScript);
		objOptions.command_script_file.Value("file:" + "abcd.ef");

		objOptions.setAllOptions(createMap());
		objE.Execute();

	}

	@Test (expected=com.sos.JSHelper.Exceptions.JobSchedulerException.class)
	public void testExecute8() throws Exception {

		setOptions();
		String strCmdScript = "Set Echo on" + conNL + //
				"WHENEVER SQLERROR EXIT SQL.SQLCODE" + conNL + //
				"WHENEVER OSERROR EXIT FAILURE" + conNL + //
				"selct &SELECT from dual;" + conNL + //
				"exit;" + conNL;
		objOptions.command_script_file.Value(strCmdScript);
		objOptions.setAllOptions(createMap());
		objE.Execute();

	}

	@Test
	public void testJunitSqlPlusIdentifier() throws Exception {
		String s = "012345678901234567890123456789TooLongForSqlPlus";
		//Testen, ob zu lange abgeschnitten werden.
		s = objE.sqlPlusVariableName(s);
		assertEquals("testJunitSqlPlusIdentifier","01234567890123456789012345678_",s);

		//Testen ob, 30 Zeichen lange Ketten nicht verändert werden.
		s = objE.sqlPlusVariableName(s);
		assertEquals("testJunitSqlPlusIdentifier","01234567890123456789012345678_",s);
	}


    @Ignore    // Test hängt
	// @Test (expected=com.sos.JSHelper.Exceptions.JobSchedulerException.class)
	public void testExecute9() throws Exception {

		setOptions();
		objOptions.Start_Shell_command.Value("none");
		String strCmdScript = "Set Echo on" + conNL + //
				"WHENEVER SQLERROR EXIT SQL.SQLCODE" + conNL + //
				"WHENEVER OSERROR EXIT FAILURE" + conNL + //
				"select &SELECT from dual;" + conNL + //
				"prompt fertig;" + conNL + //
				"prompt set db_user is &DB_USER;" + conNL + //
				"prompt set huhu is &SELECT;" + conNL + //
				"prompt huhu = &SELECT;" + conNL + //
				"prompt SET varname IS varWert;" + conNL + //
				"exit;" + conNL;
		objOptions.command_script_file.Value(strCmdScript);
		objOptions.setAllOptions(createMap());
		objE.Execute();
	}

	private void setOptions() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setOptions";

		//		objOptions.shell_command.Value("C:/oraclexe/app/oracle/product/11.2.0/server/bin/sqlplus.exe");
//		objOptions.shell_command.Value("sqlplus.exe");
		objOptions.CommandLineOptions.Value("-S -L");
		objOptions.db_user.Value("sys");
		objOptions.db_password.Value("scheduler");
		objOptions.db_url.Value("localhost as sysdba");

	} // private void setOptions
	

	private void setScript(final String pstrScript) throws Exception {
		String strSQLFileName = File.createTempFile("SOS", "sql").getAbsolutePath();
		JSTextFile objSQL = new JSTextFile(strSQLFileName);
		objSQL.WriteLine(pstrScript);
		objSQL.close();

		objOptions.command_script_file.Value("file:" + strSQLFileName);

	}

	private HashMap<String, String>	paramMap	= null;

	@Override
	public void setJSParam(final String pstrKey, final String pstrValue) {
		if (paramMap == null) {
			paramMap = new HashMap<String, String>();
		}

		paramMap.put(pstrKey, pstrValue);
		logger.debug(String.format("*mock* set param '%1$s' to value '%2$s'", pstrKey, pstrValue));

	}

} // class SOSSQLPlusJobJUnitTest