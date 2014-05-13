

package sos.scheduler.misc;

import static org.junit.Assert.assertEquals;

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
 * \class 		CopyJob2OrderParameterOptionsJUnitTest - CopyJob2OrderParameter
 *
 * \brief 
 *
 *

 *
 * see \see J:\E\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\CopyJob2OrderParameter.xml for (more) details.
 * 
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse\xsl\JSJobDoc2JSJUnitOptionSuperClass.xsl from http://www.sos-berlin.com at 20111104174419 
 * \endverbatim
 *
 * \section TestData Eine Hilfe zum Erzeugen einer HashMap mit Testdaten
 *
 * Die folgenden Methode kann verwendet werden, um für einen Test eine HashMap
 * mit sinnvollen Werten für die einzelnen Optionen zu erzeugen.
 *
 * \verbatim
 private HashMap <String, String> SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM) {
	pobjHM.put ("		CopyJob2OrderParameterOptionsJUnitTest.auth_file", "test");  // This parameter specifies the path and name of a user's pr
		return pobjHM;
  }  //  private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim
 */
public class CopyJob2OrderParameterOptionsJUnitTest extends  JSToolBox {
	private final String					conClassName						= "CopyJob2OrderParameterOptionsJUnitTest"; //$NON-NLS-1$
		@SuppressWarnings("unused") //$NON-NLS-1$
	private static Logger		logger			= Logger.getLogger(CopyJob2OrderParameterOptionsJUnitTest.class);
	@SuppressWarnings("unused")
	private static Log4JHelper	objLogger		= null;
	private CopyJob2OrderParameter objE = null;

	protected CopyJob2OrderParameterOptions	objOptions			= null;

	public CopyJob2OrderParameterOptionsJUnitTest() {
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
		objE = new CopyJob2OrderParameter();
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
 * \brief testoperation : 
 * 
 * \details
 * 
 *
 */
    @Test
    public void testoperation() {  // SOSOptionString
    	 objOptions.operation.Value("++copy++");
    	 assertEquals ("", objOptions.operation.Value(),"++copy++");
    	
    }

                
        
} // public class CopyJob2OrderParameterOptionsJUnitTest