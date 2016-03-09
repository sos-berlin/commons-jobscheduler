package sos.scheduler.LaunchAndObserve;

import java.util.HashMap;

import org.apache.log4j.Logger;

import sos.net.mail.options.SOSSmtpMailOptions;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.interfaces.ISOSSmtpMailOptions;

/** \class JobSchedulerLaunchAndObserveOptions - Launch and observe any given job
 * or job chain
 *
 * \brief An Options as a container for the Options super class. The Option
 * class will hold all the things, which would be otherwise overwritten at a
 * re-creation of the super-class.
 *
 *
 * 
 *
 * see \see J:\E\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\
 * JobSchedulerLaunchAndObserve.xml for (more) details.
 * 
 * \verbatim ; mechanicaly created by JobDocu2OptionsClass.xslt from
 * http://www.sos-berlin.com at 20111124184709 \endverbatim */
@JSOptionClass(name = "JobSchedulerLaunchAndObserveOptions", description = "Launch and observe any given job or job chain")
public class JobSchedulerLaunchAndObserveOptions extends JobSchedulerLaunchAndObserveOptionsSuperClass {

    /**
	 * 
	 */
    private static final long serialVersionUID = -2227906861871375733L;
    @SuppressWarnings("unused")
    private final String conClassName = "JobSchedulerLaunchAndObserveOptions";						//$NON-NLS-1$
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(JobSchedulerLaunchAndObserveOptions.class);

    private SOSSmtpMailOptions objMailOnRestartOptions = null;
    private SOSSmtpMailOptions objMailOnKillOptions = null;

    /** constructors */

    public JobSchedulerLaunchAndObserveOptions() {
    } // public JobSchedulerLaunchAndObserveOptions

    public JobSchedulerLaunchAndObserveOptions(final JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    } // public JobSchedulerLaunchAndObserveOptions

    //

    public JobSchedulerLaunchAndObserveOptions(final HashMap<String, String> JSSettings) throws Exception {
        super(JSSettings);
        setChildClasses(JSSettings);
    } // public JobSchedulerLaunchAndObserveOptions (HashMap JSSettings)

    @Override
    public void setChildClasses(final HashMap<String, String> pobjJSSettings) throws Exception {
        // objMailOnRestartOptions = new SOSSmtpMailOptions(pobjJSSettings);
        // objMailOnKillOptions = new SOSSmtpMailOptions(pobjJSSettings);
        //
        // logger.debug(String.format("set par ameter for prefix '%1$s'",
        // "MailOnRestart_"));
        // objMailOnRestartOptions.setAllOptions(pobjJSSettings,
        // "MailOnRestart_");
        // logger.debug(String.format("set parameter for prefix '%1$s'",
        // "MailOnKill_"));
        // objMailOnKillOptions.setAllOptions(pobjJSSettings, "MailOnKill_");
    }

    public ISOSSmtpMailOptions getMailOnRestartOptions() {

        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::getMailOnRestartOptions";

        if (objMailOnRestartOptions == null) {
            objMailOnRestartOptions = new SOSSmtpMailOptions();
        }

        return objMailOnRestartOptions;
    } // private ISOSSmtpMailOptions getMailOnRestartOptions

    public ISOSSmtpMailOptions getMailOnKillOptions() {

        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::getMailOnKillOptions";

        if (objMailOnKillOptions == null) {
            objMailOnKillOptions = new SOSSmtpMailOptions();
        }
        return objMailOnKillOptions;
    } // private ISOSSmtpMailOptions getMailOnRestartOptions

    /** \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
     *
     * \details
     * 
     * @throws Exception
     *
     * @throws Exception - wird ausgelöst, wenn eine mandatory-Option keinen
     *             Wert hat */
    @Override
    // JobSchedulerLaunchAndObserveOptionsSuperClass
    public void CheckMandatory() {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    } // public void CheckMandatory ()
}
