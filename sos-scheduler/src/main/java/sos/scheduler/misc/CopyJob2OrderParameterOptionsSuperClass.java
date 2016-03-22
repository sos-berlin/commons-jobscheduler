package sos.scheduler.misc;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionString;

/** \class CopyJob2OrderParameterOptionsSuperClass - CopyJob2OrderParameter
 *
 * \brief An Options-Super-Class with all Options. This Class will be extended
 * by the "real" Options-class (\see CopyJob2OrderParameterOptions. The "real"
 * Option class will hold all the things, which are normaly overwritten at a new
 * generation of the super-class.
 *
 *
 * 
 *
 * see \see J:\E\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\
 * CopyJob2OrderParameter.xml for (more) details.
 * 
 * \verbatim ; mechanicaly created by from http://www.sos-berlin.com at
 * 20111104174144 \endverbatim \section OptionsTable Tabelle der vorhandenen
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
 * ("		CopyJob2OrderParameterOptionsSuperClass.auth_file", "test"); // This
 * parameter specifies the path and name of a user's pr return pobjHM; } //
 * private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim */
@JSOptionClass(name = "CopyJob2OrderParameterOptionsSuperClass", description = "CopyJob2OrderParameterOptionsSuperClass")
public class CopyJob2OrderParameterOptionsSuperClass extends JSOptionsClass {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    private final String conClassName = "CopyJob2OrderParameterOptionsSuperClass";
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(CopyJob2OrderParameterOptionsSuperClass.class);

    /** \var operation : */
    @JSOptionDefinition(name = "operation", description = "", key = "operation", type = "SOSOptionString", mandatory = false)
    public SOSOptionString operation = new SOSOptionString(this, conClassName + ".operation", // HashMap-Key
    "", // Titel
    "copy", // InitValue
    "copy", // DefaultValue
    false // isMandatory
    );

    /** \brief getoperation :
     * 
     * \details
     * 
     *
     * \return */
    public SOSOptionString getoperation() {
        return operation;
    }

    /** \brief setoperation :
     * 
     * \details
     * 
     *
     * @param operation : */
    public void setoperation(SOSOptionString p_operation) {
        this.operation = p_operation;
    }

    public CopyJob2OrderParameterOptionsSuperClass() {
        objParentClass = this.getClass();
    } // public CopyJob2OrderParameterOptionsSuperClass

    public CopyJob2OrderParameterOptionsSuperClass(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    } // public CopyJob2OrderParameterOptionsSuperClass

    //

    public CopyJob2OrderParameterOptionsSuperClass(HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    } // public CopyJob2OrderParameterOptionsSuperClass (HashMap JSSettings)

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
     * @param pobjJSSettings
     * @throws Exception */
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
     * @param pstrArgs
     * @throws Exception */
    @Override
    public void CommandLineArgs(String[] pstrArgs) {
        super.CommandLineArgs(pstrArgs);
        this.setAllOptions(super.objSettings);
    }
} // public class CopyJob2OrderParameterOptionsSuperClass