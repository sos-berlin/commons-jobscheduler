package sos.scheduler.file;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.i18n.annotation.I18NResourceBundle;

/**
 * \class 		JobSchedulerFolderTreeOptions - check wether a file exist
 *
 * \brief 
 * An Options as a container for the Options super class. 
 * The Option class will hold all the things, which would be otherwise overwritten at a re-creation
 * of the super-class.
 *
 *

 *
 * see \see J:\E\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\JobSchedulerFolderTree.xml for (more) details.
 * 
 * \verbatim ;
 * mechanicaly created by JobDocu2OptionsClass.xslt from http://www.sos-berlin.com at 20110805104747 
 * \endverbatim
 */
@I18NResourceBundle(baseName = "com.sos.scheduler.messages", defaultLocale = "en")
@JSOptionClass(name = "JobSchedulerFolderTreeOptions", description = "check wether a file exist")
public class JobSchedulerFolderTreeOptions extends JobSchedulerFolderTreeOptionsSuperClass {
	private static final long	serialVersionUID	= -2549063484040650336L;
	@SuppressWarnings("unused")//$NON-NLS-1$
	private final String		conClassName		= "JobSchedulerFolderTreeOptions";										//$NON-NLS-1$
	@SuppressWarnings("unused")
	private static Logger		logger				= Logger.getLogger(JobSchedulerFolderTreeOptions.class);
	private final String		conSVNVersion		= "$Id$";

	/**
	* constructors
	*/

	public JobSchedulerFolderTreeOptions() {
	} // public JobSchedulerFolderTreeOptions

	public JobSchedulerFolderTreeOptions(JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public JobSchedulerFolderTreeOptions

	//

	public JobSchedulerFolderTreeOptions(HashMap<String, String> JSSettings) throws Exception {
		super(JSSettings);
	} // public JobSchedulerFolderTreeOptions (HashMap JSSettings)

	/**
	 * \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
	 *
	 * \details
	 * @throws Exception
	 *
	 * @throws Exception
	 * - wird ausgelöst, wenn eine mandatory-Option keinen Wert hat
	 */
	@Override
	// JobSchedulerFolderTreeOptionsSuperClass
	public void CheckMandatory() {
		try {
			super.CheckMandatory();
		}
		catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
	} // public void CheckMandatory ()
}
