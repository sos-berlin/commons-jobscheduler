

package com.sos.VirtualFileSystem.Options.keepass4j;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

/**
 * \class 		SOSCredentialStoreOptions - SOSCredentialStore
 *
 * \brief
 * An Options as a container for the Options super class.
 * The Option class will hold all the things, which would be otherwise overwritten at a re-creation
 * of the super-class.
 *
 *

 *
 * see \see C:\Users\Mahendra\AppData\Local\Temp\scheduler_editor-3900348294966099242.html for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by JobDocu2OptionsClass.xslt from http://www.sos-berlin.com at 20140304141232
 * \endverbatim
 */
@JSOptionClass(name = "SOSCredentialStoreOptions", description = "SOSCredentialStore") public class SOSCredentialStoreOptions extends
		SOSCredentialStoreOptionsSuperClass {
	/**
	 *
	 */
	private static final long								serialVersionUID	= 1L;
	@SuppressWarnings("unused") private final String		conClassName		= this.getClass().getSimpleName();
	@SuppressWarnings("unused") private static final String	conSVNVersion		= "$Id$";
	@SuppressWarnings("unused") private final Logger		logger				= Logger.getLogger(this.getClass());

	/**
	* constructors
	*/
	public SOSCredentialStoreOptions() {
		logger.trace("constructor SOSCredentialStoreOptions");
	} // public SOSCredentialStoreOptions

	@Deprecated
	public SOSCredentialStoreOptions(final JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public SOSCredentialStoreOptions

		//

	public SOSCredentialStoreOptions (final HashMap <String, String> JSSettings) throws Exception {
		super(JSSettings);
	} // public SOSCredentialStoreOptions (HashMap JSSettings)
/**
 * \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
 *
 * \details
 * @throws Exception
 *
 * @throws Exception
 * - wird ausgelöst, wenn eine mandatory-Option keinen Wert hat
 */
		@Override  // SOSCredentialStoreOptionsSuperClass
	public void CheckMandatory() {
		try {
			super.CheckMandatory();
		}
		catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
	} // public void CheckMandatory ()
}

