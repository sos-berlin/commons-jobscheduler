

package sos.scheduler.file;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import com.sos.i18n.annotation.I18NResourceBundle;
import org.apache.log4j.Logger;
import org.junit.*;

import static org.junit.Assert.assertEquals;

/**
 * \class 		JobSchedulerFolderTreeOptionsJUnitTest - check wether a file exist
 *
 * \brief 
 *
 *

 *
 * see \see J:\E\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\JobSchedulerFolderTree.xml for (more) details.
 * 
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse\sos.scheduler.xsl\JSJobDoc2JSJUnitOptionSuperClass.xsl from http://www.sos-berlin.com at 20110805104851 
 * \endverbatim
 *
 * \section TestData Eine Hilfe zum Erzeugen einer HashMap mit Testdaten
 *
 * Die folgenden Methode kann verwendet werden, um für einen Test eine HashMap
 * mit sinnvollen Werten für die einzelnen Optionen zu erzeugen.
 *
 * \verbatim
 private HashMap <String, String> SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM) {
	pobjHM.put ("		JobSchedulerFolderTreeOptionsJUnitTest.auth_file", "test");  // This parameter specifies the path and name of a user's pr
		return pobjHM;
  }  //  private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim
 */
@I18NResourceBundle(baseName = "com.sos.scheduler.messages", defaultLocale = "en")
public class JobSchedulerFolderTreeOptionsJUnitTest extends  JSToolBox {
	private final String					conClassName						= "JobSchedulerFolderTreeOptionsJUnitTest"; //$NON-NLS-1$
		@SuppressWarnings("unused") //$NON-NLS-1$
	private static Logger		logger			= Logger.getLogger(JobSchedulerFolderTreeOptionsJUnitTest.class);
	private JobSchedulerFolderTree objE = null;
	private final String	conSVNVersion	= "$Id$";

	protected JobSchedulerFolderTreeOptions	objOptions			= null;

	public JobSchedulerFolderTreeOptionsJUnitTest() {
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
		objE = new JobSchedulerFolderTree();
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
 * \brief testfile_path : This parameter is used alternatively to the parame
 * 
 * \details
 * This parameter is used alternatively to the parameter file_spec to specify a single file for transfer. When receiving files the following applies: This parameter accepts the absolute name and path of file at the FTP/SFTP server that should be transferred. The file name has to include both name and path of the file at the FTP/SFTP server. The file will be stored unter its name in the directory that is specified by the parameter local_dir. The following parameters are ignored should this parameter be used: file_spec and remote_dir. When sending files the following applies: This parameter accepts the absolute name and path of file that should be transferred. An absolute path has to be specified. The file will be stored under its name in the directory at the FTP/SFTP server that has been specified by the parameter remote_dir. The following parameters are ignored should this parameter be used: file_spec and local_dir.
 *
 */
    @Test
    public void testfile_path() {  // SOSOptionFileName
    	objOptions.file_path.Value("++----++");
    	assertEquals ("This parameter is used alternatively to the parame", objOptions.file_path.Value(),"++----++");
    	
    }

                
        
} // public class JobSchedulerFolderTreeOptionsJUnitTest