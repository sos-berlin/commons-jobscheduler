package com.sos.localization;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;

/**
 * \class 		PropertyFactory - Workerclass for "PropertyFactora - a Factoroy to maintain I18N Files"
 *
 * \brief AdapterClass of PropertyFactory for the SOSJobScheduler
 *
 * This Class PropertyFactory is the worker-class.
 *

 *
 * see \see C:\Users\KB\AppData\Local\Temp\scheduler_editor-297718331111000308.html for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by com/sos/resources/xsl/jobdoc/sourcegenerator/java/JSJobDoc2JSWorkerClass.xsl from http://www.sos-berlin.com at 20141009200110 
 * \endverbatim
 */
public class PropertyFactory extends JSJobUtilitiesClass<PropertyFactoryOptions> {
	@SuppressWarnings("unused")
	private final String		conClassName	= this.getClass().getSimpleName();
	@SuppressWarnings("unused")
	private static final String	conSVNVersion	= "$Id$";
	@SuppressWarnings("unused")
	private final Logger		logger			= Logger.getLogger(this.getClass());

	/**
	 * 
	 * \brief PropertyFactory
	 *
	 * \details
	 *
	 */
	public PropertyFactory() {
		super(new PropertyFactoryOptions());
	}

	/**
	 * 
	 * \brief Options - returns the PropertyFactoryOptionClass
	 * 
	 * \details
	 * The PropertyFactoryOptionClass is used as a Container for all Options (Settings) which are
	 * needed.
	 *  
	 * \return PropertyFactoryOptions
	 *
	 */
	@Override
	public PropertyFactoryOptions getOptions() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Options";

		if (objOptions == null) {
			objOptions = new PropertyFactoryOptions();
		}
		return objOptions;
	}

	/**
	 * 
	 * \brief Execute - Start the Execution of PropertyFactory
	 * 
	 * \details
	 * 
	 * For more details see
	 * 
	 * \see JobSchedulerAdapterClass 
	 * \see PropertyFactoryMain
	 * 
	 * \return PropertyFactory
	 *
	 * @return
	 */
	public PropertyFactory Execute() throws Exception {
		final String conMethodName = conClassName + "::Execute";

		//		logger.debug(String.format(new JSMsg("JSJ-I-110").get(), conMethodName));

		try {
			getOptions().CheckMandatory();
			logger.debug(getOptions().dirtyString());

			String strPropertyFileName = objOptions.PropertyFileNamePrefix.Value();
			HashMap <String, HashMap <String, I18NObject> > allKeys = new HashMap<>();
			HashMap<String, I18NObject> mapKeys = new HashMap<>();
			Vector <String> vecLanguages = new Vector <>();
			for (File objFile : objOptions.SourceFolderName.listFiles()) {

				String strFileName = objFile.getName();
				if (strFileName.startsWith(strPropertyFileName) && strFileName.endsWith(".properties")) {
					logger.info("FileName = " + objFile.getName());
					InputStream ips = new FileInputStream(objFile);
					Properties objProps = new Properties();
					objProps.load(ips);
					ips.close();
					String strLanguage = "de";
					strLanguage = strFileName.replaceAll("\\.properties", "");
					strLanguage = strLanguage.substring(strPropertyFileName.length() + 1);
					vecLanguages.add(strLanguage);
					Enumeration<String> e = (Enumeration<String>) objProps.propertyNames();

					while (e.hasMoreElements()) {
						String key = e.nextElement();
						String[] strA = key.split("\\.");

						if (strA.length < 2) {
							System.out.println(key + " = " + objProps.getProperty(key));
						}
						else {
							mapKeys = allKeys.get(strA[0]);
							if (mapKeys == null) {
								mapKeys = new HashMap<>();
								allKeys.put(strA[0], mapKeys);
							}
							String strIDent = strA[0] + "_" + strLanguage;
							I18NObject objI18NO = mapKeys.get(strIDent);
							if (objI18NO == null) {
								objI18NO = new I18NObject(strIDent, strLanguage);
								mapKeys.put(strIDent, objI18NO);
							}
							else {

							}
							objI18NO.setBeanProperty(strA[1], objProps.getProperty(key));
						}
					}
					
					for (String objK  : allKeys.keySet()) {
						logger.debug(objK);
						mapKeys = allKeys.get(objK);
						for (String objL : mapKeys.keySet()) {
							logger.debug("_______" + objL);
						}
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			//			logger.error(String.format(new JSMsg("JSJ-I-107").get(), conMethodName), e);
			throw e;
		}
		finally {
			//			logger.debug(String.format(new JSMsg("JSJ-I-111").get(), conMethodName));
		}

		return this;
	}

	public void init() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::init";
		doInitialize();
	}

	private void doInitialize() {
	} // doInitialize

} // class PropertyFactory