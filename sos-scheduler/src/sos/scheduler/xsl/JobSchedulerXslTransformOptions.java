

package sos.scheduler.xsl;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

/**
 * \class 		JobSchedulerXslTransformationOptions - JobSchedulerXslTransform
 *
 * \brief 
 * An Options as a container for the Options super class. 
 * The Option class will hold all the things, which would be otherwise overwritten at a re-creation
 * of the super-class.
 *
 *

 *
 * see \see J:\E\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\JobSchedulerXslTransform.xml for (more) details.
 * 
 * \verbatim ;
 * mechanicaly created by JobDocu2OptionsClass.xslt from http://www.sos-berlin.com at 20110815114150 
 * \endverbatim
 */
@JSOptionClass(name = "JobSchedulerXslTransformationOptions", description = "JobSchedulerXslTransform")
public class JobSchedulerXslTransformOptions extends JobSchedulerXslTransformOptionsSuperClass {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -5277454257958660505L;
	@SuppressWarnings("unused")  //$NON-NLS-1$
	private final String					conClassName						= "JobSchedulerXslTransformationOptions";  //$NON-NLS-1$
	@SuppressWarnings("unused")
	private static Logger		logger			= Logger.getLogger(JobSchedulerXslTransformOptions.class);

    /**
    * constructors
    */
    
	public JobSchedulerXslTransformOptions() {
	} // public JobSchedulerXslTransformationOptions

	public JobSchedulerXslTransformOptions(JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public JobSchedulerXslTransformationOptions

		//

	public JobSchedulerXslTransformOptions (HashMap <String, String> JSSettings) throws Exception {
		super(JSSettings);
	} // public JobSchedulerXslTransformationOptions (HashMap JSSettings)
/**
 * \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
 *
 * \details
 * @throws Exception
 *
 * @throws Exception
 * - wird ausgelöst, wenn eine mandatory-Option keinen Wert hat
 */
		@Override  // JobSchedulerXslTransformOptionsSuperClass
	public void CheckMandatory() {
		try {
			super.CheckMandatory();
		}
		catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
	} // public void CheckMandatory ()
}

