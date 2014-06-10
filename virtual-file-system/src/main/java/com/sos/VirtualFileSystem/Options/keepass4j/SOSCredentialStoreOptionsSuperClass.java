

package com.sos.VirtualFileSystem.Options.keepass4j;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionInFileName;
import com.sos.JSHelper.Options.SOSOptionOutFileName;
import com.sos.JSHelper.Options.SOSOptionPassword;
import com.sos.JSHelper.Options.SOSOptionString;

/**
 * \class 		SOSCredentialStoreOptionsSuperClass - SOSCredentialStore
 *
 * \brief
 * An Options-Super-Class with all Options. This Class will be extended by the "real" Options-class (\see SOSCredentialStoreOptions.
 * The "real" Option class will hold all the things, which are normaly overwritten at a new generation
 * of the super-class.
 *
 *

 *
 * see \see C:\Users\Mahendra\AppData\Local\Temp\scheduler_editor-3900348294966099242.html for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Program Files\sos-berlin.com\jobscheduler.1.6.4043\djsmp02_4445\config\JOETemplates\java\xsl\JSJobDoc2JSOptionSuperClass.xsl from http://www.sos-berlin.com at 20140304141232
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
	pobjHM.put ("		SOSCredentialStoreOptionsSuperClass.auth_file", "test");  // This parameter specifies the path and name of a user's pr
		return pobjHM;
  }  //  private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim
 */
@JSOptionClass(name = "SOSCredentialStoreOptionsSuperClass", description = "SOSCredentialStoreOptionsSuperClass")
public class SOSCredentialStoreOptionsSuperClass extends JSOptionsClass  {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -4388209310315139254L;
	private final String					conClassName						= "SOSCredentialStoreOptionsSuperClass";
		@SuppressWarnings("unused")
	private static Logger		logger			= Logger.getLogger(SOSCredentialStoreOptionsSuperClass.class);



/**
 * \var CredentialStore_AuthenticationMethod :
 *
 *
 */
    @JSOptionDefinition(name = "CredentialStore_AuthenticationMethod",
    description = "",
    key = "CredentialStore_AuthenticationMethod",
    type = "SOSOptionString",
    mandatory = true)

    public SOSOptionString CredentialStore_AuthenticationMethod = new SOSOptionString(this, conClassName + ".CredentialStore_AuthenticationMethod", // HashMap-Key
                                                                "", // Titel
                                                                "privatekey", // InitValue
                                                                "privatekey", // DefaultValue
                                                                true // isMandatory
                    );

/**
 * \brief getCredentialStore_AuthenticationMethod :
 *
 * \details
 *
 *
 * \return
 *
 */
    public SOSOptionString  getCredentialStore_AuthenticationMethod() {
        return CredentialStore_AuthenticationMethod;
    }

/**
 * \brief setCredentialStore_AuthenticationMethod :
 *
 * \details
 *
 *
 * @param CredentialStore_AuthenticationMethod :
 */
    public void setCredentialStore_AuthenticationMethod (final SOSOptionString p_CredentialStore_AuthenticationMethod) {
        CredentialStore_AuthenticationMethod = p_CredentialStore_AuthenticationMethod;
    }


    public SOSOptionString CS_AuthenticationMethod =
    (SOSOptionString) CredentialStore_AuthenticationMethod.SetAlias(conClassName + ".CS_AuthenticationMethod");

/**
 * \var CredentialStore_DeleteExportedFileOnExit :
 *
 *
 */
    @JSOptionDefinition(name = "CredentialStore_DeleteExportedFileOnExit",
    description = "",
    key = "CredentialStore_DeleteExportedFileOnExit",
    type = "SOSOptionBoolean",
    mandatory = false)

    public SOSOptionBoolean CredentialStore_DeleteExportedFileOnExit = new SOSOptionBoolean(this, conClassName + ".CredentialStore_DeleteExportedFileOnExit", // HashMap-Key
                                                                "", // Titel
                                                                "true", // InitValue
                                                                "true", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getCredentialStore_DeleteExportedFileOnExit :
 *
 * \details
 *
 *
 * \return
 *
 */
    public SOSOptionBoolean  getCredentialStore_DeleteExportedFileOnExit() {
        return CredentialStore_DeleteExportedFileOnExit;
    }

/**
 * \brief setCredentialStore_DeleteExportedFileOnExit :
 *
 * \details
 *
 *
 * @param CredentialStore_DeleteExportedFileOnExit :
 */
    public void setCredentialStore_DeleteExportedFileOnExit (final SOSOptionBoolean p_CredentialStore_DeleteExportedFileOnExit) {
        CredentialStore_DeleteExportedFileOnExit = p_CredentialStore_DeleteExportedFileOnExit;
    }


    public SOSOptionBoolean CS_DeleteExportedFileOnExit =
    (SOSOptionBoolean) CredentialStore_DeleteExportedFileOnExit.SetAlias(conClassName + ".CS_DeleteExportedFileOnExit");

/**
 * \var CredentialStore_ExportAttachment :
 *
 *
 */
    @JSOptionDefinition(name = "CredentialStore_ExportAttachment",
    description = "",
    key = "CredentialStore_ExportAttachment",
    type = "SOSOptionBoolean",
    mandatory = false)

    public SOSOptionBoolean CredentialStore_ExportAttachment = new SOSOptionBoolean(this, conClassName + ".CredentialStore_ExportAttachment", // HashMap-Key
                                                                "", // Titel
                                                                "false", // InitValue
                                                                "false", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getCredentialStore_ExportAttachment :
 *
 * \details
 *
 *
 * \return
 *
 */
    public SOSOptionBoolean  getCredentialStore_ExportAttachment() {
        return CredentialStore_ExportAttachment;
    }

/**
 * \brief setCredentialStore_ExportAttachment :
 *
 * \details
 *
 *
 * @param CredentialStore_ExportAttachment :
 */
    public void setCredentialStore_ExportAttachment (final SOSOptionBoolean p_CredentialStore_ExportAttachment) {
        CredentialStore_ExportAttachment = p_CredentialStore_ExportAttachment;
    }


    public SOSOptionBoolean CS_ExportAttachment =
    (SOSOptionBoolean) CredentialStore_ExportAttachment.SetAlias(conClassName + ".CS_ExportAttachment");

/**
 * \var CredentialStore_ExportAttachment2FileName :
 *
 *
 */
    @JSOptionDefinition(name = "CredentialStore_ExportAttachment2FileName",
    description = "",
    key = "CredentialStore_ExportAttachment2FileName",
    type = "SOSOptionOutFileName",
    mandatory = false)

    public SOSOptionOutFileName CredentialStore_ExportAttachment2FileName = new SOSOptionOutFileName(this, conClassName + ".CredentialStore_ExportAttachment2FileName", // HashMap-Key
                                                                "", // Titel
                                                                " ", // InitValue
                                                                " ", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getCredentialStore_ExportAttachment2FileName :
 *
 * \details
 *
 *
 * \return
 *
 */
    public SOSOptionOutFileName  getCredentialStore_ExportAttachment2FileName() {
        return CredentialStore_ExportAttachment2FileName;
    }

/**
 * \brief setCredentialStore_ExportAttachment2FileName :
 *
 * \details
 *
 *
 * @param CredentialStore_ExportAttachment2FileName :
 */
    public void setCredentialStore_ExportAttachment2FileName (final SOSOptionOutFileName p_CredentialStore_ExportAttachment2FileName) {
        CredentialStore_ExportAttachment2FileName = p_CredentialStore_ExportAttachment2FileName;
    }


    public SOSOptionOutFileName CS_ExportAttachment2FileName =
    (SOSOptionOutFileName) CredentialStore_ExportAttachment2FileName.SetAlias(conClassName + ".CS_ExportAttachment2FileName");

/**
 * \var CredentialStore_FileName :
 *
 *
 */
    @JSOptionDefinition(name = "CredentialStore_FileName",
    description = "",
    key = "CredentialStore_FileName",
    type = "SOSOptionInFileName",
    mandatory = true)

    public SOSOptionInFileName CredentialStore_FileName = new SOSOptionInFileName(this, conClassName + ".CredentialStore_FileName", // HashMap-Key
                                                                "", // Titel
                                                                " ", // InitValue
                                                                " ", // DefaultValue
                                                                true // isMandatory
                    );

/**
 * \brief getCredentialStore_FileName :
 *
 * \details
 *
 *
 * \return
 *
 */
    public SOSOptionInFileName  getCredentialStore_FileName() {
        return CredentialStore_FileName;
    }

/**
 * \brief setCredentialStore_FileName :
 *
 * \details
 *
 *
 * @param CredentialStore_FileName :
 */
    public void setCredentialStore_FileName (final SOSOptionInFileName p_CredentialStore_FileName) {
        CredentialStore_FileName = p_CredentialStore_FileName;
    }


    public SOSOptionInFileName CS_FileName =
    (SOSOptionInFileName) CredentialStore_FileName.SetAlias(conClassName + ".CS_FileName");

/**
 * \var CredentialStore_KeyFileName :
 *
 *
 */
    @JSOptionDefinition(name = "CredentialStore_KeyFileName",
    description = "",
    key = "CredentialStore_KeyFileName",
    type = "SOSOptionInFileName",
    mandatory = false)

    public SOSOptionInFileName CredentialStore_KeyFileName = new SOSOptionInFileName(this, conClassName + ".CredentialStore_KeyFileName", // HashMap-Key
                                                                "", // Titel
                                                                " ", // InitValue
                                                                " ", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getCredentialStore_KeyFileName :
 *
 * \details
 *
 *
 * \return
 *
 */
    public SOSOptionInFileName  getCredentialStore_KeyFileName() {
        return CredentialStore_KeyFileName;
    }

/**
 * \brief setCredentialStore_KeyFileName :
 *
 * \details
 *
 *
 * @param CredentialStore_KeyFileName :
 */
    public void setCredentialStore_KeyFileName (final SOSOptionInFileName p_CredentialStore_KeyFileName) {
        CredentialStore_KeyFileName = p_CredentialStore_KeyFileName;
    }


    public SOSOptionInFileName CS_KeyFileName =
    (SOSOptionInFileName) CredentialStore_KeyFileName.SetAlias(conClassName + ".CS_KeyFileName");

/**
 * \var CredentialStore_KeyPath :
 *
 *
 */
    @JSOptionDefinition(name = "CredentialStore_KeyPath",
    description = "",
    key = "CredentialStore_KeyPath",
    type = "SOSOptionString",
    mandatory = true)

    public SOSOptionString CredentialStore_KeyPath = new SOSOptionString(this, conClassName + ".CredentialStore_KeyPath", // HashMap-Key
                                                                "", // Titel
                                                                " ", // InitValue
                                                                " ", // DefaultValue
                                                                true // isMandatory
                    );

/**
 * \brief getCredentialStore_KeyPath :
 *
 * \details
 *
 *
 * \return
 *
 */
    public SOSOptionString  getCredentialStore_KeyPath() {
        return CredentialStore_KeyPath;
    }

/**
 * \brief setCredentialStore_KeyPath :
 *
 * \details
 *
 *
 * @param CredentialStore_KeyPath :
 */
    public void setCredentialStore_KeyPath (final SOSOptionString p_CredentialStore_KeyPath) {
        CredentialStore_KeyPath = p_CredentialStore_KeyPath;
    }


    public SOSOptionString CS_KeyPath =
    (SOSOptionString) CredentialStore_KeyPath.SetAlias(conClassName + ".CS_KeyPath");

/**
 * \var CredentialStore_OverwriteExportedFile :
 *
 *
 */
    @JSOptionDefinition(name = "CredentialStore_OverwriteExportedFile",
    description = "",
    key = "CredentialStore_OverwriteExportedFile",
    type = "SOSOptionBoolean",
    mandatory = false)

    public SOSOptionBoolean CredentialStore_OverwriteExportedFile = new SOSOptionBoolean(this, conClassName + ".CredentialStore_OverwriteExportedFile", // HashMap-Key
                                                                "", // Titel
                                                                "true", // InitValue
                                                                "true", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getCredentialStore_OverwriteExportedFile :
 *
 * \details
 *
 *
 * \return
 *
 */
    public SOSOptionBoolean  getCredentialStore_OverwriteExportedFile() {
        return CredentialStore_OverwriteExportedFile;
    }

/**
 * \brief setCredentialStore_OverwriteExportedFile :
 *
 * \details
 *
 *
 * @param CredentialStore_OverwriteExportedFile :
 */
    public void setCredentialStore_OverwriteExportedFile (final SOSOptionBoolean p_CredentialStore_OverwriteExportedFile) {
        CredentialStore_OverwriteExportedFile = p_CredentialStore_OverwriteExportedFile;
    }


    public SOSOptionBoolean CS_OverwriteExportedFile =
    (SOSOptionBoolean) CredentialStore_OverwriteExportedFile.SetAlias(conClassName + ".CS_OverwriteExportedFile");

/**
 * \var CredentialStore_Permissions4ExportedFile :
 *
 *
 */
    @JSOptionDefinition(name = "CredentialStore_Permissions4ExportedFile",
    description = "",
    key = "CredentialStore_Permissions4ExportedFile",
    type = "SOSOptionString",
    mandatory = false)

    public SOSOptionString CredentialStore_Permissions4ExportedFile = new SOSOptionString(this, conClassName + ".CredentialStore_Permissions4ExportedFile", // HashMap-Key
                                                                "", // Titel
                                                                "600", // InitValue
                                                                "600", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getCredentialStore_Permissions4ExportedFile :
 *
 * \details
 *
 *
 * \return
 *
 */
    public SOSOptionString  getCredentialStore_Permissions4ExportedFile() {
        return CredentialStore_Permissions4ExportedFile;
    }

/**
 * \brief setCredentialStore_Permissions4ExportedFile :
 *
 * \details
 *
 *
 * @param CredentialStore_Permissions4ExportedFile :
 */
    public void setCredentialStore_Permissions4ExportedFile (final SOSOptionString p_CredentialStore_Permissions4ExportedFile) {
        CredentialStore_Permissions4ExportedFile = p_CredentialStore_Permissions4ExportedFile;
    }


    public SOSOptionString CS_Permissions4ExportedFile =
    (SOSOptionString) CredentialStore_Permissions4ExportedFile.SetAlias(conClassName + ".CS_Permissions4ExportedFile");

/**
 * \var CredentialStore_ProcessNotesParams :
 *
 *
 */
    @JSOptionDefinition(name = "CredentialStore_ProcessNotesParams",
    description = "",
    key = "CredentialStore_ProcessNotesParams",
    type = "SOSOptionBoolean",
    mandatory = false)

    public SOSOptionBoolean CredentialStore_ProcessNotesParams = new SOSOptionBoolean(this, conClassName + ".CredentialStore_ProcessNotesParams", // HashMap-Key
                                                                "", // Titel
                                                                "false", // InitValue
                                                                "false", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getCredentialStore_ProcessNotesParams :
 *
 * \details
 *
 *
 * \return
 *
 */
    public SOSOptionBoolean  getCredentialStore_ProcessNotesParams() {
        return CredentialStore_ProcessNotesParams;
    }

/**
 * \brief setCredentialStore_ProcessNotesParams :
 *
 * \details
 *
 *
 * @param CredentialStore_ProcessNotesParams :
 */
    public void setCredentialStore_ProcessNotesParams (final SOSOptionBoolean p_CredentialStore_ProcessNotesParams) {
        CredentialStore_ProcessNotesParams = p_CredentialStore_ProcessNotesParams;
    }


    public SOSOptionBoolean CS_ProcessNotesParams =
    (SOSOptionBoolean) CredentialStore_ProcessNotesParams.SetAlias(conClassName + ".CS_ProcessNotesParams");

/**
 * \var CredentialStore_StoreType :
 *
 *
 */
    @JSOptionDefinition(name = "CredentialStore_StoreType",
    description = "",
    key = "CredentialStore_StoreType",
    type = "SOSOptionString",
    mandatory = false)

    public SOSOptionString CredentialStore_StoreType = new SOSOptionString(this, conClassName + ".CredentialStore_StoreType", // HashMap-Key
                                                                "", // Titel
                                                                "KeePass", // InitValue
                                                                "KeePass", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getCredentialStore_StoreType :
 *
 * \details
 *
 *
 * \return
 *
 */
    public SOSOptionString  getCredentialStore_StoreType() {
        return CredentialStore_StoreType;
    }

/**
 * \brief setCredentialStore_StoreType :
 *
 * \details
 *
 *
 * @param CredentialStore_StoreType :
 */
    public void setCredentialStore_StoreType (final SOSOptionString p_CredentialStore_StoreType) {
        CredentialStore_StoreType = p_CredentialStore_StoreType;
    }


    public SOSOptionString CS_StoreType =
    (SOSOptionString) CredentialStore_StoreType.SetAlias(conClassName + ".CS_StoreType");

/**
 * \var CredentialStore_password :
 *
 *
 */
    @JSOptionDefinition(name = "CredentialStore_password",
    description = "",
    key = "CredentialStore_password",
    type = "SOSOptionPassword",
    mandatory = false)

    public SOSOptionPassword CredentialStore_password = new SOSOptionPassword(this, conClassName + ".CredentialStore_password", // HashMap-Key
                                                                "", // Titel
                                                                " ", // InitValue
                                                                " ", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getCredentialStore_password :
 *
 * \details
 *
 *
 * \return
 *
 */
    public SOSOptionPassword  getCredentialStore_password() {
        return CredentialStore_password;
    }

/**
 * \brief setCredentialStore_password :
 *
 * \details
 *
 *
 * @param CredentialStore_password :
 */
    public void setCredentialStore_password (final SOSOptionPassword p_CredentialStore_password) {
        CredentialStore_password = p_CredentialStore_password;
    }


    public SOSOptionPassword CS_password =
    (SOSOptionPassword) CredentialStore_password.SetAlias(conClassName + ".CS_password");


/**
 * \var use_credential_Store :
 *
 *
 */
    @JSOptionDefinition(name = "use_credential_Store",
    description = "",
    key = "use_credential_Store",
    type = "SOSOptionBoolean",
    mandatory = false)

    public SOSOptionBoolean use_credential_Store = new SOSOptionBoolean(this, conClassName + ".use_credential_Store", // HashMap-Key
                                                                "", // Titel
                                                                "false", // InitValue
                                                                "false", // DefaultValue
                                                                false // isMandatory
                    );

/**
 * \brief getuse_credential_Store :
 *
 * \details
 *
 *
 * \return
 *
 */
    public SOSOptionBoolean  getuse_credential_Store() {
        return use_credential_Store;
    }

/**
 * \brief setuse_credential_Store :
 *
 * \details
 *
 *
 * @param use_credential_Store :
 */
    public void setuse_credential_Store (final SOSOptionBoolean p_use_credential_Store) {
        use_credential_Store = p_use_credential_Store;
    }

	public SOSCredentialStoreOptionsSuperClass() {
		objParentClass = this.getClass();
	} // public SOSCredentialStoreOptionsSuperClass

	public SOSCredentialStoreOptionsSuperClass(final JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public SOSCredentialStoreOptionsSuperClass

		//

	public SOSCredentialStoreOptionsSuperClass (final HashMap <String, String> JSSettings) throws Exception {
		this();
		this.setAllOptions(JSSettings);
	} // public SOSCredentialStoreOptionsSuperClass (HashMap JSSettings)
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
	@Override public void setAllOptions(final HashMap <String, String> pobjJSSettings)  {
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
//		this.setAllOptions(super.objSettings);
	}
} // public class SOSCredentialStoreOptionsSuperClass