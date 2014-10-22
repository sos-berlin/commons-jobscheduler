package com.sos.VirtualFileSystem.Options;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionAuthenticationMethod;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.JSHelper.Options.SOSOptionHostName;
import com.sos.JSHelper.Options.SOSOptionInFileName;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionJavaClassName;
import com.sos.JSHelper.Options.SOSOptionPassphrase;
import com.sos.JSHelper.Options.SOSOptionPassword;
import com.sos.JSHelper.Options.SOSOptionPlatform;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.JSHelper.Options.SOSOptionRegExp;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.JSHelper.Options.SOSOptionStringValueList;
import com.sos.JSHelper.Options.SOSOptionTransferMode;
import com.sos.JSHelper.Options.SOSOptionTransferType;
import com.sos.JSHelper.Options.SOSOptionUrl;
import com.sos.JSHelper.Options.SOSOptionUserName;
import com.sos.JSHelper.interfaces.ISOSDataProviderOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.i18n.annotation.I18NResourceBundle;

/**
 * \class 		SOSConnection2OptionsSuperClass - Options for a connection to an uri (server, site, e.g.)
 *
 * \brief
 * An Options-Super-Class with all Options. This Class will be extended by the "real" Options-class (\see SOSConnection2Options.
 * The "real" Option class will hold all the things, which are normaly overwritten at a new generation
 * of the super-class.
 *
 *

 *
 * see \see j:\e\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\SOSConnection2.xml for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by  from http://www.sos-berlin.com at 20100917112403
 * \endverbatim
 * \section OptionsTable Tabelle der vorhandenen Optionen
 *
 * Tabelle mit allen Optionen
 *
 * MethodName
 * Title
 * Setting
 * Description
 * IsMandatory
 * DataType
 * InitialValue
 * TestValue
 *
 *
 *
 * \section TestData Eine Hilfe zum Erzeugen einer HashMap mit Testdaten
 *
 * Die folgenden Methode kann verwendet werden, um für einen Test eine HashMap
 * mit sinnvollen Werten für die einzelnen Optionen zu erzeugen.
 *
 * \verbatim
 private HashMap <String, String> SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM) {
	pobjHM.put ("		SOSConnection2OptionsSuperClass.auth_file", "test");  // This parameter specifies the path and name of a user's pr
		return pobjHM;
  }  //  private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim
 */
@JSOptionClass(name = "SOSConnection2OptionsSuperClass", description = "SOSConnection2OptionsSuperClass")
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSConnection2OptionsSuperClass extends JSOptionsClass implements ISOSAuthenticationOptions, ISOSDataProviderOptions {
	/**
	 *
	 */
	private static final long	serialVersionUID	= 1997338600688654140L;
	@SuppressWarnings("unused")
	private final String		conClassName		= this.getClass().getSimpleName();
	@SuppressWarnings("unused")
	private static final String	conSVNVersion		= "$Id$";
	@SuppressWarnings("unused")
	private final static Logger	logger				= Logger.getLogger(SOSConnection2OptionsSuperClass.class);
	/**
	 * \option url
	 * \type SOSOptionURL
	 * \brief url - the url to which a connection have to be made
	 *
	 * \details
	 * the url for the connection
	 *
	 * \mandatory: false
	 *
	 * \created 30.04.2014 16:32:05 by KB
	 */
	@JSOptionDefinition(name = "url", description = "the url for the connection", key = "url", type = "SOSOptionURL", mandatory = false)
	public SOSOptionUrl			url					= new SOSOptionUrl( // ...
															this, // ....
															conClassName + ".url", // ...
															"the url for the connection", // ...
															"", // ...
															"", // ...
															false);

	public SOSOptionUrl			URI					= (SOSOptionUrl) url.SetAlias("uri");

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#geturl()
	 */
	@Override
	public SOSOptionUrl geturl() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::geturl";
		return url;
	} // public String geturl

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#seturl(com.sos.JSHelper.Options.SOSOptionUrl)
	 */
	@Override
	public ISOSDataProviderOptions seturl(final SOSOptionUrl pstrValue) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::seturl";
		url = pstrValue;
		return this;
	} // public SOSConnection2OptionsSuperClass seturl
	/**
	 * \option include
	 * \type SOSOptionString
	 * \brief include - the include directive as an option
	 *
	 * \details
	 * the include directive as an option
	 *
	 * \mandatory: false
	 *
	 * \created 17.12.2013 19:51:59 by KB
	 */
	@JSOptionDefinition(name = "include", description = "the include directive as an option", key = "include", type = "SOSOptionString", mandatory = false)
	public SOSOptionString	include	= new SOSOptionString(
									// ...
											this, // ....
											conClassName + ".include", // ...
											"the include directive as an option", // ...
											"", // ...
											"", // ...
											false);

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#getinclude()
	 */
	@Override
	public String getinclude() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getinclude";
		return include.Value();
	} // public String getinclude

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#setinclude(java.lang.String)
	 */
	@Override
	public ISOSDataProviderOptions setinclude(final String pstrValue) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setinclude";
		include.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setinclude
	/**
	 * \option use_zlib_compression
	 * \type SOSOptionBoolean
	 * \brief use_zlib_compression - Use the zlib compression on sftp
	 *
	 * \details
	 * Use the zlib cmpression on sftp
	 *
	 * \mandatory: false
	 *
	 * \created 04.03.2014 20:15:20 by KB
	 */
	@JSOptionDefinition(name = "use_zlib_compression", description = "Use the zlib cmpression on sftp", key = "use_zlib_compression", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean	use_zlib_compression	= new SOSOptionBoolean( // ...
															this, // ....
															conClassName + ".use_zlib_compression", // ...
															"Use the zlib cmpression on sftp", // ...
															"false", // ...
															"false", // ...
															false);

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#getuse_zlib_compression()
	 */
	@Override
	public SOSOptionBoolean getuse_zlib_compression() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getuse_zlib_compression";
		return use_zlib_compression;
	} // public String getuse_zlib_compression

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#setuse_zlib_compression(com.sos.JSHelper.Options.SOSOptionBoolean)
	 */
	@Override
	public ISOSDataProviderOptions setuse_zlib_compression(final SOSOptionBoolean pstrValue) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setuse_zlib_compression";
		use_zlib_compression = pstrValue;
		return this;
	} // public SOSConnection2OptionsSuperClass setuse_zlib_compression
	/**
	 * \option zlib_compression_level
	 * \type SOSOptionInteger
	 * \brief zlib_compression_level - the compression level to use
	 *
	 * \details
	 * the compression level to use
	 *
	 * \mandatory: false
	 *
	 * \created 04.03.2014 20:21:38 by KB
	 */
	@JSOptionDefinition(name = "zlib_compression_level", description = "the compression level to use", key = "zlib_compression_level", type = "SOSOptionInteger", mandatory = false)
	public SOSOptionInteger	zlib_compression_level	= new SOSOptionInteger( // ...
															this, // ....
															conClassName + ".zlib_compression_level", // ...
															"the compression level to use", // ...
															"1", // ...
															"1", // ...
															false);

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#getzlib_compression_level()
	 */
	@Override
	public SOSOptionInteger getzlib_compression_level() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getzlib_compression_level";
		return zlib_compression_level;
	} // public String getzlib_compression_level

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#setzlib_compression_level(com.sos.JSHelper.Options.SOSOptionInteger)
	 */
	@Override
	public ISOSDataProviderOptions setzlib_compression_level(final SOSOptionInteger pstrValue) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setzlib_compression_level";
		zlib_compression_level = pstrValue;
		return this;
	} // public SOSConnection2OptionsSuperClass setzlib_compression_level
	/**
	 * \option ProtocolCommandListener
	 * \type SOSOptionBoolean
	 * \brief ProtocolCommandListener - Activate the logging for Apache ftp client
	 *
	 * \details
	 * Activate the logging for Apache ftp client
	 *
	 * \mandatory: true
	 *
	 * \created 05.07.2013 11:02:58 by KB
	 */
	@JSOptionDefinition(name = "ProtocolCommandListener", description = "Activate the logging for Apache ftp client", key = "Protocol_Command_Listener", type = "SOSOptionBoolean", mandatory = true)
	public SOSOptionBoolean	ProtocolCommandListener	= new SOSOptionBoolean(
													// ...
															this, // ....
															conClassName + ".Protocol_Command_Listener", // ...
															"Activate the logging for Apache ftp client", // ...
															"false", // ...
															"false", // ...
															true);

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#getProtocolCommandListener()
	 */
	@Override
	public String getProtocolCommandListener() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getProtocolCommandListener";
		return ProtocolCommandListener.Value();
	} // public String getProtocolCommandListener

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#setProtocolCommandListener(java.lang.String)
	 */
	@Override
	public ISOSDataProviderOptions setProtocolCommandListener(final String pstrValue) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setProtocolCommandListener";
		ProtocolCommandListener.Value(pstrValue);
		return this;
	} // public SOSConnection2OptionsSuperClass setProtocolCommandListener
	/**
	* \var account : Optional account info for authentication with an
	*
	Optional account info for authentication with an (FTP) server.
	*
	*/
	@JSOptionDefinition(name = "account", description = "Optional account info for authentication with an", key = "account", type = "SOSOptionString", mandatory = false)
	public SOSOptionString	account	= new SOSOptionString(this, conClassName + ".account", // HashMap-Key
											"Optional account info for authentication with an", // Titel
											"", // InitValue
											"", // DefaultValue
											false // isMandatory
									);

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#getaccount()
	 */
	@Override
	public SOSOptionString getaccount() {
		return account;
	}

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#setaccount(com.sos.JSHelper.Options.SOSOptionString)
	 */
	@Override
	public void setaccount(final SOSOptionString p_account) {
		account = p_account;
	}
	/**
	 * \option make_Dirs
	 * \type SOSOptionBoolean
	 * \brief make_Dirs - Create missing Directory on Target
	 *
	 * \details
	 * Create missing Directory on Target
	 *
	 * \mandatory: false
	 *
	 * \created 20.07.2012 18:19:14 by KB
	 */
	@JSOptionDefinition(name = "make_Dirs", description = "Create missing Directory on Target", key = "make_Dirs", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean	makeDirs				= new SOSOptionBoolean(
													// ...
															this, // ....
															conClassName + ".make_Dirs", // ...
															"Create missing Directory on Source/Target", // ...
															"true", // ...
															"true", // ...
															false);
	public SOSOptionBoolean	createFoldersOnTarget	= (SOSOptionBoolean) makeDirs.SetAlias("create_folders_on_target");
	public SOSOptionBoolean	createFolders			= (SOSOptionBoolean) makeDirs.SetAlias("create_folders");

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#getmake_Dirs()
	 */
	@Override
	public String getmake_Dirs() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getmake_Dirs";
		return makeDirs.Value();
	} // public String getmake_Dirs

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#setmake_Dirs(java.lang.String)
	 */
	@Override
	public ISOSDataProviderOptions setmake_Dirs(final String pstrValue) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setmake_Dirs";
		makeDirs.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setmake_Dirs
	@JSOptionDefinition(name = "dir", description = "Optional account info for authentication with an", key = "dir", type = "SOSOptionFolderName", mandatory = false)
	public SOSOptionFolderName	Directory	= new SOSOptionFolderName(this, conClassName + ".dir", // HashMap-Key
													"local_dir Local directory into which or from which", // Titel
													"", // InitValue
													"", // DefaultValue
													false // isMandatory
											);
	public SOSOptionFolderName	FolderName	= (SOSOptionFolderName) Directory.SetAlias("Folder_Name");
	/**
	 * \option platform
	 * \type SOSOptionString
	 * \brief platform - platform on which the app is running
	 *
	 * \details
	 * platform on which the app is running
	 *
	 * \mandatory: false
	 *
	 * \created 22.02.2013 18:21:22 by KB
	 */
	@JSOptionDefinition(name = "platform", description = "platform on which the app is running", key = "platform", type = "SOSOptionString", mandatory = false)
	public SOSOptionPlatform	platform	= new SOSOptionPlatform(
											// ...
													this, // ....
													conClassName + ".platform", // ...
													"platform on which the app is running", // ...
													"", // ...
													"", // ...
													false);

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#getplatform()
	 */
	@Override
	public String getplatform() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getplatform";
		return platform.Value();
	} // public String getplatform

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#setplatform(java.lang.String)
	 */
	@Override
	public ISOSDataProviderOptions setplatform(final String pstrValue) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setplatform";
		platform.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setplatform
	/**
	* \var replacement : String for replacement of matching character seque
	*
	String for replacement of matching character sequences within file names that are specified with the value of the parameter replacing. If multiple "capturing groups" shall be replaced then one replacement string per group has to be specified. These strings are separated by a semicolon ";": replacement: aa;[filename:];bb Supports masks for substitution in the file name with format strings that are enclosed with [ and ] . The following format strings are supported: [date: date format ] date format must be a valid Java data format string, e.g. yyyyMMddHHmmss , yyyy-MM-dd.HHmmss etc. [filename:] Will be substituted by the original file name including the file extension. [filename:lowercase] Will be substituted by the original file name including the file extension with all characters converted to lower case. [filename:uppercase] Will be substituted by the original file name including the file extension with all characters converted to upper case. Requires the parameter replacing to be specified.
	*
	*/
	@JSOptionDefinition(name = "replacement", description = "String for replacement of matching character seque", key = "replacement", type = "SOSOptionString", mandatory = false)
	public SOSOptionString	replacement	= new SOSOptionString(this, conClassName + ".replacement", // HashMap-Key
												"String for replacement of matching character seque", // Titel
												null, // InitValue
												null, // DefaultValue
												false // isMandatory
										);

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#getreplacement()
	 */
	@Override
	public SOSOptionString getreplacement() {
		return replacement;
	}

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#setreplacement(com.sos.JSHelper.Options.SOSOptionString)
	 */
	@Override
	public void setreplacement(final SOSOptionString p_replacement) {
		replacement = p_replacement;
	}
	public SOSOptionString	ReplaceWith	= (SOSOptionString) replacement.SetAlias(conClassName + ".ReplaceWith");
	/**
	* \var replacing : Regular expression for filename replacement with
	*
	Regular expression for filename replacement with the value of the parameter replacement.
	If the expression matches the filename then the groups found are replaced.
	a) For replacement "capturing groups" are used.
	Only the content of the capturing groups is replaced. Replacements are separated by a semicolon ";". Example: replacing : (1)abc(12)def(.*) replacement : A;BB;CCC Input file: 1abc12def123.txt Output file: AabcBBdefCCC b) If no "capturing groups" are specified then the entire match is replaced. Example: replacing : Hello replacement : 1234 Input file: Hello_World.txt Output file: 1234_World.txt Requires the parameter replacement to be specified.
	*
	*/
	@JSOptionDefinition(name = "replacing", description = "Regular expression for filename replacement with", key = "replacing", type = "SOSOptionRegExp", mandatory = false)
	public SOSOptionRegExp	replacing	= new SOSOptionRegExp(this, conClassName + ".replacing", // HashMap-Key
												"Regular expression for filename replacement with", // Titel
												"", // InitValue
												"", // DefaultValue
												false // isMandatory
										);

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#getreplacing()
	 */
	@Override
	public SOSOptionRegExp getreplacing() {
		return replacing;
	}

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#setreplacing(com.sos.JSHelper.Options.SOSOptionRegExp)
	 */
	@Override
	public void setreplacing(final SOSOptionRegExp p_replacing) {
		replacing = p_replacing;
	}
	public SOSOptionRegExp			ReplaceWhat				= (SOSOptionRegExp) replacing.SetAlias(conClassName + ".ReplaceWhat");
	/**
	 * \option Strict_HostKey_Checking
	 * \type SOSOptionValueList
	 * \brief Strict_HostKey_Checking - Check the hostKey against known hosts for SSH
	 *
	 * \details
	 * Check the hostkey against known hosts for SSH
	 *
	 * \mandatory: false
	 *
	 * \created 13.11.2012 18:40:25 by KB
	 */
	@JSOptionDefinition(name = "Strict_HostKey_Checking", description = "Check the hostkey against known hosts for SSH", key = "Strict_HostKey_Checking", type = "SOSOptionValueList", mandatory = false)
	public SOSOptionStringValueList	StrictHostKeyChecking	= new SOSOptionStringValueList(
															// ...
																	this, // ....
																	conClassName + ".strict_hostkey_checking", // ...
																	"Check the hostkey against known hosts for SSH", // ...
																	"ask;yes;no", // ...
																	"no", // ...
																	false);

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#getStrict_HostKey_Checking()
	 */
	@Override
	public String getStrict_HostKey_Checking() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getStrict_HostKey_Checking";
		return StrictHostKeyChecking.Value();
	} // public String getStrict_HostKey_Checking

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#setStrict_HostKey_Checking(java.lang.String)
	 */
	@Override
	public ISOSDataProviderOptions setStrict_HostKey_Checking(final String pstrValue) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setStrict_HostKey_Checking";
		StrictHostKeyChecking.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setStrict_HostKey_Checking
	/**
	 * \option TFN_Post_Command
	 * \type SOSOptionString
	 * \brief TFN_Post_Command - Post commands executed after creating the final TargetFile
	 *
	 * \details
	 * Post commands after creating the final TargetFileName
	 *
	 * \mandatory: false
	 *
	 * \created 08.04.2014 16:23:51 by KB
	 */
	@JSOptionDefinition(name = "TFN_Post_Command", description = "Post commands executed after creating the final TargetFile", key = "TFN_Post_Command", type = "SOSOptionString", mandatory = false)
	public SOSOptionString	TFN_Post_Command	= new SOSOptionString( // ...
														this, // ....
														conClassName + ".TFN_Post_Command", // ...
														"Post commands executed after creating the final TargetFileName", // ...
														"", // ...
														"", // ...
														false);

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#getTFN_Post_Command()
	 */
	@Override
	public SOSOptionString getTFN_Post_Command() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getTFN_Post_Command";
		return TFN_Post_Command;
	} // public String getTFN_Post_Command

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#setTFN_Post_Command(com.sos.JSHelper.Options.SOSOptionString)
	 */
	@Override
	public ISOSDataProviderOptions setTFN_Post_Command(final SOSOptionString pstrValue) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setTFN_Post_Command";
		TFN_Post_Command = pstrValue;
		return this;
	} // public SOSConnection2OptionsSuperClass setTFN_Post_Command
	/**
	 * \option Post_Command
	 * \type SOSOptionString
	 * \brief Post_Command - FTP-Command to be executed after transfer
	 *
	 * \details
	 * FTP-Command to be executed after transfer for each file.
	 *
	 * \mandatory: false
	 *
	 * \created 12.10.2011 12:28:42 by KB
	 */
	@JSOptionDefinition(name = "Post_Command", description = "FTP-Command to be executed after transfer", key = "Post_Command", type = "SOSOptionString", mandatory = false)
	public SOSOptionString	Post_Command	= new SOSOptionString(
											// ...
													this, // ....
													conClassName + ".Post_Command", // ...
													"FTP-Command to be executed after transfer", // ...
													"", // ...
													"", // ...
													false);

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#getPost_Command()
	 */
	@Override
	public String getPost_Command() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getPost_Command";
		return Post_Command.Value();
	} // public String getPost_Command

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#setPost_Command(java.lang.String)
	 */
	@Override
	public ISOSDataProviderOptions setPost_Command(final String pstrValue) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setPost_Command";
		Post_Command.Value(pstrValue);
		return this;
	} // public SOSFTPOptions setPost_Command
	/**
	 * \option Pre_Command
	 * \type SOSOptionString
	 * \brief Pre_Command - A FTP-Command to be execute before transfer of each file is starting
	 *
	 * \details
	 *
	 *
	 * \mandatory: false
	 *
	 * \created 12.10.2011 12:25:27 by KB
	 */
	@JSOptionDefinition(name = "Pre_Command", description = "FTP-Command to be execute before transfer", key = "Pre_Command", type = "SOSOptionString  ", mandatory = false)
	public SOSOptionString	Pre_Command	= new SOSOptionString(
										// ...
												this, // ....
												conClassName + ".Pre_Command", // ...
												"", // ...
												"", // ...
												"", // ...
												false);

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#getPre_Command()
	 */
	@Override
	public String getPre_Command() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getPre_Command";
		return Pre_Command.Value();
	} // public String getPre_Command

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#setPre_Command(java.lang.String)
	 */
	@Override
	public ISOSDataProviderOptions setPre_Command(final String pstrValue) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setPre_Command";
		Pre_Command.Value(pstrValue);
		return this;
	} // public SOSFTPOptions setPre_Command
	/**
	 * \option FtpS_protocol
	 * \type SOSOptionString
	 * \brief FtpS_protocol - Type of FTPS-Protocol (SSL / TLS)
	 *
	 * \details
	 * Type of FTPS-Protocol, e.g. SSL, TLS
	 *
	 * \mandatory: true
	 *
	 * \created 21.04.2011 18:15:36 by KB
	 */
	@JSOptionDefinition(name = "FtpS_protocol", description = "Type of FTPS-Protocol, e.g. SSL, TLS", key = "FtpS_protocol", type = "SOSOptionString", mandatory = true)
	public SOSOptionString	FtpS_protocol	= new SOSOptionString(
											// ...
													this, // ....
													conClassName + ".FtpS_protocol", // ...
													"Type of FTPS-Protocol, e.g. SSL, TLS", // ...
													"SSL", // ...
													"SSL", // ...
													true);

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#getFtpS_protocol()
	 */
	@Override
	public String getFtpS_protocol() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getFtpS_protocol";
		return FtpS_protocol.Value();
	} // public String getFtpS_protocol

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#setFtpS_protocol(java.lang.String)
	 */
	@Override
	public ISOSDataProviderOptions setFtpS_protocol(final String pstrValue) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setFtpS_protocol";
		FtpS_protocol.Value(pstrValue);
		return this;
	} // public SOSConnection2OptionsAlternate setFtpS_protocol
	/**
	 * \option loadClassName
	 * \type SOSOptionString
	 * \brief loadClassName - Java class name of Class to load
	 *
	 * \details
	 * Java Class which implements the ISOSVFSHandlerInterface
	 *
	 * \mandatory: false
	 *
	 * \created 08.06.2012 13:31:04 by KB
	 */
	@JSOptionDefinition(name = "loadClassName", description = "Java Class which implements the ISOSVFSHandlerInterface", key = "load_Class_Name", type = "SOSOptionString ", mandatory = false)
	public SOSOptionJavaClassName	loadClassName			= new SOSOptionJavaClassName(
															// ...
																	this, // ....
																	conClassName + ".load_Class_Name", // ...
																	"Java Class which implements the ISOSVFSHandlerInterface", // ...
																	"", // ...
																	"", // ...
																	false);
	public SOSOptionJavaClassName	DataProviderClassName	= (SOSOptionJavaClassName) loadClassName.SetAlias("Data_provider_class_name");

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#getloadClassName()
	 */
	@Override
	public String getloadClassName() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getloadClassName";
		return loadClassName.Value();
	} // public String getloadClassName

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#setloadClassName(java.lang.String)
	 */
	@Override
	public ISOSDataProviderOptions setloadClassName(final String pstrValue) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setloadClassName";
		loadClassName.Value(pstrValue);
		return this;
	} // public SOSConnection2OptionsSuperClass setloadClassName
	/**
	 * \var javaClassPath :
	 *
	 *
	 */
	@JSOptionDefinition(name = "javaClassPath", description = "", key = "javaClassPath", type = "SOSOptionString", mandatory = false)
	public SOSOptionString	javaClassPath	= new SOSOptionString(this, conClassName + ".javaClassPath", // HashMap-Key
													"", // Titel
													"", // InitValue
													"", // DefaultValue
													false // isMandatory
											);

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#getjavaClassPath()
	 */
	@Override
	public SOSOptionString getjavaClassPath() {
		return javaClassPath;
	}

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#setjavaClassPath(com.sos.JSHelper.Options.SOSOptionString)
	 */
	@Override
	public void setjavaClassPath(final SOSOptionString p_javaClassPath) {
		javaClassPath = p_javaClassPath;
	}
	/**
	 * \var host : Host-Name This parameter specifies th
	 * This parameter specifies the hostname or IP address of the server to which a connection has to be made.
	 *
	 */
	@JSOptionDefinition(name = "host", description = "Host-Name This parameter specifies th", key = "host", type = "SOSOptionHostName", mandatory = false)
	public SOSOptionHostName	host	= new SOSOptionHostName(this, conClassName + ".host", // HashMap-Key
												"Host-Name This parameter specifies th", // Titel
												"", // InitValue
												"", // DefaultValue
												false // isMandatory
										);

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#getHost()
	 */
	@Override
	public SOSOptionHostName getHost() {
		return host;
	}

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#setHost(com.sos.JSHelper.Options.SOSOptionHostName)
	 */
	@Override
	public void setHost(final SOSOptionHostName p_host) {
		host = p_host;
	}
	public SOSOptionHostName	HostName		= (SOSOptionHostName) host.SetAlias(conClassName + ".HostName");
	public SOSOptionHostName	FtpHostName		= (SOSOptionHostName) host.SetAlias(conClassName + ".ftp_host");
	/**
	 * \var passive_mode : passive_mode Passive mode for FTP is often used wit
	 * Passive mode for FTP is often used with firewalls. Valid values are 0 or 1.
	 *
	 */
	@JSOptionDefinition(name = "passive_mode", description = "passive_mode Passive mode for FTP is often used wit", key = "passive_mode", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean		passive_mode	= new SOSOptionBoolean(this, conClassName + ".passive_mode", // HashMap-Key
														"passive_mode Passive mode for FTP is often used wit", // Titel
														"false", // InitValue
														"false", // DefaultValue
														false // isMandatory
												);

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#getpassive_mode()
	 */
	@Override
	public SOSOptionBoolean getpassive_mode() {
		return passive_mode;
	}

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#setpassive_mode(com.sos.JSHelper.Options.SOSOptionBoolean)
	 */
	@Override
	public void setpassive_mode(final SOSOptionBoolean p_passive_mode) {
		passive_mode = p_passive_mode;
	}
	public SOSOptionBoolean		FTPTransferModeIsPassive	= (SOSOptionBoolean) passive_mode.SetAlias(conClassName + ".FTPTransferModeIsPassive");
	public SOSOptionBoolean		FTPPassiveMode				= (SOSOptionBoolean) passive_mode.SetAlias(conClassName + ".ftp_passive_mode");
	/**
	 * \var port : Port-Number to be used for Data-Transfer
	 * Port by which files should be transferred. For FTP this is usually port 21, for SFTP this is usually port 22.
	 *
	 */
	@JSOptionDefinition(name = "port", description = "Port-Number to be used for Data-Transfer", key = "port", type = "SOSOptionPortNumber", mandatory = true)
	public SOSOptionPortNumber	port						= new SOSOptionPortNumber(this, conClassName + ".port", // HashMap-Key
																	"Port-Number to be used for Data-Transfer", // Titel
																	"21", // InitValue
																	"21", // DefaultValue
																	true // isMandatory
															);
	public SOSOptionPortNumber	ftp_port					= (SOSOptionPortNumber) port.SetAlias("ftp_port");

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#getport()
	 */
	@Override
	public SOSOptionPortNumber getport() {
		return port;
	}

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#setport(com.sos.JSHelper.Options.SOSOptionPortNumber)
	 */
	@Override
	public void setport(final SOSOptionPortNumber p_port) {
		port = p_port;
	}
	/**
	 * \var protocol : Type of requested Datatransfer The values ftp, sftp
	 * The values ftp, sftp or ftps are valid for this parameter. If sftp is used, then the ssh_* parameters will be applied.
	 *
	 */
	@JSOptionDefinition(name = "protocol", description = "Type of requested Datatransfer The values ftp, sftp", key = "protocol", type = "SOSOptionStringValueList", mandatory = true)
	public SOSOptionTransferType	protocol		= new SOSOptionTransferType(this, conClassName + ".protocol", // HashMap-Key
															"Type of requested Datatransfer The values ftp, sftp", // Titel
															"", // InitValue
															"ftp", // DefaultValue
															true // isMandatory
													);
	public SOSOptionTransferType	ftp_protocol	= (SOSOptionTransferType) protocol.SetAlias("ftp_protocol");

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#getprotocol()
	 */
	@Override
	public SOSOptionTransferType getprotocol() {
		return protocol;
	}

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#setprotocol(com.sos.JSHelper.Options.SOSOptionTransferType)
	 */
	@Override
	public void setprotocol(final SOSOptionTransferType p_protocol) {
		protocol = p_protocol;
	}
	public SOSOptionTransferType	TransferProtocol	= (SOSOptionTransferType) protocol.SetAlias(conClassName + ".TransferProtocol");
	/**
	 * \var transfer_mode : Type of Character-Encoding Transfe
	 * Transfer mode is used for FTP exclusively and can be either ascii or binary.
	 *
	 */
	@JSOptionDefinition(name = "transfer_mode", description = "Type of Character-Encoding Transfe", key = "transfer_mode", type = "SOSOptionTransferMode", mandatory = false)
	public SOSOptionTransferMode	transfer_mode		= new SOSOptionTransferMode(this, conClassName + ".transfer_mode", // HashMap-Key
																"Type of Character-Encoding Transfe", // Titel
																"ascii;binary;text", // InitValue
																"binary", // DefaultValue
																false // isMandatory
														);
	public SOSOptionTransferMode	ftp_transfer_mode	= (SOSOptionTransferMode) transfer_mode.SetAlias("ftp_transfer_mode");

	//	@SuppressWarnings("unused")
	//	protected final String			strAlternativePrefix	= "";

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#gettransfer_mode()
	 */
	@Override
	public SOSOptionTransferMode gettransfer_mode() {
		return transfer_mode;
	}

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#settransfer_mode(com.sos.JSHelper.Options.SOSOptionTransferMode)
	 */
	@Override
	public void settransfer_mode(final SOSOptionTransferMode p_transfer_mode) {
		transfer_mode = p_transfer_mode;
	}
	/**
	* \var user : UserID of user in charge User name
	*
	User name for authentication at the (FTP/SFTP) server.
	*
	*/
	@JSOptionDefinition(name = "user", description = "UserID of user in charge User name", key = "user", type = "SOSOptionUserName", mandatory = true)
	public SOSOptionUserName	user	= new SOSOptionUserName(this, conClassName + ".user", // HashMap-Key
												"UserID of user in charge User name", // Titel
												"", // InitValue
												"anonymous", // DefaultValue
												false // isMandatory
										);

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#getUser()
	 */
	@Override
	public SOSOptionUserName getUser() {
		return user;
	}
	/**
	* \var password : Password for UserID Password for a
	*
	Password for authentication at the FTP/SFTP server. For SSH/SFTP connections that make use of public/private key authentication the password parameter is specified for the passphrase that optionally secures a private key.
	*
	*/
	@JSOptionDefinition(name = "password", description = "Password for UserID Password for a", key = "password", type = "SOSOptionPassword", mandatory = false)
	public SOSOptionPassword	password	= new SOSOptionPassword(this, conClassName + ".password", // HashMap-Key
													"Password for UserID Password for a", // Titel
													"", // InitValue
													"", // DefaultValue
													false // isMandatory
											);

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#getPassword()
	 */
	@Override
	public SOSOptionPassword getPassword() {
		return password;
	}

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#setPassword(com.sos.JSHelper.Options.SOSOptionPassword)
	 */
	@Override
	public void setPassword(final SOSOptionPassword p_password) {
		password = p_password;
	}
	/**
	* \var passphrase : passphrase for UserID passphrase for a
	*
	passphrase for authentication at the SFTP server. For SSH/SFTP connections that make use of public/private key authentication the passphrase parameter is specified for the passphrase that optionally secures a private key.
	*
	*/
	@JSOptionDefinition(name = "passphrase", description = "passphrase for UserID passphrase for a", key = "passphrase", type = "SOSOptionpassphrase", mandatory = false)
	public SOSOptionPassphrase	passphrase	= new SOSOptionPassphrase(this, conClassName + ".passphrase", // HashMap-Key
													"passphrase for UserID passphrase for a", // Titel
													"", // InitValue
													"", // DefaultValue
													false // isMandatory
											);

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#getpassphrase()
	 */
	@Override
	public SOSOptionPassphrase getpassphrase() {
		return passphrase;
	}

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#setpassphrase(com.sos.JSHelper.Options.SOSOptionPassphrase)
	 */
	@Override
	public void setpassphrase(final SOSOptionPassphrase p_passphrase) {
		passphrase = p_passphrase;
	}

	public SOSConnection2OptionsSuperClass() {
		objParentClass = this.getClass();
	} // public SOSConnection2OptionsSuperClass

	public SOSConnection2OptionsSuperClass(final JSListener pobjListener) {
		this();
		// this.registerMessageListener(pobjListener);
	} // public SOSConnection2OptionsSuperClass

	//
	public SOSConnection2OptionsSuperClass(final HashMap<String, String> JSSettings) throws Exception {
		this();
		this.setAllOptions(JSSettings);
	} // public SOSConnection2OptionsSuperClass (HashMap JSSettings)

	/**
	 * \brief getAllOptionsAsString - liefert die Werte und Beschreibung aller
	 * Optionen als String
	 *
	 * \details
	 *
	 * \see toString
	 * \see toOut
	 */
	private String getAllOptionsAsString() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getAllOptionsAsString";
		String strT = conClassName + "\n";
		//		final StringBuffer strBuffer = new StringBuffer();
		// strT += IterateAllDataElementsByAnnotation(objParentClass, this,
		// JSOptionsClass.IterationTypes.toString, strBuffer);
		// strT += IterateAllDataElementsByAnnotation(objParentClass, this, 13,
		// strBuffer);
		strT += this.toString(); // fix
		//
		return strT;
	} // private String getAllOptionsAsString ()

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#setAllOptions(java.util.HashMap)
	 */
	@Override
	public void setAllOptions(final HashMap<String, String> pobjJSSettings) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setAllOptions";
		flgSetAllOptions = true;
		objSettings = pobjJSSettings;
		super.Settings(objSettings);
		super.setAllOptions(pobjJSSettings);
		flgSetAllOptions = false;
	} // public void setAllOptions (HashMap <String, String> JSSettings)

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
	public void CheckMandatory() throws JSExceptionMandatoryOptionMissing //
			, Exception {
		try {
			super.CheckMandatory();
		}
		catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
	} // public void CheckMandatory ()

	/**
	 *
	 * \brief CommandLineArgs - Übernehmen der Options/Settings aus der
	 * Kommandozeile
	 *
	 * \details Die in der Kommandozeile beim Starten der Applikation
	 * angegebenen Parameter werden hier in die HashMap übertragen und danach
	 * den Optionen als Wert zugewiesen.
	 *
	 * \return void
	 *
	 * @param pstrArgs
	 * @throws Exception
	 */
	@Override
	public void CommandLineArgs(final String[] pstrArgs) {
		super.CommandLineArgs(pstrArgs);
		this.setAllOptions(super.objSettings);
	}
	/**
	* \var ssh_auth_file : This parameter specifies the path and name of a us
	*
	This parameter specifies the path and name of a user's private key file that is used for authentication with an SSH server. This parameter has to be specified should the publickey authentication method have been specified in the ssh_auth_method parameter. Should the private key file be secured by a passphrase, then the passphrase has to be specified using the password parameter.
	*
	*/
	@JSOptionDefinition(name = "ssh_auth_file", description = "This parameter specifies the path and name of a us", key = "ssh_auth_file", type = "SOSOptionInFileName", mandatory = false)
	public SOSOptionInFileName	ssh_auth_file	= new SOSOptionInFileName(this, conClassName + ".ssh_auth_file", // HashMap-Key
														"This parameter specifies the path and name of a us", // Titel
														" ", // InitValue
														" ", // DefaultValue
														false // isMandatory
												);
	public SOSOptionInFileName	auth_file		= (SOSOptionInFileName) ssh_auth_file.SetAlias(conClassName + ".auth_file");

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#getAuth_file()
	 */
	@Override
	public SOSOptionInFileName getAuth_file() {
		return ssh_auth_file;
	}

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#setAuth_file(com.sos.JSHelper.Options.SOSOptionInFileName)
	 */
	@Override
	public void setAuth_file(final SOSOptionInFileName p_ssh_auth_file) {
		ssh_auth_file = p_ssh_auth_file;
	}
	/**
	* \var ssh_auth_method : This parameter specifies the authentication method
	*
	This parameter specifies the authentication method for the SSH server - the publickey and password methods are supported. Should the publickey authentication method be used, then the path name of the private key file has to be specified with the ssh_auth_file parameter. Should the private key file be secured by a passphrase then the passphrase has to be specified with the password parameter. For the password authentication method the password for the user account has to be specified using the password parameter. The authentication methods that are enabled depend on the SSH server configuration. Not all SSH servers are configured for password authentication.
	*
	*/
	@JSOptionDefinition(name = "ssh_auth_method", description = "This parameter specifies the authentication method", key = "ssh_auth_method", type = "SOSOptionStringValueList", mandatory = false)
	public SOSOptionAuthenticationMethod	ssh_auth_method	= new SOSOptionAuthenticationMethod(this, conClassName + ".ssh_auth_method", // HashMap-Key
																	"This parameter specifies the authentication method", // Titel
																	"publickey", // InitValue
																	"publickey", // DefaultValue
																	false // isMandatory
															);
	public SOSOptionAuthenticationMethod	auth_method		= (SOSOptionAuthenticationMethod) ssh_auth_method.SetAlias(conClassName + ".auth_method");

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#getAuth_method()
	 */
	@Override
	public SOSOptionAuthenticationMethod getAuth_method() {
		return ssh_auth_method;
	}

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#setAuth_method(com.sos.JSHelper.Options.SOSOptionAuthenticationMethod)
	 */
	@Override
	public void setAuth_method(final SOSOptionAuthenticationMethod p_ssh_auth_method) {
		ssh_auth_method = p_ssh_auth_method;
	}

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#setUser(com.sos.JSHelper.Options.SOSOptionUserName)
	 */
	@Override
	public void setUser(final SOSOptionUserName pobjUser) {
		user.Value(pobjUser.Value());
	}
	/**
	* \var proxy_host : host name or the IP address of a proxy
	*
	* The value of this parameter is the host name or the IP address of a proxy.
	*
	*/
	@JSOptionDefinition(name = "proxy_host", description = "Host name or the IP address of a proxy", key = "proxy_host", type = "SOSOptionHostName", mandatory = false)
	public SOSOptionHostName	proxy_host	= new SOSOptionHostName(this, conClassName + ".proxy_host", // HashMap-Key
													"The value of this parameter is the host name or th", // Titel
													"", // InitValue
													"", // DefaultValue
													false // isMandatory
											);

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#getproxy_host()
	 */
	public SOSOptionHostName getproxy_host() {
		return proxy_host;
	}

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#setproxy_host(com.sos.JSHelper.Options.SOSOptionHostName)
	 */
	public void setproxy_host(final SOSOptionHostName p_proxy_host) {
		proxy_host = p_proxy_host;
	}
	/**
	* \var proxy_port : Port-Number to be used for a proxy
	*
	* This parameter specifies the port of a proxy that is used in order to establish a connection to the SSH server via SSL/TLS, see parameter http_proxy_host.
	*
	*/
	@JSOptionDefinition(name = "proxy_port", description = "Port-Number to be used for a proxy", key = "proxy_port", type = "SOSOptionPortNumber", mandatory = false)
	public SOSOptionPortNumber	proxy_port	= new SOSOptionPortNumber(this, conClassName + ".proxy_port", // HashMap-Key
													"This parameter specifies the port of a proxy that", // Titel
													"", // InitValue
													"", // DefaultValue
													false // isMandatory
											);

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#getproxy_port()
	 */
	public SOSOptionPortNumber getproxy_port() {
		return proxy_port;
	}

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#setproxy_port(com.sos.JSHelper.Options.SOSOptionPortNumber)
	 */
	public void setproxy_port(final SOSOptionPortNumber p_proxy_port) {
		proxy_port = p_proxy_port;
	}
	/**
	* \var proxy_user : User name to be used for a proxy
	*
	* This parameter specifies the user name of a proxy that is used in order to establish a connection to the SSH server via SSL/TLS, see parameter proxy_host.
	*
	*/
	@JSOptionDefinition(name = "proxy_user", description = "User name to be used for a proxy", key = "proxy_user", type = "SOSOptionUserName", mandatory = false)
	public SOSOptionUserName	proxy_user	= new SOSOptionUserName(this, conClassName + ".proxy_user", // HashMap-Key
													"This parameter specifies the user name of a proxy that", // Titel
													"", // InitValue
													"", // DefaultValue
													false // isMandatory
											);

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#getproxy_user()
	 */
	public SOSOptionUserName getproxy_user() {
		return proxy_user;
	}

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#setproxy_user(com.sos.JSHelper.Options.SOSOptionUserName)
	 */
	public void setproxy_user(final SOSOptionUserName p_proxy_user) {
		proxy_user = p_proxy_user;
	}
	/**
	* \var proxy_password : Password to be used for a proxy
	*
	* This parameter specifies the password of a proxy that is used in order to establish a connection to the SSH server via SSL/TLS, see parameter proxy_host.
	*
	*/
	@JSOptionDefinition(name = "proxy_password", description = "Password to be used for a proxy", key = "proxy_password", type = "SOSOptionPassword", mandatory = false)
	public SOSOptionPassword	proxy_password	= new SOSOptionPassword(this, conClassName + ".proxy_password", // HashMap-Key
														"This parameter specifies the password of a proxy that", // Titel
														"", // InitValue
														"", // DefaultValue
														false // isMandatory
												);

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#getproxy_password()
	 */
	public SOSOptionPassword getproxy_password() {
		return proxy_password;
	}

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#setproxy_password(com.sos.JSHelper.Options.SOSOptionPassword)
	 */
	public void setproxy_password(final SOSOptionPassword p_proxy_password) {
		proxy_password = p_proxy_password;
	}
	/**
	* \var domain : domain
	*
	* This parameter specifies the domain.
	*
	*/
	@JSOptionDefinition(name = "domain", description = "Domain", key = "domain", type = "SOSOptionString", mandatory = false)
	public SOSOptionString	domain	= new SOSOptionString(this, conClassName + ".domain", // HashMap-Key
											"This parameter specifies the domain", // Titel
											"", // InitValue
											"", // DefaultValue
											false // isMandatory
									);

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#getdomain()
	 */
	@Override
	public SOSOptionString getdomain() {
		return domain;
	}

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#setdomain(com.sos.JSHelper.Options.SOSOptionString)
	 */
	@Override
	public void setdomain(final SOSOptionString p_domain) {
		domain = p_domain;
	}
} // public class SOSConnection2OptionsSuperClass