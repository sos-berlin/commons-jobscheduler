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
import com.sos.JSHelper.Options.SOSOptionTransferType;
import com.sos.JSHelper.Options.SOSOptionUrl;
import com.sos.JSHelper.Options.SOSOptionUserName;
import com.sos.JSHelper.interfaces.ISOSConnectionOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSShellOptions;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "com_sos_net_messages", defaultLocale = "en")
@JSOptionClass(name = "SOSSSHJobOptionsSuperClass", description = "Option-Class for a SSH-Connection")
public class SOSSSHJobOptionsSuperClass extends JSOptionsClass implements ISOSConnectionOptions, ISOSAuthenticationOptions, ISOSShellOptions,
        ISOSCredentialStoreOptionsBridge {

    protected SOSCredentialStoreImpl objCredentialStore = null;
    private static final long serialVersionUID = 526076781389979326L;
    private static final String CLASSNAME = "SOSSSHJobOptions";

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
    public SOSOptionUrl url = new SOSOptionUrl(this, CLASSNAME + ".url", "the url for the connection", "", "", false);

    public SOSOptionUrl getUrl() {
        return url;
    }

    public void setUrl(final SOSOptionUrl pstrValue) {
        url = pstrValue;
    }

    @JSOptionDefinition(name = "raise_exception_on_error", description = "Raise an Exception if an error occured", key = "raise_exception_on_error",
            type = "SOSOptionBoolean", mandatory = true)
    public SOSOptionBoolean raiseExceptionOnError = new SOSOptionBoolean(this, CLASSNAME + ".raise_exception_on_error", 
            "Raise an Exception if an error occured", "true", "true", true);

    public SOSOptionBoolean getRaiseExceptionOnError() {
        return raiseExceptionOnError;
    }

    public void setRaiseExceptionOnError(final SOSOptionBoolean raiseExceptionOnError) {
        this.raiseExceptionOnError = raiseExceptionOnError;
    }

    @JSOptionDefinition(name = "auth_file", description = "auth_file", key = "auth_file", type = "SOSOptionString", mandatory = false)
    public SOSOptionInFileName authFile = new SOSOptionInFileName(this, CLASSNAME + ".auth_file", "auth_file", null, null, false);

    @JSOptionDefinition(name = "auth_method", description = "This parameter specifies the authorization method for the", key = "auth_method", 
            type = "SOSOptionString", mandatory = false)
    public SOSOptionAuthenticationMethod authMethod = new SOSOptionAuthenticationMethod(this, CLASSNAME + ".auth_method", "auth_method", 
            "publickey", "publickey", false);
    
    @JSOptionDefinition(name = "command", description = "This parameter specifies a command that is to be executed", key = "command", 
            type = "SOSOptionString", mandatory = false)
    public SOSOptionCommandString command = new SOSOptionCommandString(this, CLASSNAME + ".command", 
            "This parameter specifies a command that is to be executed", null, null, false);

    @JSOptionDefinition(name = "command_delimiter", description = "Command delimiter characters are specified using this par", 
            key = "command_delimiter", type = "SOSOptionString", mandatory = true)
    public SOSOptionRegExp commandDelimiter = new SOSOptionRegExp(this, CLASSNAME + ".command_delimiter", 
            "Command delimiter characters are specified using this par", "%%", "%%", true);

    @JSOptionDefinition(name = "command_script", description = "This parameter can be used as an alternative to command,", key = "command_script", 
            type = "SOSOptionString", mandatory = false)
    public SOSOptionCommandString commandScript = new SOSOptionCommandString(this, CLASSNAME + ".command_script", 
            "This parameter can be used as an alternative to command,", null, null, false);

    @JSOptionDefinition(name = "command_script_file", description = "This parameter can be used as an alternative to command,", 
            key = "command_script_file", type = "SOSOptionString", mandatory = false)
    public SOSOptionInFileName commandScriptFile = new SOSOptionInFileName(this, CLASSNAME + ".command_script_file", 
            "This parameter can be used as an alternative to command,", null, null, false);

    @JSOptionDefinition(name = "command_script_param", description = "This parameter contains a parameterstring, which will be", 
            key = "command_script_param", type = "SOSOptionString", mandatory = false)
    public SOSOptionString commandScriptParam = new SOSOptionString(this, CLASSNAME + ".command_script_param", 
            "This parameter contains a parameterstring, which will be", null, null, false);

    @JSOptionDefinition(name = "host", description = "This parameter specifies the hostname or IP address of th", key = "host", 
            type = "SOSOptionString", mandatory = true)
    public SOSOptionHostName host = new SOSOptionHostName(this, CLASSNAME + ".host", "This parameter specifies the hostname or IP address of th", 
            "localhost", null, true);
    
    public SOSOptionHostName hostName = (SOSOptionHostName) host.setAlias("host_name", "ssh_server_name");

    @JSOptionDefinition(name = "protocol", description = "Type of requested Datatransfer The values ftp, sftp", key = "protocol", 
            type = "SOSOptionStringValueList", mandatory = true)
    public SOSOptionTransferType protocol = new SOSOptionTransferType(this, CLASSNAME + ".protocol", 
            "Type of requested Datatransfer The values ftp, sftp", "ssh", "ssh", true);
    
    public SOSOptionTransferType ftpProtocol = (SOSOptionTransferType) protocol.setAlias("ftp_protocol");

    public SOSOptionTransferType getProtocol() {
        return protocol;
    }

    public void setProtocol(final SOSOptionTransferType pProtocol) {
        protocol = pProtocol;
    }

    public SOSOptionTransferType transferProtocol = (SOSOptionTransferType) protocol.setAlias(CLASSNAME + ".TransferProtocol");

    @JSOptionDefinition(name = "ignore_error", description = "Should the value true be specified, then execution errors", key = "ignore_error", 
            type = "SOSOptionString", mandatory = false)
    public SOSOptionBoolean ignoreError = new SOSOptionBoolean(this, CLASSNAME + ".ignore_error", 
            "Should the value true be specified, then execution errors", "false", "false", false);

    @JSOptionDefinition(name = "ignore_exit_code", description = "This parameter configures one or more exit codes which wi", 
            key = "ignore_exit_code", type = "SOSOptionString", mandatory = false)
    public SOSOptionIntegerArray ignoreExitCode = new SOSOptionIntegerArray(this, CLASSNAME + ".ignore_exit_code", 
            "This parameter configures one or more exit codes which wi", null, null, false);

    @JSOptionDefinition(name = "ignore_signal", description = "Should the value true be specified, then on", key = "ignore_signal", 
            type = "SOSOptionString", mandatory = false)
    public SOSOptionBoolean ignoreSignal = new SOSOptionBoolean(this, CLASSNAME + ".ignore_signal", "Should the value true be specified, then on", 
            "false", "false", false);

    @JSOptionDefinition(name = "ignore_stderr", description = "This job checks if any output to stderr has been created", key = "ignore_stderr", 
            type = "SOSOptionString", mandatory = false)
    public SOSOptionBoolean ignoreStderr = new SOSOptionBoolean(this, CLASSNAME + ".ignore_stderr", 
            "This job checks if any output to stderr has been created", "false", "false", false);

    @JSOptionDefinition(name = "password", description = "This parameter specifies the user account password for au", key = "password", 
            type = "SOSOptionString", mandatory = false)
    public SOSOptionPassword password = new SOSOptionPassword(this, CLASSNAME + ".password", 
            "This parameter specifies the user account password for au", null, null, false);

    @JSOptionDefinition(name = "port", description = "This parameter specifies the port number of the SSH serve", key = "port", 
            type = "SOSOptionString", mandatory = true)
    public SOSOptionPortNumber port = new SOSOptionPortNumber(this, CLASSNAME + ".port", "This parameter specifies the port number of the SSH serve", 
            "22", "22", true);

    @JSOptionDefinition(name = "proxy_host", description = "The value of this parameter is the host name or the IP ad", key = "proxy_host", 
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString proxyHost = new SOSOptionString(this, CLASSNAME + ".proxy_host", 
            "The value of this parameter is the host name or the IP ad", null, null, false);

    @JSOptionDefinition(name = "proxy_password", description = "This parameter specifies the password for the proxy serve", key = "proxy_password", 
            type = "SOSOptionString", mandatory = false)
    public SOSOptionPassword proxyPassword = new SOSOptionPassword(this, CLASSNAME + ".proxy_password", 
            "This parameter specifies the password for the proxy serve", null, null, false);

    @JSOptionDefinition(name = "proxy_port", description = "This parameter specifies the port number of the proxy,", key = "proxy_port", 
            type = "SOSOptionString", mandatory = false)
    public SOSOptionPortNumber proxyPort = new SOSOptionPortNumber(this, CLASSNAME + ".proxy_port", 
            "This parameter specifies the port number of the proxy,", null, null, false);

    @JSOptionDefinition(name = "proxy_user", description = "The value of this parameter specifies the user account fo", key = "proxy_user", 
            type = "SOSOptionString", mandatory = false)
    public SOSOptionUserName proxyUser = new SOSOptionUserName(this, CLASSNAME + ".proxy_user", 
            "The value of this parameter specifies the user account fo", "user", null, false);

    @JSOptionDefinition(name = "simulate_shell", description = "Should the value true be specified for this parameter,", key = "simulate_shell", 
            type = "SOSOptionString", mandatory = false)
    public SOSOptionBoolean simulateShell = new SOSOptionBoolean(this, CLASSNAME + ".simulate_shell", 
            "Should the value true be specified for this parameter,", "false", "false", false);

    @JSOptionDefinition(name = "simulate_shell_inactivity_timeout", description = "If no new characters are written to stdout or stderr afte", 
            key = "simulate_shell_inactivity_timeout", type = "SOSOptionString", mandatory = false)
    public SOSOptionInteger simulateShellInactivityTimeout = new SOSOptionInteger(this, CLASSNAME + ".simulate_shell_inactivity_timeout", 
            "If no new characters are written to stdout or stderr afte", "0", "0", false);

    @JSOptionDefinition(name = "simulate_shell_login_timeout", description = "If no new characters are written to stdout or stderr afte", 
            key = "simulate_shell_login_timeout", type = "SOSOptionString", mandatory = false)
    public SOSOptionInteger simulateShellLoginTimeout = new SOSOptionInteger(this, CLASSNAME + ".simulate_shell_login_timeout", 
            "If no new characters are written to stdout or stderr afte", "0", "0", false);

    @JSOptionDefinition(name = "simulate_shell_prompt_trigger", description = "The expected comman line prompt. Using this prompt the jo", 
            key = "simulate_shell_prompt_trigger", type = "SOSOptionString", mandatory = false)
    public SOSOptionString simulateShellPromptTrigger = new SOSOptionString(this, CLASSNAME + ".simulate_shell_prompt_trigger", 
            "The expected comman line prompt. Using this prompt the jo", null, null, false);

    @JSOptionDefinition(name = "user", description = "This parameter specifies the user account to be used when", key = "user", 
            type = "SOSOptionString", mandatory = true)
    public SOSOptionUserName user = new SOSOptionUserName(this, CLASSNAME + ".user", "This parameter specifies the user account to be used when", 
            "user", null, true);

    @JSOptionDefinition(name = "ignore_hangup_signal", description = "Should the value true be specified, then execution errors", 
            key = "ignore_hangup_signal", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean ignoreHangupSignal = new SOSOptionBoolean(this, CLASSNAME + ".ignore_hangup_signal", 
            "Should the value true be specified, then execution errors", "true", "true", false);

    @JSOptionDefinition(name = "preCommand", description = "the preCommand to set an environmental variable on the remote host", key = "preCommand", 
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString preCommand = new SOSOptionString(this, CLASSNAME + ".preCommand", 
            "the preCommand to set an environmental variable on the remote host", "export %1$s=%2$s", "export %1$s=%2$s", false);

    public SOSOptionString getPreCommand() {
        return preCommand;
    }

    public void setPreCommand(final SOSOptionString newPreCommand) {
        this.preCommand = newPreCommand;
    }

    @JSOptionDefinition(name = "postCommandRead", description = "the postCommand to read temporary file and write its content to stdout", 
            key = "postCommandRead", type = "SOSOptionString", mandatory = false)
    public SOSOptionString postCommandRead = new SOSOptionString(this, CLASSNAME + ".postCommandRead", 
            "the postCommand to read temporary file and write its content to stdout", "cat %s", "cat %s", false);

    public SOSOptionString getPostCommandRead() {
        return postCommandRead;
    }

    public void setPostCommandRead(final SOSOptionString newPostCommandRead) {
        this.postCommandRead = newPostCommandRead;
    }

    @JSOptionDefinition(name = "postCommandDelete", description = "the postCommand to delete the temporary file", key = "postCommandDelete", 
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString postCommandDelete = new SOSOptionString(this, CLASSNAME + ".postCommandDelete", 
            "the postCommand to delete the temporary file", "rm %s", "rm %s", false);

    public SOSOptionString getPostCommandDelete() {
        return postCommandDelete;
    }

    public void setPostCommandDelete(final SOSOptionString newPostCommandDelete) {
        this.postCommandDelete = newPostCommandDelete;
    }

    @JSOptionDefinition(name = "runWithWatchdog", description = "This parameter can be used to activate ssh session management", 
            key = "runWithWatchdog", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean runWithWatchdog = new SOSOptionBoolean(this, CLASSNAME + ".runWithWatchdog", 
            "This parameter can be used to activate ssh session management", "false", "false", false);

    public SOSOptionBoolean getRunWithWatchdog() {
        return runWithWatchdog;
    }

    public void setRunWithWatchdog(SOSOptionBoolean runWithWatchdog) {
        this.runWithWatchdog = runWithWatchdog;
    }

    @JSOptionDefinition(name = "create_environment_variables", description = "This parameter can be used to activate ssh environment variables", 
            key = "create_environment_variables", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean createEnvironmentVariables = new SOSOptionBoolean(this, CLASSNAME + ".createEnvironmentVariables", 
            "This parameter can be used to activate ssh environment variables", "true", "true", false);

    public SOSOptionBoolean getCreateEnvironmentVariables() {
        return createEnvironmentVariables;
    }

    public void setCreateEnvironmentVariables(SOSOptionBoolean createEnvironmentVariables) {
        this.createEnvironmentVariables = createEnvironmentVariables;
    }

    @JSOptionDefinition(name = "cleanupJobchain", description = "This parameter is used to determine the name of the jobchain for cleanup", 
            key = "cleanupJobchain", type = "SOSOptionString", mandatory = false)
    public SOSOptionString cleanupJobchain = new SOSOptionString(this, CLASSNAME + ".cleanupJobchain", 
            "This parameter is used to determine the name of the jobchain for cleanup", "", "", false);

    public SOSOptionString getCleanupJobchain() {
        return cleanupJobchain;
    }

    public void setCleanupJobchain(SOSOptionString cleanupJobchain) {
        this.cleanupJobchain = cleanupJobchain;
    }

    @JSOptionDefinition(name = "ssh_job_kill_pid_command", description = "The command to kill a remote running pid", 
            key = "ssh_job_kill_pid_command", type = "SOSOptionString", mandatory = false)
    public SOSOptionString sshJobKillPidCommand = new SOSOptionString(this, CLASSNAME + ".ssh_job_kill_pid_command", 
            "The command to to kill a remote running pid", "", "kill -9 ${pid}", false);

    public SOSOptionString getSshJobKillPidCommand() {
        return sshJobKillPidCommand;
    }

    public void setSshJobKillPidCommand(SOSOptionString value) {
        this.sshJobKillPidCommand = value;
    }

    @JSOptionDefinition(name = "ssh_job_terminate_pid_command", description = "The command to to terminate a remote running pid", 
            key = "ssh_job_terminate_pid_command", type = "SOSOptionString", mandatory = false)
    public SOSOptionString sshJobTerminatePidCommand = new SOSOptionString(this, CLASSNAME + ".ssh_job_terminate_pid_command", 
            "The command to to terminate a remote running pid", "", "kill -15 ${pid}", false);

    public SOSOptionString getSshJobTerminatePidCommand() {
        return sshJobTerminatePidCommand;
    }

    public void setSshJobTerminatePidCommand(SOSOptionString value) {
        this.sshJobTerminatePidCommand = value;
    }

    @JSOptionDefinition(name = "ssh_job_get_pid_command", description = "The command to get the PID of the active shell", 
            key = "ssh_job_get_pid_command", type = "SOSOptionString", mandatory = false)
    public SOSOptionString sshJobGetPidCommand = new SOSOptionString(this, CLASSNAME + ".ssh_job_get_pid_command", 
            "The command to get the PID of the active shell", "", "echo $$", false);

    public SOSOptionString getSshJobGetPidCommand() {
        return sshJobGetPidCommand;
    }

    public void setSshJobGetPidCommand(SOSOptionString value) {
        this.sshJobGetPidCommand = value;
    }

    @JSOptionDefinition(name = "ssh_job_get_child_processes_command", description = "The command to get the child processes related to the given pid", 
            key = "ssh_job_get_child_processes_command", type = "SOSOptionString", mandatory = false)
    public SOSOptionString sshJobGetChildProcessesCommand = new SOSOptionString(this, CLASSNAME + ".ssh_job_get_child_processes_command", 
            "The command to get the child processes related to the given pid", "", "/bin/ps -ef | pgrep -P${pid}", false);

    public SOSOptionString getSshJobGetChildProcessesCommand() {
        return sshJobGetChildProcessesCommand;
    }

    public void setSshJobGetChildProcessesCommand(SOSOptionString value) {
        this.sshJobGetChildProcessesCommand = value;
    }

    @JSOptionDefinition(name = "ssh_job_get_active_processes_command", description = "The command to check if the given process is still running", 
            key = "ssh_job_get_active_processes_command", type = "SOSOptionString", mandatory = false)
    public SOSOptionString sshJobGetActiveProcessesCommand = new SOSOptionString(this, CLASSNAME + ".ssh_job_get_active_processes_command", 
            "The command to check if the given process is still running", "", "/bin/ps -ef | grep ${pid} | grep ${user} | grep -v grep", false);

    public SOSOptionString getSshJobGetActiveProcessesCommand() {
        return sshJobGetActiveProcessesCommand;
    }

    public void setSshJobGetActiveProcessesCommand(SOSOptionString value) {
        this.sshJobGetActiveProcessesCommand = value;
    }

    @JSOptionDefinition(name = "ssh_job_timeout_kill_after", description = "The timeout in seconds after which a kill signal will be send", 
            key = "ssh_job_timeout_kill_after", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger sshJobTimeoutKillAfter = new SOSOptionInteger(this, CLASSNAME + ".ssh_job_timeout_kill_after", 
            "The timeout in seconds after which a kill signal will be send", "", "", false);

    public SOSOptionInteger getSshJobTimeoutKillAfter() {
        return sshJobTimeoutKillAfter;
    }

    public void setSshJobTimeoutKillAfter(SOSOptionInteger value) {
        this.sshJobTimeoutKillAfter = value;
    }

    @Override
    public void setAllOptions(final HashMap<String, String> pobjJSSettings) {
        flgSetAllOptions = true;
        objSettings = pobjJSSettings;
        super.setSettings(objSettings);
        super.setAllOptions(pobjJSSettings);
        flgSetAllOptions = false;
    }

    @Override
    public void checkMandatory() throws JSExceptionMandatoryOptionMissing {
        try {
            getCredentialStore().checkCredentialStoreOptions();
            super.checkMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

    @Override
    public void commandLineArgs(final String[] pstrArgs) {
        super.commandLineArgs(pstrArgs);
        this.setAllOptions(super.objSettings);
    }

    @Override
    public void commandLineArgs(final String value) {
        super.commandLineArgs(value);
        this.setAllOptions(super.objSettings);
    }

    @Override
    public SOSOptionInFileName getAuthFile() {
        return authFile;
    }

    @Override
    public void setAuthFile(final SOSOptionInFileName authFile) {
        this.authFile = authFile;
    }

    @Override
    public SOSOptionAuthenticationMethod getAuthMethod() {
        return authMethod;
    }

    @Override
    public void setAuthMethod(final SOSOptionAuthenticationMethod authMethod) {
        this.authMethod = authMethod;
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
    public SOSOptionRegExp getCommandDelimiter() {
        return commandDelimiter;
    }

    @Override
    public void setCommandDelimiter(final SOSOptionRegExp commandDelimiter) {
        this.commandDelimiter = commandDelimiter;
    }

    @Override
    public SOSOptionCommandString getCommandScript() {
        return commandScript;
    }

    @Override
    public void setCommandScript(final SOSOptionCommandString commandScript) {
        this.commandScript = commandScript;
    }

    @Override
    public SOSOptionInFileName getCommandScriptFile() {
        return commandScriptFile;
    }

    @Override
    public void setCommandScriptFile(final SOSOptionInFileName commandScriptFile) {
        this.commandScriptFile = commandScriptFile;
    }

    @Override
    public SOSOptionString getCommandScriptParam() {
        return commandScriptParam;
    }

    @Override
    public void setCommandScriptParam(final SOSOptionString commandScriptParam) {
        this.commandScriptParam = commandScriptParam;
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
    public SOSOptionBoolean getIgnoreError() {
        return ignoreError;
    }

    @Override
    public void setIgnoreError(final SOSOptionBoolean ignoreError) {
        this.ignoreError = ignoreError;
    }

    @Override
    public SOSOptionIntegerArray getIgnoreExitCode() {
        return ignoreExitCode;
    }

    @Override
    public void setIgnoreExitCode(final SOSOptionIntegerArray ignoreExitCode) {
        this.ignoreExitCode = ignoreExitCode;
    }

    @Override
    public SOSOptionBoolean getIgnoreSignal() {
        return ignoreSignal;
    }

    @Override
    public void setIgnoreSignal(final SOSOptionBoolean ignoreSignal) {
        this.ignoreSignal = ignoreSignal;
    }

    @Override
    public SOSOptionBoolean getIgnoreStderr() {
        return ignoreStderr;
    }

    @Override
    public void setIgnoreStderr(final SOSOptionBoolean ignoreStderr) {
        this.ignoreStderr = ignoreStderr;
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
    public SOSOptionString getProxyHost() {
        return proxyHost;
    }

    @Override
    public void setProxyHost(final SOSOptionString proxyHost) {
        this.proxyHost = proxyHost;
    }

    @Override
    public SOSOptionPassword getProxyPassword() {
        return proxyPassword;
    }

    @Override
    public void setProxyPassword(final SOSOptionPassword proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    @Override
    public SOSOptionPortNumber getProxyPort() {
        return proxyPort;
    }

    @Override
    public void setProxyPort(final SOSOptionPortNumber proxyPort) {
        this.proxyPort = proxyPort;
    }

    @Override
    public SOSOptionUserName getProxyUser() {
        return proxyUser;
    }

    @Override
    public void setProxyUser(final SOSOptionUserName proxyUser) {
        this.proxyUser = proxyUser;
    }

    @Override
    public SOSOptionBoolean getSimulateShell() {
        return simulateShell;
    }

    @Override
    public void setSimulateShell(final SOSOptionBoolean simulateShell) {
        this.simulateShell = simulateShell;
    }

    @Override
    public SOSOptionInteger getSimulateShellInactivityTimeout() {
        return simulateShellInactivityTimeout;
    }

    @Override
    public void setSimulateShellInactivityTimeout(final SOSOptionInteger simulateShellInactivityTimeout) {
        this.simulateShellInactivityTimeout = simulateShellInactivityTimeout;
    }

    @Override
    public SOSOptionInteger getSimulateShellLoginTimeout() {
        return simulateShellLoginTimeout;
    }

    @Override
    public void setSimulateShellLoginTimeout(final SOSOptionInteger simulateShellLoginTimeout) {
        this.simulateShellLoginTimeout = simulateShellLoginTimeout;
    }

    @Override
    public SOSOptionString getSimulateShellPromptTrigger() {
        return simulateShellPromptTrigger;
    }

    @Override
    public void setSimulateShellPromptTrigger(final SOSOptionString simulateShellPromptTrigger) {
        this.simulateShellPromptTrigger = simulateShellPromptTrigger;
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
    public SOSOptionBoolean getIgnoreHangupSignal() {
        return ignoreHangupSignal;
    }

    @Override
    public void setIgnoreHangupSignal(final SOSOptionBoolean pIgnoreHangupSignal) {
        ignoreHangupSignal = pIgnoreHangupSignal;
    }

    @Override
    public SOSOptionString getAlternativeAccount() {
        return null;
    }

    @Override
    public SOSOptionHostName getAlternativeHost() {
        return null;
    }

    @Override
    public SOSOptionString getAlternativePassiveMode() {
        return null;
    }

    @Override
    public SOSOptionPassword getAlternativePassword() {
        return null;
    }

    @Override
    public SOSOptionPortNumber getAlternativePort() {
        return null;
    }

    @Override
    public void setAlternativeHost(final SOSOptionHostName pAlternativeHost) {
        //
    }

    @Override
    public void setAlternativePassword(final SOSOptionPassword pAlternativePassword) {
        //
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

    @JSOptionDefinition(name = "strict_hostKey_checking", description = "Check the hostkey against known hosts for SSH", 
            key = "strict_hostKey_checking", type = "SOSOptionBoolen", mandatory = false)
    public SOSOptionBoolean strictHostKeyChecking = new SOSOptionBoolean(this, CLASSNAME + ".strict_hostkey_checking", 
            "Check the hostkey against known hosts for SSH", "false", "false", false);

    public String getStrictHostKeyChecking() {
        return strictHostKeyChecking.getValue();
    }

    public void setStrictHostKeyChecking(final String pstrValue) {
        strictHostKeyChecking.setValue(pstrValue);
    }

    @JSOptionDefinition(name = "proxy_protocol", description = "Proxy protocol", key = "proxy_protocol", type = "SOSOptionProxyProtocol", 
            mandatory = false)
    public SOSOptionProxyProtocol proxyProtocol = new SOSOptionProxyProtocol(this, CLASSNAME + ".proxy_protocol", "Proxy protocol", 
            SOSOptionProxyProtocol.Protocol.http.name(), SOSOptionProxyProtocol.Protocol.http.name(), false);

    public SOSOptionProxyProtocol getProxyProtocol() {
        return proxyProtocol;
    }

    public void setProxyHost(SOSOptionProxyProtocol val) {
        proxyProtocol = val;
    }

}