package sos.mail.options;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

/** \class Mail2ActionOptions - Pro Email eine oder mehrere Aktionen ausführen
 *
 * \brief An Options as a container for the Options super class. The Option
 * class will hold all the things, which would be otherwise overwritten at a
 * re-creation of the super-class.
 *
 *
 * 
 *
 * see \see
 * C:\Users\oh\AppData\Local\Temp\scheduler_editor-2042452986889562531.html for
 * (more) details.
 *
 * \verbatim ; mechanicaly created by JobDocu2OptionsClass.xslt from
 * http://www.sos-berlin.com at 20121019122956 \endverbatim */
@JSOptionClass(name = "Mail2ActionOptions", description = "Pro Email eine oder mehrere Aktionen ausführen")
public class SOSMailProcessInboxOptions extends SOSMailProcessInboxOptionsSuperClass {

    /**
	 *
	 */
    private static final long serialVersionUID = -9129523596171879292L;
    @SuppressWarnings("unused")
    private final String conClassName = "SOSMailProcessInboxOptions";						//$NON-NLS-1$
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(SOSMailProcessInboxOptions.class);

    /** constructors */

    public SOSMailProcessInboxOptions() {
    } // public Mail2ActionOptions

    public SOSMailProcessInboxOptions(final JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    } // public Mail2ActionOptions

    //

    public SOSMailProcessInboxOptions(final HashMap<String, String> JSSettings) throws Exception {
        super(JSSettings);
    } // public Mail2ActionOptions (HashMap JSSettings)

    /** \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
     *
     * \details
     * 
     * @throws Exception
     *
     * @throws Exception - wird ausgelöst, wenn eine mandatory-Option keinen
     *             Wert hat */
    @Override
    // Mail2ActionOptionsSuperClass
    public void CheckMandatory() {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    } // public void CheckMandatory ()
}
