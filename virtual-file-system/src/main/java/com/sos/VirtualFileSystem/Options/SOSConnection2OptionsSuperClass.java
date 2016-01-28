package com.sos.VirtualFileSystem.Options;
import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionAuthenticationMethod;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionFTPSClientSecurity;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.JSHelper.Options.SOSOptionHostName;
import com.sos.JSHelper.Options.SOSOptionInFileName;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionJavaClassName;
import com.sos.JSHelper.Options.SOSOptionKeyStoreType;
import com.sos.JSHelper.Options.SOSOptionPassword;
import com.sos.JSHelper.Options.SOSOptionPlatform;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.JSHelper.Options.SOSOptionProxyProtocol;
import com.sos.JSHelper.Options.SOSOptionRegExp;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.JSHelper.Options.SOSOptionTransferMode;
import com.sos.JSHelper.Options.SOSOptionTransferType;
import com.sos.JSHelper.Options.SOSOptionUrl;
import com.sos.JSHelper.Options.SOSOptionUserName;
import com.sos.JSHelper.interfaces.ISOSDataProviderOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.i18n.annotation.I18NResourceBundle;

@JSOptionClass( name = "SOSConnection2OptionsSuperClass", description = "SOSConnection2OptionsSuperClass")
@I18NResourceBundle( baseName = "SOSVirtualFileSystem",	defaultLocale = "en")
public class SOSConnection2OptionsSuperClass extends JSOptionsClass implements ISOSAuthenticationOptions, ISOSDataProviderOptions {
	private static final long	serialVersionUID	= 1997338600688654140L;
	private final String		className		= this.getClass().getSimpleName();

	@JSOptionDefinition(
						name = "url",
						description = "the url for the connection",
						key = "url",
						type = "SOSOptionURL",
						mandatory = false)
	public SOSOptionUrl			url					= new SOSOptionUrl(
															this,
															className + ".url", 
															"the url for the connection", 
															"",
															"", 
															false);

	@Override
	public SOSOptionUrl geturl() {
		return url;
	}

	@Override
	public ISOSDataProviderOptions seturl(final SOSOptionUrl val) {
		url = val;
		return this;
	} 
	
	@JSOptionDefinition(
						name = "include",
						description = "the include directive as an option",
						key = "include",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	include	= new SOSOptionString(
											this,
											className + ".include",
											"the include directive as an option",
											"", 
											"",
											false);

	@Override
	public String getinclude() {
		return include.Value();
	}

	@Override
	public ISOSDataProviderOptions setinclude(final String val) {
		include.Value(val);
		return this;
	} 
	
	@JSOptionDefinition(
						name = "use_zlib_compression",
						description = "Use the zlib cmpression on sftp",
						key = "use_zlib_compression",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	use_zlib_compression	= new SOSOptionBoolean( 
															this,
															className + ".use_zlib_compression",
															"Use the zlib cmpression on sftp",
															"false",
															"false",
															false);

	@Override
	public SOSOptionBoolean getuse_zlib_compression() {
		return use_zlib_compression;
	} 
	
	@Override
	public ISOSDataProviderOptions setuse_zlib_compression(final SOSOptionBoolean val) {
		use_zlib_compression = val;
		return this;
	} 
	
	@JSOptionDefinition(
						name = "zlib_compression_level",
						description = "the compression level to use",
						key = "zlib_compression_level",
						type = "SOSOptionInteger",
						mandatory = false)
	public SOSOptionInteger	zlib_compression_level	= new SOSOptionInteger(
															this,
															className + ".zlib_compression_level",
															"the compression level to use",
															"1",
															"1",
															false);

	@Override
	public SOSOptionInteger getzlib_compression_level() {
		return zlib_compression_level;
	}
	
	@Override
	public ISOSDataProviderOptions setzlib_compression_level(final SOSOptionInteger val) {
		zlib_compression_level = val;
		return this;
	} 
	
	@JSOptionDefinition(
						name = "ProtocolCommandListener",
						description = "Activate the logging for Apache ftp client",
						key = "Protocol_Command_Listener",
						type = "SOSOptionBoolean",
						mandatory = true)
	public SOSOptionBoolean	ProtocolCommandListener	= new SOSOptionBoolean(
															this,
															className + ".Protocol_Command_Listener", 
															"Activate the logging for Apache ftp client",
															"false",
															"false",
															true);

	@Override
	public String getProtocolCommandListener() {
		return ProtocolCommandListener.Value();
	} 
	
	@Override
	public ISOSDataProviderOptions setProtocolCommandListener(final String val) {
		ProtocolCommandListener.Value(val);
		return this;
	} 
	
	@JSOptionDefinition(
						name = "account",
						description = "Optional account info for authentication with an",
						key = "account",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	account	= new SOSOptionString(this, className + ".account", // HashMap-Key
											"Optional account info for authentication with an", // Titel
											"", // InitValue
											"", // DefaultValue
											false // isMandatory
									);

	@Override
	public SOSOptionString getaccount() {
		return account;
	}

	@Override
	public void setaccount(final SOSOptionString val) {
		account = val;
	}

	@JSOptionDefinition(
						name = "make_Dirs",
						description = "Create missing Directory on Target",
						key = "make_Dirs",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	makeDirs				= new SOSOptionBoolean(
															this, 
															className + ".make_Dirs", 
															"Create missing Directory on Source/Target", 
															"true",
															"true",
															false);
	
	public SOSOptionBoolean	createFoldersOnTarget	= (SOSOptionBoolean) makeDirs.SetAlias("create_folders_on_target");
	
	public SOSOptionBoolean	createFolders			= (SOSOptionBoolean) makeDirs.SetAlias("create_folders");

	@Override
	public String getmake_Dirs() {
		return makeDirs.Value();
	} 

	@Override
	public ISOSDataProviderOptions setmake_Dirs(final String val) {
		makeDirs.Value(val);
		return this;
	} 
	
	@JSOptionDefinition(
						name = "dir",
						description = "Optional account info for authentication with an",
						key = "dir",
						type = "SOSOptionFolderName",
						mandatory = false)
	public SOSOptionFolderName	Directory	= new SOSOptionFolderName(this, className + ".dir", // HashMap-Key
													"local_dir Local directory into which or from which", // Titel
													"", // InitValue
													"", // DefaultValue
													false // isMandatory
											);
	
	public SOSOptionFolderName	FolderName	= (SOSOptionFolderName) Directory.SetAlias("Folder_Name");
		
	@JSOptionDefinition(
						name = "platform",
						description = "platform on which the app is running",
						key = "platform",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionPlatform	platform	= new SOSOptionPlatform(
													this, 
													className + ".platform",
													"platform on which the app is running",
													"", 
													"", 
													false);

	@Override
	public String getplatform() {
		return platform.Value();
	}

	@Override
	public ISOSDataProviderOptions setplatform(final String val) {
		platform.Value(val);
		return this;
	} 
	
	@JSOptionDefinition(
						name = "replacement",
						description = "String for replacement of matching character seque",
						key = "replacement",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	replacement	= new SOSOptionString(this, className + ".replacement", // HashMap-Key
												"String for replacement of matching character seque", // Titel
												null, // InitValue
												null, // DefaultValue
												false // isMandatory
										);

	@Override
	public SOSOptionString getreplacement() {
		return replacement;
	}

	@Override
	public void setreplacement(final SOSOptionString p_replacement) {
		replacement = p_replacement;
	}
	
	public SOSOptionString	ReplaceWith	= (SOSOptionString) replacement.SetAlias(className + ".ReplaceWith");
	
	@JSOptionDefinition(
						name = "replacing",
						description = "Regular expression for filename replacement with",
						key = "replacing",
						type = "SOSOptionRegExp",
						mandatory = false)
	public SOSOptionRegExp	replacing	= new SOSOptionRegExp(this, className + ".replacing", // HashMap-Key
												"Regular expression for filename replacement with", // Titel
												"", // InitValue
												"", // DefaultValue
												false // isMandatory
										);

	@Override
	public SOSOptionRegExp getreplacing() {
		return replacing;
	}

	@Override
	public void setreplacing(final SOSOptionRegExp val) {
		replacing = val;
	}
	
	public SOSOptionRegExp			ReplaceWhat				= (SOSOptionRegExp) replacing.SetAlias(className + ".ReplaceWhat");
	
	@JSOptionDefinition(
			name = "strict_hostKey_checking",
			description = "Check the hostkey against known hosts for SSH",
			key = "strict_hostKey_checking",
			type = "SOSOptionBoolean",
			mandatory = false)
	public SOSOptionBoolean	strictHostKeyChecking	= new SOSOptionBoolean(
																	this,
																	className + ".strict_hostkey_checking",
																	"Check the hostkey against known hosts for SSH",
																	"false",
																	"false",
																	false);

	@Override
	public SOSOptionBoolean getstrict_hostKey_checking() {
		return strictHostKeyChecking;
	}

	@Override
	public void setstrict_hostKey_checking(final String val) {
		strictHostKeyChecking.Value(val);
	}
	
	@JSOptionDefinition(
						name = "TFN_Post_Command",
						description = "Post commands executed after creating the final TargetFile",
						key = "TFN_Post_Command",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	TFN_Post_Command	= new SOSOptionString(
														this,
														className + ".TFN_Post_Command", 
														"Post commands executed after creating the final TargetFileName",
														"", 
														"",
														false);

	@Override
	public SOSOptionString getTFN_Post_Command() {
		return TFN_Post_Command;
	} 
	
	@Override
	public ISOSDataProviderOptions setTFN_Post_Command(final SOSOptionString val) {
		TFN_Post_Command = val;
		return this;
	} 
	
	@JSOptionDefinition(
						name = "Post_Command",
						description = "FTP-Command to be executed after transfer",
						key = "Post_Command",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	Post_Command	= new SOSOptionString(
													this,
													className + ".Post_Command", 
													"FTP-Command to be executed after transfer",
													"",
													"",
													false);

	@Override
	public String getPost_Command() {
		return Post_Command.Value();
	}
	
	@Override
	public ISOSDataProviderOptions setPost_Command(final String val) {
		Post_Command.Value(val);
		return this;
	} 
	
	@JSOptionDefinition(
						name = "Pre_Command",
						description = "FTP-Command to be execute before transfer",
						key = "Pre_Command",
						type = "SOSOptionString  ",
						mandatory = false)
	public SOSOptionString	Pre_Command	= new SOSOptionString(
												this,
												className + ".Pre_Command", 
												"", 
												"", 
												"", 
												false);

	@Override
	public String getPre_Command() {
		return Pre_Command.Value();
	} 
	
	@Override
	public ISOSDataProviderOptions setPre_Command(final String val) {
		Pre_Command.Value(val);
		return this;
	} 
	
	@JSOptionDefinition(
						name = "FtpS_protocol",
						description = "Type of FTPS-Protocol, e.g. SSL, TLS",
						key = "FtpS_protocol",
						type = "SOSOptionString",
						mandatory = true)
	public SOSOptionString	FtpS_protocol	= new SOSOptionString(
													this, 
													className + ".FtpS_protocol", 
													"Type of FTPS-Protocol, e.g. SSL, TLS", 
													"SSL",
													"SSL", 
													true);

	@Override
	public String getFtpS_protocol() {
		return FtpS_protocol.Value();
	} 
	
	@Override
	public ISOSDataProviderOptions setFtpS_protocol(final String val) {
		FtpS_protocol.Value(val);
		return this;
	} 
	
	@JSOptionDefinition(
						name = "loadClassName",
						description = "Java Class which implements the ISOSVFSHandlerInterface",
						key = "load_Class_Name",
						type = "SOSOptionString ",
						mandatory = false)
	public SOSOptionJavaClassName	loadClassName			= new SOSOptionJavaClassName(
																	this, 
																	className + ".load_Class_Name", 
																	"Java Class which implements the ISOSVFSHandlerInterface", 
																	"", 
																	"", 
																	false);
	
	public SOSOptionJavaClassName	DataProviderClassName	= (SOSOptionJavaClassName) loadClassName.SetAlias("Data_provider_class_name");

	@Override
	public String getloadClassName() {
		return loadClassName.Value();
	} 

	@Override
	public ISOSDataProviderOptions setloadClassName(final String val) {
		loadClassName.Value(val);
		return this;
	} 
	
	@JSOptionDefinition(
						name = "javaClassPath",
						description = "",
						key = "javaClassPath",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	javaClassPath	= new SOSOptionString(this, className + ".javaClassPath", // HashMap-Key
													"", // Titel
													"", // InitValue
													"", // DefaultValue
													false // isMandatory
											);

	@Override
	public SOSOptionString getjavaClassPath() {
		return javaClassPath;
	}

	@Override
	public void setjavaClassPath(final SOSOptionString val) {
		javaClassPath = val;
	}
	
	@JSOptionDefinition(
						name = "host",
						description = "Host-Name This parameter specifies th",
						key = "host",
						type = "SOSOptionHostName",
						mandatory = false)
	public SOSOptionHostName	host	= new SOSOptionHostName(this, className + ".host", // HashMap-Key
												"Host-Name This parameter specifies th", // Titel
												"", // InitValue
												"", // DefaultValue
												false // isMandatory
										);

	@Override
	public SOSOptionHostName getHost() {
		return host;
	}

	@Override
	public void setHost(final SOSOptionHostName val) {
		host = val;
	}
	
	public SOSOptionHostName	HostName		= (SOSOptionHostName) host.SetAlias(className + ".HostName");
	
	public SOSOptionHostName	FtpHostName		= (SOSOptionHostName) host.SetAlias(className + ".ftp_host");
	
	@JSOptionDefinition(
						name = "passive_mode",
						description = "passive_mode Passive mode for FTP is often used wit",
						key = "passive_mode",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean		passive_mode	= new SOSOptionBoolean(this, className + ".passive_mode", // HashMap-Key
														"passive_mode Passive mode for FTP is often used wit", // Titel
														"false", // InitValue
														"false", // DefaultValue
														false // isMandatory
												);

	@Override
	public SOSOptionBoolean getpassive_mode() {
		return passive_mode;
	}

	@Override
	public void setpassive_mode(final SOSOptionBoolean val) {
		passive_mode = val;
	}
	
	public SOSOptionBoolean		FTPTransferModeIsPassive	= (SOSOptionBoolean) passive_mode.SetAlias(className + ".FTPTransferModeIsPassive");
	
	public SOSOptionBoolean		FTPPassiveMode				= (SOSOptionBoolean) passive_mode.SetAlias(className + ".ftp_passive_mode");
	
	@JSOptionDefinition(
						name = "port",
						description = "Port-Number to be used for Data-Transfer",
						key = "port",
						type = "SOSOptionPortNumber",
						mandatory = true)
	public SOSOptionPortNumber	port						= new SOSOptionPortNumber(this, className + ".port", // HashMap-Key
																	"Port-Number to be used for Data-Transfer", // Titel
																	"21", // InitValue
																	"21", // DefaultValue
																	true // isMandatory
															);
	
	public SOSOptionPortNumber	ftp_port					= (SOSOptionPortNumber) port.SetAlias("ftp_port");

	@Override
	public SOSOptionPortNumber getport() {
		return port;
	}

	@Override
	public void setport(final SOSOptionPortNumber val) {
		port = val;
	}
	
	@JSOptionDefinition(
						name = "protocol",
						description = "Type of requested Datatransfer The values ftp, sftp, htttp ...",
						key = "protocol",
						type = "SOSOptionStringValueList",
						mandatory = true)
	public SOSOptionTransferType	protocol		= new SOSOptionTransferType(this, className + ".protocol", // HashMap-Key
															"Type of requested Datatransfer The values ftp, sftp", // Titel
															"", // InitValue
															"ftp", // DefaultValue
															true // isMandatory
													);
	
	public SOSOptionTransferType	ftp_protocol	= (SOSOptionTransferType) protocol.SetAlias("ftp_protocol");

	@Override
	public SOSOptionTransferType getprotocol() {
		return protocol;
	}

	@Override
	public void setprotocol(final SOSOptionTransferType val) {
		protocol = val;
	}
	
	public SOSOptionTransferType	TransferProtocol		= (SOSOptionTransferType) protocol.SetAlias(className + ".TransferProtocol");
	
	@JSOptionDefinition(
						name = "transfer_mode",
						description = "Type of Character-Encoding Transfe",
						key = "transfer_mode",
						type = "SOSOptionTransferMode",
						mandatory = false)
	public SOSOptionTransferMode	transfer_mode			= new SOSOptionTransferMode(this, className + ".transfer_mode", // HashMap-Key
																	"Type of Character-Encoding Transfe", // Titel
																	"ascii;binary;text", // InitValue
																	"binary", // DefaultValue
																	false // isMandatory
															);
	
	public SOSOptionTransferMode	ftp_transfer_mode		= (SOSOptionTransferMode) transfer_mode.SetAlias("ftp_transfer_mode");
		
	@Override
	public SOSOptionTransferMode gettransfer_mode() {
		return transfer_mode;
	}

	@Override
	public void settransfer_mode(final SOSOptionTransferMode val) {
		transfer_mode = val;
	}

	@JSOptionDefinition(
						name = "user",
						description = "UserID of user in charge User name",
						key = "user",
						type = "SOSOptionUserName",
						mandatory = true)
	public SOSOptionUserName	user	= new SOSOptionUserName(this, className + ".user", // HashMap-Key
												"UserID of user in charge User name", // Titel
												"", // InitValue
												"anonymous", // DefaultValue
												false // isMandatory
										);

	@Override public SOSOptionUserName getUser() {
		return user;
	}

	@JSOptionDefinition(
						name = "password",
						description = "Password for UserID Password for a",
						key = "password",
						type = "SOSOptionPassword",
						mandatory = false)
	public SOSOptionPassword	password	= new SOSOptionPassword(this, className + ".password", // HashMap-Key
													"Password for UserID Password for a", // Titel
													"", // InitValue
													"", // DefaultValue
													false // isMandatory
											);

	@Override public SOSOptionPassword getPassword() {
		return password;
	}

	@Override public void setPassword(final SOSOptionPassword val) {
		password = val;
	}

	public SOSConnection2OptionsSuperClass() {
		objParentClass = this.getClass();
	} 

	public SOSConnection2OptionsSuperClass(final JSListener listener) {
		this();
	} 

	public SOSConnection2OptionsSuperClass(final HashMap<String, String> settings) throws Exception {
		this();
		this.setAllOptions(settings);
	} 
	
	@Override public void setAllOptions(final HashMap<String, String> settings)  {
		flgSetAllOptions = true;
		objSettings = settings;
		super.Settings(objSettings);
		super.setAllOptions(settings);
		flgSetAllOptions = false;
	} 

	@Override 
	public void CheckMandatory() throws JSExceptionMandatoryOptionMissing , Exception {
		try {
			super.CheckMandatory();
		}
		catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
	} 

	
	@Override 
	public void CommandLineArgs(final String[] args)  {
		super.CommandLineArgs(args);
		this.setAllOptions(super.objSettings);
	}
	
	@JSOptionDefinition(
						name = "ssh_auth_file",
						description = "This parameter specifies the path and name of a us",
						key = "ssh_auth_file",
						type = "SOSOptionInFileName",
						mandatory = false)
	public SOSOptionInFileName	ssh_auth_file	= new SOSOptionInFileName(this, className + ".ssh_auth_file", // HashMap-Key
														"This parameter specifies the path and name of a us", // Titel
														" ", // InitValue
														" ", // DefaultValue
														false // isMandatory
												);
	
	public SOSOptionInFileName	auth_file		= (SOSOptionInFileName) ssh_auth_file.SetAlias(className + ".auth_file");

	@Override 
	public SOSOptionInFileName getAuth_file() {
		return ssh_auth_file;
	}

	@Override 
	public void setAuth_file(final SOSOptionInFileName val) {
		ssh_auth_file = val;
	}
	
	@JSOptionDefinition(
						name = "ssh_auth_method",
						description = "This parameter specifies the authentication method",
						key = "ssh_auth_method",
						type = "SOSOptionStringValueList",
						mandatory = false)
	public SOSOptionAuthenticationMethod	ssh_auth_method	= new SOSOptionAuthenticationMethod(this, className + ".ssh_auth_method", // HashMap-Key
																	"This parameter specifies the authentication method", // Titel
																	"publickey", // InitValue
																	"publickey", // DefaultValue
																	false // isMandatory
															);
	
	public SOSOptionAuthenticationMethod	auth_method		= (SOSOptionAuthenticationMethod) ssh_auth_method.SetAlias(className + ".auth_method");

	@Override 
	public SOSOptionAuthenticationMethod getAuth_method() {
		return ssh_auth_method;
	}

	@Override 
	public void setAuth_method(final SOSOptionAuthenticationMethod val) {
		ssh_auth_method = val;
	}

	@Override 
	public void setUser(final SOSOptionUserName pobjUser) {
		user.Value(pobjUser.Value());
	}
	
	@JSOptionDefinition(
						name = "ftps_client_security",
						description = "FTPS Client Security",
						key = "ftps_client_secutity",
						type = "SOSOptionFTPSClientSecurity",
						mandatory = false)
	public SOSOptionFTPSClientSecurity	ftps_client_security	= new SOSOptionFTPSClientSecurity(this, className + ".ftps_client_security", // HashMap-Key
													"FTPS Client Security", // Titel
													SOSOptionFTPSClientSecurity.ClientSecurity.explicit.name(), // InitValue
													SOSOptionFTPSClientSecurity.ClientSecurity.explicit.name(), // DefaultValue
													false // isMandatory
											);

	public SOSOptionFTPSClientSecurity getftps_client_security() {
		return ftps_client_security;
	}

	public void setftps_client_security(SOSOptionFTPSClientSecurity val) {
		ftps_client_security = val;
	}

	@JSOptionDefinition(
						name = "proxy_protocol",
						description = "Proxy protocol http, socks4 or socks5",
						key = "proxy_protocol",
						type = "SOSOptionProxyProtocol",
						mandatory = false)
	public SOSOptionProxyProtocol	proxy_protocol	= new SOSOptionProxyProtocol(this, className + ".proxy_protocol", // HashMap-Key
													"Proxy protocol", // Titel
													SOSOptionProxyProtocol.Protocol.http.name(), // InitValue
													SOSOptionProxyProtocol.Protocol.http.name(), // DefaultValue
													false // isMandatory
											);

	public SOSOptionProxyProtocol getproxy_protocol() {
		return proxy_protocol;
	}

	public void setproxy_host(SOSOptionProxyProtocol val) {
		proxy_protocol = val;
	}

	@JSOptionDefinition(
						name = "accept_untrusted_certificate",
						description = "Accept a valid certificat that could not be verified to be trusted",
						key = "accept_untrusted_certificate",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	accept_untrusted_certificate	= new SOSOptionBoolean(this, className + ".accept_untrusted_certificate", // HashMap-Key
													"Accept a valid certificat that could not be verified to be trusted", // Titel
													"", // InitValue
													"false", // DefaultValue
													false // isMandatory
											);

	public SOSOptionBoolean getaccept_untrusted_certificate() {
		return accept_untrusted_certificate;
	}

	public void setaccept_untrusted_certificate(SOSOptionBoolean val) {
		accept_untrusted_certificate = val;
	}

	@JSOptionDefinition(
						name = "verify_certificate_hostname",
						description = "The certificate verification process will always verify the DNS name of the certificate presented by the server, with the hostname of the server in the URL used by the client.",
						key = "verify_certificate_hostname",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	verify_certificate_hostname	= new SOSOptionBoolean(this, className + ".verify_certificate_hostname", // HashMap-Key
													"The certificate verification process will always verify the DNS name of the certificate presented by the server, with the hostname of the server in the URL used by the client", // Titel
													"true", // InitValue
													"true", // DefaultValue
													false // isMandatory
											);

	public SOSOptionBoolean getverify_certificate_hostname() {
		return verify_certificate_hostname;
	}

	public void setverify_certificate_hostname(SOSOptionBoolean val) {
		verify_certificate_hostname = val;
	}
	
	@JSOptionDefinition(
						name = "proxy_host",
						description = "Host name or the IP address of a proxy",
						key = "proxy_host",
						type = "SOSOptionHostName",
						mandatory = false)
	public SOSOptionHostName	proxy_host	= new SOSOptionHostName(this, className + ".proxy_host", // HashMap-Key
													"The value of this parameter is the host name or th", // Titel
													"", // InitValue
													"", // DefaultValue
													false // isMandatory
											);
	public SOSOptionHostName getproxy_host() {
		return proxy_host;
	}

	public void setproxy_host(final SOSOptionHostName val) {
		proxy_host = val;
	}
	
	@JSOptionDefinition(
						name = "proxy_port",
						description = "Port-Number to be used for a proxy",
						key = "proxy_port",
						type = "SOSOptionPortNumber",
						mandatory = false)
	public SOSOptionPortNumber	proxy_port	= new SOSOptionPortNumber(this, className + ".proxy_port", // HashMap-Key
													"This parameter specifies the port of a proxy that", // Titel
													"", // InitValue
													"", // DefaultValue
													false // isMandatory
											);

	public SOSOptionPortNumber getproxy_port() {
		return proxy_port;
	}

	public void setproxy_port(final SOSOptionPortNumber val) {
		proxy_port = val;
	}
	
	@JSOptionDefinition(
						name = "proxy_user",
						description = "User name to be used for a proxy",
						key = "proxy_user",
						type = "SOSOptionUserName",
						mandatory = false)
	public SOSOptionUserName	proxy_user	= new SOSOptionUserName(this, className + ".proxy_user", // HashMap-Key
													"This parameter specifies the user name of a proxy that", // Titel
													"", // InitValue
													"", // DefaultValue
													false // isMandatory
											);

	public SOSOptionUserName getproxy_user() {
		return proxy_user;
	}

	public void setproxy_user(final SOSOptionUserName val) {
		proxy_user = val;
	}

	@JSOptionDefinition(
						name = "proxy_password",
						description = "Password to be used for a proxy",
						key = "proxy_password",
						type = "SOSOptionPassword",
						mandatory = false)
	public SOSOptionPassword	proxy_password	= new SOSOptionPassword(this, className + ".proxy_password", // HashMap-Key
														"This parameter specifies the password of a proxy that", // Titel
														"", // InitValue
														"", // DefaultValue
														false // isMandatory
												);

	public SOSOptionPassword getproxy_password() {
		return proxy_password;
	}

	public void setproxy_password(final SOSOptionPassword val) {
		proxy_password = val;
	}

	@JSOptionDefinition(
						name = "domain",
						description = "Domain",
						key = "domain",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	domain	= new SOSOptionString(this, className + ".domain", // HashMap-Key
											"This parameter specifies the domain", // Titel
											"", // InitValue
											"", // DefaultValue
											false // isMandatory
									);

	@Override
	public SOSOptionString getdomain() {
		return domain;
	}

	@Override
	public void setdomain(final SOSOptionString val) {
		domain = val;
	}

	@JSOptionDefinition(
						name = "keystore_type",
						description = "keystore type. e.g. JKS,JCEKS,PKCS12,PKCS11,DKS",
						key = "keystore_type",
						type = "SOSOptionKeyStoreType",
						mandatory = false)
	public SOSOptionKeyStoreType	keystore_type	= new SOSOptionKeyStoreType(this, className + ".keystore_type", // HashMap-Key
											"This parameter specifies the keystore type", // Titel
											SOSOptionKeyStoreType.Type.JKS.name(), // InitValue
											SOSOptionKeyStoreType.Type.JKS.name(), // DefaultValue
											false // isMandatory
									);
	
	public SOSOptionKeyStoreType getkeystore_type() {
		return keystore_type;
	}

	public void setkeystore_type(final SOSOptionKeyStoreType val) {
		keystore_type = val;
	}

	@JSOptionDefinition(
						name = "keystore_file",
						description = "keystore file",
						key = "keystore_file",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	keystore_file	= new SOSOptionString(this, className + ".keystore_file", // HashMap-Key
											"This parameter specifies the keystore file path", // Titel
											"", // InitValue
											"", // DefaultValue
											false // isMandatory
									);
	
	public SOSOptionString getkeystore_file() {
		return keystore_file;
	}

	public void setkeystore_file(final SOSOptionString val) {
		keystore_file = val;
	}
	
	@JSOptionDefinition(
						name = "keystore_password",
						description = "Password to be used for a keystore",
						key = "keystore_password",
						type = "SOSOptionPassword",
						mandatory = false)
	public SOSOptionPassword	keystore_password	= new SOSOptionPassword(this, className + ".keystore_password", // HashMap-Key
														"This parameter specifies the password of a keystore", // Titel
														"", // InitValue
														"", // DefaultValue
														false // isMandatory
												);
	
	public SOSOptionPassword getkeystore_password() {
		return keystore_password;
	}

	public void setkeystore_password(final SOSOptionPassword val) {
		keystore_password = val;
	}
	
	
  @JSOptionDefinition(name = "raise_exception_on_error", description = "Raise an Exception if an error occured", key = "raise_exception_on_error", type = "SOSOptionBoolean", mandatory = true)
  public SOSOptionBoolean   raise_exception_on_error  = new SOSOptionBoolean( 
                                this,
                                className + ".raise_exception_on_error", 
                                "Raise an Exception if an error occured", 
                                "true", 
                                "true", 
                                true);
  

  public SOSOptionBoolean getraise_exception_on_error() {
    return raise_exception_on_error;
  } 

  public void setraise_exception_on_error(final SOSOptionBoolean val) {
    this.raise_exception_on_error = val;
  } 

  @JSOptionDefinition(name = "ignore_error", description = "Should the value true be specified, then execution errors", key = "ignore_error", type = "SOSOptionString", mandatory = false)
  public SOSOptionBoolean         ignore_error            = new SOSOptionBoolean(this, className + ".ignore_error", // HashMap-Key
                                            "Should the value true be specified, then execution errors", // Titel
                                            "false", // InitiValue
                                            "false", // DefaultValue
                                            false // isMandatory
                                        );  // Should the value true be specified, then execution errors
  
  public SOSOptionBoolean getIgnore_error() {
    return ignore_error;
  }

  public void setIgnore_error(final SOSOptionBoolean val) {
    this.ignore_error = val;
  }
   
}