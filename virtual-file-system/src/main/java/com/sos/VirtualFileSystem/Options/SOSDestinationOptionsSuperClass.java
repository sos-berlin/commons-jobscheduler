package com.sos.VirtualFileSystem.Options;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
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

@JSOptionClass(name = "SOSDestinationOptionsSuperClass", description = "SOSDestinationOptionsSuperClass")
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSDestinationOptionsSuperClass extends JSOptionsClass implements ISOSAuthenticationOptions, ISOSDataProviderOptions {

    private static final long serialVersionUID = 1997338600688654140L;
    private static final String CLASSNAME = SOSDestinationOptionsSuperClass.class.getSimpleName();

    @JSOptionDefinition(name = "url", description = "the url for the connection", key = "url", type = "SOSOptionURL", mandatory = false)
    public SOSOptionUrl url = new SOSOptionUrl(this, CLASSNAME + ".url", "the url for the connection", "", "", false);

    @Override
    public SOSOptionUrl getUrl() {
        return url;
    }

    @Override
    public ISOSDataProviderOptions setUrl(final SOSOptionUrl val) {
        return this;
    }

    @JSOptionDefinition(name = "include", description = "the include directive as an option", key = "include", type = "SOSOptionString", mandatory = false)
    public SOSOptionString include = new SOSOptionString(this, CLASSNAME + ".include", "the include directive as an option", "", "", false);

    @Override
    public String getInclude() {
        return include.getValue();
    }

    @Override
    public ISOSDataProviderOptions setInclude(final String val) {
        include.setValue(val);
        return this;
    }

    @JSOptionDefinition(name = "use_zlib_compression", description = "Use the zlib cmpression on sftp", key = "use_zlib_compression", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean useZlibCompression = new SOSOptionBoolean(this, CLASSNAME + ".use_zlib_compression", "Use the zlib cmpression on sftp",
            "false", "false", false);

    @Override
    public SOSOptionBoolean getUseZlibCompression() {
        return useZlibCompression;
    }

    @Override
    public ISOSDataProviderOptions setUseZlibCompression(final SOSOptionBoolean val) {
        useZlibCompression = val;
        return this;
    }

    @JSOptionDefinition(name = "zlib_compression_level", description = "the compression level to use", key = "zlib_compression_level", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger zlibCompressionLevel = new SOSOptionInteger(this, CLASSNAME + ".zlib_compression_level", "the compression level to use",
            "1", "1", false);

    @Override
    public SOSOptionInteger getZlibCompressionLevel() {
        return zlibCompressionLevel;
    }

    @Override
    public ISOSDataProviderOptions setZlibCompressionLevel(final SOSOptionInteger val) {
        zlibCompressionLevel = val;
        return this;
    }

    @JSOptionDefinition(name = "ProtocolCommandListener", description = "Activate the logging for Apache ftp client", key = "Protocol_Command_Listener", type = "SOSOptionBoolean", mandatory = true)
    public SOSOptionBoolean protocolCommandListener = new SOSOptionBoolean(this, CLASSNAME + ".Protocol_Command_Listener",
            "Activate the logging for Apache ftp client", "false", "false", true);

    @Override
    public String getProtocolCommandListener() {
        return protocolCommandListener.getValue();
    }

    @Override
    public ISOSDataProviderOptions setProtocolCommandListener(final String val) {
        protocolCommandListener.setValue(val);
        return this;
    }

    @JSOptionDefinition(name = "account", description = "Optional account info for authentication with an", key = "account", type = "SOSOptionString", mandatory = false)
    public SOSOptionString account = new SOSOptionString(this, CLASSNAME + ".account", "Optional account info for authentication with an", "", "",
            false);

    @Override
    public SOSOptionString getAccount() {
        return account;
    }

    @Override
    public void setAccount(final SOSOptionString pAccount) {
        account = pAccount;
    }

    @JSOptionDefinition(name = "make_Dirs", description = "Create missing Directory on Target", key = "make_Dirs", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean makeDirs = new SOSOptionBoolean(this, CLASSNAME + ".make_Dirs", "Create missing Directory on Source/Target", "true",
            "true", false);
    public SOSOptionBoolean createFoldersOnTarget = (SOSOptionBoolean) makeDirs.setAlias("create_folders_on_target");
    public SOSOptionBoolean createFolders = (SOSOptionBoolean) makeDirs.setAlias("create_folders");

    @Override
    public String getMakeDirs() {
        return makeDirs.getValue();
    }

    @Override
    public ISOSDataProviderOptions setMakeDirs(final String val) {
        makeDirs.setValue(val);
        return this;
    }

    @JSOptionDefinition(name = "dir", description = "Optional account info for authentication with an", key = "dir", type = "SOSOptionFolderName", mandatory = false)
    public SOSOptionFolderName directory = new SOSOptionFolderName(this, CLASSNAME + ".dir", "local_dir Local directory into which or from which", "",
            "", false);
    public SOSOptionFolderName folderName = (SOSOptionFolderName) directory.setAlias("Folder_Name");

    @JSOptionDefinition(name = "platform", description = "platform on which the app is running", key = "platform", type = "SOSOptionString", mandatory = false)
    public SOSOptionPlatform platform = new SOSOptionPlatform(this, CLASSNAME + ".platform", "platform on which the app is running", "", "", false);

    @Override
    public String getPlatform() {
        return platform.getValue();
    }

    @Override
    public ISOSDataProviderOptions setPlatform(final String val) {
        platform.setValue(val);
        return this;
    }

    @JSOptionDefinition(name = "replacement", description = "String for replacement of matching character seque", key = "replacement", type = "SOSOptionString", mandatory = false)
    public SOSOptionString replacement = new SOSOptionString(this, CLASSNAME + ".replacement", "String for replacement of matching character seque",
            null, null, false);

    @Override
    public SOSOptionString getReplacement() {
        return replacement;
    }

    @Override
    public void setReplacement(final SOSOptionString val) {
        replacement = val;
    }

    public SOSOptionString replaceWith = (SOSOptionString) replacement.setAlias(CLASSNAME + ".ReplaceWith");

    @JSOptionDefinition(name = "replacing", description = "Regular expression for filename replacement with", key = "replacing", type = "SOSOptionRegExp", mandatory = false)
    public SOSOptionRegExp replacing = new SOSOptionRegExp(this, CLASSNAME + ".replacing", "Regular expression for filename replacement with", "", "",
            false);

    @Override
    public SOSOptionRegExp getReplacing() {
        return replacing;
    }

    @Override
    public void setReplacing(final SOSOptionRegExp val) {
        replacing = val;
    }

    public SOSOptionRegExp replaceWhat = (SOSOptionRegExp) replacing.setAlias(CLASSNAME + ".ReplaceWhat");

    @JSOptionDefinition(name = "strict_hostKey_checking", description = "Check the hostkey against known hosts for SSH", key = "strict_hostKey_checking", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean strictHostKeyChecking = new SOSOptionBoolean(this, CLASSNAME + ".strict_hostkey_checking",
            "Check the hostkey against known hosts for SSH", "false", "false", false);

    @Override
    public SOSOptionBoolean getStrictHostKeyChecking() {
        return strictHostKeyChecking;
    }

    @Override
    public void setStrictHostKeyChecking(final String val) {
        strictHostKeyChecking.setValue(val);
    }

    @JSOptionDefinition(name = "TFN_Post_Command", description = "Post commands executed after creating the final TargetFile", key = "TFN_Post_Command", type = "SOSOptionString", mandatory = false)
    public SOSOptionString tfnPostCommand = new SOSOptionString(this, CLASSNAME + ".TFN_Post_Command",
            "Post commands executed after creating the final TargetFileName", "", "", false);

    @Override
    public SOSOptionString getTfnPostCommand() {
        return tfnPostCommand;
    }

    @Override
    public ISOSDataProviderOptions setTfnPostCommand(final SOSOptionString val) {
        tfnPostCommand = val;
        return this;
    }

    @JSOptionDefinition(name = "Post_Command", description = "FTP-Command to be executed after transfer", key = "Post_Command", type = "SOSOptionString", mandatory = false)
    public SOSOptionString postCommand = new SOSOptionString(this, CLASSNAME + ".Post_Command", "FTP-Command to be executed after transfer", "", "",
            false);

    @Override
    public String getPostCommand() {
        return postCommand.getValue();
    }

    @Override
    public ISOSDataProviderOptions setPostCommand(final String val) {
        postCommand.setValue(val);
        return this;
    }

    @JSOptionDefinition(name = "post_command_disable_for_skipped_transfer", description = "Disable Command to be execute after transfer", key = "post_command_disable_for_skipped_transfer", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean post_command_disable_for_skipped_transfer = new SOSOptionBoolean(this, CLASSNAME
            + ".post_command_disable_for_skipped_transfer", "", "false", "false", false);

    @JSOptionDefinition(name = "Pre_Command", description = "FTP-Command to be execute before transfer", key = "Pre_Command", type = "SOSOptionString  ", mandatory = false)
    public SOSOptionString preCommand = new SOSOptionString(this, CLASSNAME + ".Pre_Command", "", "", "", false);

    @Override
    public String getPreCommand() {
        return preCommand.getValue();
    }

    @Override
    public ISOSDataProviderOptions setPreCommand(final String val) {
        preCommand.setValue(val);
        return this;
    }

    @JSOptionDefinition(name = "pre_command_enable_for_skipped_transfer", description = "Enable Command to be execute before transfer", key = "pre_command_enable_for_skipped_transfer", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean pre_command_enable_for_skipped_transfer = new SOSOptionBoolean(this, CLASSNAME
            + ".pre_command_enable_for_skipped_transfer", "", "false", "false", false);

    @JSOptionDefinition(name = "FtpS_protocol", description = "Type of FTPS-Protocol, e.g. SSL, TLS", key = "FtpS_protocol", type = "SOSOptionString", mandatory = true)
    public SOSOptionString ftpsProtocol = new SOSOptionString(this, CLASSNAME + ".FtpS_protocol", "Type of FTPS-Protocol, e.g. SSL, TLS", "SSL",
            "SSL", true);

    @Override
    public String getFtpsProtocol() {
        return ftpsProtocol.getValue();
    }

    @Override
    public ISOSDataProviderOptions setFtpsProtocol(final String val) {
        ftpsProtocol.setValue(val);
        return this;
    }

    @JSOptionDefinition(name = "loadClassName", description = "Java Class which implements the ISOSVFSHandlerInterface", key = "load_Class_Name", type = "SOSOptionString ", mandatory = false)
    public SOSOptionJavaClassName loadClassName = new SOSOptionJavaClassName(this, CLASSNAME + ".load_Class_Name",
            "Java Class which implements the ISOSVFSHandlerInterface", "", "", false);
    public SOSOptionJavaClassName dataProviderClassName = (SOSOptionJavaClassName) loadClassName.setAlias("Data_provider_class_name");

    @Override
    public String getLoadClassName() {
        return loadClassName.getValue();
    }

    @Override
    public ISOSDataProviderOptions setLoadClassName(final String val) {
        loadClassName.setValue(val);
        return this;
    }

    @JSOptionDefinition(name = "javaClassPath", description = "", key = "javaClassPath", type = "SOSOptionString", mandatory = false)
    public SOSOptionString javaClassPath = new SOSOptionString(this, CLASSNAME + ".javaClassPath", "", "", "", false);

    @Override
    public SOSOptionString getJavaClassPath() {
        return javaClassPath;
    }

    @Override
    public void setJavaClassPath(final SOSOptionString val) {
        javaClassPath = val;
    }

    @JSOptionDefinition(name = "host", description = "Host-Name This parameter specifies th", key = "host", type = "SOSOptionHostName", mandatory = false)
    public SOSOptionHostName host = new SOSOptionHostName(this, CLASSNAME + ".host", "Host-Name This parameter specifies th", "", "", false);

    @Override
    public SOSOptionHostName getHost() {
        return host;
    }

    @Override
    public void setHost(final SOSOptionHostName val) {
        host = val;
    }

    public SOSOptionHostName hostName = (SOSOptionHostName) host.setAlias(CLASSNAME + ".HostName");
    public SOSOptionHostName ftpHostName = (SOSOptionHostName) host.setAlias(CLASSNAME + ".ftp_host");

    @JSOptionDefinition(name = "passive_mode", description = "passive_mode Passive mode for FTP is often used wit", key = "passive_mode", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean passiveMode = new SOSOptionBoolean(this, CLASSNAME + ".passive_mode",
            "passive_mode Passive mode for FTP is often used wit", "false", "false", false);

    @Override
    public SOSOptionBoolean getPassiveMode() {
        return passiveMode;
    }

    @Override
    public void setPassiveMode(final SOSOptionBoolean val) {
        passiveMode = val;
    }

    public SOSOptionBoolean ftpTransferModeIsPassive = (SOSOptionBoolean) passiveMode.setAlias(CLASSNAME + ".FTPTransferModeIsPassive");
    public SOSOptionBoolean ftpPassiveMode = (SOSOptionBoolean) passiveMode.setAlias(CLASSNAME + ".ftp_passive_mode");

    @JSOptionDefinition(name = "port", description = "Port-Number to be used for Data-Transfer", key = "port", type = "SOSOptionPortNumber", mandatory = true)
    public SOSOptionPortNumber port = new SOSOptionPortNumber(this, CLASSNAME + ".port", "Port-Number to be used for Data-Transfer", "21", "21",
            true);
    public SOSOptionPortNumber ftpPort = (SOSOptionPortNumber) port.setAlias("ftp_port");

    @Override
    public SOSOptionPortNumber getPort() {
        return port;
    }

    @Override
    public void setPort(final SOSOptionPortNumber val) {
        port = val;
    }

    @JSOptionDefinition(name = "protocol", description = "Type of requested Datatransfer The values ftp, sftp", key = "protocol", type = "SOSOptionStringValueList", mandatory = true)
    public SOSOptionTransferType protocol = new SOSOptionTransferType(this, CLASSNAME + ".protocol",
            "Type of requested Datatransfer The values ftp, sftp", "", "ftp", true);
    public SOSOptionTransferType ftpProtocol = (SOSOptionTransferType) protocol.setAlias("ftp_protocol");

    @Override
    public SOSOptionTransferType getProtocol() {
        return protocol;
    }

    @Override
    public void setProtocol(final SOSOptionTransferType val) {
        protocol = val;
    }

    public SOSOptionTransferType transferProtocol = (SOSOptionTransferType) protocol.setAlias(CLASSNAME + ".TransferProtocol");

    @JSOptionDefinition(name = "transfer_mode", description = "Type of Character-Encoding Transfe", key = "transfer_mode", type = "SOSOptionTransferMode", mandatory = false)
    public SOSOptionTransferMode transferMode = new SOSOptionTransferMode(this, CLASSNAME + ".transfer_mode", "Type of Character-Encoding Transfe",
            "ascii;binary;text", "binary", false);
    public SOSOptionTransferMode ftpTransferMode = (SOSOptionTransferMode) transferMode.setAlias("ftp_transfer_mode");

    @Override
    public SOSOptionTransferMode getTransferMode() {
        return transferMode;
    }

    @Override
    public void setTransferMode(final SOSOptionTransferMode val) {
        transferMode = val;
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
    public void setPassword(final SOSOptionPassword val) {
        password = val;
    }

    @JSOptionDefinition(name = "passphrase", description = "Passphrase", key = "passphrase", type = "SOSOptionPassword", mandatory = false)
    public SOSOptionPassword passphrase = new SOSOptionPassword(this, CLASSNAME + ".passphrase", "Passphrase", "", "", false);

    @Override
    public SOSOptionPassword getPassphrase() {
        return passphrase;
    }

    @Override
    public void setPassphrase(final SOSOptionPassword val) {
        passphrase = val;
    }

    public SOSDestinationOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public SOSDestinationOptionsSuperClass(final HashMap<String, String> settings) throws Exception {
        this();
        this.setSettings(settings);
    }

    @Override
    public void setAllOptions(final HashMap<String, String> settings) {
        // objSettings = settings;
        // super.setAllOptions(settings);
        // super.setSettings(objSettings);
        super.setSettings(settings);
    }

    @Override
    public void checkMandatory() throws JSExceptionMandatoryOptionMissing, Exception {
        try {
            super.checkMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

    @Override
    public void commandLineArgs(final String[] args) {
        super.commandLineArgs(args);
        this.setAllOptions(super.objSettings);
    }

    @JSOptionDefinition(name = "ssh_auth_file", description = "This parameter specifies the path and name of a us", key = "ssh_auth_file", type = "SOSOptionInFileName", mandatory = false)
    public SOSOptionInFileName sshAuthFile = new SOSOptionInFileName(this, CLASSNAME + ".ssh_auth_file",
            "This parameter specifies the path and name of a us", " ", " ", false);
    public SOSOptionInFileName authFile = (SOSOptionInFileName) sshAuthFile.setAlias(CLASSNAME + ".auth_file");

    @Override
    public SOSOptionInFileName getAuthFile() {
        return sshAuthFile;
    }

    @Override
    public void setAuthFile(final SOSOptionInFileName val) {
        sshAuthFile = val;
    }

    @JSOptionDefinition(name = "ssh_auth_method", description = "This parameter specifies the authentication method", key = "ssh_auth_method", type = "SOSOptionStringValueList", mandatory = false)
    public SOSOptionAuthenticationMethod sshAuthMethod = new SOSOptionAuthenticationMethod(this, CLASSNAME + ".ssh_auth_method",
            "This parameter specifies the authentication method", "publickey", "publickey", false);
    public SOSOptionAuthenticationMethod authMethod = (SOSOptionAuthenticationMethod) sshAuthMethod.setAlias(CLASSNAME + ".auth_method");

    @Override
    public SOSOptionAuthenticationMethod getAuthMethod() {
        return sshAuthMethod;
    }

    @Override
    public void setAuthMethod(final SOSOptionAuthenticationMethod val) {
        sshAuthMethod = val;
    }

    @JSOptionDefinition(name = "preferred_authentications", description = "This parameter specifies preferred authentication methods,e.g password,publickey,...", key = "preferred_authentications", type = "SOSOptionString", mandatory = false)
    public SOSOptionString preferred_authentications = new SOSOptionString(this, CLASSNAME + ".preferred_authentications",
            "This parameter specifies the preferred authentication methods", "", "", false);

    @JSOptionDefinition(name = "required_authentications", description = "This parameter specifies the required authentication methods,e.g password,publickey,...", key = "required_authentications", type = "SOSOptionString", mandatory = false)
    public SOSOptionString required_authentications = new SOSOptionString(this, CLASSNAME + ".required_authentications",
            "This parameter specifies the required authentication methods", "", "", false);

    @Override
    public void setUser(final SOSOptionUserName val) {
        user.setValue(val.getValue());
    }

    @JSOptionDefinition(name = "ftps_client_security", description = "FTPS Client Security", key = "ftps_client_secutity", type = "SOSOptionFTPSClientSecurity", mandatory = false)
    public SOSOptionFTPSClientSecurity ftpsClientSecurity = new SOSOptionFTPSClientSecurity(this, CLASSNAME + ".ftps_client_security",
            "FTPS Client Security", SOSOptionFTPSClientSecurity.ClientSecurity.explicit.name(), SOSOptionFTPSClientSecurity.ClientSecurity.explicit
                    .name(), false);

    public SOSOptionFTPSClientSecurity getFtpsClientSecurity() {
        return ftpsClientSecurity;
    }

    public void setFtpsClientSecurity(SOSOptionFTPSClientSecurity val) {
        ftpsClientSecurity = val;
    }

    @JSOptionDefinition(name = "proxy_protocol", description = "Proxy protocol", key = "proxy_protocol", type = "SOSOptionProxyProtocol", mandatory = false)
    public SOSOptionProxyProtocol proxyProtocol = new SOSOptionProxyProtocol(this, CLASSNAME + ".proxy_protocol", "Proxy protocol",
            SOSOptionProxyProtocol.Protocol.http.name(), SOSOptionProxyProtocol.Protocol.http.name(), false);

    public SOSOptionProxyProtocol getProxyProtocol() {
        return proxyProtocol;
    }

    public void setProxyHost(SOSOptionProxyProtocol val) {
        proxyProtocol = val;
    }

    @JSOptionDefinition(name = "accept_untrusted_certificate", description = "Accept a valid certificat that could not be verified to be trusted", key = "accept_untrusted_certificate", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean acceptUntrustedCertificate = new SOSOptionBoolean(this, CLASSNAME + ".accept_untrusted_certificate",
            "Accept a valid certificat that could not be verified to be trusted", "", "false", false);

    public SOSOptionBoolean getAcceptUntrustedCertificate() {
        return acceptUntrustedCertificate;
    }

    public void setAcceptUntrustedCertificate(SOSOptionBoolean val) {
        acceptUntrustedCertificate = val;
    }

    @JSOptionDefinition(name = "verify_certificate_hostname", description = "The certificate verification process will always verify the DNS name of the certificate presented by the server, "
            + "with the hostname of the server in the URL used by the client.", key = "verify_certificate_hostname", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean verifyCertificateHostname = new SOSOptionBoolean(this, CLASSNAME + ".verify_certificate_hostname",
            "The certificate verification process will always verify the DNS name of the certificate presented by the server, "
                    + "with the hostname of the server in the URL used by the client", "true", "true", false);

    public SOSOptionBoolean getVerifyCertificateHostname() {
        return verifyCertificateHostname;
    }

    public void setVerifyCertificateHostname(SOSOptionBoolean val) {
        verifyCertificateHostname = val;
    }

    @JSOptionDefinition(name = "proxy_host", description = "Host name or the IP address of a proxy", key = "proxy_host", type = "SOSOptionHostName", mandatory = false)
    public SOSOptionHostName proxyHost = new SOSOptionHostName(this, CLASSNAME + ".proxy_host", "The value of this parameter is the host name or th",
            "", "", false);

    public SOSOptionHostName getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(final SOSOptionHostName val) {
        proxyHost = val;
    }

    @JSOptionDefinition(name = "proxy_port", description = "Port-Number to be used for a proxy", key = "proxy_port", type = "SOSOptionPortNumber", mandatory = false)
    public SOSOptionPortNumber proxyPort = new SOSOptionPortNumber(this, CLASSNAME + ".proxy_port",
            "This parameter specifies the port of a proxy that", "", "", false);

    public SOSOptionPortNumber getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(final SOSOptionPortNumber pProxyPort) {
        proxyPort = pProxyPort;
    }

    @JSOptionDefinition(name = "proxy_user", description = "User name to be used for a proxy", key = "proxy_user", type = "SOSOptionUserName", mandatory = false)
    public SOSOptionUserName proxyUser = new SOSOptionUserName(this, CLASSNAME + ".proxy_user",
            "This parameter specifies the user name of a proxy that", "", "", false);

    public SOSOptionUserName getProxyUser() {
        return proxyUser;
    }

    public void setProxyUser(final SOSOptionUserName val) {
        proxyUser = val;
    }

    @JSOptionDefinition(name = "proxy_password", description = "Password to be used for a proxy", key = "proxy_password", type = "SOSOptionPassword", mandatory = false)
    public SOSOptionPassword proxyPassword = new SOSOptionPassword(this, CLASSNAME + ".proxy_password",
            "This parameter specifies the password of a proxy that", "", "", false);

    public SOSOptionPassword getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(final SOSOptionPassword val) {
        proxyPassword = val;
    }

    @JSOptionDefinition(name = "domain", description = "Domain", key = "domain", type = "SOSOptionString", mandatory = false)
    public SOSOptionString domain = new SOSOptionString(this, CLASSNAME + ".domain", "This parameter specifies the domain", "", "", false);

    @Override
    public SOSOptionString getDomain() {
        return domain;
    }

    @Override
    public void setDomain(final SOSOptionString val) {
        domain = val;
    }

    @JSOptionDefinition(name = "keystore_type", description = "keystore type", key = "keystore_type", type = "SOSOptionKeyStoreType", mandatory = false)
    public SOSOptionKeyStoreType keystoreType = new SOSOptionKeyStoreType(this, CLASSNAME + ".keystore_type",
            "This parameter specifies the keystore type", SOSOptionKeyStoreType.Type.JKS.name(), SOSOptionKeyStoreType.Type.JKS.name(), false);

    public SOSOptionKeyStoreType getKeystoreType() {
        return keystoreType;
    }

    public void setKeystoreType(final SOSOptionKeyStoreType val) {
        keystoreType = val;
    }

    @JSOptionDefinition(name = "keystore_file", description = "keystore file", key = "keystore_file", type = "SOSOptionString", mandatory = false)
    public SOSOptionString keystoreFile = new SOSOptionString(this, CLASSNAME + ".keystore_file", "This parameter specifies the keystore file path",
            "", "", false);

    public SOSOptionString getKeystoreFile() {
        return keystoreFile;
    }

    public void setKeystoreFile(final SOSOptionString val) {
        keystoreFile = val;
    }

    @JSOptionDefinition(name = "keystore_password", description = "Password to be used for a keystore", key = "keystore_password", type = "SOSOptionPassword", mandatory = false)
    public SOSOptionPassword keystorePassword = new SOSOptionPassword(this, CLASSNAME + ".keystore_password",
            "This parameter specifies the password of a keystore", "", "", false);

    public SOSOptionPassword getKeystorePassword() {
        return keystorePassword;
    }

    public void setKeystorePassword(final SOSOptionPassword val) {
        keystorePassword = val;
    }

    @JSOptionDefinition(name = "raise_exception_on_error", description = "Raise an Exception if an error occured", key = "raise_exception_on_error", type = "SOSOptionBoolean", mandatory = true)
    public SOSOptionBoolean raiseExceptionOnError = new SOSOptionBoolean(this, CLASSNAME + ".raise_exception_on_error",
            "Raise an Exception if an error occured", "true", "true", true);

    public SOSOptionBoolean getRaiseExceptionOnError() {
        return raiseExceptionOnError;
    }

    public void setRaiseExceptionOnError(final SOSOptionBoolean val) {
        raiseExceptionOnError = val;
    }

    @JSOptionDefinition(name = "ignore_error", description = "Should the value true be specified, then execution errors", key = "ignore_error", type = "SOSOptionString", mandatory = false)
    public SOSOptionBoolean ignoreError = new SOSOptionBoolean(this, CLASSNAME + ".ignore_error",
            "Should the value true be specified, then execution errors", "false", "false", false);

    public SOSOptionBoolean getIgnoreError() {
        return ignoreError;
    }

    public void setIgnoreError(final SOSOptionBoolean val) {
        ignoreError = val;
    }

    @JSOptionDefinition(name = "configuration_files", description = "List of the app configuration files separated by semicolon", key = "configuration_files", type = "SOSOptionString", mandatory = false)
    public SOSOptionString configuration_files = new SOSOptionString(this, CLASSNAME + ".configuration_files",
            "List of the app configuration files separated by semicolon", "", "", false);

    @JSOptionDefinition(name = "server_alive_interval", description = "Sets the interval to send a keep-alive message. can contains not integer value", key = "server_alive_interval", type = "SOSOptionString", mandatory = false)
    public SOSOptionString server_alive_interval = new SOSOptionString(this, CLASSNAME + ".server_alive_interval",
            "Sets the interval to send a keep-alive message", "", "", false);

    @JSOptionDefinition(name = "server_alive_count_max", description = "Sets the number of keep-alive messages which may be sent without receiving any messages back from the server.", key = "server_alive_count_max", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger server_alive_count_max = new SOSOptionInteger(this, CLASSNAME + ".server_alive_count_max",
            "Sets the number of keep-alive messages which may be sent without receiving any messages back from the server.", "", "", false);

    @JSOptionDefinition(name = "use_keyagent", description = "Using a keyagent to get the privat key file", key = "use_keyagent", type = "SOSOptionBoolean", mandatory = true)
    public SOSOptionBoolean useKeyAgent = new SOSOptionBoolean(this, CLASSNAME + ".use_keyagent", "Using a keyagent to get the privat key file",
            "false", "false", false);

    public SOSOptionBoolean isUseKeyAgent() {
        return useKeyAgent;
    }

    public void setUseKeyAgent(final SOSOptionBoolean val) {
        useKeyAgent = val;
    }

    @JSOptionDefinition(name = "session_connect_timeout", description = "Sets the interval for session connect", key = "session_connect_timeout", type = "SOSOptionString", mandatory = false)
    public SOSOptionString session_connect_timeout = new SOSOptionString(this, CLASSNAME + ".session_connect_timeout",
            "Sets the interval for session connect", "", "", false);

    @JSOptionDefinition(name = "channel_connect_timeout", description = "Sets the interval for channel connect", key = "channel_connect_timeout", type = "SOSOptionString", mandatory = false)
    public SOSOptionString channel_connect_timeout = new SOSOptionString(this, CLASSNAME + ".channel_connect_timeout",
            "Sets the interval for cannel connect", "", "", false);

}