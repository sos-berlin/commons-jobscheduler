

package com.sos.scheduler.converter.graphviz;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import org.apache.log4j.Logger;
import org.junit.*;

/**
 * \class 		JSObjects2GraphvizJUnitTest - JUnit-Test for "JSObjects2Graphviz"
 *
 * \brief MainClass to launch JSObjects2Graphviz as an executable command-line program
 *

 *
 * see \see C:\Users\KB\AppData\Local\Temp\scheduler_editor-2781494595910967227.html for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\sos-berlin.com\jobscheduler\scheduler\config\JOETemplates\java\xsl\JSJobDoc2JSJUnitClass.xsl from http://www.sos-berlin.com at 20121108150924
 * \endverbatim
 */
public class JSObjects2GraphvizJUnitTest extends JSToolBox {
	@SuppressWarnings("unused")
	private final static String					conClassName						= "JSObjects2GraphvizJUnitTest"; //$NON-NLS-1$
	@SuppressWarnings("unused")
	private static Logger		logger			= Logger.getLogger(JSObjects2GraphvizJUnitTest.class);

	protected JSObjects2GraphvizOptions	objOptions			= null;
	private JSObjects2Graphviz objE = null;


	public JSObjects2GraphvizJUnitTest() {
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

	private final String				conLiveFolderLocation		= "/8of9_buildjars_4210/config/live/";
	// see http://blogs.msdn.com/b/ie/archive/2006/12/06/file-uris-in-windows.aspx for the 3 ///
//	private final String				conLiveLocalFolderLocation	= "file:///Z:" + conLiveFolderLocation;
	private final String				conLiveLocalFolderLocation	= "Z:" + conLiveFolderLocation;

	@Test
	public void testExecute() throws Exception {

		objOptions.output_folder_name.Value("c:/temp");
		objOptions.live_folder_name.Value(conLiveLocalFolderLocation);
		objE.Execute();

	}
}  // class JSObjects2GraphvizJUnitTest