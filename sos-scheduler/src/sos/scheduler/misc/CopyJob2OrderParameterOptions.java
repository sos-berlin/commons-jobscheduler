

package sos.scheduler.misc;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

/**
 * \class 		CopyJob2OrderParameterOptions - CopyJob2OrderParameter
 *
 * \brief 
 * An Options as a container for the Options super class. 
 * The Option class will hold all the things, which would be otherwise overwritten at a re-creation
 * of the super-class.
 *
 *

 *
 * see \see J:\E\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\CopyJob2OrderParameter.xml for (more) details.
 * 
 * \verbatim ;
 * mechanicaly created by JobDocu2OptionsClass.xslt from http://www.sos-berlin.com at 20111104174214 
 * \endverbatim
 */
@JSOptionClass(name = "CopyJob2OrderParameterOptions", description = "CopyJob2OrderParameter")
public class CopyJob2OrderParameterOptions extends CopyJob2OrderParameterOptionsSuperClass {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -2512565386435668056L;
	@SuppressWarnings("unused")  //$NON-NLS-1$
	private final String					conClassName						= "CopyJob2OrderParameterOptions";  //$NON-NLS-1$
	@SuppressWarnings("unused")
	private static Logger		logger			= Logger.getLogger(CopyJob2OrderParameterOptions.class);

    /**
    * constructors
    */
    
	public CopyJob2OrderParameterOptions() {
	} // public CopyJob2OrderParameterOptions

	public CopyJob2OrderParameterOptions(JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public CopyJob2OrderParameterOptions

		//

	public CopyJob2OrderParameterOptions (HashMap <String, String> JSSettings) throws Exception {
		super(JSSettings);
	} // public CopyJob2OrderParameterOptions (HashMap JSSettings)
/**
 * \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
 *
 * \details
 * @throws Exception
 *
 * @throws Exception
 * - wird ausgelöst, wenn eine mandatory-Option keinen Wert hat
 */
		@Override  // CopyJob2OrderParameterOptionsSuperClass
	public void CheckMandatory() {
		try {
			super.CheckMandatory();
		}
		catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
	} // public void CheckMandatory ()
}

