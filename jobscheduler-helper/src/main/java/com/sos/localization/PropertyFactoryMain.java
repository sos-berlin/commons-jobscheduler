

package com.sos.localization;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.JSHelper.Exceptions.JobSchedulerException;


/**
 * \class 		PropertyFactoryMain - Main-Class for "PropertyFactora - a Factoroy to maintain I18N Files"
 *
 * \brief MainClass to launch PropertyFactory as an executable command-line program
 *
 * This Class PropertyFactoryMain is the worker-class.
 *

 *
 * see \see C:\Users\KB\AppData\Local\Temp\scheduler_editor-297718331111000308.html for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by com/sos/resources/xsl/jobdoc/sourcegenerator/java/JSJobDoc2JSMainClass.xsl from http://www.sos-berlin.com at 20141009200110 
 * \endverbatim
 */
@SuppressWarnings("deprecation")
public class PropertyFactoryMain extends JSJobUtilitiesClass <PropertyFactoryOptions >{
	// see http://stackoverflow.com/questions/8275499/how-to-call-getclass-from-a-static-method-in-java
	private static Class<?> currentClass = new Object() { }.getClass().getEnclosingClass();
	private static final String conClassName = currentClass.getSimpleName();
	private static final Logger logger = Logger.getLogger("PropertyFactoryMain");
	@SuppressWarnings("unused")
	private static final String conSVNVersion = "$Id$";

	/**
	 * 
	 * \brief main
	 * 
	 * \details
	 *
	 * \return void
	 *
	 * @param pstrArgs
	 * @throws Exception
	 */
	public final static void main(String[] pstrArgs) {

		final String conMethodName = conClassName + "::Main"; 

		logger.info("PropertyFactory - Main"); 
		logger.info(conSVNVersion);

		try {
			PropertyFactory objM = new PropertyFactory(); 
			PropertyFactoryOptions objO = objM.Options();

			objO.AllowEmptyParameterList.setFalse();
			objO.CheckNotProcessedOptions.setTrue();
						
			BasicConfigurator.configure();
			logger.setLevel(Level.DEBUG);
			
			objO.CommandLineArgs(pstrArgs);
			
			if (objO.CheckNotProcessedOptions.isTrue()) {
				if (objO.ReportNotProcessedOptions() == true) {
				}
				else {
					throw new JobSchedulerException("Unsupported or wrong Options found.");
				}
			}
			objM.Execute();
		}
		
		catch (Exception e) {
			System.err.println(conMethodName + ": " + "Error occured ..." + e.getMessage()); 
			e.printStackTrace(System.err);
			int intExitCode = 99;
//			logger.error(String.format(new JSMsg("JSJ-E-105").get(), conMethodName, intExitCode), e);		
			System.exit(intExitCode);
		}
		
//		logger.info(String.format(new JSMsg("JSJ-I-106").get(), conMethodName));		
	}

}  // class PropertyFactoryMain