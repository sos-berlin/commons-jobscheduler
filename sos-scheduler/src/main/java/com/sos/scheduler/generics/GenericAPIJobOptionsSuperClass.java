package com.sos.scheduler.generics;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionString;

/** \class GenericAPIJobOptionsSuperClass - A generic internal API job
 *
 * \brief An Options-Super-Class with all Options. This Class will be extended
 * by the "real" Options-class (\see GenericAPIJobOptions. The "real" Option
 * class will hold all the things, which are normaly overwritten at a new
 * generation of the super-class.
 *
 *
 * 
 *
 * see \see
 * C:\Users\KB\AppData\Local\Temp\scheduler_editor-2864692299059909179.html for
 * (more) details.
 * 
 * \verbatim ; mechanicaly created by
 * C:\Users\KB\sos-berlin.com\jobscheduler\scheduler
 * \config\JOETemplates\java\xsl\JSJobDoc2JSOptionSuperClass.xsl from
 * http://www.sos-berlin.com at 20120611173607 \endverbatim \section
 * OptionsTable Tabelle der vorhandenen Optionen
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
 * ("		GenericAPIJobOptionsSuperClass.auth_file", "test"); // This parameter
 * specifies the path and name of a user's pr return pobjHM; } // private void
 * SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM) \endverbatim */
@JSOptionClass(name = "GenericAPIJobOptionsSuperClass", description = "GenericAPIJobOptionsSuperClass")
public class GenericAPIJobOptionsSuperClass extends JSOptionsClass {

    /**
	 * 
	 */
    private static final long serialVersionUID = 7680682721378489041L;
    private final String conClassName = "GenericAPIJobOptionsSuperClass";
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(GenericAPIJobOptionsSuperClass.class);

    /** \var javaClassName : The Name of the Java Class (e.g. a JS Adapter Class)
     * which has to be e The Name of the Java Class (e.g. a JS Adapter Class)
     * which has to be executed by this generic JS adapter. */
    @JSOptionDefinition(name = "javaClassName", description = "The Name of the Java Class (e.g. a JS Adapter Class) which has to be e", key = "javaClassName", type = "SOSOptionString", mandatory = true)
    public SOSOptionString javaClassName = new SOSOptionString(this, conClassName + ".javaClassName", // HashMap-Key
    "The Name of the Java Class (e.g. a JS Adapter Class) which has to be e", // Titel
    "", // InitValue
    "", // DefaultValue
    true // isMandatory
    );

    /** \brief getjavaClassName : The Name of the Java Class (e.g. a JS Adapter
     * Class) which has to be e
     * 
     * \details The Name of the Java Class (e.g. a JS Adapter Class) which has
     * to be executed by this generic JS adapter.
     *
     * \return The Name of the Java Class (e.g. a JS Adapter Class) which has to
     * be e */
    public SOSOptionString getjavaClassName() {
        return javaClassName;
    }

    /** \brief setjavaClassName : The Name of the Java Class (e.g. a JS Adapter
     * Class) which has to be e
     * 
     * \details The Name of the Java Class (e.g. a JS Adapter Class) which has
     * to be executed by this generic JS adapter.
     *
     * @param javaClassName : The Name of the Java Class (e.g. a JS Adapter
     *            Class) which has to be e */
    public void setjavaClassName(final SOSOptionString p_javaClassName) {
        javaClassName = p_javaClassName;
    }

    /** \var javaClassPath : */
    @JSOptionDefinition(name = "javaClassPath", description = "", key = "javaClassPath", type = "SOSOptionString", mandatory = false)
    public SOSOptionString javaClassPath = new SOSOptionString(this, conClassName + ".javaClassPath", // HashMap-Key
    "", // Titel
    " ", // InitValue
    " ", // DefaultValue
    false // isMandatory
    );

    /** \brief getjavaClassPath :
     * 
     * \details
     * 
     *
     * \return */
    public SOSOptionString getjavaClassPath() {
        return javaClassPath;
    }

    /** \brief setjavaClassPath :
     * 
     * \details
     * 
     *
     * @param javaClassPath : */
    public void setjavaClassPath(final SOSOptionString p_javaClassPath) {
        javaClassPath = p_javaClassPath;
    }

    public GenericAPIJobOptionsSuperClass() {
        objParentClass = this.getClass();
    } // public GenericAPIJobOptionsSuperClass

    public GenericAPIJobOptionsSuperClass(final JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    } // public GenericAPIJobOptionsSuperClass

    //

    public GenericAPIJobOptionsSuperClass(final HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    } // public GenericAPIJobOptionsSuperClass (HashMap JSSettings)

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
    public void CommandLineArgs(final String[] pstrArgs) {
        super.CommandLineArgs(pstrArgs);
        this.setAllOptions(super.objSettings);
    }
} // public class GenericAPIJobOptionsSuperClass