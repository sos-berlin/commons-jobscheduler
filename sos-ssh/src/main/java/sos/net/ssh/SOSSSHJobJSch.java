package sos.net.ssh;

import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.CredentialStore.Options.SOSCredentialStoreOptions;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionTransferType.enuTransferTypes;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.SFTP.SOSVfsSFtpJCraft;
import com.sos.VirtualFileSystem.common.SOSCommandResult;
import com.sos.VirtualFileSystem.common.SOSVfsEnv;
import com.sos.VirtualFileSystem.common.SOSVfsMessageCodes;
import com.sos.exception.SOSSSHAutoDetectionException;
import com.sos.i18n.annotation.I18NResourceBundle;

import sos.net.ssh.exceptions.SSHConnectionError;
import sos.net.ssh.exceptions.SSHExecutionError;
import sos.net.ssh.exceptions.SSHMissingCommandError;

@I18NResourceBundle(baseName = "com_sos_net_messages", defaultLocale = "en")
public class SOSSSHJobJSch extends SOSSSHJob2 {

    protected ISOSVFSHandler prePostCommandVFSHandler = null;
    protected ISOSVFSHandler vfsHandler;
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSSHJobJSch.class);
    private static final String SCHEDULER_RETURN_VALUES = "SCHEDULER_RETURN_VALUES";
    private static final String DEFAULT_LINUX_DELIMITER = ";";
    private static final String DEFAULT_WINDOWS_DELIMITER = "&";
    private static final String DEFAULT_LINUX_GET_PID_COMMAND = "echo $$";
    private static final String DEFAULT_WINDOWS_GET_PID_COMMAND = "echo Add command to get PID of active shell here!";
    private static final String DEFAULT_WINDOWS_PRE_COMMAND = "set \"%s=%s\"";
    private static final String DEFAULT_LINUX_PRE_COMMAND = "export %s='%s'";
    private static final String DEFAULT_WINDOWS_POST_COMMAND_READ = "if exist \"%s\" type \"%s\"";
    private static final String DEFAULT_LINUX_POST_COMMAND_READ = "test -r %s && cat %s; exit 0";
    private static final String DEFAULT_WINDOWS_POST_COMMAND_DELETE = "del \"%s\"";
    private static final String DEFAULT_LINUX_POST_COMMAND_DELETE = "test -r %s && rm %s; exit 0";
    private String tempFileName;
    private String resolvedTempFileName;
    private String pidFileName;
    private String ssh_job_get_pid_command = DEFAULT_LINUX_GET_PID_COMMAND;
    private Map allParams = null;
    private Map<String, String> returnValues = new HashMap<String, String>();
    private Map schedulerEnvVars;
    private Future<Void> commandExecution;
    private SOSVfsEnv envVars = new SOSVfsEnv();

    @Override
    public ISOSVFSHandler getVFSSSH2Handler() {
        try {
            vfsHandler = VFSFactory.getHandler("SSH2.JSCH");
        } catch (Exception e) {
            throw new JobSchedulerException("SOS-VFS-E-0010: unable to initialize VFS", e);
        }
        return vfsHandler;
    }

    private void openPrePostCommandsSession() {
        try {
            if (!prePostCommandVFSHandler.isConnected()) {
                SOSConnection2OptionsAlternate postAlternateOptions = getAlternateOptions(objOptions);
                postAlternateOptions.raiseExceptionOnError.value(false);
                prePostCommandVFSHandler.connect(postAlternateOptions);
            }
            prePostCommandVFSHandler.authenticate(objOptions);
            LOGGER.debug("connection established");
        } catch (Exception e) {
            throw new SSHConnectionError("Error occured during connection/authentication: " + e.getMessage(), e);
        }
        prePostCommandVFSHandler.setJSJobUtilites(objJSJobUtilities);
    }

    @Override
    public String getTempFileName() {
        return tempFileName;
    }

    @Override
    public StringBuffer getStdErr() throws Exception {
        return vfsHandler.getStdErr();
    }

    @Override
    public StringBuffer getStdOut() throws Exception {
        return vfsHandler.getStdOut();
    }

    @Override
    public SOSSSHJob2 execute() throws Exception {
        clear();
        boolean flgScriptFileCreated = false;
        vfsHandler.setJSJobUtilites(objJSJobUtilities);
        String executedCommand = "";
        ExecutorService executorService = null;
        Future<Void> sendSignalExecution = null;
        try {
            if (!isConnected) {
                this.connect();
            }
            // first check if windows is running on the remote host
            checkOsAndShell();
            flgIsWindowsShell = vfsHandler.remoteIsWindowsShell();
            if (objOptions.command.isNotEmpty()) {
                strCommands2Execute = objOptions.command.values();
            } else {
                if (objOptions.isScript()) {
                    strCommands2Execute = new String[1];
                    String strTemp = objOptions.commandScript.getValue();
                    if (objOptions.commandScript.IsEmpty()) {
                        strTemp = objOptions.commandScriptFile.getJSFile().file2String();
                    }
                    strTemp = objJSJobUtilities.replaceSchedulerVars(strTemp);
                    strCommands2Execute[0] = vfsHandler.createScriptFile(strTemp);
                    add2Files2Delete(strCommands2Execute[0]);
                    flgScriptFileCreated = true;
                    strCommands2Execute[0] += " " + objOptions.commandScriptParam.getValue();
                } else {
                    throw new SSHMissingCommandError(objMsg.getMsg(SOS_SSH_E_100));
                }
            }
            envVars.setGlobalEnvs(new HashMap<String, String>());
            envVars.setLocalEnvs(new HashMap<String, String>());
            setReturnValuesEnvVar();
            for (String strCmd : strCommands2Execute) {
                executedCommand = strCmd;
                LOGGER.debug("createEnvironmentVariables (Options) = " + objOptions.createEnvironmentVariables.value());
                String preCommand = null;
                if (objOptions.createEnvironmentVariables.value()) {
                    if (objOptions.autoDetectOS.value()) {
                        setSOSVfsEnvs();
                    } else if (objOptions.postCommandDelete.getValue().contains("del")) {
                        final boolean oldFlg = flgIsWindowsShell;
                        flgIsWindowsShell = true;
                        setSOSVfsEnvs();
                        // only if autoDetectOS is false flgIsWindowsShell is not trustworthy when callingsetReturnValuesEnvVar();
                        // we have to set the env var explicitely here
                        envVars.getGlobalEnvs().put(SCHEDULER_RETURN_VALUES, resolvedTempFileName);
                        flgIsWindowsShell = oldFlg;
                    } else {
                        preCommand = getPreCommand();
                    }
                }
                try {
                    strCmd = objJSJobUtilities.replaceSchedulerVars(strCmd);
                    LOGGER.info(String.format(objMsg.getMsg(SOS_SSH_D_110), strCmd));
                    vfsHandler.setSimulateShell(objOptions.simulateShell.value());
                    executorService = Executors.newFixedThreadPool(2);
                    String completeCommand = null;
                    if (objOptions.autoDetectOS.value()) {
                        completeCommand = strCmd;
                    } else {
                        if (preCommand != null) {
                            completeCommand = preCommand + strCmd;
                        } else {
                            completeCommand = strCmd;
                        }
                    }
                    final String cmdToExecute = completeCommand;
                    Callable<Void> runCompleteCmd = new Callable<Void>() {

                        @Override
                        public Void call() throws Exception {
                            LOGGER.debug("***** Command Execution started! *****");
                            if (objOptions.autoDetectOS.value()) {
                                vfsHandler.executeCommand(cmdToExecute, envVars);
                            } else if (objOptions.postCommandDelete.getValue().contains("del")) {
                                vfsHandler.executeCommand(cmdToExecute, envVars);
                            } else {
                                vfsHandler.executeCommand(cmdToExecute);
                            }
                            LOGGER.debug("***** Command Execution finished! *****");
                            return null;
                        }
                    };

                    commandExecution = executorService.submit(runCompleteCmd);
                    Callable<Void> sendSignal = new Callable<Void>() {

                        @Override
                        public Void call() throws Exception {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                            }
                            while (!commandExecution.isDone()) {
                                for (int i = 0; i < 600; i++) {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                    }
                                }
                                ((SOSVfsSFtpJCraft) vfsHandler).getChannelExec().sendSignal("CONT");
                            }
                            return null;
                        }
                    };
                    sendSignalExecution = executorService.submit(sendSignal);
                    // wait until command execution is finished
                    commandExecution.get();
                    executorService.shutdownNow();
                    objJSJobUtilities.setJSParam(conExit_code, "0");
                    checkStdOut();
                    checkStdErr();
                    checkExitCode();
                    changeExitSignal();
                } catch (Exception e) {
                    checkStdOut();
                    checkStdErr();
                    checkExitCode();
                    changeExitSignal();
                    if (objOptions.raiseExceptionOnError.value()) {
                        if (objOptions.ignoreError.value()) {
                            if (objOptions.ignoreStderr.value()) {
                                LOGGER.debug(this.stackTrace2String(e));
                            } else {
                                LOGGER.error(this.stackTrace2String(e));
                                throw new SSHExecutionError("Exception raised: " + e, e);
                            }
                        } else {
                            LOGGER.error(this.stackTrace2String(e));
                            throw new SSHExecutionError("Exception raised: " + e, e);
                        }
                    }
                } finally {
                    if (executorService != null) {
                        if (commandExecution != null) {
                            commandExecution.cancel(true);
                        }
                        if (sendSignalExecution != null) {
                            sendSignalExecution.cancel(true);
                        }
                        executorService.shutdownNow();
                    }
                }
            }
            if (resolvedTempFileName != null) {
                processPostCommands(resolvedTempFileName);
            }
        } catch (Exception e) {
            if (objOptions.raiseExceptionOnError.value()) {
                String strErrMsg = "SOS-SSH-E-120: error occurred processing ssh command: \"" + executedCommand + "\"" + e.getMessage() + " " + e
                        .getCause();
                if (objOptions.ignoreError.value()) {
                    if (objOptions.ignoreStderr.value()) {
                        LOGGER.debug(this.stackTrace2String(e));
                        LOGGER.debug(strErrMsg, e);
                    } else {
                        LOGGER.error(this.stackTrace2String(e));
                        LOGGER.error(strErrMsg, e);
                        throw new SSHExecutionError(strErrMsg, e);
                    }
                } else {
                    LOGGER.error(this.stackTrace2String(e));
                    LOGGER.error(strErrMsg, e);
                    throw new SSHExecutionError(strErrMsg, e);
                }
            }
        } finally {
            if (vfsHandler.getStdOut() != null) {
                vfsHandler.getStdOut().setLength(0);
            }
            if (vfsHandler.getStdErr() != null) {
                vfsHandler.getStdErr().setLength(0);
            }
            if (executorService != null) {
                if (commandExecution != null) {
                    commandExecution.cancel(true);
                }
                if (sendSignalExecution != null) {
                    sendSignalExecution.cancel(true);
                }
                executorService.shutdownNow();
            }
            if (keepConnected == false) {
                disconnect();
            }
            if(vfsHandler != null && ((SOSVfsSFtpJCraft) vfsHandler).getChannelExec() != null) {
                ((SOSVfsSFtpJCraft) vfsHandler).getChannelExec().sendSignal("KILL");
            }
        }
        return this;
    }

    @Override
    public void disconnect() {
        if (isConnected) {
            try {
                if (prePostCommandVFSHandler != null) {
                    LOGGER.debug("***** prePostCommandVFSHandler disconnecting... *****");
                    prePostCommandVFSHandler.closeConnection();
                    LOGGER.debug("***** prePostCommandVFSHandler disconnected! *****");
                }
                LOGGER.debug("***** vfsHandler disconnecting... *****");
                vfsHandler.closeConnection();
                LOGGER.debug("***** vfsHandler disconnected! *****");
            } catch (Exception e) {
                throw new SSHConnectionError("problems closing connection", e);
            }
            isConnected = false;
        }
    }

    private void add2Files2Delete(final String fileNameToDelete) {
        if (tempFilesToDelete == null) {
            tempFilesToDelete = new Vector<String>();
        }
        tempFilesToDelete.add(fileNameToDelete);
        LOGGER.debug(String.format(SOSVfsMessageCodes.SOSVfs_D_254.params(fileNameToDelete)));
    }

    @Override
    public SOSSSHJob2 connect() {
        getVFS();
        try {
            SOSConnection2OptionsAlternate alternateOptions = getAlternateOptions(objOptions);
            alternateOptions.checkMandatory();
            vfsHandler.connect(alternateOptions);
            vfsHandler.authenticate(alternateOptions);
            LOGGER.debug("connection established");
        } catch (Exception e) {
            throw new SSHConnectionError("Error occured during connection/authentication: " + e.getMessage(), e);
        }
        isConnected = true;
        preparePostCommandHandler();
        return this;
    }

    @Override
    public void generateTemporaryFilename() {
        UUID uuid = UUID.randomUUID();
        tempFileName = "sos-ssh-return-values-" + uuid + ".txt";
    }

    @Override
    public String getPreCommand() {
        String delimiter;
        String preCommand = objOptions.getPreCommand().getValue();
        if (flgIsWindowsShell) {
            delimiter = DEFAULT_WINDOWS_DELIMITER;
            if (objOptions.getPreCommand().isNotDirty()) {
                preCommand = DEFAULT_WINDOWS_PRE_COMMAND;
            }
        } else {
            delimiter = DEFAULT_LINUX_DELIMITER;
            if (objOptions.getPreCommand().isNotDirty()) {
                preCommand = DEFAULT_LINUX_PRE_COMMAND;
            }
        }
        StringBuilder strb = new StringBuilder();
        if (objOptions.runWithWatchdog.value()) {
            readGetPidCommandFromPropertiesFile();
            strb.append(ssh_job_get_pid_command).append(delimiter).append(ssh_job_get_pid_command);
            strb.append(" >> ").append(pidFileName).append(delimiter);
        }
        if (objOptions.tempDirectory.isDirty()) {
            resolvedTempFileName = resolveTempFileName(objOptions.tempDirectory.getValue(), tempFileName);
        } else {
            resolvedTempFileName = tempFileName;
        }
        strb.append(String.format(preCommand, SCHEDULER_RETURN_VALUES, resolvedTempFileName));
        strb.append(delimiter);
        return strb.toString();
    }

    private void setReturnValuesEnvVar() {
        resolvedTempFileName = null;
        if (objOptions.tempDirectory.isDirty()) {
            resolvedTempFileName = resolveTempFileName(objOptions.tempDirectory.getValue(), tempFileName);
            LOGGER.debug(String.format("*** resolved tempFileName: %1$s", resolvedTempFileName));
        } else {
            resolvedTempFileName = tempFileName;
        }
        if (objOptions.autoDetectOS.value()) {
            if (flgIsWindowsShell) {
                envVars.getGlobalEnvs().put(SCHEDULER_RETURN_VALUES, resolvedTempFileName);
            } else {
                envVars.getLocalEnvs().put(SCHEDULER_RETURN_VALUES, resolvedTempFileName);
            }
        }
    }

    private void setSOSVfsEnvs() {
        if (schedulerEnvVars != null) {
            for (Object key : schedulerEnvVars.keySet()) {
                if (!"SCHEDULER_PARAM_JOBSCHEDULEREVENTJOB.EVENTS".equals(key.toString())) {
                    String envVarValue = schedulerEnvVars.get(key).toString();
                    if (key.toString().contains("()")) {
                        LOGGER.debug("*******************************");
                        LOGGER.debug("  KEY BEFORE REPLACEMENT: " + key);
                        LOGGER.debug("*******************************");
                    }
                    String keyVal = key.toString().replaceAll("\\.|\\(|\\)|%{2}", "_");
                    if (key.toString().contains("()")) {
                        LOGGER.debug("  KEY AFTER REPLACEMENT: " + keyVal);
                        LOGGER.debug("*******************************");
                    }
                    envVarValue = envVarValue.replaceAll("\"", "\\\"");
                    if (!flgIsWindowsShell) {
                        envVarValue = envVarValue.replaceAll("\\\\", "\\\\\\\\");
                        envVars.getLocalEnvs().put(keyVal, envVarValue);
                    } else {
                        envVars.getGlobalEnvs().put(keyVal, envVarValue);
                    }
                }
            }
        }
    }

    private String resolveTempFileName(String tempDir, String filename) {
        if (flgIsWindowsShell) {
            return Paths.get(tempDir, filename).toString().replace('/', '\\');
        } else {
            return Paths.get(tempDir, filename).toString().replace('\\', '/');
        }
    }

    private String getEnvCommand() {
        String delimiter;
        if (flgIsWindowsShell) {
            delimiter = DEFAULT_WINDOWS_DELIMITER;
        } else {
            delimiter = DEFAULT_LINUX_DELIMITER;
        }
        StringBuilder sb = new StringBuilder();
        if (schedulerEnvVars != null) {
            for (Object key : schedulerEnvVars.keySet()) {
                if (!"SCHEDULER_PARAM_JOBSCHEDULEREVENTJOB.EVENTS".equals(key.toString())) {
                    String envVarValue = schedulerEnvVars.get(key).toString();
                    if (key.toString().contains("()")) {
                        LOGGER.debug("*******************************");
                        LOGGER.debug("  KEY BEFORE REPLACEMENT: " + key);
                        LOGGER.debug("*******************************");
                    }
                    String keyVal = key.toString().replaceAll("\\.|\\(|\\)|%{2}|\\p{Space}", "_");
                    if (!key.toString().equalsIgnoreCase(keyVal)) {
                        LOGGER.debug("  KEY AFTER REPLACEMENT: " + keyVal);
                        LOGGER.debug("*******************************");
                    }
                    envVarValue = envVarValue.replaceAll("\"", "\\\"");
                    // do not wrap between ' because it would cause problems under windows,
                    // use the pre-command format instead
                    // envVarValue = "'" + envVarValue + "'";
                    if (!flgIsWindowsShell) {
                        envVarValue = envVarValue.replaceAll("\\\\", "\\\\\\\\");
                    }
                    if (!"SCHEDULER_PARAM_std_out_output".equalsIgnoreCase(keyVal) && !"SCHEDULER_PARAM_std_err_output".equalsIgnoreCase(keyVal)) {
                        sb.append(String.format(objOptions.getPreCommand().getValue(), keyVal.toUpperCase(), envVarValue));
                        sb.append(delimiter);
                    }
                }
            }
        }
        return sb.toString();
    }

    @Override
    public void preparePostCommandHandler() {
        if (prePostCommandVFSHandler == null) {
            try {
                prePostCommandVFSHandler = VFSFactory.getHandler("SSH2.JSCH");
            } catch (Exception e) {
                throw new JobSchedulerException("SOS-VFS-E-0010: unable to initialize second VFS", e);
            }
        }
    }

    @Override
    public void processPostCommands(String tmpFileName) {
        openPrePostCommandsSession();
        String postCommandRead = null;
        if (objOptions.postCommandRead.isDirty()) {
            postCommandRead = String.format(objOptions.postCommandRead.getValue(), tmpFileName);
        } else {
            try {
                if (flgIsWindowsShell) {
                    postCommandRead = String.format(DEFAULT_WINDOWS_POST_COMMAND_READ, tmpFileName, tmpFileName);
                } else {
                    postCommandRead = String.format(DEFAULT_LINUX_POST_COMMAND_READ, tmpFileName, tmpFileName);
                }
            } catch (Exception e) {
            }
        }
        String stdErr = "";
        // try {
        if (tempFilesToDelete != null && !tempFilesToDelete.isEmpty()) {
            for (String tempFileName : tempFilesToDelete) {
                ((SOSVfsSFtpJCraft) vfsHandler).delete(tempFileName);
                LOGGER.debug(SOSVfsMessageCodes.SOSVfs_I_0113.params(tempFileName));
            }
        }
        // } catch (Exception e) {}
        tempFilesToDelete = null;
        try {
            prePostCommandVFSHandler.executeCommand(postCommandRead);
            if (prePostCommandVFSHandler.getExitCode() == 0) {
                if (!prePostCommandVFSHandler.getStdOut().toString().isEmpty()) {
                    BufferedReader reader = new BufferedReader(new StringReader(new String(prePostCommandVFSHandler.getStdOut())));
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        Matcher regExMatcher = Pattern.compile("^([^=]+)=(.*)").matcher(line);
                        if (regExMatcher.find()) {
                            String key = regExMatcher.group(1).trim();
                            String value = regExMatcher.group(2).trim();
                            returnValues.put(key, value);
                        }
                    }
                    String postCommandDelete = null;
                    if (objOptions.postCommandDelete.isDirty()) {
                        postCommandDelete = String.format(objOptions.postCommandDelete.getValue(), tmpFileName);
                    } else {
                        if (flgIsWindowsShell) {
                            postCommandDelete = String.format(DEFAULT_WINDOWS_POST_COMMAND_DELETE, tmpFileName);
                        } else {
                            postCommandDelete = String.format(DEFAULT_LINUX_POST_COMMAND_DELETE, tmpFileName);
                        }
                    }
                    prePostCommandVFSHandler.executeCommand(postCommandDelete);
                }
            }
        } catch (Exception e) {
            // LOGGER.debug(SOSVfsMessageCodes.SOSVfs_D_282.getFullMessage());
        } finally {
            try {
                LOGGER.debug("[processPostCommand] prePostCommandVFSHandler connection closing... *****");
                prePostCommandVFSHandler.closeConnection();
                prePostCommandVFSHandler.closeSession();
                LOGGER.debug("[processPostCommand] prePostCommandVFSHandler connection closed! *****");
            } catch (Exception e) {
                LOGGER.debug("Error closing connection from prePostCommandVFSHandler", e);
            }
            if (prePostCommandVFSHandler.isConnected()) {
                try {
                    prePostCommandVFSHandler.closeConnection();
                    prePostCommandVFSHandler.closeSession();
                } catch (Exception e) {
                    LOGGER.debug("Error closing connection from prePostCommandVFSHandler - second try", e);
                }
            }

        }
    }

    public SOSConnection2OptionsAlternate getAlternateOptions(SOSSSHJobOptions options) {
        SOSConnection2OptionsAlternate alternateOptions = new SOSConnection2OptionsAlternate();
        alternateOptions.strictHostKeyChecking.value(options.strictHostKeyChecking.value());
        alternateOptions.host.setValue(options.getHost().getValue());
        alternateOptions.port.value(options.getPort().value());
        alternateOptions.user.setValue(options.getUser().getValue());
        alternateOptions.password.setValue(options.getPassword().getValue());
        alternateOptions.passphrase.setValue(options.passphrase.getValue());
        alternateOptions.authMethod.setValue(options.authMethod.getValue());
        alternateOptions.authFile.setValue(options.authFile.getValue());
        alternateOptions.protocol.setValue(enuTransferTypes.ssh2);

        alternateOptions.proxyProtocol.setValue(options.getProxyProtocol().getValue());
        alternateOptions.proxyHost.setValue(options.getProxyHost().getValue());
        alternateOptions.proxyPort.value(options.getProxyPort().value());
        alternateOptions.proxyUser.setValue(options.getProxyUser().getValue());
        alternateOptions.proxyPassword.setValue(options.getProxyPassword().getValue());
        alternateOptions.raiseExceptionOnError.value(options.getRaiseExceptionOnError().value());
        alternateOptions.ignoreError.value(options.getIgnoreError().value());

        if (options.credential_store_filename.isNotEmpty()) {
            SOSCredentialStoreOptions csOptions = new SOSCredentialStoreOptions();
            csOptions.useCredentialStore.setValue("true");
            csOptions.credentialStoreFileName.setValue(options.credential_store_filename.getValue());
            csOptions.credentialStoreKeyFileName.setValue(options.credential_store_key_filename.getValue());
            csOptions.credentialStorePassword.setValue(options.credential_store_password.getValue());
            csOptions.credentialStoreKeyPath.setValue(options.credential_store_entry_path.getValue());
            alternateOptions.setCredentialStore(csOptions);
            alternateOptions.checkCredentialStoreOptions();
            
            mapBackOptionsFromCS(alternateOptions);
        }

        if ((objOptions.commandScript.getValue() != null && !objOptions.commandScript.getValue().isEmpty()) || objOptions.commandScriptFile
                .getValue() != null && !objOptions.commandScriptFile.getValue().isEmpty()) {
            alternateOptions.setWithoutSFTPChannel(false);
        } else {
            alternateOptions.setWithoutSFTPChannel(true);
        }
        return alternateOptions;
    }
    
    private void mapBackOptionsFromCS (SOSConnection2OptionsAlternate alternateOptions) {
        objOptions.host.setValue(alternateOptions.host.getValue());
        objOptions.port.setValue(alternateOptions.port.getValue());
        objOptions.user.setValue(alternateOptions.user.getValue());
        objOptions.password.setValue(alternateOptions.password.getValue());
        objOptions.passphrase.setValue(alternateOptions.passphrase.getValue());
        
        objOptions.proxyHost.setValue(alternateOptions.proxyHost.getValue());
        objOptions.proxyPort.setValue(alternateOptions.proxyPort.getValue());
        objOptions.proxyUser.setValue(alternateOptions.proxyUser.getValue());
        objOptions.proxyPassword.setValue(alternateOptions.proxyPassword.getValue());
    }

    public String getPidFileName() {
        return pidFileName;
    }

    public void setPidFileName(String pidFileName) {
        this.pidFileName = pidFileName;
    }

    private void readGetPidCommandFromPropertiesFile() {
        if (objOptions.sshJobGetPidCommand.isDirty() && !objOptions.sshJobGetPidCommand.getValue().isEmpty()) {
            ssh_job_get_pid_command = objOptions.sshJobGetPidCommand.getValue();
            LOGGER.debug("Command to receive PID of the active shell from Job Parameter used!");
        } else {
            if (flgIsWindowsShell) {
                ssh_job_get_pid_command = DEFAULT_WINDOWS_GET_PID_COMMAND;
                LOGGER.debug("Default Windows command used to receive PID of the active shell!");
            } else {
                ssh_job_get_pid_command = DEFAULT_LINUX_GET_PID_COMMAND;
                LOGGER.debug("Default Linux command used to receive PID of the active shell!");
            }
        }
    }

    private void checkOsAndShell() throws SOSSSHAutoDetectionException {
        String cmdToExecute = "uname";
        LOGGER.info("*** Checking for remote Operating System and shell! ***");
        boolean forceAutoDetection = objOptions.autoDetectOS.value();
        if (!forceAutoDetection) {
            LOGGER.info("*** parameter 'auto_detect_os' was set to 'false', only checking without setting commands automatically! ***");
        }
        StringBuilder strb = new StringBuilder();
        strb.append("Can´t detect OS and shell automatically!\r\n");
        strb.append("Set parameter 'auto_os_detection' to false and specify the parameters ");
        strb.append("preCommand, postCommandRead and postCommandDelete according to your remote shell!\r\n");
        strb.append("For further details see knowledge base article https://kb.sos-berlin.com/x/EQaX");
        SOSCommandResult commandResult = null;
        try {
            commandResult = vfsHandler.executePrivateCommand(cmdToExecute);
            if (commandResult.getExitCode() == 0) {
                // command uname was execute successfully -> OS is Unix like
                if (commandResult.getStdOut().toString().toLowerCase().contains("linux") || commandResult.getStdOut().toString().toLowerCase()
                        .contains("darwin") || commandResult.getStdOut().toString().toLowerCase().contains("aix") || commandResult.getStdOut()
                                .toString().toLowerCase().contains("hp-ux") || commandResult.getStdOut().toString().toLowerCase().contains("solaris")
                        || commandResult.getStdOut().toString().toLowerCase().contains("sunos") || commandResult.getStdOut().toString().toLowerCase()
                                .contains("freebsd")) {
                    if (forceAutoDetection) {
                        flgIsWindowsShell = false;
                    }
                    LOGGER.info("*** Command uname was executed successfully, remote OS and shell are Unix like! ***");
                } else if (commandResult.getStdOut().toString().toLowerCase().contains("cygwin")) {
                    // OS is Windows but shell is Unix like
                    // unix commands have to be used
                    LOGGER.info("*** Command uname was executed successfully, remote OS is Windows with cygwin and shell is Unix like! ***");
                    if (forceAutoDetection) {
                        flgIsWindowsShell = false;
                    }
                } else {
                    LOGGER.info("*** Command uname was executed successfully, but the remote OS was not determined, Unix like shell is assumed! ***");
                    if (forceAutoDetection) {
                        flgIsWindowsShell = false;
                    }
                }
            } else if (commandResult.getExitCode() == 9009 || commandResult.getExitCode() == 1) {
                // call of uname under Windows OS delivers exit code 9009 or exit code 1 and target shell cmd.exe
                // the exit code depends on the remote SSH implementation
                if (forceAutoDetection) {
                    flgIsWindowsShell = true;
                }
                LOGGER.info("*** execute Command uname failed with exit code 1 or 9009, remote OS is Windows with cmd shell! ***");
            } else if (commandResult.getExitCode() == 127) {
                // call of uname under Windows OS with CopSSH (cygwin) and target shell /bin/bash delivers exit code 127
                // command uname is not installed by default through CopSSH installation
                LOGGER.info("*** execute Command uname failed with exit code 127, remote OS is Windows with cygwin and shell is Unix like! ***");
                if (forceAutoDetection) {
                    flgIsWindowsShell = false;
                }
            } else {
                if (forceAutoDetection) {
                    throw new SOSSSHAutoDetectionException(strb.toString());
                } else {
                    flgIsWindowsShell = false;
                    LOGGER.info(strb.toString());
                }
            }
        } catch (Exception e) {
            if (forceAutoDetection) {
                throw new SOSSSHAutoDetectionException(strb.toString());
            } else {
                flgIsWindowsShell = false;
                LOGGER.error(strb.toString(), e);
            }
        }
    }

    public void setAllParams(Map allParams) {
        this.allParams = allParams;
    }

    public Map<String, String> getReturnValues() {
        return returnValues;
    }

    public Map getSchedulerEnvVars() {
        return schedulerEnvVars;
    }

    public void setSchedulerEnvVars(Map schedulerEnvVars) {
        this.schedulerEnvVars = schedulerEnvVars;
    }

}
