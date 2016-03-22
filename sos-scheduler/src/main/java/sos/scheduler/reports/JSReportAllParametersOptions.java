package sos.scheduler.reports;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;

import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import org.apache.log4j.Logger;

/** \class JSReportAllParametersOptions - Report all Parameters
 *
 * \brief An Options as a container for the Options super class. The Option
 * class will hold all the things, which would be otherwise overwritten at a
 * re-creation of the super-class.
 *
 *
 * 
 *
 * see \see J:\E\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\
 * JSReportAllParameters.xml for (more) details.
 * 
 * \verbatim ; mechanicaly created by JobDocu2OptionsClass.xslt from
 * http://www.sos-berlin.com at 20110516150353 \endverbatim */
@JSOptionClass(name = "JSReportAllParametersOptions", description = "Report all Parameters")
public class JSReportAllParametersOptions extends JSReportAllParametersOptionsSuperClass {

    @SuppressWarnings("unused")//$NON-NLS-1$
    private final String conClassName = "JSReportAllParametersOptions";  //$NON-NLS-1$
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(JSReportAllParametersOptions.class);

    /** constructors */

    public JSReportAllParametersOptions() {
    } // public JSReportAllParametersOptions

    public JSReportAllParametersOptions(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    } // public JSReportAllParametersOptions

    //

    public JSReportAllParametersOptions(HashMap<String, String> JSSettings) throws Exception {
        super(JSSettings);
    } // public JSReportAllParametersOptions (HashMap JSSettings)

    /** \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
     *
     * \details
     * 
     * @throws Exception
     *
     * @throws Exception - wird ausgelöst, wenn eine mandatory-Option keinen
     *             Wert hat */
    @Override
    // JSReportAllParametersOptionsSuperClass
    public void CheckMandatory() {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    } // public void CheckMandatory ()
}
