package com.sos.localization;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.JSHelper.Options.SOSOptionString;

/** \class PropertyFactoryOptionsSuperClass - PropertyFactora - a Factoroy to
 * maintain I18N Files
 *
 * \brief An Options-Super-Class with all Options. This Class will be extended
 * by the "real" Options-class (\see PropertyFactoryOptions. The "real" Option
 * class will hold all the things, which are normaly overwritten at a new
 * generation of the super-class.
 *
 *
 * 
 *
 * see \see
 * C:\Users\KB\AppData\Local\Temp\scheduler_editor-297718331111000308.html for
 * (more) details.
 * 
 * \verbatim ; mechanicaly created by
 * com/sos/resources/xsl/jobdoc/sourcegenerator
 * /java/JSJobDoc2JSOptionSuperClass.xsl from http://www.sos-berlin.com at
 * 20141009200110 \endverbatim \section OptionsTable Tabelle der vorhandenen
 * Optionen
 * 
 * Tabelle mit allen Optionen
 * 
 * MethodName Title Setting Description IsMandatory DataType InitialValue
 * TestValue
 * 
 * 
 *
 * \section TestData Eine Hilfe zum Erzeugen einer HashMap mit Testdaten
 *
 * Die folgenden Methode kann verwendet werden, um für einen Test eine HashMap
 * mit sinnvollen Werten für die einzelnen Optionen zu erzeugen.
 *
 * \verbatim private HashMap <String, String> SetJobSchedulerSSHJobOptions
 * (HashMap <String, String> pobjHM) { pobjHM.put
 * ("		PropertyFactoryOptionsSuperClass.auth_file", "test"); // This parameter
 * specifies the path and name of a user's pr return pobjHM; } // private void
 * SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM) \endverbatim */
@JSOptionClass(name = "PropertyFactoryOptionsSuperClass", description = "PropertyFactoryOptionsSuperClass")
public class PropertyFactoryOptionsSuperClass extends JSOptionsClass {

    private final String conClassName = "PropertyFactoryOptionsSuperClass";
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(PropertyFactoryOptionsSuperClass.class);

    /** \var Operation : */
    @JSOptionDefinition(name = "Operation", description = "", key = "Operation", type = "SOSOptionString", mandatory = false)
    public SOSOptionString Operation = new SOSOptionString(this, conClassName + ".Operation", // HashMap-Key
    "", // Titel
    "merge", // InitValue
    "merge", // DefaultValue
    false // isMandatory
    );

    /** \brief getOperation :
     * 
     * \details
     * 
     *
     * \return */
    public SOSOptionString getOperation() {
        return Operation;
    }

    public SOSOptionString Operation() {
        return Operation;
    }

    /** \brief setOperation :
     * 
     * \details
     * 
     *
     * @param Operation : */
    public void setOperation(SOSOptionString p_Operation) {
        this.Operation = p_Operation;
    }

    public void Operation(SOSOptionString p_Operation) {
        this.Operation = p_Operation;
    }

    /** \var PropertyFileNamePrefix : */
    @JSOptionDefinition(name = "PropertyFileNamePrefix", description = "", key = "PropertyFileNamePrefix", type = "SOSOptionString", mandatory = true)
    public SOSOptionString PropertyFileNamePrefix = new SOSOptionString(this, conClassName + ".PropertyFileNamePrefix", // HashMap-Key
    "", // Titel
    " ", // InitValue
    " ", // DefaultValue
    true // isMandatory
    );

    /** \brief getPropertyFileNamePrefix :
     * 
     * \details
     * 
     *
     * \return */
    public SOSOptionString getPropertyFileNamePrefix() {
        return PropertyFileNamePrefix;
    }

    public SOSOptionString PropertyFileNamePrefix() {
        return PropertyFileNamePrefix;
    }

    /** \brief setPropertyFileNamePrefix :
     * 
     * \details
     * 
     *
     * @param PropertyFileNamePrefix : */
    public void setPropertyFileNamePrefix(SOSOptionString p_PropertyFileNamePrefix) {
        this.PropertyFileNamePrefix = p_PropertyFileNamePrefix;
    }

    public void PropertyFileNamePrefix(SOSOptionString p_PropertyFileNamePrefix) {
        this.PropertyFileNamePrefix = p_PropertyFileNamePrefix;
    }

    /** \var SourceFolderName : The Folder, which has all the I18N Property
     * files. The Folder, which has all the I18N Property files. */
    @JSOptionDefinition(name = "SourceFolderName", description = "The Folder, which has all the I18N Property files.", key = "SourceFolderName", type = "SOSOptionFolderName", mandatory = true)
    public SOSOptionFolderName SourceFolderName = new SOSOptionFolderName(this, conClassName + ".SourceFolderName", // HashMap-Key
    "The Folder, which has all the I18N Property files.", // Titel
    " ", // InitValue
    " ", // DefaultValue
    true // isMandatory
    );

    /** \brief getSourceFolderName : The Folder, which has all the I18N Property
     * files.
     * 
     * \details The Folder, which has all the I18N Property files.
     *
     * \return The Folder, which has all the I18N Property files. */
    public SOSOptionFolderName getSourceFolderName() {
        return SourceFolderName;
    }

    public SOSOptionFolderName SourceFolderName() {
        return SourceFolderName;
    }

    /** \brief setSourceFolderName : The Folder, which has all the I18N Property
     * files.
     * 
     * \details The Folder, which has all the I18N Property files.
     *
     * @param SourceFolderName : The Folder, which has all the I18N Property
     *            files. */
    public void setSourceFolderName(SOSOptionFolderName p_SourceFolderName) {
        this.SourceFolderName = p_SourceFolderName;
    }

    public void SourceFolderName(SOSOptionFolderName p_SourceFolderName) {
        this.SourceFolderName = p_SourceFolderName;
    }

    public PropertyFactoryOptionsSuperClass() {
        objParentClass = this.getClass();
    } // public PropertyFactoryOptionsSuperClass

    public PropertyFactoryOptionsSuperClass(HashMap<String, String> JSSettings) {
        this();
        this.setAllOptions(JSSettings);
    } // public PropertyFactoryOptionsSuperClass (HashMap JSSettings)

    /** \brief getAllOptionsAsString - liefert die Werte und Beschreibung aller
     * Optionen als String
     *
     * \details
     * 
     * \see toString \see toOut */
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

    /** \brief setAllOptions - übernimmt die OptionenWerte aus der HashMap
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
     * @param pobjJSSettings */
    @Override
    public void setAllOptions(HashMap<String, String> pobjJSSettings) {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::setAllOptions";
        flgSetAllOptions = true;
        objSettings = pobjJSSettings;
        super.Settings(objSettings);
        super.setAllOptions(pobjJSSettings);
        flgSetAllOptions = false;
    } // public void setAllOptions (HashMap <String, String> JSSettings)

    /** \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
     *
     * \details
     * 
     * @throws Exception
     *
     * @throws Exception - wird ausgelöst, wenn eine mandatory-Option keinen
     *             Wert hat */
    @Override
    public void CheckMandatory() throws JSExceptionMandatoryOptionMissing //
            , Exception {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    } // public void CheckMandatory ()

    /** \brief CommandLineArgs - Übernehmen der Options/Settings aus der
     * Kommandozeile
     *
     * \details Die in der Kommandozeile beim Starten der Applikation
     * angegebenen Parameter werden hier in die HashMap übertragen und danach
     * den Optionen als Wert zugewiesen.
     *
     * \return void
     *
     * @param pstrArgs */
    @Override
    public void CommandLineArgs(String[] pstrArgs) {
        super.CommandLineArgs(pstrArgs);
        this.setAllOptions(super.objSettings);
    }
} // public class PropertyFactoryOptionsSuperClass