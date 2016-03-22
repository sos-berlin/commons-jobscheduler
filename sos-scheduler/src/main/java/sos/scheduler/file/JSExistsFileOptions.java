package sos.scheduler.file;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

/** \class JSExistsFileOptions - check wether a file exist
 *
 * \brief An Options as a container for the Options super class. The Option
 * class will hold all the things, which would be otherwise overwritten at a
 * re-creation of the super-class.
 *
 *
 * 
 *
 * see \see C:\Users\KB\Documents\xmltest\JSExistFile.xml for (more) details.
 * 
 * \verbatim ; mechanicaly created by JobDocu2OptionsClass.xslt from
 * http://www.sos-berlin.com at 20110820120923 \endverbatim */
@JSOptionClass(name = "JSExistsFileOptions", description = "check wether a file exist")
public class JSExistsFileOptions extends JSExistsFileOptionsSuperClass {

    private static final long serialVersionUID = 1731085195875420660L;
    @SuppressWarnings("unused")//$NON-NLS-1$
    private final String conClassName = "JSExistsFileOptions";						//$NON-NLS-1$
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(JSExistsFileOptions.class);

    /** constructors */

    public JSExistsFileOptions() {
    } // public JSExistsFileOptions

    public JSExistsFileOptions(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    } // public JSExistsFileOptions

    //

    public JSExistsFileOptions(HashMap<String, String> JSSettings) throws Exception {
        super(JSSettings);
    } // public JSExistsFileOptions (HashMap JSSettings)

    /** \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
     *
     * \details
     * 
     * @throws Exception
     *
     * @throws Exception - wird ausgelöst, wenn eine mandatory-Option keinen
     *             Wert hat */
    @Override
    // JSExistFileOptionsSuperClass
    public void CheckMandatory() {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    } // public void CheckMandatory ()
}
