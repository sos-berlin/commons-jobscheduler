package sos.net.ssh;

import com.sos.i18n.annotation.I18NResourceBundle;
import org.apache.log4j.Logger;

import java.util.HashMap;

/**
 * \class SOSSSHJob2Main - Start a Script using SSH
 * 
 * \brief AdapterClass of SOSSSHJob2Main for the SOSJobScheduler
 * 
 * Start a Script using SSH
 * 
 * This Class SOSSSHJob2Main works as a root-class for the worker-class \ref
 * SOSSSHJob2.
 * 
 * \section
 * 
 * \see
 * 
 * \code 
 * .... code goes here ... 
 * \endcode
 * 
 * \author Klaus Buettner - http://www.sos-berlin.com 
* @version $Id$1.1.0.20100506
 * 
 * This Source-Code was created by JETTemplate SOSJobSchedulerMainclass.javajet,
 * Version 1.0 from 2009-12-26, written by kb
 */

@I18NResourceBundle(baseName = "com_sos_net_messages", defaultLocale = "en")
public class SOSSSHJob2Main {

	private final static String	conClassName	= "SOSSSHJob2Main";

	private static Logger		logger			= Logger.getLogger(SOSSSHJob2Main.class);

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
	public final static void main(final String[] pstrArgs) throws Exception {

		final String conMethodName = conClassName + "::Main"; //$NON-NLS-1$

		logger.info("SOSSSHJob2 - Main"); //$NON-NLS-1$
		logger.info("User-Dir : " + System.getProperty("user.dir"));   //$NON-NLS-1$

		try {
			SOSSSHJob2 objM = new SOSSSHJobTrilead();
			SOSSSHJobOptions objO = objM.Options();
			
			objO.CommandLineArgs(pstrArgs);
			objM.execute();
		}
		catch (Exception e) {
			System.out.println(conMethodName + ": " + "Error occured ..." + e.getMessage()); 
			e.printStackTrace();
			// TODO check exitcode from SSH Server. if not zero take the exit-code from the server instead of 99
			logger.info(conMethodName + ": terminated with exit-code 99");		
			System.exit(99);
		}
		
		logger.info(conMethodName + ": terminated without errors");		
	}

	/**
	 * 
	 * \brief setOptions
	 * 
	 * \details
	 *
	 * \return HashMap<String,String>
	 *
	 * @return
	 */
	@SuppressWarnings("unused")
	private HashMap<String, String> setOptions() {

		final String conMethodName = conClassName + "::setOptions";

		String conClassName = "SOSSSHJobOptions";
		HashMap<String, String> mapSettings = new HashMap<String, String>();
		String strKeyPrefix = conClassName + ".";

		mapSettings.put(strKeyPrefix + "UserName", "TestUser");

		return mapSettings;
	}

} // SOSSSHJob2Main
