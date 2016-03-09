package sos.scheduler.reports;

import java.util.HashMap;

import com.sos.JSHelper.Options.*;
import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import org.apache.log4j.Logger;

import com.sos.JSHelper.Options.*;

/** \class JSReportAllParametersOptionsSuperClass - Report all Parameters
 *
 * \brief An Options-Super-Class with all Options. This Class will be extended
 * by the "real" Options-class (\see JSReportAllParametersOptions. The "real"
 * Option class will hold all the things, which are normaly overwritten at a new
 * generation of the super-class.
 *
 *
 * 
 *
 * see \see J:\E\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\
 * JSReportAllParameters.xml for (more) details.
 * 
 * \verbatim ; mechanicaly created by from http://www.sos-berlin.com at
 * 20110516150342 \endverbatim \section OptionsTable Tabelle der vorhandenen
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
 * ("		JSReportAllParametersOptionsSuperClass.auth_file", "test"); // This
 * parameter specifies the path and name of a user's pr return pobjHM; } //
 * private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim */
@JSOptionClass(name = "JSReportAllParametersOptionsSuperClass", description = "JSReportAllParametersOptionsSuperClass")
public class JSReportAllParametersOptionsSuperClass extends JSOptionsClass {

    private final String conClassName = "JSReportAllParametersOptionsSuperClass";
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(JSReportAllParametersOptionsSuperClass.class);

    /** \var ReportFileName : The Name of the Report-File. The names and values
     * of all parameters a The Name of the Report-File. The names and values of
     * all parameters are written to this file if a file- (and path)name ist
     * specified. The format of the report is specified by the parameter
     * reportFormat. */
    @JSOptionDefinition(name = "ReportFileName", description = "The Name of the Report-File. The names and values of all parameters a", key = "ReportFileName", type = "SOSOptionString", mandatory = false)
    public SOSOptionString ReportFileName = new SOSOptionString(this, conClassName + ".ReportFileName", // HashMap-Key
    "The Name of the Report-File. The names and values of all parameters a", // Titel
    " ", // InitValue
    " ", // DefaultValue
    false // isMandatory
    );

    /** \brief getReportFileName : The Name of the Report-File. The names and
     * values of all parameters a
     * 
     * \details The Name of the Report-File. The names and values of all
     * parameters are written to this file if a file- (and path)name ist
     * specified. The format of the report is specified by the parameter
     * reportFormat.
     *
     * \return The Name of the Report-File. The names and values of all
     * parameters a */
    public SOSOptionString getReportFileName() {
        return ReportFileName;
    }

    /** \brief setReportFileName : The Name of the Report-File. The names and
     * values of all parameters a
     * 
     * \details The Name of the Report-File. The names and values of all
     * parameters are written to this file if a file- (and path)name ist
     * specified. The format of the report is specified by the parameter
     * reportFormat.
     *
     * @param ReportFileName : The Name of the Report-File. The names and values
     *            of all parameters a */
    public void setReportFileName(SOSOptionString p_ReportFileName) {
        this.ReportFileName = p_ReportFileName;
    }

    /** \var ReportFormat : The Format of the report is specified with this
     * parameter. possbile V The Format of the report is specified with this
     * parameter. possbile Values are 'text', 'xml', ... */
    @JSOptionDefinition(name = "ReportFormat", description = "The Format of the report is specified with this parameter. possbile V", key = "ReportFormat", type = "SOSOptionString", mandatory = false)
    public SOSOptionString ReportFormat = new SOSOptionString(this, conClassName + ".ReportFormat", // HashMap-Key
    "The Format of the report is specified with this parameter. possbile V", // Titel
    "text", // InitValue
    "text", // DefaultValue
    false // isMandatory
    );

    /** \brief getReportFormat : The Format of the report is specified with this
     * parameter. possbile V
     * 
     * \details The Format of the report is specified with this parameter.
     * possbile Values are 'text', 'xml', ...
     *
     * \return The Format of the report is specified with this parameter.
     * possbile V */
    public SOSOptionString getReportFormat() {
        return ReportFormat;
    }

    /** \brief setReportFormat : The Format of the report is specified with this
     * parameter. possbile V
     * 
     * \details The Format of the report is specified with this parameter.
     * possbile Values are 'text', 'xml', ...
     *
     * @param ReportFormat : The Format of the report is specified with this
     *            parameter. possbile V */
    public void setReportFormat(SOSOptionString p_ReportFormat) {
        this.ReportFormat = p_ReportFormat;
    }

    public JSReportAllParametersOptionsSuperClass() {
        objParentClass = this.getClass();
    } // public JSReportAllParametersOptionsSuperClass

    public JSReportAllParametersOptionsSuperClass(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    } // public JSReportAllParametersOptionsSuperClass

    //

    public JSReportAllParametersOptionsSuperClass(HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    } // public JSReportAllParametersOptionsSuperClass (HashMap JSSettings)

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
} // public class JSReportAllParametersOptionsSuperClass