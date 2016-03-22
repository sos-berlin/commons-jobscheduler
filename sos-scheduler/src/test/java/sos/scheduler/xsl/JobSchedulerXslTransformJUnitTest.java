package sos.scheduler.xsl;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.DataElements.JSDataElementDateISO;
import com.sos.JSHelper.Listener.JSListenerClass;
import com.sos.JSHelper.io.Files.JSXMLFile;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.junit.*;

import java.io.File;

@Ignore("Class has to be reviewed")
public class JobSchedulerXslTransformJUnitTest extends JSToolBox {

    protected JobSchedulerXslTransformOptions objOptions = null;
    private static final Logger LOGGER = Logger.getLogger(JobSchedulerXslTransformJUnitTest.class);
    private JobSchedulerXslTransform objE = null;
    String strBaseFolder = "R:/backup/sos/";
    String strBaseDirName = strBaseFolder + "java/development/com.sos.scheduler/src/sos/scheduler/jobdoc/";

    public JobSchedulerXslTransformJUnitTest() {
        BasicConfigurator.configure();
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("user.dir", strBaseDirName);
        objE = new JobSchedulerXslTransform();
        objOptions = objE.Options();
        JSListenerClass.bolLogDebugInformation = true;
        JSListenerClass.intMaxDebugLevel = 9;
        LOGGER.debug(System.getProperty("java.class.path"));
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
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.debug(new JSXMLFile(objTemp.getAbsolutePath()).getContent());
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
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.debug(new JSXMLFile(objTemp.getAbsolutePath()).getContent());
    }

    @Test
    public void testCopy() throws Exception {
        String strFileName = "JobSchedulerLaunchAndObserve";
        objOptions.FileName.Value(strBaseDirName + strFileName + ".xml");
        File objTemp = File.createTempFile("sos", ".tmp");
        objOptions.OutputFileName.Value(objTemp.getAbsolutePath());
        try {
            objE.Execute();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.debug(new JSXMLFile(objTemp.getAbsolutePath()).getContent());
    }

    @Test
    public void testExecute2MediaWiki() throws Exception {
        JSDataElementDateISO objISODate = new JSDataElementDateISO();
        LOGGER.info("sos.timestamp = " + objISODate.Now());
        String strFileName = "JobSchedulerPLSQLJob";
        objOptions.FileName.Value(strBaseDirName + strFileName + ".xml");
        objOptions.XslFileName.Value(strBaseDirName + "xsl/CreateMediaWikiFromSOSDoc.xsl");
        String strOutputFileName = objOptions.TempDirName() + strFileName + ".mediaWiki";
        LOGGER.info(strOutputFileName);
        objOptions.OutputFileName.Value(strOutputFileName);
        try {
            objE.Execute();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.debug(new JSXMLFile(strOutputFileName).getContent());
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testResolveXInclude() throws Exception {
        JSDataElementDateISO objISODate = new JSDataElementDateISO();
        LOGGER.info("sos.timestamp = " + objISODate.Now());
        LOGGER.debug(System.getProperty("java.class.path"));
        String strFileName = "JobSchedulerLaunchAndObserve";
        objOptions.FileName.Value(strBaseDirName + strFileName + ".xml");
        JSXMLFile objXF = new JSXMLFile(strBaseDirName + strFileName + ".xml");
        File objTemp = File.createTempFile("sos", ".tmp");
        objTemp.deleteOnExit();
        objXF.writeDocument(objTemp.getAbsolutePath());
        LOGGER.debug(new JSXMLFile(objTemp.getAbsolutePath()).getContent());
    }

}