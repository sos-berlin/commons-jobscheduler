package com.sos.VirtualFileSystem.Options;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.CredentialStore.KeePass.pl.sind.keepass.kdb.KeePassDataBase;
import com.sos.CredentialStore.KeePass.pl.sind.keepass.kdb.KeePassDataBaseManager;
import com.sos.CredentialStore.KeePass.pl.sind.keepass.kdb.v1.Entry;
import com.sos.CredentialStore.KeePass.pl.sind.keepass.kdb.v1.KeePassDataBaseV1;
import com.sos.CredentialStore.Options.SOSCredentialStoreOptions;
import com.sos.CredentialStore.exceptions.CredentialStoreEntryExpired;
import com.sos.CredentialStore.exceptions.CredentialStoreKeyNotFound;
import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionCommandString;
import com.sos.JSHelper.Options.SOSOptionElement;
import com.sos.i18n.annotation.I18NResourceBundle;

/**
 * \class 		SOSConnection2OptionsAlternate - Options for a connection to an uri (server, site, e.g.)
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
@JSOptionClass(
				name = "SOSConnection2OptionsAlternate",
				description = "Options for a connection to an uri (server, site, e.g.)")
@I18NResourceBundle(
					baseName = "SOSVirtualFileSystem",
					defaultLocale = "en")
public class SOSConnection2OptionsAlternate extends SOSConnection2OptionsSuperClass {
	private final String		conClassName			= this.getClass().getSimpleName();
	private KeePassDataBase		keePassDb				= null;
	private KeePassDataBaseV1	kdb1					= null;
	@SuppressWarnings("unused")
	private static final String	conSVNVersion			= "$Id$";
	@SuppressWarnings("unused")
	private final Logger		logger					= Logger.getLogger(this.getClass());
	/**
	 *
	 */
	private static final long	serialVersionUID		= 5924032437179660014L;
	private String				strAlternativePrefix	= "";
	public boolean				isSource				= false;
	
	/**
	 * \option PreFtpCommands
	 * \type SOSOptionString
	 * \brief PreFtpCommands - FTP commands, which has to be executed before the transfer started
	 *
	 * \details
	 * FTP commands, which has to be executed before the transfer started.
	 *
	 * see also: PostFtpCommands, PostCommand, PreCommand
	 *
	 * \mandatory: false
	 *
	 * \created 05.04.2011 15:45:52 by KB
	 */
	@JSOptionDefinition(
						name = "PreTransferCommands",
						description = "FTP commands, which has to be executed before the transfer started.",
						key = "PreTransferCommands",
						type = "SOSOptionCommandString",
						mandatory = false)
	public SOSOptionCommandString	PreTransferCommands		= new SOSOptionCommandString(
														// ...
																this, // ....
																conClassName + ".pre_transfer_commands", // ...
																"FTP commands, which has to be executed before the transfer started.", // ...
																"", // ...
																"", // ...
																false);
	/**
	 * \see PreFtpCommands
	 */
	public SOSOptionCommandString			PreFtpCommands	= (SOSOptionCommandString) PreTransferCommands.SetAlias("pre_transfer_commands");

	public String getPreTransferCommands() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getPreTransferCommands";
		return PreTransferCommands.Value();
	} // public String getPreFtpCommands

	public SOSConnection2OptionsAlternate setPreTransferCommands(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setPreTransferCommands";
		PreTransferCommands.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setPreFtpCommands
	/**
	 * \option PostTransferCommands
	 * \type SOSOptionString
	 * \brief PostTransferCommands - FTP commands, which has to be executed after the transfer ended
	 *
	 * \details
	 * FTP commands, which has to be executed after the transfer ended.
	 *
	 * see also: PostFtpCommands, PostCommand, PreCommand
	 *
	 * \mandatory: false
	 *
	 * \created 05.04.2011 15:45:52 by KB
	 */
	@JSOptionDefinition(
						name = "PostTransferCommands",
						description = "FTP commands, which has to be executed after the transfer ended.",
						key = "PostTransferCommands",
						type = "SOSOptionCommandString",
						mandatory = false)
	public SOSOptionCommandString	PostTransferCommands	= new SOSOptionCommandString(
															// ...
																	this, // ....
																	conClassName + ".post_transfer_Commands", // ...
																	"FTP commands, which has to be executed after the transfer ended.", // ...
																	"", // ...
																	"", // ...
																	false);
	/**
	 * \see PostTransferCommands
	 */
	public SOSOptionCommandString	PostFtpCommands			= (SOSOptionCommandString) PostTransferCommands.SetAlias("post_Transfer_commands");

	public String getPostTransferCommands() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getPostTransferCommands";
		return PostTransferCommands.Value();
	} // public String getPostTransferCommands

	public SOSConnection2OptionsAlternate setPostTransferCommands(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setPostTransferCommands";
		PostTransferCommands.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setPostTransferCommands

	/**
	 * \option IgnoreCertificateError
	 * \type SOSOptionBoolean
	 * \brief IgnoreCertificateError - Ignore a SSL Certificate Error
	 *
	 * \details
	 * Ignore a SSL Certificate Error
	 * see http://www.sos-berlin.com/jira/browse/SOSFTP-173
	 * \mandatory: true
	 *
	 * \created 11.07.2013 20:04:54 by KB
	 */
	@JSOptionDefinition(
						name = "IgnoreCertificateError",
						description = "Ignore a SSL Certificate Error",
						key = "IgnoreCertificateError",
						type = "SOSOptionBoolean",
						mandatory = true)
	public SOSOptionBoolean		IgnoreCertificateError	= new SOSOptionBoolean(
														// ...
																this, // ....
																conClassName + ".IgnoreCertificateError", // ...
																"Ignore a SSL Certificate Error", // ...
																"true", // ...
																"true", // ...
																true);

	public boolean getIgnoreCertificateError() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getIgnoreCertificateError";
		return IgnoreCertificateError.value();
	} // public String getIgnoreCertificateError

	public SOSConnection2OptionsAlternate setIgnoreCertificateError(final boolean pflgValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setIgnoreCertificateError";
		IgnoreCertificateError.value(pflgValue);
		return this;
	} // public SOSConnection2OptionsAlternate setIgnoreCertificateError
	/**
	 * \option AlternateOptionsUsed
	 * \type SOSOptionBoolean
	 * \brief AlternateOptionsUsed - Alternate Options used for connection and/or authentication
	 *
	 * \details
	 * Alternate Options used for connection and/or authentication
	 *
	 * \mandatory: false
	 *
	 * \created 24.08.2012 20:44:05 by KB
	 */
	@JSOptionDefinition(
						name = "AlternateOptionsUsed",
						description = "Alternate Options used for connection and/or authentication",
						key = "AlternateOptionsUsed",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	AlternateOptionsUsed	= new SOSOptionBoolean(
													// ...
															this, // ....
															conClassName + ".AlternateOptionsUsed", // ...
															"Alternate Options used for connection and/or authentication", // ...
															"false", // ...
															"false", // ...
															false);

	public String getAlternateOptionsUsed() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getAlternateOptionsUsed";
		return AlternateOptionsUsed.Value();
	} // public String getAlternateOptionsUsed

	public SOSConnection2OptionsAlternate setAlternateOptionsUsed(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setAlternateOptionsUsed";
		AlternateOptionsUsed.Value(pstrValue);
		return this;
	} // public SOSConnection2OptionsAlternate setAlternateOptionsUsed
	@JSOptionClass(description = "", name = "SOSConnection2OptionsAlternate", prefix="alternate_") private SOSConnection2OptionsAlternate	objAlternativeOptions		= null;
	@JSOptionClass(description = "", name = "SOSConnection2OptionsAlternate", prefix="proxy_") private SOSConnection2OptionsAlternate	objProxyOptions				= null;
	@JSOptionClass(description = "", name = "SOSConnection2OptionsAlternate", prefix="jump_") private SOSConnection2OptionsAlternate	objJumpServerOptions		= null;
	@JSOptionClass(description = "", name = "SOSCredentialStoreOptions") private SOSCredentialStoreOptions		objCredentialStoreOptions	= null;

	/**
	* constructors
	*/
	public SOSConnection2OptionsAlternate() {
	} // public SOSConnection2OptionsAlternate

	public SOSConnection2OptionsAlternate(final String pstrPrefix) {
		strAlternativePrefix = pstrPrefix;
	} // public SOSConnection2OptionsAlternate

	public SOSConnection2OptionsAlternate(final JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public SOSConnection2OptionsAlternate

	public SOSConnection2OptionsAlternate(final HashMap<String, String> pobjJSSettings) throws Exception {
		super(pobjJSSettings);
		getAlternativeOptions().setAllOptions(pobjJSSettings, "alternative_" + strAlternativePrefix);
		this.addProcessedOptions(objAlternativeOptions.getProcessedOptions());
	} // public SOSConnection2OptionsAlternate (HashMap JSSettings)

	public SOSConnection2OptionsAlternate(final HashMap<String, String> pobjJSSettings, final String pstrPrefix) throws Exception {
		//		super(pobjJSSettings);   // wrong, because every options which has not the prefix will be set as well.
		strAlternativePrefix = pstrPrefix;
		setAllOptions(pobjJSSettings, strAlternativePrefix);
		setChildClasses(pobjJSSettings, pstrPrefix);
	} // public SOSConnection2OptionsAlternate (HashMap JSSettings)

	public void setChildClasses(final HashMap<String, String> pobjJSSettings, final String pstrPrefix) throws Exception {
		//		super(pobjJSSettings);   // wrong, because every options which has not the prefix will be set as well.
		strAlternativePrefix = pstrPrefix;
		getCredentialStore().setAllOptions(pobjJSSettings, strAlternativePrefix);
		logger.trace("setChildClasses 1= " + objCredentialStoreOptions.dirtyString());
		getAlternativeOptions().setAllOptions(pobjJSSettings, "alternative_" + strAlternativePrefix);
		getAlternativeOptions().setAllOptions(pobjJSSettings, "alternate_" + strAlternativePrefix);
		getProxyOptions().setAllOptions(pobjJSSettings, "proxy_" + strAlternativePrefix);
		getJumpServerOptions().setAllOptions(pobjJSSettings, "jump_" + strAlternativePrefix);
		this.addProcessedOptions(objAlternativeOptions.getProcessedOptions());
		//		checkCredentialStoreOptions();
		//		logger.trace("setChildClasses 2= " + objCredentialStoreOptions.dirtyString());
	} // public SOSConnection2OptionsAlternate (HashMap JSSettings)

	public SOSConnection2OptionsAlternate getAlternativeOptions() {
		if (objAlternativeOptions == null) {
			objAlternativeOptions = new SOSConnection2OptionsAlternate();
		}
		return objAlternativeOptions;
	}

	public SOSConnection2OptionsAlternate getProxyOptions() {
		if (objProxyOptions == null) {
			objProxyOptions = new SOSConnection2OptionsAlternate();
		}
		return objProxyOptions;
	}

	public SOSConnection2OptionsAlternate getJumpServerOptions() {
		if (objJumpServerOptions == null) {
			objJumpServerOptions = new SOSConnection2OptionsAlternate();
		}
		return objJumpServerOptions;
	}

	public SOSCredentialStoreOptions getCredentialStore() {
		if (objCredentialStoreOptions == null) {
			objCredentialStoreOptions = new SOSCredentialStoreOptions();
		}
		return objCredentialStoreOptions;
	}

	public void checkCredentialStoreOptions() {
		if (objCredentialStoreOptions.use_credential_Store.isTrue()) {
			logger.trace("entering checkCredentialStoreOptions ");
			objCredentialStoreOptions.CredentialStore_FileName.CheckMandatory(true);
			objCredentialStoreOptions.CredentialStore_KeyPath.CheckMandatory(true);
			String strPassword = null;
			File fleKeyFile = null;
			if (objCredentialStoreOptions.CredentialStore_KeyFileName.isDirty()) {
				fleKeyFile = new File(objCredentialStoreOptions.CredentialStore_KeyFileName.Value());
			}
			if (objCredentialStoreOptions.CredentialStore_password.isDirty()) {
				strPassword = objCredentialStoreOptions.CredentialStore_password.Value();
			}
			File fleKeePassDataBase = new File(objCredentialStoreOptions.CredentialStore_FileName.Value());
			try {
				// TODO keePassDB als Pool um evtl. mehrfachladen zu vermeiden.
				keePassDb = KeePassDataBaseManager.openDataBase(fleKeePassDataBase, fleKeyFile, strPassword);
			}
			catch (Exception e) {
				logger.error(e.getLocalizedMessage());
				throw new JobSchedulerException(e);
			}
			kdb1 = (KeePassDataBaseV1) keePassDb;
			Entry objEntry = kdb1.getEntry(objCredentialStoreOptions.CredentialStore_KeyPath.Value());
			if (objEntry == null) {
				throw new CredentialStoreKeyNotFound(objCredentialStoreOptions);
			}
			
			Date objExpDate = objEntry.ExpirationDate();
			if (new Date().after(objExpDate)) {
				throw new CredentialStoreEntryExpired(objExpDate);
			}
			
			boolean flgHideValuesFromCredentialStore = false;
			if (objEntry.Url().length() > 0) {
				logger.trace(objEntry.Url());
				// Possible Elements of an URL are:
				//
				// http://hans:geheim@www.example.org:80/demo/example.cgi?land=de&stadt=aa#geschichte
				// |        |     |   |               |  |                  |                 |
				// |        |     |  host                | url-path        searchpart      fragment
				// |        |   password            port
				// |       user
				// protocol				
				//
				//  ftp://<user>:<password>@<host>:<port>/<url-path>;type=<typecode>
				// see http://docs.oracle.com/javase/7/docs/api/java/net/URL.html
				String strUrl = objEntry.Url(); // 
				try {
					URL objURL = new URL(strUrl);
					setIfNotDirty(host, objURL.getHost());
					String strPort = String.valueOf(objURL.getPort());
					if (isEmpty(strPort) || strPort.equals("-1")) {
						strPort = String.valueOf(objURL.getDefaultPort());
					}
					setIfNotDirty(port, strPort);
					setIfNotDirty(protocol, objURL.getProtocol());
					String strUserInfo = objURL.getUserInfo();
					String[] strU = strUserInfo.split(":");
					setIfNotDirty(user, strU[0]);
					if (strU.length > 1) {
						setIfNotDirty(password, strU[1]);
					}
					String strAuthority = objURL.getAuthority();
					String[] strA = strAuthority.split("@"); // user:pw  host
				}
				catch (MalformedURLException e) {
					// not a valid url. ignore it, because it could be a host name only
				}
			}
			if (isNotEmpty(objEntry.UserName())) {
				user.Value(objEntry.UserName());
				user.setHideValue(flgHideValuesFromCredentialStore);
			}
			if (isNotEmpty(objEntry.Password())) {
				password.Value(objEntry.Password());
				password.setHideValue(flgHideValuesFromCredentialStore);
			}
			if (isNotEmpty(objEntry.Url())) {
				setIfNotDirty(host, objEntry.Url());
				host.setHideValue(flgHideValuesFromCredentialStore);
			}
			objEntry.ExpirationDate();
			//			
			if (HostName.isNotDirty()) {
				HostName.Value(objEntry.getUrl().toString());
			}
			//			assertEquals("note ", "-dburl=test -verbose=-2 -password=12345", objEntry.Notes());
			//			System.out.println("binary Description: " + objEntry.getBinaryDescription().getText());
			if (objCredentialStoreOptions.CredentialStore_ExportAttachment.isTrue()) {
				File fleO = objEntry.saveAttachmentAsFile(objCredentialStoreOptions.CredentialStore_ExportAttachment2FileName.Value());
				if (objCredentialStoreOptions.CredentialStore_DeleteExportedFileOnExit.isTrue()) {
					fleO.deleteOnExit();
				}
			}
			
			if (objCredentialStoreOptions.CredentialStore_ProcessNotesParams.isTrue()) {
				CommandLineArgs(objEntry.getNotesText());
			}

		}
	}

	private void setIfNotDirty(final SOSOptionElement objOption, final String pstrValue) {
		if (objOption.isNotDirty() && isNotEmpty(pstrValue)) {
			logger.trace("setValue = " + pstrValue);
			objOption.Value(pstrValue);
		}
	}

	/**
	 * \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
	 *
	 * \details
	 * @throws Exception
	 *
	 * @throws Exception
	 * - wird ausgelöst, wenn eine mandatory-Option keinen Wert hat
	 */
	@Override// SOSConnection2OptionsAlternateSuperClass
	public void CheckMandatory() {
		try {
			super.CheckMandatory();
		}
		catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
	} // public void CheckMandatory ()

	/**
	 * \brief AlternativeOptions
	 *
	 * \details
	 * getter
	 *
	 * @return the objAlternativeOptions
	 */
	public SOSConnection2OptionsAlternate Alternatives() {
		if (objAlternativeOptions == null) {
			objAlternativeOptions = new SOSConnection2OptionsAlternate("");
		}
		return objAlternativeOptions;
	}

	/**
	 * \brief AlternativeOptions -
	 *
	 * \details
	 * setter
	 *
	 * @param objAlternativeOptions the value for objAlternativeOptions to set
	 */
	public void AlternativeOptions(final SOSConnection2OptionsAlternate pobjAlternativeOptions) {
		objAlternativeOptions = pobjAlternativeOptions;
	}
	
	
	/**
	 * \brief optionsHaveMinRequirements
	 *
	 * \details
	 * is used before alternative connection is used to check 
	 * wether host, port, user is set
	 *
	 * @return boolean
	 */
	public boolean optionsHaveMinRequirements() {
		if (AlternateOptionsUsed.isTrue()) {
			return false;
		}
		if (protocol.Value().equalsIgnoreCase("local")) {
			return true;
		}
		if (host.isNotDirty() || host.IsEmpty()) {
			return false;
		}
		if (port.isNotDirty() || port.value() <= 0) {
			return false;
		}
		if (user.isNotDirty() || user.IsEmpty()) {
			return false;
		}
		return true;
	}
}
