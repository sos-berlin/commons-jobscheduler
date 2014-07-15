package sos.scheduler.xsl;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.DataElements.JSDataElementDateISO;
import com.sos.JSHelper.Listener.JSListenerClass;
import com.sos.JSHelper.io.Files.JSXMLFile;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.junit.*;

import java.io.File;

/**
 * \class 		JobSchedulerXslTransformationJUnitTest - JUnit-Test for "JobSchedulerXslTransform"
 *
 * \brief MainClass to launch JobSchedulerXslTransform as an executable command-line program
 *

 *
 * see \see J:\E\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\JobSchedulerXslTransform.xml for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse\sos.scheduler.xsl\JSJobDoc2JSJUnitClass.xsl from http://www.sos-berlin.com at 20110815114248
 * \endverbatim
 */
public class JobSchedulerXslTransformJUnitTest extends JSToolBox {
	@SuppressWarnings("unused")
	private final static String					conClassName	= "JobSchedulerXslTransformationJUnitTest";
	@SuppressWarnings("unused")
	private static Logger						logger			= Logger.getLogger(JobSchedulerXslTransformJUnitTest.class);

	protected JobSchedulerXslTransformOptions	objOptions		= null;
	private JobSchedulerXslTransform			objE			= null;
	String										strBaseFolder	= "R:/backup/sos/";
	String										strBaseDirName	= strBaseFolder + "java/development/com.sos.scheduler/src/sos/scheduler/jobdoc/";

	public JobSchedulerXslTransformJUnitTest() {
		BasicConfigurator.configure();
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		System.setProperty("user.dir", strBaseDirName);
		objE = new JobSchedulerXslTransform();
		objOptions = objE.Options();

		JSListenerClass.bolLogDebugInformation = true;
		JSListenerClass.intMaxDebugLevel = 9;
		logger.debug(System.getProperty("java.class.path"));

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testExecute() throws Exception {
		String strFileName = "JobSchedulerLaunchAndObserve";
		objOptions.FileName.Value(strBaseDirName + strFileName + ".xml");
		objOptions.XslFileName.Value(strBaseDirName + "xsl/ResolveXIncludes.xsl");
		File objTemp = File.createTempFile("sos", ".tmp");
		objTemp.deleteOnExit();
		objOptions.OutputFileName.Value(objTemp.getAbsolutePath());

		try {
			objE.Execute();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		logger.debug(new JSXMLFile(objTemp.getAbsolutePath()).getContent());
	}

	@Test
	public void testExecuteWOXsl() throws Exception {
		String strFileName = "JobSchedulerLaunchAndObserve";
		objOptions.FileName.Value(strBaseDirName + strFileName + ".xml");
		File objTemp = File.createTempFile("sos", ".tmp");
		objTemp.deleteOnExit();
		objOptions.OutputFileName.Value(objTemp.getAbsolutePath());

		try {
			objE.Execute();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		logger.debug(new JSXMLFile(objTemp.getAbsolutePath()).getContent());
	}

	@Test
	public void testCopy() throws Exception {
		String strFileName = "JobSchedulerLaunchAndObserve";
		objOptions.FileName.Value(strBaseDirName + strFileName + ".xml");
		File objTemp = File.createTempFile("sos", ".tmp");
		objOptions.OutputFileName.Value(objTemp.getAbsolutePath());

		try {
			objE.Execute();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		logger.debug(new JSXMLFile(objTemp.getAbsolutePath()).getContent());
	}

	//! [testExecute2MediaWiki]
	@Test
	public void testExecute2MediaWiki() throws Exception {

		JSDataElementDateISO objISODate = new JSDataElementDateISO();
		System.out.println("sos.timestamp = " + objISODate.Now());

		String strFileName = "JobSchedulerPLSQLJob";

		objOptions.FileName.Value(strBaseDirName + strFileName + ".xml");
		objOptions.XslFileName.Value(strBaseDirName + "xsl/CreateMediaWikiFromSOSDoc.xsl");
		String strOutputFileName = objOptions.TempDirName() + strFileName + ".mediaWiki";
		logger.info(strOutputFileName);
		objOptions.OutputFileName.Value(strOutputFileName);

		try {
			objE.Execute();
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}

		logger.debug(new JSXMLFile(strOutputFileName).getContent());
	}

	//! [testExecute2MediaWiki]

	//! [testResolveXInclude]
	@Test
	public void testResolveXInclude() throws Exception {

		JSDataElementDateISO objISODate = new JSDataElementDateISO();
		//		objISODate.Value(objISODate.Now());
		System.out.println("sos.timestamp = " + objISODate.Now());

		logger.debug(System.getProperty("java.class.path"));

		String strFileName = "JobSchedulerLaunchAndObserve";

		objOptions.FileName.Value(strBaseDirName + strFileName + ".xml");

		JSXMLFile objXF = new JSXMLFile(strBaseDirName + strFileName + ".xml");
		File objTemp = File.createTempFile("sos", ".tmp");
		objTemp.deleteOnExit();
		objXF.writeDocument(objTemp.getAbsolutePath());
		logger.debug(new JSXMLFile(objTemp.getAbsolutePath()).getContent());
	}
	//! [testResolveXInclude]

} // class JobSchedulerXslTransformationJUnitTest