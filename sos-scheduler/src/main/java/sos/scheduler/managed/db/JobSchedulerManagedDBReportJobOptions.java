

package sos.scheduler.managed.db;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;

import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener; 
import org.apache.log4j.Logger;

/**
 * \class 		JobSchedulerManagedDBReportJobOptions - Launch Database Report
 *
 * \brief 
 * An Options as a container for the Options super class. 
 * The Option class will hold all the things, which would be otherwise overwritten at a re-creation
 * of the super-class.
 *
 *

 *
 * see \see R:\backup\sos\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\JobSchedulerManagedDBReportJob.xml for (more) details.
 * 
 * \verbatim ;
 * mechanicaly created by JobDocu2OptionsClass.xslt from http://www.sos-berlin.com at 20120830214259 
 * \endverbatim
 */
@JSOptionClass(name = "JobSchedulerManagedDBReportJobOptions", description = "Launch Database Report")
public class JobSchedulerManagedDBReportJobOptions extends JobSchedulerManagedDBReportJobOptionsSuperClass {
	@SuppressWarnings("unused")  //$NON-NLS-1$
	private final String					conClassName						= "JobSchedulerManagedDBReportJobOptions";  //$NON-NLS-1$
	@SuppressWarnings("unused")
	private static Logger		logger			= Logger.getLogger(JobSchedulerManagedDBReportJobOptions.class);

    /**
    * constructors
    */
    
	public JobSchedulerManagedDBReportJobOptions() {
	} // public JobSchedulerManagedDBReportJobOptions

	public JobSchedulerManagedDBReportJobOptions(JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public JobSchedulerManagedDBReportJobOptions

		//

	public JobSchedulerManagedDBReportJobOptions (HashMap <String, String> JSSettings) throws Exception {
		super(JSSettings);
	} // public JobSchedulerManagedDBReportJobOptions (HashMap JSSettings)
/**
 * \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
 *
 * \details
 * @throws Exception
 *
 * @throws Exception
 * - wird ausgelöst, wenn eine mandatory-Option keinen Wert hat
 */
		@Override  // JobSchedulerManagedDBReportJobOptionsSuperClass
	public void CheckMandatory() {
		try {
			super.CheckMandatory();
		}
		catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
	} // public void CheckMandatory ()
}

