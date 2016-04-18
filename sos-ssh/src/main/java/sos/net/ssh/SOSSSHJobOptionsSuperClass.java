package sos.net.ssh;

import java.util.HashMap;

import com.sos.CredentialStore.SOSCredentialStoreImpl;
import com.sos.CredentialStore.Options.ISOSCredentialStoreOptionsBridge;
import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionAuthenticationMethod;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionCommandString;
import com.sos.JSHelper.Options.SOSOptionHostName;
import com.sos.JSHelper.Options.SOSOptionInFileName;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionIntegerArray;
import com.sos.JSHelper.Options.SOSOptionPassword;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.JSHelper.Options.SOSOptionProxyProtocol;
import com.sos.JSHelper.Options.SOSOptionRegExp;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.JSHelper.Options.SOSOptionStringValueList;
import com.sos.JSHelper.Options.SOSOptionTransferType;
import com.sos.JSHelper.Options.SOSOptionUrl;
import com.sos.JSHelper.Options.SOSOptionUserName;
import com.sos.JSHelper.interfaces.ISOSConnectionOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSShellOptions;
import com.sos.i18n.annotation.I18NResourceBundle;

/** OptionsTable Table of existing Options
 * <TABLE border="1">
 * <CAPTION>Tabelle mit allen Optionen</CAPTION>
 * <TR style="bold">
 * <TD><b>MethodName</b></TD>
 * <TD><b>Title</b></TD>
 * <TD><b>Setting</b></TD>
 * <TD><b>Description</b></TD>
 * <TD><b>IsMandatory</b></TD>
 * <TD><b>DataType</b></TD>
 * <TD align="center"><b>InitialValue</b></TD>
 * <TD align="center"><b>TestValue</b></TD>
 * </TR>
 * <TR>
 * <TD>::auth_file</TD>
 * <TD>This parameter specifies the path and name of a user's pr</TD>
 * <TD>Auth_file</TD>
 * <TD>This parameter specifies the path and name of a user's pr</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">n.a.</TD>
 * <TD align="center">n.a.</TD>
 * </TR>
 * <TR>
 * <TD>::auth_method</TD>
 * <TD>This parameter specifies the authorization method for the</TD>
 * <TD>Auth_method</TD>
 * <TD>This parameter specifies the authorization method for the</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">publickey</TD>
 * <TD align="center">publickey</TD>
 * </TR>
 * <TR>
 * <TD>::command</TD>
 * <TD>This parameter specifies a command that is to be executed</TD>
 * <TD>Command</TD>
 * <TD>This parameter specifies a command that is to be executed</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">n.a.</TD>
 * <TD align="center">n.a.</TD>
 * </TR>
 * <TR>
 * <TD>::command_delimiter</TD>
 * <TD>Command delimiter characters are specified using this par</TD>
 * <TD>Command_delimiter</TD>
 * <TD>Command delimiter characters are specified using this par</TD>
 * <TD>true</TD>
 * <TD>String</TD>
 * <TD align="center">%%</TD>
 * <TD align="center">%%</TD>
 * </TR>
 * <TR>
 * <TD>::command_script</TD>
 * <TD>This parameter can be used as an alternative to command,</TD>
 * <TD>Command_script</TD>
 * <TD>This parameter can be used as an alternative to command,</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">n.a.</TD>
 * <TD align="center">n.a.</TD>
 * </TR>
 * <TR>
 * <TD>::command_script_file</TD>
 * <TD>This parameter can be used as an alternative to command,</TD>
 * <TD>Command_script_file</TD>
 * <TD>This parameter can be used as an alternative to command,</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">n.a.</TD>
 * <TD align="center">n.a.</TD>
 * </TR>
 * <TR>
 * <TD>::command_script_param</TD>
 * <TD>This parameter contains a parameterstring, which will be</TD>
 * <TD>Command_script_param</TD>
 * <TD>This parameter contains a parameterstring, which will be</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">n.a.</TD>
 * <TD align="center">n.a.</TD>
 * </TR>
 * <TR>
 * <TD>::host</TD>
 * <TD>This parameter specifies the hostname or IP address of th</TD>
 * <TD>Host</TD>
 * <TD>This parameter specifies the hostname or IP address of th</TD>
 * <TD>true</TD>
 * <TD>String</TD>
 * <TD align="center">n.a.</TD>
 * <TD align="center">n.a.</TD>
 * </TR>
 * <TR>
 * <TD>::ignore_error</TD>
 * <TD>Should the value true be specified, then execution errors</TD>
 * <TD>Ignore_error</TD>
 * <TD>Should the value true be specified, then execution errors</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">false</TD>
 * <TD align="center">false</TD>
 * </TR>
 * <TR>
 * <TD>::ignore_exit_code</TD>
 * <TD>This parameter configures one or more exit codes which wi</TD>
 * <TD>Ignore_exit_code</TD>
 * <TD>This parameter configures one or more exit codes which wi</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">n.a.</TD>
 * <TD align="center">n.a.</TD>
 * </TR>
 * <TR>
 * <TD>::ignore_signal</TD>
 * <TD>Should the value true be specified, then on</TD>
 * <TD>Ignore_signal</TD>
 * <TD>Should the value true be specified, then on</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">false</TD>
 * <TD align="center">false</TD>
 * </TR>
 * <TR>
 * <TD>::ignore_stderr</TD>
 * <TD>This job checks if any output to stderr has been created</TD>
 * <TD>Ignore_stderr</TD>
 * <TD>This job checks if any output to stderr has been created</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">false</TD>
 * <TD align="center">false</TD>
 * </TR>
 * <TR>
 * <TD>::password</TD>
 * <TD>This parameter specifies the user account password for au</TD>
 * <TD>Password</TD>
 * <TD>This parameter specifies the user account password for au</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">n.a.</TD>
 * <TD align="center">n.a.</TD>
 * </TR>
 * <TR>
 * <TD>::port</TD>
 * <TD>This parameter specifies the port number of the SSH serve</TD>
 * <TD>Port</TD>
 * <TD>This parameter specifies the port number of the SSH serve</TD>
 * <TD>true</TD>
 * <TD>String</TD>
 * <TD align="center">22</TD>
 * <TD align="center">22</TD>
 * </TR>
 * <TR>
 * <TD>::proxy_host</TD>
 * <TD>The value of this parameter is the host name or the IP ad</TD>
 * <TD>Proxy_host</TD>
 * <TD>The value of this parameter is the host name or the IP ad</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">n.a.</TD>
 * <TD align="center">n.a.</TD>
 * </TR>
 * <TR>
 * <TD>::proxy_password</TD>
 * <TD>This parameter specifies the password for the proxy serve</TD>
 * <TD>Proxy_password</TD>
 * <TD>This parameter specifies the password for the proxy serve</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">n.a.</TD>
 * <TD align="center">n.a.</TD>
 * </TR>
 * <TR>
 * <TD>::proxy_port</TD>
 * <TD>This parameter specifies the port number of the proxy,</TD>
 * <TD>Proxy_port</TD>
 * <TD>This parameter specifies the port number of the proxy,</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">n.a.</TD>
 * <TD align="center">n.a.</TD>
 * </TR>
 * <TR>
 * <TD>::proxy_user</TD>
 * <TD>The value of this parameter specifies the user account fo</TD>
 * <TD>Proxy_user</TD>
 * <TD>The value of this parameter specifies the user account fo</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">n.a.</TD>
 * <TD align="center">n.a.</TD>
 * </TR>
 * <TR>
 * <TD>::simulate_shell</TD>
 * <TD>Should the value true be specified for this parameter,</TD>
 * <TD>Simulate_shell</TD>
 * <TD>Should the value true be specified for this parameter,</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">false</TD>
 * <TD align="center">false</TD>
 * </TR>
 * <TR>
 * <TD>::simulate_shell_inactivity_timeout</TD>
 * <TD>If no new characters are written to stdout or stderr afte</TD>
 * <TD>Simulate_shell_inactivity_timeout</TD>
 * <TD>If no new characters are written to stdout or stderr afte</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">n.a.</TD>
 * <TD align="center">n.a.</TD>
 * </TR>
 * <TR>
 * <TD>::simulate_shell_login_timeout</TD>
 * <TD>If no new characters are written to stdout or stderr afte</TD>
 * <TD>Simulate_shell_login_timeout</TD>
 * <TD>If no new characters are written to stdout or stderr afte</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">n.a.</TD>
 * <TD align="center">n.a.</TD>
 * </TR>
 * <TR>
 * <TD>::simulate_shell_prompt_trigger</TD>
 * <TD>The expected comman line prompt. Using this prompt the jo</TD>
 * <TD>Simulate_shell_prompt_trigger</TD>
 * <TD>The expected comman line prompt. Using this prompt the jo</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">n.a.</TD>
 * <TD align="center">n.a.</TD>
 * </TR>
 * <TR>
 * <TD>::user</TD>
 * <TD>This parameter specifies the user account to be used when</TD>
 * <TD>User</TD>
 * <TD>This parameter specifies the user account to be used when</TD>
 * <TD>true</TD>
 * <TD>String</TD>
 * <TD align="center">n.a.</TD>
 * <TD align="center">n.a.</TD>
 * </TR>
 * </TABLE> */
@I18NResourceBundle(baseName = "com_sos_net_messages", defaultLocale = "en")
@JSOptionClass(name = "SOSSSHJobOptionsSuperClass", description = "Option-Class for a SSH-Connection")
public class SOSSSHJobOptionsSuperClass extends JSOptionsClass implements ISOSConnectionOptions, ISOSAuthenticationOptions, ISOSShellOptions,
        ISOSCredentialStoreOptionsBridge {

    protected SOSCredentialStoreImpl objCredentialStore = null;
    private static final long serialVersionUID = 526076781389979326L;
    private final String conClassName = "SOSSSHJobOptions";

    public SOSSSHJobOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public SOSSSHJobOptionsSuperClass(final JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public SOSSSHJobOptionsSuperClass(final HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    }

    @JSOptionDefinition(name = "url", description = "the url for the connection", key = "url", type = "SOSOptionURL", mandatory = false)
    public SOSOptionUrl url = new SOSOptionUrl(this, conClassName + ".url", "the url for the connection", "", "", false);

    public SOSOptionUrl getUrl() {
        return url;
    }

    public void setUrl(final SOSOptionUrl pstrValue) {
        url = pstrValue;
    }

    @JSOptionDefinition(name = "raise_exception_on_error", description = "Raise an Exception if an error occured", key = "raise_exception_on_error",
            type = "SOSOptionBoolean", mandatory = true)
    public SOSOptionBoolean raise_exception_on_error = new SOSOptionBoolean(this, conClassName + ".raise_exception_on_error", 
            "Raise an Exception if an error occured", "true", "true", true);

    public SOSOptionBoolean getraise_exception_on_error() {
        return raise_exception_on_error;
    }

    public void setraise_exception_on_error(final SOSOptionBoolean raiseExceptionOnError) {
        this.raise_exception_on_error = raiseExceptionOnError;
    }

    @JSOptionDefinition(name = "auth_file", description = "auth_file", key = "auth_file", type = "SOSOptionString", mandatory = false)
    public SOSOptionInFileName auth_file = new SOSOptionInFileName(this, conClassName + ".auth_file", "auth_file", null, null, false);

    @JSOptionDefinition(name = "auth_method", description = "This parameter specifies the authorization method for the", key = "auth_method",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionAuthenticationMethod auth_method = new SOSOptionAuthenticationMethod(this, conClassName + ".auth_method", "auth_method", 
            "publickey", "publickey", false);

    @JSOptionDefinition(name = "command", description = "This parameter specifies a command that is to be executed", key = "command", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionCommandString command = new SOSOptionCommandString(this, conClassName + ".command", "This parameter specifies a command that is to be executed",
            null, null, false);

    @JSOptionDefinition(name = "command_delimiter", description = "Command delimiter characters are specified using this par", key = "command_delimiter",
            type = "SOSOptionString", mandatory = true)
    public SOSOptionRegExp command_delimiter = new SOSOptionRegExp(this, conClassName + ".command_delimiter", 
            "Command delimiter characters are specified using this par", "%%", "%%", true);

    @JSOptionDefinition(name = "command_script", description = "This parameter can be used as an alternative to command,", key = "command_script",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionCommandString command_script = new SOSOptionCommandString(this, conClassName + ".command_script", 
            "This parameter can be used as an alternative to command,", null, null, false);

    @JSOptionDefinition(name = "command_script_file", description = "This parameter can be used as an alternative to command,", key = "command_script_file",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionInFileName command_script_file = new SOSOptionInFileName(this, conClassName + ".command_script_file", 
            "This parameter can be used as an alternative to command,", null, null, false);
    
    @JSOptionDefinition(name = "command_script_param", description = "This parameter contains a parameterstring, which will be", key = "command_script_param",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString command_script_param = new SOSOptionString(this, conClassName + ".command_script_param", 
            "This parameter contains a parameterstring, which will be", null, null, false);
    
    @JSOptionDefinition(name = "host", description = "This parameter specifies the hostname or IP address of th", key = "host", type = "SOSOptionString",
            mandatory = true)
    public SOSOptionHostName host = new SOSOptionHostName(this, conClassName + ".host", "This parameter specifies the hostname or IP address of th", 
            "localhost", null, true);

    public SOSOptionHostName HostName = (SOSOptionHostName) host.SetAlias("host_name", "ssh_server_name");

    @JSOptionDefinition(name = "protocol", description = "Type of requested Datatransfer The values ftp, sftp", key = "protocol",
            type = "SOSOptionStringValueList", mandatory = true)
    public SOSOptionTransferType protocol = new SOSOptionTransferType(this, conClassName + ".protocol", "Type of requested Datatransfer The values ftp, sftp", 
            "ssh", "ssh", true);
    
    public SOSOptionTransferType ftp_protocol = (SOSOptionTransferType) protocol.SetAlias("ftp_protocol");

    public SOSOptionTransferType getProtocol() {
        return protocol;
    }

    public void setProtocol(final SOSOptionTransferType p_protocol) {
        protocol = p_protocol;
    }

    public SOSOptionTransferType TransferProtocol = (SOSOptionTransferType) protocol.SetAlias(conClassName + ".TransferProtocol");

    @JSOptionDefinition(name = "ignore_error", description = "Should the value true be specified, then execution errors", key = "ignore_error",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionBoolean ignore_error = new SOSOptionBoolean(this, conClassName + ".ignore_error", "Should the value true be specified, then execution errors",
            "false", "false", false);

    @JSOptionDefinition(name = "ignore_exit_code", description = "This parameter configures one or more exit codes which wi", key = "ignore_exit_code",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionIntegerArray ignore_exit_code = new SOSOptionIntegerArray(this, conClassName + ".ignore_exit_code", 
            "This parameter configures one or more exit codes which wi", null, null, false);

    @JSOptionDefinition(name = "ignore_signal", description = "Should the value true be specified, then on", key = "ignore_signal", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionBoolean ignore_signal = new SOSOptionBoolean(this, conClassName + ".ignore_signal", "Should the value true be specified, then on", "false", 
            "false", false);

    @JSOptionDefinition(name = "ignore_stderr", description = "This job checks if any output to stderr has been created", key = "ignore_stderr",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionBoolean ignore_stderr = new SOSOptionBoolean(this, conClassName + ".ignore_stderr", "This job checks if any output to stderr has been created",
            "false", "false", false);

    @JSOptionDefinition(name = "password", description = "This parameter specifies the user account password for au", key = "password",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionPassword password = new SOSOptionPassword(this, conClassName + ".password", "This parameter specifies the user account password for au",
            null, null, false);
    
    @JSOptionDefinition(name = "port", description = "This parameter specifies the port number of the SSH serve", key = "port", type = "SOSOptionString",
            mandatory = true)
    public SOSOptionPortNumber port = new SOSOptionPortNumber(this, conClassName + ".port", "This parameter specifies the port number of the SSH serve", "22", 
            "22", true);

    @JSOptionDefinition(name = "proxy_host", description = "The value of this parameter is the host name or the IP ad", key = "proxy_host",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString proxy_host = new SOSOptionString(this, conClassName + ".proxy_host", "The value of this parameter is the host name or the IP ad", 
            null, null, false);

    @JSOptionDefinition(name = "proxy_password", description = "This parameter specifies the password for the proxy serve", key = "proxy_password",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionPassword proxy_password = new SOSOptionPassword(this, conClassName + ".proxy_password", 
            "This parameter specifies the password for the proxy serve", null, null, false);

    @JSOptionDefinition(name = "proxy_port", description = "This parameter specifies the port number of the proxy,", key = "proxy_port",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionPortNumber proxy_port = new SOSOptionPortNumber(this, conClassName + ".proxy_port", "This parameter specifies the port number of the proxy,",
            null, null, false);

    @JSOptionDefinition(name = "proxy_user", description = "The value of this parameter specifies the user account fo", key = "proxy_user",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionUserName proxy_user = new SOSOptionUserName(this, conClassName + ".proxy_user", "The value of this parameter specifies the user account fo",
            "user", null, false);

    @JSOptionDefinition(name = "simulate_shell", description = "Should the value true be specified for this parameter,", key = "simulate_shell",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionBoolean simulate_shell = new SOSOptionBoolean(this, conClassName + ".simulate_shell", "Should the value true be specified for this parameter,",
            "false", "false", false);

    @JSOptionDefinition(name = "simulate_shell_inactivity_timeout", description = "If no new characters are written to stdout or stderr afte",
            key = "simulate_shell_inactivity_timeout", type = "SOSOptionString", mandatory = false)
    public SOSOptionInteger simulate_shell_inactivity_timeout = new SOSOptionInteger(this, conClassName + ".simulate_shell_inactivity_timeout",
            "If no new characters are written to stdout or stderr afte", "0", "0", false);

    @JSOptionDefinition(name = "simulate_shell_login_timeout", description = "If no new characters are written to stdout or stderr afte",
            key = "simulate_shell_login_timeout", type = "SOSOptionString", mandatory = false)
    public SOSOptionInteger simulate_shell_login_timeout = new SOSOptionInteger(this, conClassName + ".simulate_shell_login_timeout",
            "If no new characters are written to stdout or stderr afte", "0", "0", false);

    @JSOptionDefinition(name = "simulate_shell_prompt_trigger", description = "The expected comman line prompt. Using this prompt the jo",
            key = "simulate_shell_prompt_trigger", type = "SOSOptionString", mandatory = false)
    public SOSOptionString simulate_shell_prompt_trigger = new SOSOptionString(this, conClassName + ".simulate_shell_prompt_trigger", 
            "The expected comman line prompt. Using this prompt the jo", null, null, false);

    @JSOptionDefinition(name = "user", description = "This parameter specifies the user account to be used when", key = "user", type = "SOSOptionString",
            mandatory = true)
    public SOSOptionUserName user = new SOSOptionUserName(this, conClassName + ".user", "This parameter specifies the user account to be used when", 
            "user", null, true);

    @JSOptionDefinition(name = "ignore_hangup_signal", description = "Should the value true be specified, then execution errors", key = "ignore_hangup_signal",
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean ignore_hangup_signal = new SOSOptionBoolean(this, conClassName + ".ignore_hangup_signal",
            "Should the value true be specified, then execution errors", "true", "true", false);

    @JSOptionDefinition(name = "preCommand", description = "the preCommand to set an environmental variable on the remote host", key = "preCommand",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString preCommand = new SOSOptionString(this, conClassName + ".preCommand", "the preCommand to set an environmental variable on the remote host",
            "export %1s=%2s", "export %1s=%2s", false);

    public SOSOptionString getPreCommand() {
        return preCommand;
    }

    public void setPreCommand(final SOSOptionString newPreCommand) {
        this.preCommand = newPreCommand;
    }

    @JSOptionDefinition(name = "postCommandRead", description = "the postCommand to read temporary file and write its content to stdout",
            key = "postCommandRead", type = "SOSOptionString", mandatory = false)
    public SOSOptionString postCommandRead = new SOSOptionString(this, conClassName + ".postCommandRead", 
            "the postCommand to read temporary file and write its content to stdout", "cat %s", "cat %s", false);

    public SOSOptionString getPostCommandRead() {
        return postCommandRead;
    }

    public void setPostCommandRead(final SOSOptionString newPostCommandRead) {
        this.postCommandRead = newPostCommandRead;
    }

    @JSOptionDefinition(name = "postCommandDelete", description = "the postCommand to delete the temporary file", key = "postCommandDelete",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString postCommandDelete = new SOSOptionString(this, conClassName + ".postCommandDelete", "the postCommand to delete the temporary file",
            "rm %s", "rm %s", false);

    public SOSOptionString getPostCommandDelete() {
        return postCommandDelete;
    }

    public void setPostCommandDelete(final SOSOptionString newPostCommandDelete) {
        this.postCommandDelete = newPostCommandDelete;
    }

    @JSOptionDefinition(name = "runWithWatchdog", description = "This parameter can be used to activate ssh session management", key = "runWithWatchdog",
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean runWithWatchdog = new SOSOptionBoolean(this, conClassName + ".runWithWatchdog",
            "This parameter can be used to activate ssh session management", "false", "false", false);

    public SOSOptionBoolean getRunWithWatchdog() {
        return runWithWatchdog;
    }

    public void setRunWithWatchdog(SOSOptionBoolean runWithWatchdog) {
        this.runWithWatchdog = runWithWatchdog;
    }

    @JSOptionDefinition(name = "create_environment_variables", description = "This parameter can be used to activate ssh environment variables",
            key = "create_environment_variables", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean createEnvironmentVariables = new SOSOptionBoolean(this, conClassName + ".createEnvironmentVariables",
            "This parameter can be used to activate ssh environment variables", "true", "true", false);

    public SOSOptionBoolean getCreateEnvironmentVariables() {
        return createEnvironmentVariables;
    }

    public void setCreateEnvironmentVariables(SOSOptionBoolean createEnvironmentVariables) {
        this.createEnvironmentVariables = createEnvironmentVariables;
    }

    @JSOptionDefinition(name = "cleanupJobchain", description = "This parameter is used to determine the name of the jobchain for cleanup",
            key = "cleanupJobchain", type = "SOSOptionString", mandatory = false)
    public SOSOptionString cleanupJobchain = new SOSOptionString(this, conClassName + ".cleanupJobchain", 
            "This parameter is used to determine the name of the jobchain for cleanup", "", "", false);

    public SOSOptionString getCleanupJobchain() {
        return cleanupJobchain;
    }

    public void setCleanupJobchain(SOSOptionString cleanupJobchain) {
        this.cleanupJobchain = cleanupJobchain;
    }

    @JSOptionDefinition(name = "ssh_job_kill_pid_command", description = "The command to kill a remote running pid", key = "ssh_job_kill_pid_command",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString ssh_job_kill_pid_command = new SOSOptionString(this, conClassName + ".ssh_job_kill_pid_command",
            "The command to to kill a remote running pid", "", "kill -9 ${pid}", false);

    public SOSOptionString getssh_job_kill_pid_command() {
        return ssh_job_kill_pid_command;
    }

    public void setssh_job_kill_pid_command(SOSOptionString ssh_job_kill_pid_command) {
        this.ssh_job_kill_pid_command = ssh_job_kill_pid_command;
    }

    @JSOptionDefinition(name = "ssh_job_terminate_pid_command", description = "The command to to terminate a remote running pid",
            key = "ssh_job_terminate_pid_command", type = "SOSOptionString", mandatory = false)
    public SOSOptionString ssh_job_terminate_pid_command = new SOSOptionString(this, conClassName + ".ssh_job_terminate_pid_command",
            "The command to to terminate a remote running pid", "", "kill -15 ${pid}", false);

    public SOSOptionString getssh_job_terminate_pid_command() {
        return ssh_job_terminate_pid_command;
    }

    public void setssh_job_terminate_pid_command(SOSOptionString ssh_job_terminate_pid_command) {
        this.ssh_job_terminate_pid_command = ssh_job_terminate_pid_command;
    }

    @JSOptionDefinition(name = "ssh_job_get_pid_command", description = "The command to get the PID of the active shell", key = "ssh_job_get_pid_command",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString ssh_job_get_pid_command = new SOSOptionString(this, conClassName + ".ssh_job_get_pid_command", 
            "The command to get the PID of the active shell", "", "echo $$", false);

    public SOSOptionString getssh_job_get_pid_command() {
        return ssh_job_get_pid_command;
    }

    public void setssh_job_get_pid_command(SOSOptionString ssh_job_get_pid_command) {
        this.ssh_job_get_pid_command = ssh_job_get_pid_command;
    }

    @JSOptionDefinition(name = "ssh_job_get_child_processes_command", description = "The command to get the child processes related to the given pid",
            key = "ssh_job_get_child_processes_command", type = "SOSOptionString", mandatory = false)
    public SOSOptionString ssh_job_get_child_processes_command = new SOSOptionString(this, conClassName + ".ssh_job_get_child_processes_command",
            "The command to get the child processes related to the given pid", "", "/bin/ps -ef | pgrep -P${pid}", false);

    public SOSOptionString getssh_job_get_child_processes_command() {
        return ssh_job_get_child_processes_command;
    }

    public void setssh_job_get_child_processes_command(SOSOptionString ssh_job_get_child_processes_command) {
        this.ssh_job_get_child_processes_command = ssh_job_get_child_processes_command;
    }

    @JSOptionDefinition(name = "ssh_job_get_active_processes_command", description = "The command to check if the given process is still running",
            key = "ssh_job_get_active_processes_command", type = "SOSOptionString", mandatory = false)
    public SOSOptionString ssh_job_get_active_processes_command = new SOSOptionString(this, conClassName + ".ssh_job_get_active_processes_command",
            "The command to check if the given process is still running", "", "/bin/ps -ef | grep ${pid} | grep ${user} | grep -v grep", false);

    public SOSOptionString getssh_job_get_active_processes_command() {
        return ssh_job_get_active_processes_command;
    }

    public void setssh_job_get_active_processes_command(SOSOptionString ssh_job_get_active_processes_command) {
        this.ssh_job_get_active_processes_command = ssh_job_get_active_processes_command;
    }

    @JSOptionDefinition(name = "ssh_job_timeout_kill_after", description = "The timeout in seconds after which a kill signal will be send",
            key = "ssh_job_timeout_kill_after", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger ssh_job_timeout_kill_after = new SOSOptionInteger(this, conClassName + ".ssh_job_timeout_kill_after",
            "The timeout in seconds after which a kill signal will be send", "", "", false);

    public SOSOptionInteger getssh_job_timeout_kill_after() {
        return ssh_job_timeout_kill_after;
    }

    public void setssh_job_timeout_kill_after(SOSOptionInteger ssh_job_timeout_kill_after) {
        this.ssh_job_timeout_kill_after = ssh_job_timeout_kill_after;
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
    public void CheckMandatory() throws JSExceptionMandatoryOptionMissing {
        try {
            getCredentialStore().checkCredentialStoreOptions();
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

    @Override
    public SOSOptionInFileName getAuth_file() {
        return auth_file;
    }

    @Override
    public void setAuth_file(final SOSOptionInFileName authFile) {
        auth_file = authFile;
    }

    @Override
    public SOSOptionAuthenticationMethod getAuth_method() {
        return auth_method;
    }

    @Override
    public void setAuth_method(final SOSOptionAuthenticationMethod authMethod) {
        auth_method = authMethod;
    }

    @Override
    public SOSOptionCommandString getCommand() {
        return command;
    }

    @Override
    public void setCommand(final SOSOptionCommandString command) {
        this.command = command;
    }

    @Override
    public SOSOptionRegExp getCommand_delimiter() {
        return command_delimiter;
    }

    @Override
    public void setCommand_delimiter(final SOSOptionRegExp commandDelimiter) {
        command_delimiter = commandDelimiter;
    }

    @Override
    public SOSOptionCommandString getCommand_script() {
        return command_script;
    }

    @Override
    public void setCommand_script(final SOSOptionCommandString commandScript) {
        command_script = commandScript;
    }

    @Override
    public SOSOptionInFileName getCommand_script_file() {
        return command_script_file;
    }

    @Override
    public void setCommand_script_file(final SOSOptionInFileName commandScriptFile) {
        command_script_file = commandScriptFile;
    }

    @Override
    public SOSOptionString getCommand_script_param() {
        return command_script_param;
    }

    @Override
    public void setCommand_script_param(final SOSOptionString commandScriptParam) {
        command_script_param = commandScriptParam;
    }

    @Override
    public SOSOptionHostName getHost() {
        return host;
    }

    @Override
    public void setHost(final SOSOptionHostName phost) {
        host = phost;
    }

    @Override
    public SOSOptionBoolean getIgnore_error() {
        return ignore_error;
    }

    @Override
    public void setIgnore_error(final SOSOptionBoolean ignoreError) {
        ignore_error = ignoreError;
    }

    @Override
    public SOSOptionIntegerArray getIgnore_exit_code() {
        return ignore_exit_code;
    }

    @Override
    public void setIgnore_exit_code(final SOSOptionIntegerArray ignoreExitCode) {
        ignore_exit_code = ignoreExitCode;
    }

    @Override
    public SOSOptionBoolean getIgnore_signal() {
        return ignore_signal;
    }

    @Override
    public void setIgnore_signal(final SOSOptionBoolean ignoreSignal) {
        ignore_signal = ignoreSignal;
    }

    @Override
    public SOSOptionBoolean getIgnore_stderr() {
        return ignore_stderr;
    }

    @Override
    public void setIgnore_stderr(final SOSOptionBoolean ignoreStderr) {
        ignore_stderr = ignoreStderr;
    }

    @Override
    public SOSOptionPassword getPassword() {
        return password;
    }

    @Override
    public void setPassword(final SOSOptionPassword password) {
        this.password = password;
    }

    @Override
    public SOSOptionPortNumber getPort() {
        return port;
    }

    @Override
    public void setPort(final SOSOptionPortNumber port) {
        this.port = port;
    }

    @Override
    public SOSOptionString getProxy_host() {
        return proxy_host;
    }

    @Override
    public void setProxy_host(final SOSOptionString proxyHost) {
        proxy_host = proxyHost;
    }

    @Override
    public SOSOptionPassword getProxy_password() {
        return proxy_password;
    }

    @Override
    public void setProxy_password(final SOSOptionPassword proxyPassword) {
        proxy_password = proxyPassword;
    }

    @Override
    public SOSOptionPortNumber getProxy_port() {
        return proxy_port;
    }

    @Override
    public void setProxy_port(final SOSOptionPortNumber proxyPort) {
        proxy_port = proxyPort;
    }

    @Override
    public SOSOptionUserName getProxy_user() {
        return proxy_user;
    }

    @Override
    public void setProxy_user(final SOSOptionUserName proxyUser) {
        proxy_user = proxyUser;
    }

    @Override
    public SOSOptionBoolean getSimulate_shell() {
        return simulate_shell;
    }

    @Override
    public void setSimulate_shell(final SOSOptionBoolean simulateShell) {
        simulate_shell = simulateShell;
    }

    @Override
    public SOSOptionInteger getSimulate_shell_inactivity_timeout() {
        return simulate_shell_inactivity_timeout;
    }

    @Override
    public void setSimulate_shell_inactivity_timeout(final SOSOptionInteger simulateShellInactivityTimeout) {
        simulate_shell_inactivity_timeout = simulateShellInactivityTimeout;
    }

    @Override
    public SOSOptionInteger getSimulate_shell_login_timeout() {
        return simulate_shell_login_timeout;
    }

    @Override
    public void setSimulate_shell_login_timeout(final SOSOptionInteger simulateShellLoginTimeout) {
        simulate_shell_login_timeout = simulateShellLoginTimeout;
    }

    @Override
    public SOSOptionString getSimulate_shell_prompt_trigger() {
        return simulate_shell_prompt_trigger;
    }

    @Override
    public void setSimulate_shell_prompt_trigger(final SOSOptionString simulateShellPromptTrigger) {
        simulate_shell_prompt_trigger = simulateShellPromptTrigger;
    }

    @Override
    public SOSOptionUserName getUser() {
        return user;
    }

    @Override
    public void setUser(final SOSOptionUserName user) {
        this.user = user;
    }

    @Override
    public SOSOptionBoolean getIgnore_hangup_signal() {
        return ignore_hangup_signal;
    }

    @Override
    public void setIgnore_hangup_signal(final SOSOptionBoolean pIgnoreHangupSignal) {
        ignore_hangup_signal = pIgnoreHangupSignal;
    }

    @Override
    public SOSOptionString getalternative_account() {
        // TO DO Auto-generated method stub
        return null;
    }

    @Override
    public SOSOptionHostName getalternative_host() {
        // TO DO Auto-generated method stub
        return null;
    }

    @Override
    public SOSOptionString getalternative_passive_mode() {
        // TO DO Auto-generated method stub
        return null;
    }

    @Override
    public SOSOptionPassword getalternative_password() {
        // TO DO Auto-generated method stub
        return null;
    }

    @Override
    public SOSOptionPortNumber getalternative_port() {
        return null;
    }

    @Override
    public void setalternative_host(final SOSOptionHostName pAlternativeHost) {

    }

    @Override
    public void setalternative_password(final SOSOptionPassword pAlternativePassword) {

    }

    public SOSCredentialStoreImpl getCredentialStore() {
        if (objCredentialStore == null) {
            objCredentialStore = new SOSCredentialStoreImpl(this);
        }
        return objCredentialStore;
    }

    public void setChildClasses(final HashMap<String, String> pobjJSSettings, final String pstrPrefix) throws Exception {
        getCredentialStore().setChildClasses(pobjJSSettings, pstrPrefix);
        objCredentialStore.checkCredentialStoreOptions();
    }

    @JSOptionDefinition(name = "Strict_HostKey_Checking", description = "Check the hostkey against known hosts for SSH", key = "Strict_HostKey_Checking",
            type = "SOSOptionValueList", mandatory = false)
    public SOSOptionStringValueList StrictHostKeyChecking = new SOSOptionStringValueList(this, conClassName + ".strict_hostkey_checking", 
            "Check the hostkey against known hosts for SSH", "ask;yes;no", "no", false);

    public String getStrict_HostKey_Checking() {
        return StrictHostKeyChecking.Value();
    }

    public SOSSSHJobOptionsSuperClass setStrict_HostKey_Checking(final String pstrValue) {
        StrictHostKeyChecking.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "proxy_protocol", description = "Proxy protocol", key = "proxy_protocol", type = "SOSOptionProxyProtocol", mandatory = false)
    public SOSOptionProxyProtocol proxy_protocol = new SOSOptionProxyProtocol(this, conClassName + ".proxy_protocol", "Proxy protocol", 
            SOSOptionProxyProtocol.Protocol.http.name(), SOSOptionProxyProtocol.Protocol.http.name(), false);

    public SOSOptionProxyProtocol getproxy_protocol() {
        return proxy_protocol;
    }

    public void setproxy_host(SOSOptionProxyProtocol val) {
        proxy_protocol = val;
    }
    
}