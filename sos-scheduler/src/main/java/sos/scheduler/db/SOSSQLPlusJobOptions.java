package sos.scheduler.db;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

/**
 * \class 		SOSSQLPlusJobOptions - Start SQL*Plus client and execute sql*plus programs
 *
 * \brief
 * An Options as a container for the Options super class.
 * The Option class will hold all the things, which would be otherwise overwritten at a re-creation
 * of the super-class.
 *
 *

 *
 * see \see R:\backup\sos\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\SOSSQLPlusJob.xml for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by JobDocu2OptionsClass.xslt from http://www.sos-berlin.com at 20120927163928
 * \endverbatim
 */
@JSOptionClass(name = "SOSSQLPlusJobOptions", description = "Start SQL*Plus client and execute sql*plus programs")
public class SOSSQLPlusJobOptions extends SOSSQLPlusJobOptionsSuperClass {
	/**
	 *
	 */
	private static final long	serialVersionUID	= 7612674598767191212L;
	@SuppressWarnings("unused")
	private final String		conClassName		= "SOSSQLPlusJobOptions";						//$NON-NLS-1$
	@SuppressWarnings("unused")
	private static Logger		logger				= Logger.getLogger(SOSSQLPlusJobOptions.class);

	/**
	* constructors
	*/

	public SOSSQLPlusJobOptions() {
	} // public SOSSQLPlusJobOptions

	public SOSSQLPlusJobOptions(final JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public SOSSQLPlusJobOptions

	//

	public SOSSQLPlusJobOptions(final HashMap<String, String> JSSettings) throws Exception {
		super(JSSettings);
	} // public SOSSQLPlusJobOptions (HashMap JSSettings)

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
	// SOSSQLPlusJobOptionsSuperClass
	public void CheckMandatory() {
		try {
			super.CheckMandatory();
		}
		catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
	} // public void CheckMandatory ()

	public String getConnectionString() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getConnectionString";
		String strT = "";

		if (db_user.isDirty() == true) {
			strT = db_user.Value() + "/" + db_password.Value() + "@" + db_url.Value();
		}
			return strT;
	} // private String getConnectionString
}
