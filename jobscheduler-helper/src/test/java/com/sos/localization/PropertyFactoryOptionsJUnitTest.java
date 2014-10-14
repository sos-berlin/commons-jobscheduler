

package com.sos.localization;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;

/**
 * \class 		PropertyFactoryOptionsJUnitTest - PropertyFactora - a Factoroy to maintain I18N Files
 *
 * \brief 
 *
 *

 *
 * see \see C:\Users\KB\AppData\Local\Temp\scheduler_editor-297718331111000308.html for (more) details.
 * 
 * \verbatim ;
 * mechanicaly created by com/sos/resources/xsl/jobdoc/sourcegenerator/java/JSJobDoc2JSJUnitOptionSuperClass.xsl from http://www.sos-berlin.com at 20141009200110 
 * \endverbatim
 *
 * \section TestData Eine Hilfe zum Erzeugen einer HashMap mit Testdaten
 *
 * Die folgenden Methode kann verwendet werden, um für einen Test eine HashMap
 * mit sinnvollen Werten für die einzelnen Optionen zu erzeugen.
 *
 * \verbatim
 private HashMap <String, String> SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM) {
	pobjHM.put ("		PropertyFactoryOptionsJUnitTest.auth_file", "test");  // This parameter specifies the path and name of a user's pr
		return pobjHM;
  }  //  private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim
 */
public class PropertyFactoryOptionsJUnitTest extends  JSToolBox {
	private final String					conClassName						= "PropertyFactoryOptionsJUnitTest"; //$NON-NLS-1$
		@SuppressWarnings("unused") //$NON-NLS-1$
	private static Logger		logger			= Logger.getLogger(PropertyFactoryOptionsJUnitTest.class);
	private PropertyFactory objE = null;

	protected PropertyFactoryOptions	objOptions			= null;

	public PropertyFactoryOptionsJUnitTest() {
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
		objOptions = objE.Options();
		
	}

	@After
	public void tearDown() throws Exception {
	}


		

/**
 * \brief testOperation : 
 * 
 * \details
 * 
 *
 */
    @Test
    public void testOperation() {  // SOSOptionString
    	 objOptions.Operation.Value("++merge++");
    	 assertEquals ("", objOptions.Operation.Value(),"++merge++");
    	
    }

                

/**
 * \brief testPropertyFileNamePrefix : 
 * 
 * \details
 * 
 *
 */
    @Test
    public void testPropertyFileNamePrefix() {  // SOSOptionString
    	 objOptions.PropertyFileNamePrefix.Value("++----++");
    	 assertEquals ("", objOptions.PropertyFileNamePrefix.Value(),"++----++");
    	
    }

                

/**
 * \brief testSourceFolderName : The Folder, which has all the I18N Property files.
 * 
 * \details
 * The Folder, which has all the I18N Property files.
 *
 */
    @Test
    public void testSourceFolderName() {  // SOSOptionFolderName
    	objOptions.SourceFolderName.Value("++----++");
    	assertEquals ("The Folder, which has all the I18N Property files.", objOptions.SourceFolderName.Value(),"++----++");
    	
    }

                
        
} // public class PropertyFactoryOptionsJUnitTest