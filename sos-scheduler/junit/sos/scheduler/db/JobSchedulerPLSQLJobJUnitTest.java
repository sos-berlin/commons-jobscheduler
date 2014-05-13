

package sos.scheduler.db;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import com.sos.JSHelper.Logging.Log4JHelper;

/**
 * \class 		JobSchedulerPLSQLJobJUnitTest - JUnit-Test for "Launch Database Statement"
 *
 * \brief MainClass to launch JobSchedulerPLSQLJob as an executable command-line program
 *

 *
 * see \see R:\backup\sos\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\JobSchedulerPLSQLJob.xml for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse\xsl\JSJobDoc2JSJUnitClass.xsl from http://www.sos-berlin.com at 20120905153655 
 * \endverbatim
 */
public class JobSchedulerPLSQLJobJUnitTest extends JSToolBox {
	@SuppressWarnings("unused")	 
	private final static String					conClassName						= "JobSchedulerPLSQLJobJUnitTest"; //$NON-NLS-1$
	@SuppressWarnings("unused")	 
	private static Logger		logger			= Logger.getLogger(JobSchedulerPLSQLJobJUnitTest.class);
	@SuppressWarnings("unused")	 
	private static Log4JHelper	objLogger		= null;

	protected JobSchedulerPLSQLJobOptions	objOptions			= null;
	private JobSchedulerPLSQLJob objE = null;
	
	
	public JobSchedulerPLSQLJobJUnitTest() {
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
		objLogger = new Log4JHelper("./log4j.properties"); //$NON-NLS-1$
		objE = new JobSchedulerPLSQLJob();
		objE.registerMessageListener(this);
		objOptions = objE.Options();
		objOptions.registerMessageListener(this);
		
		JSListenerClass.bolLogDebugInformation = true;
		JSListenerClass.intMaxDebugLevel = 9;
		
	}
	
	private HashMap<String, String> createMap() {
		HashMap<String, String> objT = new HashMap<String, String>();
		objT.put("db_user", objOptions.db_user.Value());
		objT.put("hw", "Hello, world!");
		objT.put("count", "4711");
		objT.put("select", "4711");

		return objT;
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testExecute() throws Exception {
		
		objOptions.db_url.Value("jdbc:oracle:thin:@8of9.sos:1521:test");
		objOptions.db_user.Value("scheduler");
		objOptions.db_password.Value("scheduler");
		String strSql = "declare " + "\n"
		+" howmany NUMBER;" + "\n"
+ "      p_id varchar2(20) := null; " + "\n" 
+ "	  result varchar2(40) := null; "  + "\n"
+ "   v_line scheduler_variables%rowtype;" + "\n"
+ "begin "  + "\n"
+ "dbms_output.put_line('set variable1 is value1');" + "\n"
+ "    p_id := '12345'; " + "\n" 
+ " select count(*) into howmany from scheduler_variables;" + "\n"
+ "dbms_output.put_line('This schema owns ' || howmany || ' tables.');" + "\n"
+ "dbms_output.put_line('set howmany is ' || howmany);" + "\n"
+ "dbms_output.put_line('set variable1 is ' || p_id);" + "\n"
+ "dbms_output.put_line('set variable2 is value2');" + "\n"
+ "end;"  + "\n";
		
//		+ "	prompt 'return_var=' || result; "  + "\n";

		objOptions.command.Value(strSql);
//		objOptions.command.Value("c:/temp/test1.sql");
//		objOptions.command.Value("c:/temp/getMetaData.plsql");
		objOptions.setAllOptions(createMap());
		objE.Execute();
		
//		assertEquals ("auth_file", objOptions.auth_file.Value(),"test"); //$NON-NLS-1$
//		assertEquals ("user", objOptions.user.Value(),"test"); //$NON-NLS-1$
		System.out.println("objE.getOutput()" + objE.getOutput());
		System.out.println("objE.getSqlError()" + objE.getSqlError());

	}
	
	@Test
	public void testExecute2() throws Exception {
		
		objOptions.db_url.Value("jdbc:oracle:thin:@8of9.sos:1521:test");
		objOptions.db_user.Value("scheduler");
		objOptions.db_password.Value("scheduler");
		String strSql = "declare " + "\n"
		+" howmany NUMBER;" + "\n"
+ "      p_id varchar2(20) := null; " + "\n" 
+ "	  result varchar2(40) := null; "  + "\n"
+ "   v_line scheduler_variables%rowtype;" + "\n"
+ "begin "  + "\n"
+ "dbms_output.put_line('set variable1=value1');" + "\n"
+ "    p_id := '12345'; " + "\n" 
+ " select count(*) into howmany from scheduler_variables;" + "\n"
+ "dbms_output.put_line('This schema owns ' || howmany || ' tables.');" + "\n"
+ "dbms_output.put_line('set howmany=' || howmany);" + "\n"
+ "dbms_output.put_line('set variable1=' || p_id);" + "\n"
+ "dbms_output.put_line('set variable.2 = value2');" + "\n"
+ "dbms_output.put_line('variable_2 = value2');" + "\n"
+ "end;"  + "\n";
		
//		+ "	prompt 'return_var=' || result; "  + "\n";

		objOptions.command.Value(strSql);
		objOptions.variable_parser_reg_expr.Value("^.*?([^= ]+?)\\s*=\\s*(.*)$");
//		objOptions.command.Value("c:/temp/test1.sql");
//		objOptions.command.Value("c:/temp/getMetaData.plsql");
		objOptions.setAllOptions(createMap());
		objE.Execute();
		
//		assertEquals ("auth_file", objOptions.auth_file.Value(),"test"); //$NON-NLS-1$
//		assertEquals ("user", objOptions.user.Value(),"test"); //$NON-NLS-1$
		System.out.println("objE.getOutput()" + objE.getOutput());
		System.out.println("objE.getSqlError()" + objE.getSqlError());

	}
	
	@Test
	public void testStringFormat () {
	    
	    String strT = String.format("%%SCHEDULER_PARAM_%1$s%%", "VarName");
	    System.out.println(strT);
	}
}  // class JobSchedulerPLSQLJobJUnitTest