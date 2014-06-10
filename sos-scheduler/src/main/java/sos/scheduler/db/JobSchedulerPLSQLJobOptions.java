

package sos.scheduler.db;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

/**
 * \class 		JobSchedulerPLSQLJobOptions - Launch Database Statement
 *
 * \brief 
 * An Options as a container for the Options super class. 
 * The Option class will hold all the things, which would be otherwise overwritten at a re-creation
 * of the super-class.
 *
 *

 *
 * see \see R:\backup\sos\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\JobSchedulerPLSQLJob.xml for (more) details.
 * 
 * \verbatim ;
 * mechanicaly created by JobDocu2OptionsClass.xslt from http://www.sos-berlin.com at 20120905153438 
 * \endverbatim
 */
@JSOptionClass(name = "JobSchedulerPLSQLJobOptions", description = "Launch Database Statement")
public class JobSchedulerPLSQLJobOptions extends JobSchedulerPLSQLJobOptionsSuperClass {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -2492091654517629849L;
	@SuppressWarnings("unused")  
	private final String					conClassName						= "JobSchedulerPLSQLJobOptions";  //$NON-NLS-1$
	@SuppressWarnings("unused")
	private static Logger		logger			= Logger.getLogger(JobSchedulerPLSQLJobOptions.class);

    /**
    * constructors
    */
    
	public JobSchedulerPLSQLJobOptions() {
	} // public JobSchedulerPLSQLJobOptions

	@Deprecated
	public JobSchedulerPLSQLJobOptions(final JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public JobSchedulerPLSQLJobOptions

		//

	public JobSchedulerPLSQLJobOptions (final HashMap <String, String> JSSettings) throws Exception {
		super(JSSettings);
		super.setChildClasses(JSSettings, EMPTY_STRING);
	} // public JobSchedulerPLSQLJobOptions (HashMap JSSettings)
/**
 * \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
 *
 * \details
 * @throws Exception
 *
 * @throws Exception
 * - wird ausgelöst, wenn eine mandatory-Option keinen Wert hat
 */
		@Override  // JobSchedulerPLSQLJobOptionsSuperClass
	public void CheckMandatory() {
		try {
			super.CheckMandatory();
		}
		catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
	} // public void CheckMandatory ()
}

