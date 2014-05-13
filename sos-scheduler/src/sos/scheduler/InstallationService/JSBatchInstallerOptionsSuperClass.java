

package sos.scheduler.InstallationService;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSJobChainName;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.JSHelper.Options.SOSOptionHostName;
import com.sos.JSHelper.Options.SOSOptionInFileName;
import com.sos.JSHelper.Options.SOSOptionPortNumber;

/**
 * \class 		JSBatchInstallerOptionsSuperClass - Unattended Batch Installation on remote servers
 *
 * \brief 
 * An Options-Super-Class with all Options. This Class will be extended by the "real" Options-class (\see JSBatchInstallerOptions.
 * The "real" Option class will hold all the things, which are normaly overwritten at a new generation
 * of the super-class.
 *
 *

 *
 * see \see C:\Users\KB\Downloads\Preislisten\JSBatchInstaller.xml for (more) details.
 * 
 * \verbatim ;
 * mechanicaly created by  from http://www.sos-berlin.com at 20110322142354 
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
	pobjHM.put ("		JSBatchInstallerOptionsSuperClass.auth_file", "test");  // This parameter specifies the path and name of a user's pr
		return pobjHM;
  }  //  private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim
 */
@JSOptionClass(name = "JSBatchInstallerOptionsSuperClass", description = "JSBatchInstallerOptionsSuperClass")
public class JSBatchInstallerOptionsSuperClass extends JSOptionsClass {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 9068599714451980002L;
	private final String					conClassName						= "JSBatchInstallerOptionsSuperClass";
		@SuppressWarnings("unused")
	private static Logger		logger			= Logger.getLogger(JSBatchInstallerOptionsSuperClass.class);

		

/**
 * \var localDir : Path to the folder with the generated installation files.
 * Path to the folder with the generated installation files.
 *
 */
    @JSOptionDefinition(name = "local_dir", 
    description = "Path to the folder with the generated installation files.", 
    key = "local_dir", 
    type = "SOSOptionFolderName", 
    mandatory = true)
    
    public SOSOptionFolderName local_dir = new SOSOptionFolderName(this, conClassName + ".local_dir", // HashMap-Key
                                                                "Path to the folder with the generated installation files.", // Titel
                                                                " ", // InitValue
                                                                " ", // DefaultValue
                                                                true // isMandatory
                    );

/**
 * \brief getlocalDir : Path to the folder with the generated installation files.
 * 
 * \details
 * Path to the folder with the generated installation files.
 *
 * \return Path to the folder with the generated installation files.
 *
 */
    public SOSOptionFolderName  getlocal_dir() {
        return local_dir;
    }

/**
 * \brief setlocal_dir : Path to the folder with the generated installation files.
 * 
 * \details
 * Path to the folder with the generated installation files.
 *
 * @param local_dir : Path to the folder with the generated installation files.
 */
    public void setlocal_dir(SOSOptionFolderName p_local_dir) { 
        this.local_dir = p_local_dir;
    }

                        

/**
 * \var update : False: Ignore value of "LastRun"
 * False: Ignore value of "LastRun" True: All installations are executed (matching a given filter).
 *
 */
    @JSOptionDefinition(name = "update", 
    description = "False: Ignore value of 'LastRun'", 
    key = "update", 
    type = "SOSOptionBoolean", 
    mandatory = false)
    
    public SOSOptionBoolean update = new SOSOptionBoolean(this, conClassName + ".update", // HashMap-Key
                                                                "False: Ignore value of LastRun", // Titel
                                                                "false", // InitValue
                                                                "false", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getupdate : False: Ignore value of "LastRun"
 * 
 * \details
 * False: Only Ignore value of "LastRun" True: All installations are executed (matching a given filter).
 *
 * \return False: Ignore value of "LastRun"
 *
 */
    public SOSOptionBoolean  getupdate() {
        return update;
    }

/**
 * \brief setupdate : False: Ignore value of "LastRun"
 * 
 * \details
 * False: Ignore value of "LastRun" True: All installations are executed (matching a given filter).
 *
 * @param update : False: Ignore value of "LastRun"LastRun"
 */
    public void setupdate (SOSOptionBoolean p_update) { 
        this.update = p_update;
    }

                        

/**
 * \var filter_install_host : Only installations are executed which belongs to this host.
 * Only installations are executed which belongs to this host.
 *
 */
    @JSOptionDefinition(name = "filter_install_host", 
    description = "Only installations are executed which belongs to this host.", 
    key = "filter_install_host", 
    type = "SOSOptionHostName", 
    mandatory = false)
    
    public SOSOptionHostName filter_install_host = new SOSOptionHostName(this, conClassName + ".filter_install_host", // HashMap-Key
                                                                "Only installations are executed which belongs to this host.", // Titel
                                                                " ", // InitValue
                                                                " ", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getfilter_install_host : Only installations are executed which belongs to this host.
 * 
 * \details
 * Only installations are executed which belongs to this host.
 *
 * \return Only installations are executed which belongs to this host.
 *
 */
    public SOSOptionHostName  getfilter_install_host() {
        return filter_install_host;
    }

/**
 * \brief setfilter_install_host : Only installations are executed which belongs to this host.
 * 
 * \details
 * Only installations are executed which belongs to this host.
 *
 * @param filter_install_host : Only installations are executed which belongs to this host.
 */
    public void setfilter_install_host (SOSOptionHostName p_filter_install_host) { 
        this.filter_install_host = p_filter_install_host;
    }

                        

/**
 * \var filter_install_port : Only installations are executed which belongs to this port.
 * Only installations are executed which belongs to this port.
 *
 */
    @JSOptionDefinition(name = "filter_install_port", 
    description = "Only installations are executed which belongs to this port.", 
    key = "filter_install_port", 
    type = "SOSOptionPortNumber", 
    mandatory = false)
    
    public SOSOptionPortNumber filter_install_port = new SOSOptionPortNumber(this, conClassName + ".filter_install_port", // HashMap-Key
                                                                "Only installations are executed which belongs to this port.", // Titel
                                                                "0", // InitValue
                                                                "0", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getfilter_install_port : Only installations are executed which belongs to this port.
 * 
 * \details
 * Only installations are executed which belongs to this port.
 *
 * \return Only installations are executed which belongs to this port.
 *
 */
    public SOSOptionPortNumber  getfilter_install_port() {
        return filter_install_port;
    }

/**
 * \brief setfilter_install_port : Only installations are executed which belongs to this port.
 * 
 * \details
 * Only installations are executed which belongs to this port.
 *
 * @param filter_install_port : Only installations are executed which belongs to this port.
 */
    public void setfilter_install_port (SOSOptionPortNumber p_filter_install_port) { 
        this.filter_install_port = p_filter_install_port;
    }

                        

/**
 * \var installation_definition_file : XML file with installation elements. One element per installation.
 * XML file with installation elements. One element per installation.
 *
 */
    @JSOptionDefinition(name = "installation_definition_file", 
    description = "XML file with installation elements. One element per installation.", 
    key = "installation_definition_file", 
    type = "SOSOptionInFileName", 
    mandatory = true)
    
    public SOSOptionInFileName installation_definition_file = new SOSOptionInFileName(this, conClassName + ".installation_definition_file", // HashMap-Key
                                                                "XML file with installation elements. One element per installation.", // Titel
                                                                " ", // InitValue
                                                                " ", // DefaultValue
                                                                true // isMandatory
                    );

/**
 * \brief getinstallation_definition_file : XML file with installation elements. One element per installation.
 * 
 * \details
 * XML file with installation elements. One element per installation.
 *
 * \return XML file with installation elements. One element per installation.
 *
 */
    public SOSOptionInFileName  getinstallation_definition_file() {
        return installation_definition_file;
    }

/**
 * \brief setinstallation_definition_file : XML file with installation elements. One element per installation.
 * 
 * \details
 * XML file with installation elements. One element per installation.
 *
 * @param installation_definition_file : XML file with installation elements. One element per installation.
 */
    public void setinstallation_definition_file (SOSOptionInFileName p_installation_definition_file) { 
        this.installation_definition_file = p_installation_definition_file;
    }

                        

/**
 * \var installation_job_chain : Job chain with the steps for transfer the installation files and perfo
 * Job chain with the steps for transfer the installation files and perform the setup. The job chain must contain the nodes: -TransferInstallationSetup (FTP) Transfering setup -TransferInstallationFile (FTP) Transfering installation file -PerformInstall (SSH) Executing setup
 *
 */
    @JSOptionDefinition(name = "installation_job_chain", 
    description = "Job chain with the steps for transfer the installation files and perfo", 
    key = "installation_job_chain", 
    type = "JSOptionJobChainName", 
    mandatory = true)
    
    public JSJobChainName installation_job_chain = new JSJobChainName(this, conClassName + ".installation_job_chain", // HashMap-Key
                                                                "Job chain with the steps for transfer the installation files and perfo", // Titel
                                                                "automatic_installation", // InitValue
                                                                "automatic_installation", // DefaultValue
                                                                true // isMandatory
                    );

/**
 * \brief getinstallation_job_chain : Job chain with the steps for transfer the installation files and perfo
 * 
 * \details
 * Job chain with the steps for transfer the installation files and perform the setup. The job chain must contain the nodes: -TransferInstallationSetup (FTP) Transfering setup -TransferInstallationFile (FTP) Transfering installation file -PerformInstall (SSH) Executing setup
 *
 * \return Job chain with the steps for transfer the installation files and perfo
 *
 */
    public JSJobChainName  getinstallation_job_chain() {
        return installation_job_chain;
    }

/**
 * \brief setinstallation_job_chain : Job chain with the steps for transfer the installation files and perfo
 * 
 * \details
 * Job chain with the steps for transfer the installation files and perform the setup. The job chain must contain the nodes: -TransferInstallationSetup (FTP) Transfering setup -TransferInstallationFile (FTP) Transfering installation file -PerformInstall (SSH) Executing setup
 *
 * @param installation_job_chain : Job chain with the steps for transfer the installation files and perfo
 */
    public void setinstallation_job_chain (JSJobChainName p_installation_job_chain) { 
        this.installation_job_chain = p_installation_job_chain;
    }

                        
        
        
	public JSBatchInstallerOptionsSuperClass() {
		objParentClass = this.getClass();
	} // public JSBatchInstallerOptionsSuperClass

	public JSBatchInstallerOptionsSuperClass(JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public JSBatchInstallerOptionsSuperClass

		//

	public JSBatchInstallerOptionsSuperClass (HashMap <String, String> JSSettings) throws Exception {
		this();
		this.setAllOptions(JSSettings);
	} // public JSBatchInstallerOptionsSuperClass (HashMap JSSettings)
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
	public void setAllOptions(HashMap <String, String> pobjJSSettings) {
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
	public void CommandLineArgs(String[] pstrArgs) {
		super.CommandLineArgs(pstrArgs);
		this.setAllOptions(super.objSettings);
	}
} // public class JSBatchInstallerOptionsSuperClass