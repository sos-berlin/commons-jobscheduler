package sos.scheduler.xsl;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionInFileName;
import com.sos.JSHelper.Options.SOSOptionOutFileName;

/**
 * \class 		JobSchedulerXslTransformationOptionsSuperClass - JobSchedulerXslTransform
 *
 * \brief
 * An Options-Super-Class with all Options. This Class will be extended by the "real" Options-class (\see JobSchedulerXslTransformationOptions.
 * The "real" Option class will hold all the things, which are normaly overwritten at a new generation
 * of the super-class.
 *
 *

 *
 * see \see J:\E\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\JobSchedulerXslTransform.xml for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by  from http://www.sos-berlin.com at 20110815114129
 * \endverbatim
 * \section OptionsTable Tabelle der vorhandenen Optionen
 *
 * Tabelle mit allen Optionen
 *
 * MethodName
 * Title
 * Setting
 * Description
 * IsMandatory
 * DataType
 * InitialValue
 * TestValue
 *
 *
 *
 * \section TestData Eine Hilfe zum Erzeugen einer HashMap mit Testdaten
 *
 * Die folgenden Methode kann verwendet werden, um für einen Test eine HashMap
 * mit sinnvollen Werten für die einzelnen Optionen zu erzeugen.
 *
 * \verbatim
 private HashMap <String, String> SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM) {
	pobjHM.put ("		JobSchedulerXslTransformationOptionsSuperClass.auth_file", "test");  // This parameter specifies the path and name of a user's pr
		return pobjHM;
  }  //  private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim
 */
@JSOptionClass(name = "JobSchedulerXslTransformationOptionsSuperClass", description = "JobSchedulerXslTransformationOptionsSuperClass")
public class JobSchedulerXslTransformOptionsSuperClass extends JSOptionsClass {
	/**
	 *
	 */
	private static final long	serialVersionUID	= -4196969125579402246L;
	private final String		conClassName		= "JobSchedulerXslTransformationOptionsSuperClass";
	@SuppressWarnings("unused")
	private static Logger		logger				= Logger.getLogger(JobSchedulerXslTransformOptionsSuperClass.class);

	/**
	 * \var FileName :
	 *
	 *
	 */
	@JSOptionDefinition(name = "FileName", description = "", key = "FileName", type = "SOSOptionString", mandatory = true)
	public SOSOptionInFileName	FileName			= new SOSOptionInFileName(this, conClassName + ".FileName", // HashMap-Key
															"", // Titel
															"", // InitValue
															"", // DefaultValue
															true // isMandatory
													);
	public SOSOptionInFileName XMLFileName = (SOSOptionInFileName) FileName.SetAlias("xml_file_name");
	
	/**
	 * \brief getFileName :
	 *
	 * \details
	 *
	 *
	 * \return
	 *
	 */
	public SOSOptionInFileName getFileName() {
		return FileName;
	}

	/**
	 * \brief setFileName :
	 *
	 * \details
	 *
	 *
	 * @param FileName :
	 */
	public void setFileName(final SOSOptionInFileName p_FileName) {
		FileName = p_FileName;
	}

	/**
	 * \var OutputFileName :
	 *
	 *
	 */
	@JSOptionDefinition(name = "OutputFileName", description = "", key = "OutputFileName", type = "SOSOptionString", mandatory = true)
	public SOSOptionOutFileName	OutputFileName	= new SOSOptionOutFileName(this, conClassName + ".OutputFileName", // HashMap-Key
														"", // Titel
														"", // InitValue
														"", // DefaultValue
														true // isMandatory
												);

	/**
	 * \brief getOutputFileName :
	 *
	 * \details
	 *
	 *
	 * \return
	 *
	 */
	public SOSOptionOutFileName getOutputFileName() {
		return OutputFileName;
	}

	/**
	 * \brief setOutputFileName :
	 *
	 * \details
	 *
	 *
	 * @param OutputFileName :
	 */
	public void setOutputFileName(final SOSOptionOutFileName p_OutputFileName) {
		OutputFileName = p_OutputFileName;
	}

	/**
	 * \var XslFileName :
	 *
	 *
	 */
	@JSOptionDefinition(name = "XslFileName", description = "", key = "XslFileName", type = "SOSOptionString", mandatory = true)
	public SOSOptionInFileName	XslFileName	= new SOSOptionInFileName(this, conClassName + ".XslFileName", // HashMap-Key
													"", // Titel
													"", // InitValue
													"", // DefaultValue
													true // isMandatory
											);

	/**
	 * \brief getXslFileName :
	 *
	 * \details
	 *
	 *
	 * \return
	 *
	 */
	public SOSOptionInFileName getXslFileName() {
		return XslFileName;
	}

	/**
	 * \brief setXslFileName :
	 *
	 * \details
	 *
	 *
	 * @param XslFileName :
	 */
	public void setXslFileName(final SOSOptionInFileName p_XslFileName) {
		XslFileName = p_XslFileName;
	}

	public JobSchedulerXslTransformOptionsSuperClass() {
		objParentClass = this.getClass();
	} // public JobSchedulerXslTransformationOptionsSuperClass

	public JobSchedulerXslTransformOptionsSuperClass(final JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public JobSchedulerXslTransformationOptionsSuperClass

	public JobSchedulerXslTransformOptionsSuperClass(final HashMap<String, String> JSSettings) throws Exception {
		this();
		this.setAllOptions(JSSettings);
	} // public JobSchedulerXslTransformationOptionsSuperClass (HashMap JSSettings)

	/**
	 * \brief getAllOptionsAsString - liefert die Werte und Beschreibung aller
	 * Optionen als String
	 *
	 * \details
	 *
	 * \see toString
	 * \see toOut
	 */
	@SuppressWarnings("unused")
	private String getAllOptionsAsString() {
		final String conMethodName = conClassName + "::getAllOptionsAsString";
		String strT = conClassName + "\n";
		final StringBuffer strBuffer = new StringBuffer();
		// strT += IterateAllDataElementsByAnnotation(objParentClass, this,
		// JSOptionsClass.IterationTypes.toString, strBuffer);
		// strT += IterateAllDataElementsByAnnotation(objParentClass, this, 13,
		// strBuffer);
		strT += this.toString(); // fix
		//
		return strT;
	} // private String getAllOptionsAsString ()

	/**
	 * \brief setAllOptions - übernimmt die OptionenWerte aus der HashMap
	 *
	 * \details In der als Parameter anzugebenden HashMap sind Schlüssel (Name)
	 * und Wert der jeweiligen Option als Paar angegeben. Ein Beispiel für den
	 * Aufbau einer solchen HashMap findet sich in der Beschreibung dieser
	 * Klasse (\ref TestData "setJobSchedulerSSHJobOptions"). In dieser Routine
	 * werden die Schlüssel analysiert und, falls gefunden, werden die
	 * dazugehörigen Werte den Properties dieser Klasse zugewiesen.
	 *
	 * Nicht bekannte Schlüssel werden ignoriert.
	 *
	 * \see JSOptionsClass::getItem
	 *
	 * @param pobjJSSettings
	 * @throws Exception
	 */
	@Override
	public void setAllOptions(final HashMap<String, String> pobjJSSettings) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setAllOptions";
		flgSetAllOptions = true;
		objSettings = pobjJSSettings;
		super.Settings(objSettings);
		super.setAllOptions(pobjJSSettings);
		flgSetAllOptions = false;
	} // public void setAllOptions (HashMap <String, String> JSSettings)

	/**
	 * \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
	 *
	 * \details
	 * @throws Exception
	 *
	 * @throws Exception
	 * - wird ausgelöst, wenn eine mandatory-Option keinen Wert hat
	 */
	@Override
	public void CheckMandatory() throws JSExceptionMandatoryOptionMissing //
			, Exception {
		try {
			super.CheckMandatory();
		}
		catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
	} // public void CheckMandatory ()

	/**
	 *
	 * \brief CommandLineArgs - Übernehmen der Options/Settings aus der
	 * Kommandozeile
	 *
	 * \details Die in der Kommandozeile beim Starten der Applikation
	 * angegebenen Parameter werden hier in die HashMap übertragen und danach
	 * den Optionen als Wert zugewiesen.
	 *
	 * \return void
	 *
	 * @param pstrArgs
	 * @throws Exception
	 */
	@Override
	public void CommandLineArgs(final String[] pstrArgs) {
		super.CommandLineArgs(pstrArgs);
		this.setAllOptions(super.objSettings);
	}
} // public class JobSchedulerXslTransformationOptionsSuperClass