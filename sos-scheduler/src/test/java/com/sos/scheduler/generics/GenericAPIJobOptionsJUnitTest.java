

package com.sos.scheduler.generics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
 * \class 		GenericAPIJobOptionsJUnitTest - A generic internal API job
 *
 * \brief 
 *
 *

 *
 * see \see C:\Users\KB\AppData\Local\Temp\scheduler_editor-2864692299059909179.html for (more) details.
 * 
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\sos-berlin.com\jobscheduler\scheduler\config\JOETemplates\java\xsl\JSJobDoc2JSJUnitOptionSuperClass.xsl from http://www.sos-berlin.com at 20120611173607 
 * \endverbatim
 *
 * \section TestData Eine Hilfe zum Erzeugen einer HashMap mit Testdaten
 *
 * Die folgenden Methode kann verwendet werden, um für einen Test eine HashMap
 * mit sinnvollen Werten für die einzelnen Optionen zu erzeugen.
 *
 * \verbatim
 private HashMap <String, String> SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM) {
	pobjHM.put ("		GenericAPIJobOptionsJUnitTest.auth_file", "test");  // This parameter specifies the path and name of a user's pr
		return pobjHM;
  }  //  private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim
 */
public class GenericAPIJobOptionsJUnitTest extends  JSToolBox {
	private final String					conClassName						= "GenericAPIJobOptionsJUnitTest"; //$NON-NLS-1$
		@SuppressWarnings("unused") //$NON-NLS-1$
	private static Logger		logger			= Logger.getLogger(GenericAPIJobOptionsJUnitTest.class);
	@SuppressWarnings("unused")
	private static Log4JHelper	objLogger		= null;
	private GenericAPIJob objE = null;

	protected GenericAPIJobOptions	objOptions			= null;

	public GenericAPIJobOptionsJUnitTest() {
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
		objE = new GenericAPIJob();
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
 * \brief testjavaClassName : The Name of the Java Class (e.g. a JS Adapter Class) which has to be e
 * 
 * \details
 * The Name of the Java Class (e.g. a JS Adapter Class) which has to be executed by this generic JS adapter.
 *
 */
    @Test
    public void testjavaClassName() {  // SOSOptionString
    	 objOptions.javaClassName.Value("++----++");
    	 assertEquals ("The Name of the Java Class (e.g. a JS Adapter Class) which has to be e", objOptions.javaClassName.Value(),"++----++");
    	
    }

                

/**
 * \brief testjavaClassPath : 
 * 
 * \details
 * 
 *
 */
    @Test
    public void testjavaClassPath() {  // SOSOptionString
    	 objOptions.javaClassPath.Value("++----++");
    	 assertEquals ("", objOptions.javaClassPath.Value(),"++----++");
    	
    }

                
        
} // public class GenericAPIJobOptionsJUnitTest