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
import com.sos.JSHelper.Options.SOSOptionStringValueList;
import com.sos.JSHelper.Options.SOSOptionTransferMode;
import com.sos.JSHelper.Options.SOSOptionTransferType;
import com.sos.JSHelper.Options.SOSOptionUrl;
import com.sos.JSHelper.Options.SOSOptionUserName;
import com.sos.JSHelper.interfaces.ISOSDataProviderOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.i18n.annotation.I18NResourceBundle;

@JSOptionClass(name = "SOSConnection2OptionsSuperClass", description = "SOSConnection2OptionsSuperClass")
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSConnection2OptionsSuperClass extends JSOptionsClass implements ISOSAuthenticationOptions, ISOSDataProviderOptions {

    private static final long serialVersionUID = 1997338600688654140L;
    private static final String CLASSNAME = "SOSConnection2OptionsSuperClass";

    @JSOptionDefinition(name = "url", description = "the url for the connection", key = "url", type = "SOSOptionURL", mandatory = false)
    public SOSOptionUrl url = new SOSOptionUrl(this, CLASSNAME + ".url", "the url for the connection", "", "", false);

    @Override
    public SOSOptionUrl geturl() {
        return url;
    }

    @Override
    public ISOSDataProviderOptions seturl(final SOSOptionUrl pstrValue) {
        url = pstrValue;
        return this;
    }

    @JSOptionDefinition(name = "include", description = "the include directive as an option", key = "include", type = "SOSOptionString", mandatory = false)
    public SOSOptionString include = new SOSOptionString(this, CLASSNAME + ".include", "the include directive as an option", "", "", false);

    @Override
    public String getinclude() {
        return include.Value();
    }

    @Override
    public ISOSDataProviderOptions setinclude(final String pstrValue) {
        include.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "use_zlib_compression", description = "Use the zlib cmpression on sftp", key = "use_zlib_compression",
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean use_zlib_compression = new SOSOptionBoolean(this, CLASSNAME + ".use_zlib_compression", "Use the zlib compression on sftp", "false", 
            "false", false);

    @Override
    public SOSOptionBoolean getuse_zlib_compression() {
        return use_zlib_compression;
    }

    @Override
    public ISOSDataProviderOptions setuse_zlib_compression(final SOSOptionBoolean pstrValue) {
        use_zlib_compression = pstrValue;
        return this;
    }

    @JSOptionDefinition(name = "zlib_compression_level", description = "the compression level to use", key = "zlib_compression_level",
            type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger zlib_compression_level = new SOSOptionInteger(this, CLASSNAME + ".zlib_compression_level", "the compression level to use", "1", "1", 
            false);

    @Override
    public SOSOptionInteger getzlib_compression_level() {
        return zlib_compression_level;
    }

    @Override
    public ISOSDataProviderOptions setzlib_compression_level(final SOSOptionInteger pstrValue) {
        zlib_compression_level = pstrValue;
        return this;
    }

    @JSOptionDefinition(name = "ProtocolCommandListener", description = "Activate the logging for Apache ftp client", key = "Protocol_Command_Listener",
            type = "SOSOptionBoolean", mandatory = true)
    public SOSOptionBoolean ProtocolCommandListener = new SOSOptionBoolean(this, CLASSNAME + ".Protocol_Command_Listener", 
            "Activate the logging for Apache ftp client", "false", "false", true);

    @Override
    public String getProtocolCommandListener() {
        return ProtocolCommandListener.Value();
    }

    @Override
    public ISOSDataProviderOptions setProtocolCommandListener(final String pstrValue) {
        ProtocolCommandListener.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "account", description = "Optional account info for authentication with an", key = "account", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionString account = new SOSOptionString(this, CLASSNAME + ".account", "Optional account info for authentication with an", "", "", false);

    @Override
    public SOSOptionString getaccount() {
        return account;
    }

    @Override
    public void setaccount(final SOSOptionString p_account) {
        account = p_account;
    }

    @JSOptionDefinition(name = "make_Dirs", description = "Create missing Directory on Target", key = "make_Dirs", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean makeDirs = new SOSOptionBoolean(this, CLASSNAME + ".make_Dirs", "Create missing Directory on Source/Target", "true", "true", false);
    public SOSOptionBoolean createFoldersOnTarget = (SOSOptionBoolean) makeDirs.SetAlias("create_folders_on_target");
    public SOSOptionBoolean createFolders = (SOSOptionBoolean) makeDirs.SetAlias("create_folders");

    @Override
    public String getmake_Dirs() {
        return makeDirs.Value();
    }

    @Override
    public ISOSDataProviderOptions setmake_Dirs(final String pstrValue) {
        makeDirs.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "dir", description = "Optional account info for authentication with an", key = "dir", type = "SOSOptionFolderName",
            mandatory = false)
    public SOSOptionFolderName Directory = new SOSOptionFolderName(this, CLASSNAME + ".dir", "local_dir Local directory into which or from which", "", "", false);
    public SOSOptionFolderName FolderName = (SOSOptionFolderName) Directory.SetAlias("Folder_Name");

    @JSOptionDefinition(name = "platform", description = "platform on which the app is running", key = "platform", type = "SOSOptionString", mandatory = false)
    public SOSOptionPlatform platform = new SOSOptionPlatform(this, CLASSNAME + ".platform", "platform on which the app is running", "", "", false);

    @Override
    public String getplatform() {
        return platform.Value();
    }

    @Override
    public ISOSDataProviderOptions setplatform(final String pstrValue) {
        platform.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "replacement", description = "String for replacement of matching character seque", key = "replacement",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString replacement = new SOSOptionString(this, CLASSNAME + ".replacement", "String for replacement of matching character seque", null, null,
            false);
    public SOSOptionString ReplaceWith = (SOSOptionString) replacement.SetAlias(CLASSNAME + ".ReplaceWith");

    @Override
    public SOSOptionString getreplacement() {
        return replacement;
    }

    @Override
    public void setreplacement(final SOSOptionString p_replacement) {
        replacement = p_replacement;
    }

    @JSOptionDefinition(name = "replacing", description = "Regular expression for filename replacement with", key = "replacing", type = "SOSOptionRegExp",
            mandatory = false)
    public SOSOptionRegExp replacing = new SOSOptionRegExp(this, CLASSNAME + ".replacing", "Regular expression for filename replacement with", "", "", false);
    public SOSOptionRegExp ReplaceWhat = (SOSOptionRegExp) replacing.SetAlias(CLASSNAME + ".ReplaceWhat");

    @Override
    public SOSOptionRegExp getreplacing() {
        return replacing;
    }

    @Override
    public void setreplacing(final SOSOptionRegExp p_replacing) {
        replacing = p_replacing;
    }

    @JSOptionDefinition(name = "Strict_HostKey_Checking", description = "Check the hostkey against known hosts for SSH", key = "Strict_HostKey_Checking",
            type = "SOSOptionValueList", mandatory = false)
    public SOSOptionStringValueList StrictHostKeyChecking = new SOSOptionStringValueList(this, CLASSNAME + ".strict_hostkey_checking", 
            "Check the hostkey against known hosts for SSH", "ask;yes;no", "no", false);

    @Override
    public String getStrict_HostKey_Checking() {
        return StrictHostKeyChecking.Value();
    }

    @Override
    public ISOSDataProviderOptions setStrict_HostKey_Checking(final String pstrValue) {
        StrictHostKeyChecking.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "TFN_Post_Command", description = "Post commands executed after creating the final TargetFile", key = "TFN_Post_Command",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString TFN_Post_Command = new SOSOptionString(this, CLASSNAME + ".TFN_Post_Command", 
            "Post commands executed after creating the final TargetFileName", "", "", false);

    @Override
    public SOSOptionString getTFN_Post_Command() {
        return TFN_Post_Command;
    }

    @Override
    public ISOSDataProviderOptions setTFN_Post_Command(final SOSOptionString pstrValue) {
        TFN_Post_Command = pstrValue;
        return this;
    }

    @JSOptionDefinition(name = "Post_Command", description = "FTP-Command to be executed after transfer", key = "Post_Command", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionString Post_Command = new SOSOptionString(this, CLASSNAME + ".Post_Command", "FTP-Command to be executed after transfer", "", "", false);

    @Override
    public String getPost_Command() {
        return Post_Command.Value();
    }

    @Override
    public ISOSDataProviderOptions setPost_Command(final String pstrValue) {
        Post_Command.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "Pre_Command", description = "FTP-Command to be execute before transfer", key = "Pre_Command", type = "SOSOptionString  ",
            mandatory = false)
    public SOSOptionString Pre_Command = new SOSOptionString(this, CLASSNAME + ".Pre_Command", "", "", "", false);

    @Override
    public String getPre_Command() {
        return Pre_Command.Value();
    }

    @Override
    public ISOSDataProviderOptions setPre_Command(final String pstrValue) {
        Pre_Command.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "FtpS_protocol", description = "Type of FTPS-Protocol, e.g. SSL, TLS", key = "FtpS_protocol", type = "SOSOptionString",
            mandatory = true)
    public SOSOptionString FtpS_protocol = new SOSOptionString(this, CLASSNAME + ".FtpS_protocol", "Type of FTPS-Protocol, e.g. SSL, TLS", "SSL", "SSL", true);

    @Override
    public String getFtpS_protocol() {
        return FtpS_protocol.Value();
    }

    @Override
    public ISOSDataProviderOptions setFtpS_protocol(final String pstrValue) {
        FtpS_protocol.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "loadClassName", description = "Java Class which implements the ISOSVFSHandlerInterface", key = "load_Class_Name",
            type = "SOSOptionString ", mandatory = false)
    public SOSOptionJavaClassName loadClassName = new SOSOptionJavaClassName(this, CLASSNAME + ".load_Class_Name", 
            "Java Class which implements the ISOSVFSHandlerInterface", "", "", false);
    public SOSOptionJavaClassName DataProviderClassName = (SOSOptionJavaClassName) loadClassName.SetAlias("Data_provider_class_name");

    @Override
    public String getloadClassName() {
        return loadClassName.Value();
    }

    @Override
    public ISOSDataProviderOptions setloadClassName(final String pstrValue) {
        loadClassName.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "javaClassPath", description = "", key = "javaClassPath", type = "SOSOptionString", mandatory = false)
    public SOSOptionString javaClassPath = new SOSOptionString(this, CLASSNAME + ".javaClassPath", "", "", "", false);

    @Override
    public SOSOptionString getjavaClassPath() {
        return javaClassPath;
    }

    @Override
    public void setjavaClassPath(final SOSOptionString p_javaClassPath) {
        javaClassPath = p_javaClassPath;
    }

    @JSOptionDefinition(name = "host", description = "Host-Name This parameter specifies th", key = "host", type = "SOSOptionHostName", mandatory = false)
    public SOSOptionHostName host = new SOSOptionHostName(this, CLASSNAME + ".host", "Host-Name This parameter specifies th", "", "", false);
    public SOSOptionHostName HostName = (SOSOptionHostName) host.SetAlias(CLASSNAME + ".HostName");
    public SOSOptionHostName FtpHostName = (SOSOptionHostName) host.SetAlias(CLASSNAME + ".ftp_host");

    @Override
    public SOSOptionHostName getHost() {
        return host;
    }

    @Override
    public void setHost(final SOSOptionHostName p_host) {
        host = p_host;
    }

    @JSOptionDefinition(name = "passive_mode", description = "passive_mode Passive mode for FTP is often used wit", key = "passive_mode",
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean passive_mode = new SOSOptionBoolean(this, CLASSNAME + ".passive_mode", "passive_mode Passive mode for FTP is often used wit", "false",
            "false", false);
    public SOSOptionBoolean FTPTransferModeIsPassive = (SOSOptionBoolean) passive_mode.SetAlias(CLASSNAME + ".FTPTransferModeIsPassive");
    public SOSOptionBoolean FTPPassiveMode = (SOSOptionBoolean) passive_mode.SetAlias(CLASSNAME + ".ftp_passive_mode");

    @Override
    public SOSOptionBoolean getpassive_mode() {
        return passive_mode;
    }

    @Override
    public void setpassive_mode(final SOSOptionBoolean p_passive_mode) {
        passive_mode = p_passive_mode;
    }

    @JSOptionDefinition(name = "port", description = "Port-Number to be used for Data-Transfer", key = "port", type = "SOSOptionPortNumber", mandatory = true)
    public SOSOptionPortNumber port = new SOSOptionPortNumber(this, CLASSNAME + ".port", "Port-Number to be used for Data-Transfer", "21", "21", true);
    public SOSOptionPortNumber ftp_port = (SOSOptionPortNumber) port.SetAlias("ftp_port");

    @Override
    public SOSOptionPortNumber getport() {
        return port;
    }

    @Override
    public void setport(final SOSOptionPortNumber p_port) {
        port = p_port;
    }

    @JSOptionDefinition(name = "protocol", description = "Type of requested Datatransfer The values ftp, sftp", key = "protocol",
            type = "SOSOptionStringValueList", mandatory = true)
    public SOSOptionTransferType protocol = new SOSOptionTransferType(this, CLASSNAME + ".protocol", "Type of requested Datatransfer The values ftp, sftp", "", 
            "ftp", true);
    public SOSOptionTransferType ftp_protocol = (SOSOptionTransferType) protocol.SetAlias("ftp_protocol");
    public SOSOptionTransferType TransferProtocol = (SOSOptionTransferType) protocol.SetAlias(CLASSNAME + ".TransferProtocol");

    @Override
    public SOSOptionTransferType getprotocol() {
        return protocol;
    }

    @Override
    public void setprotocol(final SOSOptionTransferType p_protocol) {
        protocol = p_protocol;
    }

    @JSOptionDefinition(name = "transfer_mode", description = "Type of Character-Encoding Transfe", key = "transfer_mode", type = "SOSOptionTransferMode",
            mandatory = false)
    public SOSOptionTransferMode transfer_mode = new SOSOptionTransferMode(this, CLASSNAME + ".transfer_mode", "Type of Character-Encoding Transfe", 
            "ascii;binary;text", "binary", false);
    public SOSOptionTransferMode ftp_transfer_mode = (SOSOptionTransferMode) transfer_mode.SetAlias("ftp_transfer_mode");

    @Override
    public SOSOptionTransferMode gettransfer_mode() {
        return transfer_mode;
    }

    @Override
    public void settransfer_mode(final SOSOptionTransferMode p_transfer_mode) {
        transfer_mode = p_transfer_mode;
    }

    @JSOptionDefinition(name = "user", description = "UserID of user in charge User name", key = "user", type = "SOSOptionUserName", mandatory = true)
    public SOSOptionUserName user = new SOSOptionUserName(this, CLASSNAME + ".user", "UserID of user in charge User name", "", "anonymous", false);

    @Override
    public SOSOptionUserName getUser() {
        return user;
    }

    @JSOptionDefinition(name = "password", description = "Password for UserID Password for a", key = "password", type = "SOSOptionPassword", mandatory = false)
    public SOSOptionPassword password = new SOSOptionPassword(this, CLASSNAME + ".password", "Password for UserID Password for a", "", "", false);

    @Override
    public SOSOptionPassword getPassword() {
        return password;
    }

    @Override
    public void setPassword(final SOSOptionPassword p_password) {
        password = p_password;
    }

    public SOSConnection2OptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public SOSConnection2OptionsSuperClass(final JSListener pobjListener) {
        this();
    }

    public SOSConnection2OptionsSuperClass(final HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    }

    @Override
    public void setAllOptions(final HashMap<String, String> pobjJSSettings) {
        flgSetAllOptions = true;
        objSettings = pobjJSSettings;
        super.Settings(objSettings);
        super.setAllOptions(pobjJSSettings);
        flgSetAllOptions = false;
    }

    @Override
    public void CheckMandatory() throws JSExceptionMandatoryOptionMissing, Exception {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

    @Override
    public void CommandLineArgs(final String[] pstrArgs) {
        super.CommandLineArgs(pstrArgs);
        this.setAllOptions(super.objSettings);
    }

     @JSOptionDefinition(name = "ssh_auth_file", description = "This parameter specifies the path and name of a us", key = "ssh_auth_file",
            type = "SOSOptionInFileName", mandatory = false)
    public SOSOptionInFileName ssh_auth_file = new SOSOptionInFileName(this, CLASSNAME + ".ssh_auth_file", "This parameter specifies the path and name of a us",
            " ", " ", false);
    public SOSOptionInFileName auth_file = (SOSOptionInFileName) ssh_auth_file.SetAlias(CLASSNAME + ".auth_file");

    @Override
    public SOSOptionInFileName getAuth_file() {
        return ssh_auth_file;
    }

    @Override
    public void setAuth_file(final SOSOptionInFileName p_ssh_auth_file) {
        ssh_auth_file = p_ssh_auth_file;
    }

    @JSOptionDefinition(name = "ssh_auth_method", description = "This parameter specifies the authentication method", key = "ssh_auth_method",
            type = "SOSOptionStringValueList", mandatory = false)
    public SOSOptionAuthenticationMethod ssh_auth_method = new SOSOptionAuthenticationMethod(this, CLASSNAME + ".ssh_auth_method", 
            "This parameter specifies the authentication method", "publickey", "publickey", false);
    public SOSOptionAuthenticationMethod auth_method = (SOSOptionAuthenticationMethod) ssh_auth_method.SetAlias(CLASSNAME + ".auth_method");

    @Override
    public SOSOptionAuthenticationMethod getAuth_method() {
        return ssh_auth_method;
    }

    @Override
    public void setAuth_method(final SOSOptionAuthenticationMethod p_ssh_auth_method) {
        ssh_auth_method = p_ssh_auth_method;
    }

    @Override
    public void setUser(final SOSOptionUserName pobjUser) {
        user.Value(pobjUser.Value());
    }

    @JSOptionDefinition(name = "ftps_client_security", description = "FTPS Client Security", key = "ftps_client_secutity",
            type = "SOSOptionFTPSClientSecurity", mandatory = false)
    public SOSOptionFTPSClientSecurity ftps_client_security = new SOSOptionFTPSClientSecurity(this, CLASSNAME + ".ftps_client_security", "FTPS Client Security",
            SOSOptionFTPSClientSecurity.ClientSecurity.explicit.name(), SOSOptionFTPSClientSecurity.ClientSecurity.explicit.name(), false);

    public SOSOptionFTPSClientSecurity getftps_client_security() {
        return ftps_client_security;
    }

    public void setftps_client_security(SOSOptionFTPSClientSecurity val) {
        ftps_client_security = val;
    }

    @JSOptionDefinition(name = "proxy_protocol", description = "Proxy protocol", key = "proxy_protocol", type = "SOSOptionProxyProtocol", mandatory = false)
    public SOSOptionProxyProtocol proxy_protocol = new SOSOptionProxyProtocol(this, CLASSNAME + ".proxy_protocol", "Proxy protocol", 
            SOSOptionProxyProtocol.Protocol.http.name(), SOSOptionProxyProtocol.Protocol.http.name(), false);

    public SOSOptionProxyProtocol getproxy_protocol() {
        return proxy_protocol;
    }

    public void setproxy_host(SOSOptionProxyProtocol val) {
        proxy_protocol = val;
    }

    @JSOptionDefinition(name = "accept_untrusted_certificate", description = "Accept a valid certificat that could not be verified to be trusted",
            key = "accept_untrusted_certificate", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean accept_untrusted_certificate = new SOSOptionBoolean(this, CLASSNAME + ".accept_untrusted_certificate",
            "Accept a valid certificat that could not be verified to be trusted", "", "false", false);

    public SOSOptionBoolean getaccept_untrusted_certificate() {
        return accept_untrusted_certificate;
    }

    public void setaccept_untrusted_certificate(SOSOptionBoolean val) {
        accept_untrusted_certificate = val;
    }

    @JSOptionDefinition(name = "verify_certificate_hostname",
            description = "The certificate verification process will always verify the DNS name of the certificate presented by the server, "
                    + "with the hostname of the server in the URL used by the client.",
            key = "verify_certificate_hostname", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean verify_certificate_hostname = new SOSOptionBoolean(this, CLASSNAME + ".verify_certificate_hostname",
            "The certificate verification process will always verify the DNS name of the certificate presented by the server, "
            + "with the hostname of the server in the URL used by the client", "true", "true", false);

    public SOSOptionBoolean getverify_certificate_hostname() {
        return verify_certificate_hostname;
    }

    public void setverify_certificate_hostname(SOSOptionBoolean val) {
        verify_certificate_hostname = val;
    }

    @JSOptionDefinition(name = "proxy_host", description = "Host name or the IP address of a proxy", key = "proxy_host", type = "SOSOptionHostName",
            mandatory = false)
    public SOSOptionHostName proxy_host = new SOSOptionHostName(this, CLASSNAME + ".proxy_host", "The value of this parameter is the host name or th", "", "",
            false);

    public SOSOptionHostName getproxy_host() {
        return proxy_host;
    }

    public void setproxy_host(final SOSOptionHostName p_proxy_host) {
        proxy_host = p_proxy_host;
    }

    @JSOptionDefinition(name = "proxy_port", description = "Port-Number to be used for a proxy", key = "proxy_port", type = "SOSOptionPortNumber",
            mandatory = false)
    public SOSOptionPortNumber proxy_port = new SOSOptionPortNumber(this, CLASSNAME + ".proxy_port", "This parameter specifies the port of a proxy that", "",
            "", false);

    public SOSOptionPortNumber getproxy_port() {
        return proxy_port;
    }

    public void setproxy_port(final SOSOptionPortNumber p_proxy_port) {
        proxy_port = p_proxy_port;
    }

    @JSOptionDefinition(name = "proxy_user", description = "User name to be used for a proxy", key = "proxy_user", type = "SOSOptionUserName",
            mandatory = false)
    public SOSOptionUserName proxy_user = new SOSOptionUserName(this, CLASSNAME + ".proxy_user", "This parameter specifies the user name of a proxy that", "",
            "", false);

    public SOSOptionUserName getproxy_user() {
        return proxy_user;
    }

    public void setproxy_user(final SOSOptionUserName p_proxy_user) {
        proxy_user = p_proxy_user;
    }

    @JSOptionDefinition(name = "proxy_password", description = "Password to be used for a proxy", key = "proxy_password", type = "SOSOptionPassword",
            mandatory = false)
    public SOSOptionPassword proxy_password = new SOSOptionPassword(this, CLASSNAME + ".proxy_password", "This parameter specifies the password of a proxy that",
            "", "", false);

    public SOSOptionPassword getproxy_password() {
        return proxy_password;
    }

    public void setproxy_password(final SOSOptionPassword p_proxy_password) {
        proxy_password = p_proxy_password;
    }

    @JSOptionDefinition(name = "domain", description = "Domain", key = "domain", type = "SOSOptionString", mandatory = false)
    public SOSOptionString domain = new SOSOptionString(this, CLASSNAME + ".domain", "This parameter specifies the domain", "", "", false);

    @Override
    public SOSOptionString getdomain() {
        return domain;
    }

    @Override
    public void setdomain(final SOSOptionString p_domain) {
        domain = p_domain;
    }

    @JSOptionDefinition(name = "keystore_type", description = "keystore type", key = "keystore_type", type = "SOSOptionKeyStoreType", mandatory = false)
    public SOSOptionKeyStoreType keystore_type = new SOSOptionKeyStoreType(this, CLASSNAME + ".keystore_type", "This parameter specifies the keystore type",
            SOSOptionKeyStoreType.Type.JKS.name(), SOSOptionKeyStoreType.Type.JKS.name(), false);

    public SOSOptionKeyStoreType getkeystore_type() {
        return keystore_type;
    }

    public void setkeystore_type(final SOSOptionKeyStoreType val) {
        keystore_type = val;
    }

    @JSOptionDefinition(name = "keystore_file", description = "keystore file", key = "keystore_file", type = "SOSOptionString", mandatory = false)
    public SOSOptionString keystore_file = new SOSOptionString(this, CLASSNAME + ".keystore_file", "This parameter specifies the keystore file path", "", "",
            false);

    public SOSOptionString getkeystore_file() {
        return keystore_file;
    }

    public void setkeystore_file(final SOSOptionString val) {
        keystore_file = val;
    }

    @JSOptionDefinition(name = "keystore_password", description = "Password to be used for a keystore", key = "keystore_password", type = "SOSOptionPassword",
            mandatory = false)
    public SOSOptionPassword keystore_password = new SOSOptionPassword(this, CLASSNAME + ".keystore_password", "This parameter specifies the password of a keystore",
            "", "", false);

    public SOSOptionPassword getkeystore_password() {
        return keystore_password;
    }

    public void setkeystore_password(final SOSOptionPassword val) {
        keystore_password = val;
    }

    @JSOptionDefinition(name = "raise_exception_on_error", description = "Raise an Exception if an error occured", key = "raise_exception_on_error",
            type = "SOSOptionBoolean", mandatory = true)
    public SOSOptionBoolean raise_exception_on_error = new SOSOptionBoolean(this, CLASSNAME + ".raise_exception_on_error", "Raise an Exception if an error occured",
            "true", "true", true);

    public SOSOptionBoolean getraise_exception_on_error() {
        return raise_exception_on_error;
    }

    public void setraise_exception_on_error(final SOSOptionBoolean raiseExceptionOnError) {
        this.raise_exception_on_error = raiseExceptionOnError;
    }

    @JSOptionDefinition(name = "ignore_error", description = "Should the value true be specified, then execution errors", key = "ignore_error",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionBoolean ignore_error = new SOSOptionBoolean(this, CLASSNAME + ".ignore_error", "Should the value true be specified, then execution errors",
            "false", "false", false);

    public SOSOptionBoolean getIgnore_error() {
        return ignore_error;
    }

    public void setIgnore_error(final SOSOptionBoolean ignoreError) {
        this.ignore_error = ignoreError;
    }

}