

package com.sos.scheduler.converter.graphviz;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import org.apache.log4j.Logger;
import org.junit.*;

import static org.junit.Assert.assertEquals;

/**
 * \class 		JSObjects2GraphvizOptionsJUnitTest - JSObjects2Graphviz
 *
 * \brief
 *
 *

 *
 * see \see C:\Users\KB\AppData\Local\Temp\scheduler_editor-2781494595910967227.html for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\sos-berlin.com\jobscheduler\scheduler\config\JOETemplates\java\xsl\JSJobDoc2JSJUnitOptionSuperClass.xsl from http://www.sos-berlin.com at 20121108150924
 * \endverbatim
 *
 * \section TestData Eine Hilfe zum Erzeugen einer HashMap mit Testdaten
 *
 * Die folgenden Methode kann verwendet werden, um für einen Test eine HashMap
 * mit sinnvollen Werten für die einzelnen Optionen zu erzeugen.
 *
 * \verbatim
 private HashMap <String, String> SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM) {
	pobjHM.put ("		JSObjects2GraphvizOptionsJUnitTest.auth_file", "test");  // This parameter specifies the path and name of a user's pr
		return pobjHM;
  }  //  private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim
 */
public class JSObjects2GraphvizOptionsJUnitTest extends  JSToolBox {
	private final String					conClassName						= "JSObjects2GraphvizOptionsJUnitTest"; //$NON-NLS-1$
		@SuppressWarnings("unused") 
	private static Logger		logger			= Logger.getLogger(JSObjects2GraphvizOptionsJUnitTest.class);
	private JSObjects2Graphviz objE = null;

	protected JSObjects2GraphvizOptions	objOptions			= null;

	public JSObjects2GraphvizOptionsJUnitTest() {
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
		objE = new JSObjects2Graphviz();
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
 * \brief testlive_folder_name :
 *
 * \details
 *
 *
 */
    @Test
    public void testlive_folder_name() {  // SOSOptionString
    	 objOptions.live_folder_name.Value("++----++");
    	 //Value Method of SOSOptionFolderName adds a "/" if missing, so it has to be added to the expected Result also
    	 assertEquals ("","++----++" + "/", objOptions.live_folder_name.Value());

    }



/**
 * \brief testoutput_folder_name :
 *
 * \details
 *
 *
 */
    @Test
    public void testoutput_folder_name() {  // SOSOptionString
    	 objOptions.output_folder_name.Value("++----++");
    	 //Value Method of SOSOptionFolderName adds a "/" if missing, so it has to be added to the expected Result also
    	 assertEquals ("","++----++" + "/", objOptions.output_folder_name.Value());

    }



} // public class JSObjects2GraphvizOptionsJUnitTest