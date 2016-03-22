package com.sos.localization;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;

import org.apache.log4j.Logger;
import org.junit.*;

/** \class PropertyFactoryJUnitTest - JUnit-Test for
 * "PropertyFactora - a Factoroy to maintain I18N Files"
 *
 * \brief MainClass to launch PropertyFactory as an executable command-line
 * program
 *
 * 
 *
 * see \see
 * C:\Users\KB\AppData\Local\Temp\scheduler_editor-297718331111000308.html for
 * (more) details.
 *
 * \verbatim ; mechanicaly created by
 * com/sos/resources/xsl/jobdoc/sourcegenerator/java/JSJobDoc2JSJUnitClass.xsl
 * from http://www.sos-berlin.com at 20141009200110 \endverbatim */
public class PropertyFactoryJUnitTest extends JSToolBox {

    @SuppressWarnings("unused")//$NON-NLS-1$
    private final static String conClassName = "PropertyFactoryJUnitTest"; //$NON-NLS-1$
    @SuppressWarnings("unused")//$NON-NLS-1$
    private static Logger logger = Logger.getLogger(PropertyFactoryJUnitTest.class);

    protected PropertyFactoryOptions objOptions = null;
    private PropertyFactory objE = null;

    public PropertyFactoryJUnitTest() {
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
        objE = new PropertyFactory();
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
    @Ignore("Test set to Ignore for later examination")
    public void testExecute() throws Exception {

        objE.Execute();

        //		assertEquals ("auth_file", objOptions.auth_file.Value(),"test"); //$NON-NLS-1$
        //		assertEquals ("user", objOptions.user.Value(),"test"); //$NON-NLS-1$

    }
}  // class PropertyFactoryJUnitTest