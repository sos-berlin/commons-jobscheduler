

package sos.scheduler.managed.db;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;

import org.apache.log4j.Logger;
import org.junit.*;

import static org.junit.Assert.*;

/**
 * \class 		JobSchedulerManagedDBReportJobOptionsJUnitTest - Launch Database Report
 *
 * \brief 
 *
 *

 *
 * see \see R:\backup\sos\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\JobSchedulerManagedDBReportJob.xml for (more) details.
 * 
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse\xsl\JSJobDoc2JSJUnitOptionSuperClass.xsl from http://www.sos-berlin.com at 20120830214631 
 * \endverbatim
 *
 * \section TestData Eine Hilfe zum Erzeugen einer HashMap mit Testdaten
 *
 * Die folgenden Methode kann verwendet werden, um für einen Test eine HashMap
 * mit sinnvollen Werten für die einzelnen Optionen zu erzeugen.
 *
 * \verbatim
 private HashMap <String, String> SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM) {
	pobjHM.put ("		JobSchedulerManagedDBReportJobOptionsJUnitTest.auth_file", "test");  // This parameter specifies the path and name of a user's pr
		return pobjHM;
  }  //  private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim
 */
public class JobSchedulerManagedDBReportJobOptionsJUnitTest extends  JSToolBox {
	private final String					conClassName						= "JobSchedulerManagedDBReportJobOptionsJUnitTest"; //$NON-NLS-1$
		@SuppressWarnings("unused") 
	private static Logger		logger			= Logger.getLogger(JobSchedulerManagedDBReportJobOptionsJUnitTest.class);
	private JobSchedulerManagedDBReportJob objE = null;

	protected JobSchedulerManagedDBReportJobOptions	objOptions			= null;

	public JobSchedulerManagedDBReportJobOptionsJUnitTest() {
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
		objE = new JobSchedulerManagedDBReportJob();
		objE.registerMessageListener(this);
		objOptions = objE.Options();
		objOptions.registerMessageListener(this);
		
		JSListenerClass.bolLogDebugInformation = true;
		JSListenerClass.intMaxDebugLevel = 9;
	}

	@After
	public void tearDown() throws Exception {
	}


		

/**
 * \brief testAdjust_column_names : Character conversion for column names
 * 
 * \details
 * 
 *
 */
    @Test
    public void testAdjust_column_names() {  // SOSOptionBoolean
    	 objOptions.Adjust_column_names.Value("true");
    	 assertTrue ("Character conversion for column names", objOptions.Adjust_column_names.value());
    	 objOptions.Adjust_column_names.Value("false");
    	 assertFalse ("Character conversion for column names", objOptions.Adjust_column_names.value());
    }

                

/**
 * \brief testColumn_names_case_sensitivity : Let Column names as is
 * 
 * \details
 * 
 *
 */
    @Test
    public void testColumn_names_case_sensitivity() {  // SOSOptionBoolean
    	 objOptions.Column_names_case_sensitivity.Value("true");
    	 assertTrue ("Let Column names as is", objOptions.Column_names_case_sensitivity.value());
    	 objOptions.Column_names_case_sensitivity.Value("false");
    	 assertFalse ("Let Column names as is", objOptions.Column_names_case_sensitivity.value());
    	
    }

                

/**
 * \brief testcommand : Database Commands for the Job
 * 
 * \details
 * Database Commands for the Job. It is possible to define more than one instruction in the COMMAND field. Such instructions are then carried out in the order in which they are written and must be separated by a semi colon and a subsequent new line. Parameters can be replaced in database commands. This is done by the addition of a variable in the form §{param} at any given point in a command. This variable is then given the value of the instruction parameter with the name [param] before execution.
 *
 */
    @Test
    public void testcommand() {  // SOSOptionString
    	 objOptions.command.Value("++----++");
    	 assertEquals ("Database Commands for the Job", objOptions.command.Value(),"++----++");
    	
    }

                

/**
 * \brief testdatabase_connection : database connection from table SCHEDULER_MANAGED_CONNECTIONS
 * 
 * \details
 * Name of the selected database connection in table SCHEDULER_MANAGED_CONNECTIONS.
 *
 */
    @Test
    public void testdatabase_connection() {  // SOSOptionString
    	 objOptions.database_connection.Value("++----++");
    	 assertEquals ("database connection from table SCHEDULER_MANAGED_CONNECTIONS", objOptions.database_connection.Value(),"++----++");
    	
    }

                

/**
 * \brief testdb_class : SOS Connection class
 * 
 * \details
 * Name of the SOS Connection class for the database: SOSMSSQLConnection SOSMySQLConnection SOSOracleConnection SOSPgSQLConnection SOSFbSQLConnection SOSDB2Connection
 *
 */
    @Test
    public void testdb_class() {  // SOSOptionString
    	 objOptions.db_class.Value("++----++");
    	 assertEquals ("SOS Connection class", objOptions.db_class.Value(),"++----++");
    	
    }

                

/**
 * \brief testdb_driver : Name of the jd
 * 
 * \details
 * Name of the jdbc driver Class
 *
 */
    @Test
    public void testdb_driver() {  // SOSOptionString
    	 objOptions.db_driver.Value("++----++");
    	 assertEquals ("Name of the jd", objOptions.db_driver.Value(),"++----++");
    	
    }

                

/**
 * \brief testdb_password : database password
 * 
 * \details
 * database password
 *
 */
    @Test
    public void testdb_password() {  // SOSOptionPassword
    	objOptions.db_password.Value("++----++");
    	assertEquals ("database password", objOptions.db_password.Value(),"++----++");
    	
    }

                

/**
 * \brief testdb_url : jdbc url
 * 
 * \details
 * jdbc url
 *
 */
    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testdb_url() {  // SOSOptionUrl
    	objOptions.db_url.Value("++----++");
    	assertEquals ("jdbc url", objOptions.db_url.Value(),"++----++");
    }

                

/**
 * \brief testdb_user : database user
 * 
 * \details
 * database user
 *
 */
    @Test
    public void testdb_user() {  // SOSOptionUserName
    	objOptions.db_user.Value("++----++");
    	assertEquals ("database user", objOptions.db_user.Value(),"++----++");
    	
    }

                

/**
 * \brief testexec_returns_resultset : If stored proc
 * 
 * \details
 * If stored procedures are called which return a result set, this needs to be set to true in order to run the stored procedure as a query. This does not work with the SQL Server 2000 and 2005 jdbc drivers.
 *
 */
    @Test
    public void testexec_returns_resultset() {  // SOSOptionBoolean
    	 objOptions.exec_returns_resultset.Value("true");
    	 assertTrue ("If stored proc", objOptions.exec_returns_resultset.value());
    	 objOptions.exec_returns_resultset.Value("false");
    	 assertFalse ("If stored proc", objOptions.exec_returns_resultset.value());
    	
    }

                

/**
 * \brief testMax_No_Of_Records_To_Process : Max Number of lines in the result table to process
 * 
 * \details
 * "-1" means no restriction.
 *
 */
    @Test
    public void testMax_No_Of_Records_To_Process() {  // SOSOptionInteger
    	 objOptions.Max_No_Of_Records_To_Process.Value("12345");
    	 assertEquals ("Max Number of lines in the result table to process", objOptions.Max_No_Of_Records_To_Process.Value(),"12345");
    	 assertEquals ("Max Number of lines in the result table to process", objOptions.Max_No_Of_Records_To_Process.value(),12345);
    	 objOptions.Max_No_Of_Records_To_Process.value(12345);
    	 assertEquals ("Max Number of lines in the result table to process", objOptions.Max_No_Of_Records_To_Process.Value(),"12345");
    	 assertEquals ("Max Number of lines in the result table to process", objOptions.Max_No_Of_Records_To_Process.value(),12345);
    	
    }

                

/**
 * \brief testscheduler_order_report_asbody : This setting d
 * 
 * \details
 * This setting determines whether the report should be sent as an attachment (default) or as the content of an e-mail.
 *
 */
    @Test
    public void testscheduler_order_report_asbody() {  // SOSOptionBoolean
    	 objOptions.scheduler_order_report_asbody.Value("true");
    	 assertTrue ("This setting d", objOptions.scheduler_order_report_asbody.value());
    	 objOptions.scheduler_order_report_asbody.Value("false");
    	 assertFalse ("This setting d", objOptions.scheduler_order_report_asbody.value());
    	
    }

                

/**
 * \brief testscheduler_order_report_body : 
 * 
 * \details
 * This parameter specifies the report layout in either HTML or plain text formats. The following placeholders can be used in the layout, each within square [ ] brackets, e.g. [date]: [date] - the current date [datetime] - the current date and time [orderid] - the order ID [jobname] - the job name [taskid] - the task ID [sql] - the SQL query used to create the report [xml] - untransformed XML generated from the query results Note that the content of this parameter will be ignored, should the scheduler_order_report_asbody parameter be set to false.
 *
 */
    @Test
    public void testscheduler_order_report_body() {  // SOSOptionString
    	 objOptions.scheduler_order_report_body.Value("++----++");
    	 assertEquals ("", objOptions.scheduler_order_report_body.Value(),"++----++");
    	
    }

                

/**
 * \brief testscheduler_order_report_filename : 
 * 
 * \details
 * The name to be used for the attachment file when the report is to be sent as an e-mail. This name will also be used for the permanently saved copy of the report, should a path have been specified using the scheduler_order_report_path parameter. The following placeholders can be used, each placeholder being inserted in square [ ] brackets - e.g. [date]: [date] - the current date [orderid] - the order ID [jobname] - the job name [taskid] - the task ID
 *
 */
    @Test
    public void testscheduler_order_report_filename() {  // SOSOptionOutFileName
    	objOptions.scheduler_order_report_filename.Value("++report_[date]_[taskid].xml++");
    	assertEquals ("", objOptions.scheduler_order_report_filename.Value(),"++report_[date]_[taskid].xml++");
    	
    }

                

/**
 * \brief testscheduler_order_report_mailbcc : One or more e-
 * 
 * \details
 * One or more e-mail addresses of recipients of blind carbon copies of a report. Multiple addresses are to be separated by commas.
 *
 */
    @Test
    public void testscheduler_order_report_mailbcc() {  // SOSOptionString
    	 objOptions.scheduler_order_report_mailbcc.Value("++----++");
    	 assertEquals ("One or more e-", objOptions.scheduler_order_report_mailbcc.Value(),"++----++");
    	
    }

                

/**
 * \brief testscheduler_order_report_mailcc : One or more e-
 * 
 * \details
 * One or more e-mail addresses of recipients of carbon copies of a report. Multiple addresses are to be separated by commas.
 *
 */
    @Test
    public void testscheduler_order_report_mailcc() {  // SOSOptionString
    	 objOptions.scheduler_order_report_mailcc.Value("++----++");
    	 assertEquals ("One or more e-", objOptions.scheduler_order_report_mailcc.Value(),"++----++");
    	
    }

                

/**
 * \brief testscheduler_order_report_mailto : report_mailto: recipients of a report
 * 
 * \details
 * One or more e-mail addresses of recipients of a report. Multiple addresses are to be separated by commas. Should this setting be left empty, then no reports will be sent by e-mail. The reports will however be generated and saved.
 *
 */
    @Test
    public void testscheduler_order_report_mailto() {  // SOSOptionString
    	 objOptions.scheduler_order_report_mailto.Value("++----++");
    	 assertEquals ("report_mailto: recipients of a report", objOptions.scheduler_order_report_mailto.Value(),"++----++");
    	
    }

                

/**
 * \brief testscheduler_order_report_path : 
 * 
 * \details
 * The path to the directory in which a report is saved. The name of the report file is not to be specified here. The following placeholders can be used in the path, inserted in square [ ] brackets - e.g.  [date]: [date] - the current date [orderid] - the order ID [jobname] - the job name [taskid] - the task ID
 *
 */
    @Test
    public void testscheduler_order_report_path() {  // SOSOptionString
    	 objOptions.scheduler_order_report_path.Value("++----++");
    	 assertEquals ("", objOptions.scheduler_order_report_path.Value(),"++----++");
    	
    }

                

/**
 * \brief testscheduler_order_report_send_if_no_result : This setting s
 * 
 * \details
 * This setting specifies whether a report should be sent when the SQL query does not return a result.
 *
 */
    @Test
    public void testscheduler_order_report_send_if_no_result() {  // SOSOptionBoolean
    	 objOptions.scheduler_order_report_send_if_no_result.Value("true");
    	 assertTrue ("This setting s", objOptions.scheduler_order_report_send_if_no_result.value());
    	 objOptions.scheduler_order_report_send_if_no_result.Value("false");
    	 assertFalse ("This setting s", objOptions.scheduler_order_report_send_if_no_result.value());
    	
    }

                

/**
 * \brief testscheduler_order_report_send_if_result : This setting s
 * 
 * \details
 * This setting specifies whether a report should be sent when an SQL query returns a result.
 *
 */
    @Test
    public void testscheduler_order_report_send_if_result() {  // SOSOptionBoolean
    	 objOptions.scheduler_order_report_send_if_result.Value("true");
    	 assertTrue ("This setting s", objOptions.scheduler_order_report_send_if_result.value());
    	 objOptions.scheduler_order_report_send_if_result.Value("false");
    	 assertFalse ("This setting s", objOptions.scheduler_order_report_send_if_result.value());
    	
    }

                

/**
 * \brief testscheduler_order_report_stylesheet : 
 * 
 * \details
 * The style sheet to be used to transform a report from XML format. Both the path and name of the style sheet are optional in this parameter. Should no individual style sheet be available, then the default style sheet can be used. The path can be specified relative to the JobScheduler installation directory.
 *
 */
    @Test
    public void testscheduler_order_report_stylesheet() {  // SOSOptionString
    	 objOptions.scheduler_order_report_stylesheet.Value("++config/default.xslt++");
    	 assertEquals ("", objOptions.scheduler_order_report_stylesheet.Value(),"++config/default.xslt++");
    	
    }

                

/**
 * \brief testscheduler_order_report_subject : report_subject
 * 
 * \details
 * The subject of e-mails to be used to send reports. This field is mandatory should reports be sent by e-mail. The following placeholders can be used, surrounded by square [ ] brackets - e.g. [date]: [date] - the current date [datetime] - the current date and time [orderid] - the order ID [jobname] - the job name [taskid] - the task ID
 *
 */
    @Test
    public void testscheduler_order_report_subject() {  // SOSOptionString
    	 objOptions.scheduler_order_report_subject.Value("++----++");
    	 assertEquals ("report_subject", objOptions.scheduler_order_report_subject.Value(),"++----++");
    	
    }

                
        
} // public class JobSchedulerManagedDBReportJobOptionsJUnitTest