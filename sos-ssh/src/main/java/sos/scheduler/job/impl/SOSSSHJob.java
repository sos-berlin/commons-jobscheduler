package sos.scheduler.job.impl;

import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.CredentialStore.Options.SOSCredentialStoreOptions;
import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionTransferType.enuTransferTypes;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.SFTP.SOSVfsSFtpJCraft;
import com.sos.VirtualFileSystem.common.SOSCommandResult;
import com.sos.VirtualFileSystem.common.SOSVfsEnv;
import com.sos.exception.SOSSSHAutoDetectionException;

import sos.net.ssh.SOSSSHJobOptions;
import sos.net.ssh.exceptions.SSHConnectionError;
import sos.net.ssh.exceptions.SSHExecutionError;
import sos.net.ssh.exceptions.SSHMissingCommandError;
import sos.util.SOSString;

public class SOSSSHJob extends JSJobUtilitiesClass<SOSSSHJobOptions> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSSHJob.class);

    public static final String PARAM_EXIT_SIGNAL = "exit_signal";
    public static final String PARAM_EXIT_CODE = "exit_code";
    public static final String PARAM_PIDS_TO_KILL = "PIDS_TO_KILL";

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

    private SOSVfsSFtpJCraft handler;
    private SOSVfsSFtpJCraft prePostCommandHandler = null;
    private SOSConnection2OptionsAlternate handlerOptions;
    private SOSConnection2OptionsAlternate prePostCommandHandlerOptions;
    private SOSVfsEnv envVars = new SOSVfsEnv();
    private Future<Void> commandExecution;

    private Map<String, String> returnValues = new HashMap<String, String>();
    private Map<String, String> schedulerEnvVars;
    private List<String> tempFilesToDelete = new ArrayList<String>();
    private StringBuilder stdout;
    private StringBuilder stderr;
    private String[] commands = {};

    private String tempFileName;
    private String resolvedTempFileName;
    private String pidFileName;
    private String getPidCommand = DEFAULT_LINUX_GET_PID_COMMAND;
    private boolean isWindowsShell = false;
    private boolean disableRaiseException = false;

    public SOSSSHJob() {
        super(new SOSSSHJobOptions());

        init();
        disableRaiseException(false);
    }

    public void execute() throws Exception {
        clearOutput();

        handler.setJSJobUtilites(objJSJobUtilities);
        String executedCommand = "";
        ExecutorService executorService = null;
        Future<Void> sendSignalExecution = null;
        try {
            connect();

            // first check if windows is running on the remote host
            checkOsAndShell();
            isWindowsShell = handler.remoteIsWindowsShell();
            if (objOptions.command.isNotEmpty()) {
                commands = objOptions.command.values();
            } else {
                if (objOptions.isScript()) {
                    commands = new String[1];
                    String commandScript = objOptions.commandScript.getValue();
                    if (objOptions.commandScript.IsEmpty()) {
                        commandScript = objOptions.commandScriptFile.getJSFile().file2String();
                    }
                    commandScript = objJSJobUtilities.replaceSchedulerVars(commandScript);
                    commands[0] = handler.createScriptFile(commandScript);
                    add2Files2Delete(commands[0]);
                    commands[0] += " " + objOptions.commandScriptParam.getValue();
                } else {
                    throw new SSHMissingCommandError("neither commands nor script(file) specified");
                }
            }
            envVars.setGlobalEnvs(new HashMap<String, String>());
            envVars.setLocalEnvs(new HashMap<String, String>());
            setReturnValuesEnvVar();
            for (String cmd : commands) {
                executedCommand = cmd;
                LOGGER.debug("createEnvironmentVariables (Options) = " + objOptions.createEnvironmentVariables.value());
                String preCommand = null;
                if (objOptions.createEnvironmentVariables.value()) {
                    if (objOptions.autoDetectOS.value()) {
                        setSOSVfsEnvs();
                    } else if (objOptions.postCommandDelete.getValue().contains("del")) {
                        final boolean oldFlg = isWindowsShell;
                        isWindowsShell = true;
                        setSOSVfsEnvs();
                        // only if autoDetectOS is false flgIsWindowsShell is not trustworthy when callingsetReturnValuesEnvVar();
                        // we have to set the env var explicitely here
                        envVars.getGlobalEnvs().put(SCHEDULER_RETURN_VALUES, resolvedTempFileName);
                        isWindowsShell = oldFlg;
                    } else {
                        preCommand = getPreCommand();
                    }
                }
                try {
                    cmd = objJSJobUtilities.replaceSchedulerVars(cmd);
                    LOGGER.info(String.format("executing remote command: %s", cmd));
                    handler.setSimulateShell(objOptions.simulateShell.value());
                    executorService = Executors.newFixedThreadPool(2);
                    String completeCommand = null;
                    if (objOptions.autoDetectOS.value()) {
                        completeCommand = cmd;
                    } else {
                        if (preCommand != null) {
                            completeCommand = preCommand + cmd;
                        } else {
                            completeCommand = cmd;
                        }
                    }
                    final String cmdToExecute = completeCommand;
                    Callable<Void> runCompleteCmd = new Callable<Void>() {

                        @Override
                        public Void call() throws Exception {
                            LOGGER.debug("***** Command Execution started! *****");
                            if (objOptions.autoDetectOS.value()) {
                                handler.executeCommand(cmdToExecute, envVars);
                            } else if (objOptions.postCommandDelete.getValue().contains("del")) {
                                handler.executeCommand(cmdToExecute, envVars);
                            } else {
                                handler.executeCommand(cmdToExecute);
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
                                Thread.sleep(1_000);
                            } catch (InterruptedException e) {
                            }
                            while (!commandExecution.isDone()) {
                                for (int i = 0; i < 10; i++) {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                    }
                                }
                                handler.getChannelExec().sendSignal("CONT");
                            }
                            return null;
                        }
                    };
                    sendSignalExecution = executorService.submit(sendSignal);
                    // wait until command execution is finished
                    commandExecution.get();
                    objJSJobUtilities.setJSParam(PARAM_EXIT_CODE, "0");
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
                                LOGGER.debug(stackTrace2String(e));
                            } else {
                                LOGGER.error(stackTrace2String(e));
                                throw new SSHExecutionError("Exception raised: " + e, e);
                            }
                        } else {
                            LOGGER.error(stackTrace2String(e));
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
                String msg = "SOS-SSH-E-120: error occurred processing ssh command: \"" + executedCommand + "\"" + e.getMessage() + " " + e
                        .getCause();
                if (objOptions.ignoreError.value()) {
                    if (objOptions.ignoreStderr.value()) {
                        LOGGER.debug(stackTrace2String(e));
                    } else {
                        LOGGER.error(stackTrace2String(e));
                        throw new SSHExecutionError(msg, e);
                    }
                } else {
                    LOGGER.error(msg, e);
                    throw new SSHExecutionError(msg, e);
                }
            }
        } finally {
            disconnect();

            if (handler.getStdOut() != null) {
                handler.getStdOut().setLength(0);
            }
            if (handler.getStdErr() != null) {
                handler.getStdErr().setLength(0);
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
        }

    }

    public void connect() {
        try {
            if (!handler.isConnected()) {
                if (handlerOptions == null) {
                    setHandlerOptions(objOptions);
                    handlerOptions.checkMandatory();
                }
                handler.connect(handlerOptions);
                handler.authenticate(handlerOptions);

                isWindowsShell = handler.remoteIsWindowsShell();

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("handler connection established. isWindowsShell=%s", isWindowsShell));
                }
            }
        } catch (Exception e) {
            throw new SSHConnectionError("Error occured during handler connection/authentication: " + e.toString(), e);
        }

    }

    private void connectPrePostCommandHandler() {
        try {
            if (!prePostCommandHandler.isConnected()) {
                if (prePostCommandHandlerOptions == null) {
                    if (disableRaiseException) {
                        prePostCommandHandlerOptions = handlerOptions;
                    } else {
                        setPrePostCommandHandlerOptionsHandlerOptions();
                    }
                }
                prePostCommandHandler.connect(prePostCommandHandlerOptions);
                prePostCommandHandler.authenticate(prePostCommandHandlerOptions);
                LOGGER.debug("prePostCommandHandler connection established");
            }
        } catch (Exception e) {
            throw new SSHConnectionError("Error occured during prePostCommandHandler connection/authentication: " + e.toString(), e);
        }
    }

    public void disconnect() {
        if (prePostCommandHandler.isConnected()) {
            try {
                prePostCommandHandler.disconnect();
                LOGGER.debug("***** prePostCommandHandler disconnected! *****");
            } catch (Exception e) {
                throw new SSHConnectionError("problems closing connection", e);
            }
        }
        if (handler.isConnected()) {
            try {
                handler.disconnect();
                LOGGER.debug("***** handler disconnected! *****");
            } catch (Exception e) {
                throw new SSHConnectionError("problems closing connection", e);
            }
        }
    }

    private void init() {
        handler = new SOSVfsSFtpJCraft();
        prePostCommandHandler = new SOSVfsSFtpJCraft();

        tempFileName = "sos-ssh-return-values-" + UUID.randomUUID() + ".txt";
    }

    private void clearOutput() {
        stdout = new StringBuilder();
        stderr = new StringBuilder();
    }

    public void checkStdOut() {
        try {
            stdout.append(handler.getStdOut());
        } catch (Exception e) {
            LOGGER.error(stackTrace2String(e));
            throw new JobSchedulerException(e.toString(), e);
        }
    }

    public void checkStdErr() {
        try {
            stderr.append(handler.getStdErr());
        } catch (Exception e) {
            throw new JobSchedulerException(e.getMessage(), e);
        }
        if (stderr.length() > 0) {
            if (objOptions.ignoreStderr.value()) {
                LOGGER.info("[output to stderr is ignored]" + stderr);
            } else {
                String msg = "[remote execution reports error]" + stderr;
                LOGGER.error(msg);
                if (objOptions.raiseExceptionOnError.value()) {
                    throw new SSHExecutionError(msg);
                }
            }
        }
    }

    public Integer checkExitCode() {
        objJSJobUtilities.setJSParam("exit_code_ignored", "false");

        Integer exitCode = handler.getExitCode();
        if (isNotNull(exitCode)) {
            objJSJobUtilities.setJSParam(PARAM_EXIT_CODE, exitCode.toString());
            if (!exitCode.equals(new Integer(0))) {
                if (objOptions.ignoreError.isTrue() || objOptions.ignoreExitCode.getValues().contains(exitCode)) {
                    LOGGER.info("SOS-SSH-E-140: exit code is ignored due to option-settings: " + exitCode);

                    objJSJobUtilities.setJSParam("exit_code_ignored", "true");
                } else {
                    if (objOptions.raiseExceptionOnError.value()) {
                        if (stdout.length() > 0) {
                            LOGGER.info(stdout.toString());
                        }
                    }
                    String msg = "SOS-SSH-E-150: remote command terminated with exit code: " + exitCode;
                    objJSJobUtilities.setCC(exitCode);
                    if (objOptions.raiseExceptionOnError.isTrue()) {
                        if (objOptions.ignoreError.value()) {
                            LOGGER.info(msg);
                        } else {
                            LOGGER.error(msg);
                        }
                        throw new SSHExecutionError(msg);
                    }
                }
            }
        }
        return exitCode;
    }

    public String changeExitSignal() {
        String signal = handler.getExitSignal();
        if (isNotEmpty(signal)) {
            objJSJobUtilities.setJSParam(PARAM_EXIT_SIGNAL, signal);

            if (objOptions.ignoreSignal.isTrue()) {
                LOGGER.info("SOS-SSH-I-130: exit signal is ignored due to option-settings: " + signal);
            } else {
                throw new SSHExecutionError("SOS-SSH-E-140: remote command terminated with exit signal: " + signal);
            }
        } else {
            objJSJobUtilities.setJSParam(PARAM_EXIT_SIGNAL, "");
        }
        return signal;
    }

    public void add2Files2Delete(final String file) {
        if (!SOSString.isEmpty(file)) {
            tempFilesToDelete.add(file);
            LOGGER.debug(String.format("file %s marked for deletion", file));
        }
    }

    public String getPreCommand() {
        String delimiter;
        String preCommand = objOptions.getPreCommand().getValue();
        if (isWindowsShell) {
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
            strb.append(getPidCommand).append(delimiter).append(getPidCommand);
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
            if (isWindowsShell) {
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
                    if (!isWindowsShell) {
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
        if (isWindowsShell) {
            return Paths.get(tempDir, filename).toString().replace('/', '\\');
        } else {
            return Paths.get(tempDir, filename).toString().replace('\\', '/');
        }
    }

    public void deleteTempFiles(SOSVfsSFtpJCraft handler) {
        if (tempFilesToDelete != null && !tempFilesToDelete.isEmpty()) {
            for (String file : tempFilesToDelete) {
                LOGGER.debug("file to delete: " + file);

                String cmd = null;
                if (objOptions.postCommandDelete.isDirty()) {
                    cmd = String.format(objOptions.postCommandDelete.getValue(), file);
                } else {
                    if (isWindowsShell) {
                        cmd = String.format(DEFAULT_WINDOWS_POST_COMMAND_DELETE, file);
                    } else {
                        cmd = String.format(DEFAULT_LINUX_POST_COMMAND_DELETE, file, file);
                    }
                }
                try {
                    LOGGER.debug("cmd: " + cmd);
                    handler.executeCommand(cmd);
                } catch (Exception e) {
                    LOGGER.error(String.format("error ocurred deleting %1$s: ", file), e);
                }
            }
            tempFilesToDelete.clear();
        }
    }

    public void processPostCommands(String tmpFileName) {
        String postCommandRead = null;
        if (objOptions.postCommandRead.isDirty()) {
            postCommandRead = String.format(objOptions.postCommandRead.getValue(), tmpFileName);
        } else {
            try {
                if (isWindowsShell) {
                    postCommandRead = String.format(DEFAULT_WINDOWS_POST_COMMAND_READ, tmpFileName, tmpFileName);
                } else {
                    postCommandRead = String.format(DEFAULT_LINUX_POST_COMMAND_READ, tmpFileName, tmpFileName);
                }
            } catch (Exception e) {
            }
        }

        connectPrePostCommandHandler();
        deleteTempFiles(prePostCommandHandler);
        try {
            prePostCommandHandler.executeCommand(postCommandRead);
            if (prePostCommandHandler.getExitCode() == 0) {
                if (!prePostCommandHandler.getStdOut().toString().isEmpty()) {
                    BufferedReader reader = new BufferedReader(new StringReader(new String(prePostCommandHandler.getStdOut())));
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        Matcher regExMatcher = Pattern.compile("^([^=]+)=(.*)").matcher(line);
                        if (regExMatcher.find()) {
                            String key = regExMatcher.group(1).trim();
                            String value = regExMatcher.group(2).trim();
                            returnValues.put(key, value);
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug(String.format("[return value]%s=%s", key, value));
                            }
                        }
                    }
                    String postCommandDelete = null;
                    if (objOptions.postCommandDelete.isDirty()) {
                        postCommandDelete = String.format(objOptions.postCommandDelete.getValue(), tmpFileName);
                    } else {
                        if (isWindowsShell) {
                            postCommandDelete = String.format(DEFAULT_WINDOWS_POST_COMMAND_DELETE, tmpFileName);
                        } else {
                            postCommandDelete = String.format(DEFAULT_LINUX_POST_COMMAND_DELETE, tmpFileName);
                        }
                    }
                    prePostCommandHandler.executeCommand(postCommandDelete);
                }
            }
        } catch (Exception e) {
            // prevent Exception to show in case of postCommandDelete errors
        } finally {
            try {
                prePostCommandHandler.disconnect();
                LOGGER.debug("[processPostCommand] prePostCommandVFSHandler connection closed! *****");
            } catch (Exception e) {
                LOGGER.warn(e.toString(), e);
            }
        }
    }

    private void setHandlerOptions(SOSSSHJobOptions jobOptions) {
        handlerOptions = new SOSConnection2OptionsAlternate();
        handlerOptions.strictHostKeyChecking.value(jobOptions.strictHostKeyChecking.value());
        handlerOptions.host.setValue(jobOptions.getHost().getValue());
        handlerOptions.port.value(jobOptions.getPort().value());
        handlerOptions.user.setValue(jobOptions.getUser().getValue());
        handlerOptions.password.setValue(jobOptions.getPassword().getValue());
        handlerOptions.passphrase.setValue(jobOptions.passphrase.getValue());
        handlerOptions.authMethod.setValue(jobOptions.authMethod.getValue());
        handlerOptions.authFile.setValue(jobOptions.authFile.getValue());
        handlerOptions.protocol.setValue(enuTransferTypes.ssh2);

        handlerOptions.proxyProtocol.setValue(jobOptions.getProxyProtocol().getValue());
        handlerOptions.proxyHost.setValue(jobOptions.getProxyHost().getValue());
        handlerOptions.proxyPort.value(jobOptions.getProxyPort().value());
        handlerOptions.proxyUser.setValue(jobOptions.getProxyUser().getValue());
        handlerOptions.proxyPassword.setValue(jobOptions.getProxyPassword().getValue());
        if (disableRaiseException) {
            handlerOptions.raiseExceptionOnError.value(false);
            handlerOptions.ignoreError.value(true);
        } else {
            handlerOptions.raiseExceptionOnError.value(jobOptions.getRaiseExceptionOnError().value());
            handlerOptions.ignoreError.value(jobOptions.getIgnoreError().value());
        }

        if (jobOptions.credential_store_filename.isNotEmpty()) {
            SOSCredentialStoreOptions csOptions = new SOSCredentialStoreOptions();
            csOptions.useCredentialStore.setValue("true");
            csOptions.credentialStoreFileName.setValue(jobOptions.credential_store_filename.getValue());
            csOptions.credentialStoreKeyFileName.setValue(jobOptions.credential_store_key_filename.getValue());
            csOptions.credentialStorePassword.setValue(jobOptions.credential_store_password.getValue());
            csOptions.credentialStoreKeyPath.setValue(jobOptions.credential_store_entry_path.getValue());
            handlerOptions.setCredentialStore(csOptions);
            handlerOptions.checkCredentialStoreOptions();

            mapBackOptionsFromCS(handlerOptions);
        }

        if ((objOptions.commandScript.getValue() != null && !objOptions.commandScript.getValue().isEmpty()) || objOptions.commandScriptFile
                .getValue() != null && !objOptions.commandScriptFile.getValue().isEmpty()) {
            handlerOptions.setWithoutSFTPChannel(false);
        } else {
            handlerOptions.setWithoutSFTPChannel(true);
        }
    }

    private void setPrePostCommandHandlerOptionsHandlerOptions() {
        prePostCommandHandlerOptions = new SOSConnection2OptionsAlternate();
        prePostCommandHandlerOptions.strictHostKeyChecking.value(handlerOptions.strictHostKeyChecking.value());
        prePostCommandHandlerOptions.host.setValue(handlerOptions.host.getValue());
        prePostCommandHandlerOptions.port.value(handlerOptions.port.value());
        prePostCommandHandlerOptions.user.setValue(handlerOptions.user.getValue());
        prePostCommandHandlerOptions.password.setValue(handlerOptions.password.getValue());
        prePostCommandHandlerOptions.passphrase.setValue(handlerOptions.passphrase.getValue());
        prePostCommandHandlerOptions.authMethod.setValue(handlerOptions.authMethod.getValue());
        prePostCommandHandlerOptions.authFile.setValue(handlerOptions.authFile.getValue());
        prePostCommandHandlerOptions.protocol.setValue(handlerOptions.protocol.getValue());

        prePostCommandHandlerOptions.proxyProtocol.setValue(handlerOptions.proxyProtocol.getValue());
        prePostCommandHandlerOptions.proxyHost.setValue(handlerOptions.proxyHost.getValue());
        prePostCommandHandlerOptions.proxyPort.value(handlerOptions.proxyPort.value());
        prePostCommandHandlerOptions.proxyUser.setValue(handlerOptions.proxyUser.getValue());
        prePostCommandHandlerOptions.proxyPassword.setValue(handlerOptions.proxyPassword.getValue());

        prePostCommandHandlerOptions.setWithoutSFTPChannel(handlerOptions.getWithoutSFTPChannel().value());

        handlerOptions.raiseExceptionOnError.value(false);
        handlerOptions.ignoreError.value(true);

        // credential store options already resolved by handler
    }

    private void mapBackOptionsFromCS(SOSConnection2OptionsAlternate options) {
        objOptions.host.setValue(options.host.getValue());
        objOptions.port.setValue(options.port.getValue());
        objOptions.user.setValue(options.user.getValue());
        objOptions.password.setValue(options.password.getValue());
        objOptions.passphrase.setValue(options.passphrase.getValue());

        objOptions.proxyHost.setValue(options.proxyHost.getValue());
        objOptions.proxyPort.setValue(options.proxyPort.getValue());
        objOptions.proxyUser.setValue(options.proxyUser.getValue());
        objOptions.proxyPassword.setValue(options.proxyPassword.getValue());
    }

    private void readGetPidCommandFromPropertiesFile() {
        if (objOptions.sshJobGetPidCommand.isDirty() && !objOptions.sshJobGetPidCommand.getValue().isEmpty()) {
            getPidCommand = objOptions.sshJobGetPidCommand.getValue();
            LOGGER.debug("Command to receive PID of the active shell from Job Parameter used!");
        } else {
            if (isWindowsShell) {
                getPidCommand = DEFAULT_WINDOWS_GET_PID_COMMAND;
                LOGGER.debug("Default Windows command used to receive PID of the active shell!");
            } else {
                getPidCommand = DEFAULT_LINUX_GET_PID_COMMAND;
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
            commandResult = handler.executePrivateCommand(cmdToExecute);
            if (commandResult.getExitCode() == 0) {
                // command uname was execute successfully -> OS is Unix like
                if (commandResult.getStdOut().toString().toLowerCase().contains("linux") || commandResult.getStdOut().toString().toLowerCase()
                        .contains("darwin") || commandResult.getStdOut().toString().toLowerCase().contains("aix") || commandResult.getStdOut()
                                .toString().toLowerCase().contains("hp-ux") || commandResult.getStdOut().toString().toLowerCase().contains("solaris")
                        || commandResult.getStdOut().toString().toLowerCase().contains("sunos") || commandResult.getStdOut().toString().toLowerCase()
                                .contains("freebsd")) {
                    if (forceAutoDetection) {
                        isWindowsShell = false;
                    }
                    LOGGER.info("*** Command uname was executed successfully, remote OS and shell are Unix like! ***");
                } else if (commandResult.getStdOut().toString().toLowerCase().contains("cygwin")) {
                    // OS is Windows but shell is Unix like
                    // unix commands have to be used
                    LOGGER.info("*** Command uname was executed successfully, remote OS is Windows with cygwin and shell is Unix like! ***");
                    if (forceAutoDetection) {
                        isWindowsShell = false;
                    }
                } else {
                    LOGGER.info("*** Command uname was executed successfully, but the remote OS was not determined, Unix like shell is assumed! ***");
                    if (forceAutoDetection) {
                        isWindowsShell = false;
                    }
                }
            } else if (commandResult.getExitCode() == 9009 || commandResult.getExitCode() == 1) {
                // call of uname under Windows OS delivers exit code 9009 or exit code 1 and target shell cmd.exe
                // the exit code depends on the remote SSH implementation
                if (forceAutoDetection) {
                    isWindowsShell = true;
                }
                LOGGER.info("*** execute Command uname failed with exit code 1 or 9009, remote OS is Windows with cmd shell! ***");
            } else if (commandResult.getExitCode() == 127) {
                // call of uname under Windows OS with CopSSH (cygwin) and target shell /bin/bash delivers exit code 127
                // command uname is not installed by default through CopSSH installation
                LOGGER.info("*** execute Command uname failed with exit code 127, remote OS is Windows with cygwin and shell is Unix like! ***");
                if (forceAutoDetection) {
                    isWindowsShell = false;
                }
            } else {
                if (forceAutoDetection) {
                    throw new SOSSSHAutoDetectionException(strb.toString());
                } else {
                    isWindowsShell = false;
                    LOGGER.info(strb.toString());
                }
            }
        } catch (Exception e) {
            if (forceAutoDetection) {
                throw new SOSSSHAutoDetectionException(strb.toString());
            } else {
                isWindowsShell = false;
                LOGGER.error(strb.toString(), e);
            }
        }
    }

    public List<Integer> getParamPids() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("PIDs from param: " + objOptions.getItem(PARAM_PIDS_TO_KILL));
        }

        String[] param = null;
        if (objOptions.getItem(PARAM_PIDS_TO_KILL) != null && objOptions.getItem(PARAM_PIDS_TO_KILL).length() > 0) {
            param = objOptions.getItem(PARAM_PIDS_TO_KILL).split(",");
        }
        List<Integer> pids = new ArrayList<Integer>();
        if (param != null) {
            for (String pid : param) {
                if (pid != null && !pid.isEmpty()) {
                    pids.add(Integer.parseInt(pid));
                } else {
                    LOGGER.debug("PID is empty!");
                }
            }
        }
        return pids;
    }

    public Map<String, String> getReturnValues() {
        return returnValues;
    }

    public void setSchedulerEnvVars(Map<String, String> val) {
        schedulerEnvVars = val;
    }

    public SOSVfsSFtpJCraft getHandler() {
        return handler;
    }

    public SOSVfsSFtpJCraft getPrePostCommandHandler() {
        return prePostCommandHandler;
    }

    public boolean isWindowsShell() {
        return isWindowsShell;
    }

    public String getPidFileName() {
        return pidFileName;
    }

    public void setPidFileName(String val) {
        pidFileName = val;
    }

    public void disableRaiseException(boolean val) {
        disableRaiseException = val;
    }
}
