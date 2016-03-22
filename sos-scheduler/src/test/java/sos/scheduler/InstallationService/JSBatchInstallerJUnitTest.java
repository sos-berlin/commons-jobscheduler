package sos.scheduler.InstallationService;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;

import org.apache.log4j.Logger;
import org.junit.*;

import static org.junit.Assert.assertEquals;

/** \class JSBatchInstallerJUnitTest - JUnit-Test for
 * "Unattended Batch Installation on remote servers"
 *
 * \brief MainClass to launch JSBatchInstaller as an executable command-line
 * program
 *
 * 
 *
 * see \see C:\Users\KB\Downloads\Preislisten\JSBatchInstaller.xml for (more)
 * details.
 *
 * \verbatim ; mechanicaly created by
 * C:\Users\KB\eclipse\sos.scheduler.xsl\JSJobDoc2JSJUnitClass.xsl from
 * http://www.sos-berlin.com at 20110322142434 \endverbatim */
// oh 07.04.14 deprecated [SP]
@Ignore("Test set to Ignore for later examination")
public class JSBatchInstallerJUnitTest extends JSToolBox {

    @SuppressWarnings("unused")
    private final static String conClassName = "JSBatchInstallerJUnitTest";							//$NON-NLS-1$
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(JSBatchInstallerJUnitTest.class);

    protected JSBatchInstallerOptions objOptions = null;
    private JSBatchInstaller objE = null;

    public JSBatchInstallerJUnitTest() {
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
        objE = new JSBatchInstaller();
        objE.registerMessageListener(this);
        objOptions = objE.Options();
        objOptions.registerMessageListener(this);

        JSListenerClass.bolLogDebugInformation = true;
        JSListenerClass.intMaxDebugLevel = 9;

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testExecute() throws Exception {

        objOptions.installation_definition_file.Value("R:\\nobackup\\junittests\\jsBatchInstaller\\scheduler_agent_installations.xml");
        objE.Execute();
        assertEquals("test", true, true);

        //		assertEquals ("auth_file", objO.auth_file.Value(),"test"); //$NON-NLS-1$
        //		assertEquals ("user", objO.user.Value(),"test"); //$NON-NLS-1$

    }
} // class JSBatchInstallerJUnitTest