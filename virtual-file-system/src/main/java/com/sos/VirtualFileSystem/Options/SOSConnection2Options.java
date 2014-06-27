package com.sos.VirtualFileSystem.Options;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.VirtualFileSystem.common.SOSVfsMessageCodes;
import com.sos.i18n.annotation.I18NResourceBundle;

/**
 * \class 		SOSConnection2Options - Options for a connection to an uri (server, site, e.g.)
 *
 * \brief
 * An Options as a container for the Options super class.
 * The Option class will hold all the things, which would be otherwise overwritten at a re-creation
 * of the super-class.
 *
 *

 *
 * see \see j:\e\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\SOSConnection2.xml for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by JobDocu2OptionsClass.xslt from http://www.sos-berlin.com at 20100917112404
 * \endverbatim
 */
@JSOptionClass(name = "SOSConnection2Options", description = "Options for a connection to an uri (server, site, e.g.)") @I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en") public class SOSConnection2Options
		extends SOSConnection2OptionsSuperClass {
	/**
	 *
	 */
	private static final long																							serialVersionUID		= 6485361196241983182L;
	@SuppressWarnings("unused") private final String																	conClassName			= "SOSConnection2Options";							//$NON-NLS-1$
	@SuppressWarnings("unused") private final static Logger																logger					= Logger.getLogger(SOSConnection2Options.class);
	private final String																								strAlternativePrefix	= "";
	@JSOptionClass(description = "", name = "SOSConnection2OptionsAlternate") private SOSConnection2OptionsAlternate	objAlternativeOptions	= null;
	@JSOptionClass(description = "", name = "SOSConnection2OptionsAlternate") private SOSConnection2OptionsAlternate	objSourceOptions		= null;
	@JSOptionClass(description = "", name = "SOSConnection2OptionsAlternate") private SOSConnection2OptionsAlternate	objTargetOptions		= null;
	@JSOptionClass(description = "", name = "SOSConnection2OptionsAlternate") private SOSConnection2OptionsAlternate	objJumpServerOptions	= null;
	@JSOptionClass(description = "", name = "SOSConnection2OptionsAlternate") private SOSConnection2OptionsAlternate	objProxyServerOptions	= null;

	/**
	* constructors
	*/
	public SOSConnection2Options() {
		super();
		initChildOptions();
	} // public SOSConnection2Options

	public SOSConnection2Options(final String strPrefix) {
	} // public SOSConnection2Options

	@Deprecated public SOSConnection2Options(final JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public SOSConnection2Options

	private void initChildOptions() {
		logger.trace("initChildOptions");
		if (objAlternativeOptions == null) {
			objAlternativeOptions = new SOSConnection2OptionsAlternate("");
		}
		if (objSourceOptions == null) {
			objSourceOptions = new SOSConnection2OptionsAlternate("");
		}
		objSourceOptions.isSource = true;
		if (objTargetOptions == null) {
			objTargetOptions = new SOSConnection2OptionsAlternate("");
			objTargetOptions.isSource = false;
		}
		if (objJumpServerOptions == null) {
			objJumpServerOptions = new SOSConnection2OptionsAlternate("");
		}
		if (objProxyServerOptions == null) {
			objProxyServerOptions = new SOSConnection2OptionsAlternate("");
		}
	}

	public SOSConnection2Options(final HashMap<String, String> pobjJSSettings) throws Exception {
		super(pobjJSSettings);
		initChildOptions();
		setPrefixedValues(pobjJSSettings);
	} // public SOSConnection2Options (HashMap JSSettings)

	public void setPrefixedValues(final HashMap<String, String> pobjJSSettings) throws Exception {
		logger.trace("setPrefixedValues");
		logger.trace(SOSVfsMessageCodes.SOSVfs_T_267.params(conParamNamePrefixALTERNATIVE));
		objAlternativeOptions.setAllOptions(pobjJSSettings, strAlternativePrefix + conParamNamePrefixALTERNATIVE);
		this.addProcessedOptions(objAlternativeOptions.getProcessedOptions());
		objAlternativeOptions.setAllOptions(pobjJSSettings);
		this.addProcessedOptions(objAlternativeOptions.getProcessedOptions());
		//
		logger.trace(SOSVfsMessageCodes.SOSVfs_T_267.params(conParamNamePrefixSOURCE));
		objSourceOptions.setAllOptions(pobjJSSettings, conParamNamePrefixSOURCE);
		objSourceOptions.Alternatives().setChildClasses(pobjJSSettings, conParamNamePrefixSOURCE);
		objSourceOptions.setChildClasses(pobjJSSettings, conParamNamePrefixSOURCE);
		this.addProcessedOptions(objSourceOptions.getProcessedOptions());
		logger.trace(SOSVfsMessageCodes.SOSVfs_T_268.params(objSourceOptions.dirtyString()));
		//
		logger.trace(SOSVfsMessageCodes.SOSVfs_T_267.params(conParamNamePrefixTARGET));
		objTargetOptions.setAllOptions(pobjJSSettings, conParamNamePrefixTARGET);
		objTargetOptions.Alternatives().setChildClasses(pobjJSSettings, conParamNamePrefixTARGET);
		objTargetOptions.setChildClasses(pobjJSSettings, conParamNamePrefixTARGET);
		this.addProcessedOptions(objTargetOptions.getProcessedOptions());
		logger.trace(SOSVfsMessageCodes.SOSVfs_T_269.params(objTargetOptions.dirtyString()));
		//
		//		logger.trace(String.format("set parameter for prefix '%1$s'", "jump_"));
		objJumpServerOptions.setAllOptions(pobjJSSettings, conParamNamePrefixJUMP);
		this.addProcessedOptions(objJumpServerOptions.getProcessedOptions());
	} // public SOSConnection2Options (HashMap JSSettings)

	/**
	 * \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
	 *
	 * \details
	 * @throws Exception
	 *
	 * @throws Exception
	 * - wird ausgelöst, wenn eine mandatory-Option keinen Wert hat
	 */
	@Override// SOSConnection2OptionsSuperClass
	public void CheckMandatory() {
		try {
			super.CheckMandatory();
		}
		catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
	} // public void CheckMandatory ()

	/**
	 * \brief getobjAlternativeOptions
	 *
	 * \details
	 * getter
	 *
	 * @return the objAlternativeOptions
	 */
	public SOSConnection2OptionsAlternate Alternatives() {
		return objAlternativeOptions;
	}

	/**
	 * \brief setobjAlternativeOptions -
	 *
	 * \details
	 * setter
	 *
	 * @param objAlternativeOptions the value for objAlternativeOptions to set
	 */
	public void Alternatives(final SOSConnection2OptionsAlternate pobjAlternativeOptions) {
		if (objAlternativeOptions == null) {
			objAlternativeOptions = new SOSConnection2OptionsAlternate(conParamNamePrefixALTERNATIVE);
		}
		objAlternativeOptions = pobjAlternativeOptions;
	}

	/**
	 * \brief Source
	 *
	 * \details
	 * getter
	 *
	 * @return the Source
	 */
	public SOSConnection2OptionsAlternate Source() {
		if (objSourceOptions == null) {
			objSourceOptions = new SOSConnection2OptionsAlternate(conParamNamePrefixSOURCE);
		}
		return objSourceOptions;
	}

	/**
	 * \brief setobjSourceOptions -
	 *
	 * \details
	 * setter
	 *
	 * @param objSourceOptions the value for objSourceOptions to set
	 */
	public void Source(final SOSConnection2OptionsAlternate pobjSourceOptions) {
		objSourceOptions = pobjSourceOptions;
	}

	/**
	 * \brief getobjJumpServerOptions
	 *
	 * \details
	 * getter
	 *
	 * @return the objJumpServerOptions
	 */
	public SOSConnection2OptionsAlternate JumpServer() {
		if (objJumpServerOptions == null) {
			objJumpServerOptions = new SOSConnection2OptionsAlternate("");
		}
		return objJumpServerOptions;
	}

	/**
	 * \brief setobjJumpServerOptions -
	 *
	 * \details
	 * setter
	 *
	 * @param objJumpServerOptions the value for objJumpServerOptions to set
	 */
	public void JumpServer(final SOSConnection2OptionsAlternate pobjJumpServerOptions) {
		objJumpServerOptions = pobjJumpServerOptions;
	}

	/**
	 * \brief getobjTargetOptions
	 *
	 * \details
	 * getter
	 *
	 * @return the objTargetOptions
	 */
	public SOSConnection2OptionsAlternate Target() {
		if (objTargetOptions == null) {
			objTargetOptions = new SOSConnection2OptionsAlternate("");
		}
		return objTargetOptions;
	}

	/**
	 * \brief setobjTargetOptions -
	 *
	 * \details
	 * setter
	 *
	 * @param objTargetOptions the value for objTargetOptions to set
	 */
	public void Target(final SOSConnection2OptionsAlternate pobjTargetOptions) {
		objTargetOptions = pobjTargetOptions;
	}
}
