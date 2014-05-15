package com.sos.scheduler.converter.graphviz;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionFolderName;

/**
 * \class 		JSObjects2GraphvizOptionsSuperClass - JSObjects2Graphviz
 *
 * \brief
 * An Options-Super-Class with all Options. This Class will be extended by the "real" Options-class (\see JSObjects2GraphvizOptions.
 * The "real" Option class will hold all the things, which are normaly overwritten at a new generation
 * of the super-class.
 *
 *

 *
 * see \see C:\Users\KB\AppData\Local\Temp\scheduler_editor-2781494595910967227.html for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\sos-berlin.com\jobscheduler\scheduler\config\JOETemplates\java\xsl\JSJobDoc2JSOptionSuperClass.xsl from http://www.sos-berlin.com at 20121108150924
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
	pobjHM.put ("		JSObjects2GraphvizOptionsSuperClass.auth_file", "test");  // This parameter specifies the path and name of a user's pr
		return pobjHM;
  }  //  private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim
 */
@JSOptionClass(name = "JSObjects2GraphvizOptionsSuperClass", description = "JSObjects2GraphvizOptionsSuperClass")
public class JSObjects2GraphvizOptionsSuperClass extends JSOptionsClass {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -1572067790397775406L;
	private final String	conClassName		= "JSObjects2GraphvizOptionsSuperClass";
	@SuppressWarnings("unused")
	private static Logger	logger				= Logger.getLogger(JSObjects2GraphvizOptionsSuperClass.class);

	/**
	 * \var live_folder_name :
	 *
	 *
	 */
	@JSOptionDefinition(name = "live_folder_name", description = "", key = "live_folder_name", type = "SOSOptionString", mandatory = true)
	public SOSOptionFolderName	live_folder_name	= new SOSOptionFolderName(this, conClassName + ".live_folder_name", // HashMap-Key
														"", // Titel
														" ", // InitValue
														" ", // DefaultValue
														true // isMandatory
												);

	/**
	 * \brief getlive_folder_name :
	 *
	 * \details
	 *
	 *
	 * \return
	 *
	 */
	public SOSOptionFolderName getlive_folder_name() {
		return live_folder_name;
	}

	/**
	 * \brief setlive_folder_name :
	 *
	 * \details
	 *
	 *
	 * @param live_folder_name :
	 */
	public void setlive_folder_name(final SOSOptionFolderName p_live_folder_name) {
		live_folder_name = p_live_folder_name;
	}

	/**
	 * \var output_folder_name :
	 *
	 *
	 */
	@JSOptionDefinition(name = "output_folder_name", description = "", key = "output_folder_name", type = "SOSOptionString", mandatory = true)
	public SOSOptionFolderName	output_folder_name	= new SOSOptionFolderName(this, conClassName + ".output_folder_name", // HashMap-Key
														"", // Titel
														"", // InitValue
														"", // DefaultValue
														true // isMandatory
												);

	/**
	 * \brief getoutput_folder_name :
	 *
	 * \details
	 *
	 *
	 * \return
	 *
	 */
	public SOSOptionFolderName getoutput_folder_name() {
		return output_folder_name;
	}

	/**
	 * \brief setoutput_folder_name :
	 *
	 * \details
	 *
	 *
	 * @param output_folder_name :
	 */
	public void setoutput_folder_name(final SOSOptionFolderName p_output_folder_name) {
		output_folder_name = p_output_folder_name;
	}

	public JSObjects2GraphvizOptionsSuperClass() {
		objParentClass = this.getClass();
	} // public JSObjects2GraphvizOptionsSuperClass

	public JSObjects2GraphvizOptionsSuperClass(final JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public JSObjects2GraphvizOptionsSuperClass

	//

	public JSObjects2GraphvizOptionsSuperClass(final HashMap<String, String> JSSettings) throws Exception {
		this();
		this.setAllOptions(JSSettings);
	} // public JSObjects2GraphvizOptionsSuperClass (HashMap JSSettings)

	/**
	 * \brief getAllOptionsAsString - liefert die Werte und Beschreibung aller
	 * Optionen als String
	 *
	 * \details
	 *
	 * \see toString
	 * \see toOut
	 */
	private String getAllOptionsAsString() {
		@SuppressWarnings("unused")
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
	public void setAllOptions(final HashMap<String, String> pobjJSSettings)  {
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
	public void CommandLineArgs(final String[] pstrArgs)  {
		super.CommandLineArgs(pstrArgs);
		this.setAllOptions(super.objSettings);
	}
} // public class JSObjects2GraphvizOptionsSuperClass