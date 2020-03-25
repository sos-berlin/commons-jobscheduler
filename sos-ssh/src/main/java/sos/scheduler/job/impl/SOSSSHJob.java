package sos.scheduler.job.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
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
import com.sos.JSHelper.Options.SOSOptionTransferType.TransferTypes;
import com.sos.VirtualFileSystem.Options.SOSDestinationOptions;
import com.sos.VirtualFileSystem.SFTP.SOSVfsSFtpJCraft;
import com.sos.VirtualFileSystem.common.SOSCommandResult;
import com.sos.VirtualFileSystem.common.SOSShellInfo;
import com.sos.VirtualFileSystem.common.SOSShellInfo.Shell;
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
    private SOSDestinationOptions handlerOptions;

    private Map<String, String> returnValues = new HashMap<String, String>();
    private Map<String, String> schedulerEnvVars;
    private List<String> tempFilesToDelete = new ArrayList<String>();
    private StringBuilder stdout;
    private StringBuilder stderr;

    private String returnValuesFileName;
    private String resolvedReturnValuesFileName;
    private String pidFileName;

    private String getPidCommand = DEFAULT_LINUX_GET_PID_COMMAND;
    private boolean isWindowsShell = false;
    private String delimiter;

    public SOSSSHJob() {
        super(new SOSSSHJobOptions());

        handler = new SOSVfsSFtpJCraft();

        UUID uuid = UUID.randomUUID();
        returnValuesFileName = "sos-ssh-return-values-" + uuid + ".txt";
        pidFileName = "sos-ssh-pid-" + uuid + ".txt";

        resetOutput();
    }

    public void execute() throws Exception {
        resetOutput();

        handler.setJSJobUtilites(objJSJobUtilities);

        try {
            connect();

            // first check if windows is running on the remote host
            checkOsAndShell();

            String[] commands = {};
            if (objOptions.command.isNotEmpty()) {
                commands = objOptions.command.values();
            } else {
                if (objOptions.isScript()) {
                    commands = new String[1];
                    String commandScript = objOptions.commandScript.getValue();
                    if (objOptions.commandScript.IsEmpty()) {
                        commandScript = objOptions.commandScriptFile.getJSFile().file2String();
                        LOGGER.info(String.format("[commandScriptFile]%s", objOptions.commandScriptFile.getValue()));
                    }
                    commandScript = objJSJobUtilities.replaceSchedulerVars(commandScript);
                    commands[0] = putCommandScriptFile(commandScript);

                    add2Files2Delete(commands[0]);
                    commands[0] += " " + objOptions.commandScriptParam.getValue();
                } else {
                    throw new SSHMissingCommandError("neither commands nor script(file) specified");
                }
            }

            SOSVfsEnv envVars = new SOSVfsEnv();
            envVars.setGlobalEnvs(new HashMap<String, String>());
            envVars.setLocalEnvs(new HashMap<String, String>());
            setReturnValuesEnvVar(envVars);

            handler.setSimulateShell(objOptions.simulateShell.value());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("createEnvironmentVariables=%s, simulateShell=%s, runWithWatchdog=%s",
                        objOptions.createEnvironmentVariables.value(), handler.isSimulateShell(), objOptions.runWithWatchdog.value()));
            }

            for (String cmd : commands) {
                StringBuilder preCommand = new StringBuilder();

                if (objOptions.runWithWatchdog.value()) {
                    setGetPidCommand();
                    StringBuilder sb = new StringBuilder(getPidCommand);
                    sb.append(" >> ").append(pidFileName).append(delimiter);
                    preCommand.append(sb);

                    add2Files2Delete(pidFileName);
                }
                if (objOptions.createEnvironmentVariables.value()) {
                    if (objOptions.autoDetectOS.value()) {
                        setSOSVfsEnvs(envVars, isWindowsShell);
                    } else if (objOptions.postCommandDelete.getValue().contains("del")) {
                        setSOSVfsEnvs(envVars, true);
                        // only if autoDetectOS is false flgIsWindowsShell is not trustworthy when callingsetReturnValuesEnvVar();
                        // we have to set the env var explicitely here
                        envVars.getGlobalEnvs().put(SCHEDULER_RETURN_VALUES, resolvedReturnValuesFileName);
                    }
                    preCommand = getPreCommand(preCommand);
                }
                if (LOGGER.isDebugEnabled() && preCommand.length() > 0) {
                    LOGGER.debug(String.format("[preCommand]%s", preCommand));
                }

                ExecutorService executorService = null;
                Future<Void> commandExecution = null;
                Future<Void> sendSignalExecution = null;
                Exception exception = null;
                try {
                    cmd = objJSJobUtilities.replaceSchedulerVars(cmd);
                    if (preCommand.length() > 0) {
                        cmd = preCommand.append(cmd).toString();
                    }

                    executorService = Executors.newFixedThreadPool(2);
                    commandExecution = executeCommand(executorService, cmd, envVars);
                    sendSignalExecution = sendSignalCommand(executorService, commandExecution);

                    // wait until command execution is finished
                    commandExecution.get();
                    objJSJobUtilities.setJSParam(PARAM_EXIT_CODE, "0");

                } catch (Exception e) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(String.format("[%s]%s", cmd, e.toString()), e);
                    }
                    exception = e;
                } finally {
                    close(executorService, commandExecution, sendSignalExecution);

                    try {
                        addStdOut();
                        checkStdErr();
                        checkExitCode();
                        changeExitSignal();

                    } catch (Exception ex) {
                        exception = ex;
                    }

                    if (exception != null && objOptions.raiseExceptionOnError.value()) {
                        if (!objOptions.ignoreError.value()) {
                            if (exception instanceof JobSchedulerException) {
                                throw exception;
                            }
                            throw new SSHExecutionError(String.format("[%s]%s", cmd, exception.toString()), exception);
                        }
                        if (!objOptions.ignoreStderr.value()) {
                            if (exception instanceof JobSchedulerException) {
                                throw exception;
                            }
                            throw new SSHExecutionError(String.format("[%s]%s", cmd, exception.toString()), exception);
                        }
                    }
                }
            }
            if (resolvedReturnValuesFileName != null) {
                processPostCommands(resolvedReturnValuesFileName);
            }
        } catch (Exception e) {
            if (objOptions.raiseExceptionOnError.value()) {
                if (objOptions.ignoreError.value()) {
                    LOGGER.debug(e.toString(), e);
                } else {
                    if (e instanceof JobSchedulerException) {
                        throw e;
                    } else {
                        String msg = "SOS-SSH-E-120: error occurred processing ssh command: " + e.getMessage() + " " + e.getCause();
                        throw new SSHExecutionError(msg, e);
                    }
                }
            }
        } finally {
            disconnect();

            handler.resetStdOut();
            handler.resetStdErr();
        }

    }

    private String putCommandScriptFile(String content) throws Exception {
        if (!isWindowsShell) {
            content = content.replaceAll("(?m)\r", "");
        }

        File source = File.createTempFile("sos-ssh-script-", isWindowsShell ? ".cmd" : ".sh");
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(source)));
        out.write(content);
        out.flush();
        out.close();
        source.deleteOnExit();

        LOGGER.info(String.format("[commandScriptFile][tmp file created][%s]%s", source.getCanonicalPath(), content));

        String target = source.getName();
        if (!isWindowsShell) {
            target = "./" + target;
        }
        handler.putFile(source, target, 0700);
        return target;
    }

    private Future<Void> executeCommand(ExecutorService executor, String cmd, SOSVfsEnv envVars) throws Exception {
        Callable<Void> runCompleteCmd = new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                LOGGER.debug("***** Command Execution started! *****:" + cmd);
                if (objOptions.autoDetectOS.value()) {
                    handler.executeCommand(cmd, envVars);
                } else if (objOptions.postCommandDelete.getValue().contains("del")) {
                    handler.executeCommand(cmd, envVars);
                } else {
                    handler.executeCommand(cmd);
                }
                LOGGER.debug("***** Command Execution finished! *****");
                return null;
            }
        };
        return executor.submit(runCompleteCmd);
    }

    private Future<Void> sendSignalCommand(ExecutorService executor, Future<Void> commandExecution) {
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
                    try {
                        if (handler.getChannelExec() != null) {
                            if (handler.getChannelExec().isConnected()) {
                                handler.getChannelExec().sendSignal("CONT");
                                // LOGGER.trace("send signal CONT");
                            } else {
                                LOGGER.trace("[send signal CONT][skip]channel not connected");
                                return null;
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.warn(String.format("[send signal CONT]%s", e.toString()), e);
                    }
                }
                return null;
            }
        };
        return executor.submit(sendSignal);
    }

    private void close(ExecutorService executor, Future<Void> commandExecution, Future<Void> sendSignalExecution) {
        try {
            if (executor != null) {
                if (commandExecution != null) {
                    commandExecution.cancel(true);
                }
                if (sendSignalExecution != null) {
                    sendSignalExecution.cancel(true);
                }
                executor.shutdownNow();
            }
        } catch (Throwable e) {
            LOGGER.warn(e.toString(), e);
        }
    }

    public void connect() {
        connect(null);
    }

    public void connect(TransferTypes type) {
        try {
            if (handlerOptions == null) {
                setHandlerOptions(objOptions, type);
                handlerOptions.checkMandatory();
            } else {
                if (handler.isConnected()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("handler connected");
                    }
                    return;
                }
            }
            handler.connect(handlerOptions);
            handler.authenticate(handlerOptions);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("handler connection established");
            }
        } catch (Exception e) {
            throw new SSHConnectionError("Error occured during connection/authentication: " + e.toString(), e);
        }

    }

    public void disconnect() {
        if (handler.isConnected()) {
            try {
                handler.disconnect();
            } catch (Exception e) {
                throw new SSHConnectionError("problems closing connection", e);
            }
        } else {
            LOGGER.info("not connected, logout useless");
        }
    }

    private void resetOutput() {
        stdout = new StringBuilder();
        stderr = new StringBuilder();
    }

    public void addStdOut() {
        stdout.append(handler.getStdOut());
    }

    public void checkStdErr() {
        stderr.append(handler.getStdErr());

        if (objOptions.raiseExceptionOnError.value()) {
            if (stderr.length() > 0) {
                if (objOptions.ignoreStderr.value()) {
                    LOGGER.info("output to stderr is ignored");
                } else {
                    throw new SSHExecutionError(stderr.toString());
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
                    objJSJobUtilities.setCC(exitCode);
                    if (objOptions.raiseExceptionOnError.isTrue()) {
                        throw new SSHExecutionError("SOS-SSH-E-150: remote command terminated with exit code: " + exitCode);
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

    public StringBuilder getPreCommand(StringBuilder sb) {
        String preCommand = objOptions.getPreCommand().getValue();
        if (objOptions.getPreCommand().isNotDirty()) {
            preCommand = isWindowsShell ? DEFAULT_WINDOWS_PRE_COMMAND : DEFAULT_LINUX_PRE_COMMAND;
        }
        if (objOptions.tempDirectory.isDirty()) {
            resolvedReturnValuesFileName = resolveTempFileName(objOptions.tempDirectory.getValue(), returnValuesFileName);
        } else {
            resolvedReturnValuesFileName = returnValuesFileName;
        }
        sb.append(String.format(preCommand, SCHEDULER_RETURN_VALUES, resolvedReturnValuesFileName));
        sb.append(delimiter);
        return sb;
    }

    private void setReturnValuesEnvVar(SOSVfsEnv envVars) {
        resolvedReturnValuesFileName = null;
        if (objOptions.tempDirectory.isDirty()) {
            resolvedReturnValuesFileName = resolveTempFileName(objOptions.tempDirectory.getValue(), returnValuesFileName);
            LOGGER.debug(String.format("*** resolved tempFileName: %1$s", resolvedReturnValuesFileName));
        } else {
            resolvedReturnValuesFileName = returnValuesFileName;
        }
        if (objOptions.autoDetectOS.value()) {
            if (isWindowsShell) {
                envVars.getGlobalEnvs().put(SCHEDULER_RETURN_VALUES, resolvedReturnValuesFileName);
            } else {
                envVars.getLocalEnvs().put(SCHEDULER_RETURN_VALUES, resolvedReturnValuesFileName);
            }
        }
    }

    private void setSOSVfsEnvs(SOSVfsEnv envVars, boolean isWindowsShell) {
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

    public void deleteTempFiles() {
        if (tempFilesToDelete != null && !tempFilesToDelete.isEmpty()) {
            connect();
            for (String file : tempFilesToDelete) {
                LOGGER.debug("[deleteTempFiles]" + file);

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
                    LOGGER.debug("[deleteTempFiles]" + cmd);
                    handler.executeResultCommand(cmd);
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

        try {
            deleteTempFiles();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[postCommandRead]%s", postCommandRead));
            }

            connect();
            SOSCommandResult result = handler.executeResultCommand(postCommandRead);

            if (result.getExitCode() == 0) {
                if (!result.getStdOut().toString().isEmpty()) {
                    BufferedReader reader = new BufferedReader(new StringReader(new String(result.getStdOut())));
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

                    connect();
                    handler.executeResultCommand(postCommandDelete);
                }
            }
        } catch (Exception e) {
            // prevent Exception to show in case of postCommandDelete errors
            LOGGER.warn(e.toString(), e);

        }
    }

    private void setHandlerOptions(SOSSSHJobOptions jobOptions, TransferTypes protocol) {
        handlerOptions = new SOSDestinationOptions();
        handlerOptions.strictHostKeyChecking.value(jobOptions.strictHostKeyChecking.value());
        handlerOptions.host.setValue(jobOptions.getHost().getValue());
        handlerOptions.port.value(jobOptions.getPort().value());
        handlerOptions.user.setValue(jobOptions.getUser().getValue());
        handlerOptions.password.setValue(jobOptions.getPassword().getValue());
        handlerOptions.passphrase.setValue(jobOptions.passphrase.getValue());
        handlerOptions.authMethod.setValue(jobOptions.authMethod.getValue());
        handlerOptions.authFile.setValue(jobOptions.authFile.getValue());

        handlerOptions.proxyProtocol.setValue(jobOptions.getProxyProtocol().getValue());
        handlerOptions.proxyHost.setValue(jobOptions.getProxyHost().getValue());
        handlerOptions.proxyPort.value(jobOptions.getProxyPort().value());
        handlerOptions.proxyUser.setValue(jobOptions.getProxyUser().getValue());
        handlerOptions.proxyPassword.setValue(jobOptions.getProxyPassword().getValue());
        handlerOptions.raiseExceptionOnError.value(jobOptions.getRaiseExceptionOnError().value());
        handlerOptions.ignoreError.value(jobOptions.getIgnoreError().value());

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

        if (protocol == null) {
            if ((objOptions.commandScript.getValue() != null && !objOptions.commandScript.getValue().isEmpty()) || (objOptions.commandScriptFile
                    .getValue() != null && !objOptions.commandScriptFile.getValue().isEmpty())) {
                handlerOptions.protocol.setValue(TransferTypes.sftp);
            } else {
                handlerOptions.protocol.setValue(TransferTypes.ssh);
            }
        } else {
            handlerOptions.protocol.setValue(protocol);
        }
    }

    private void mapBackOptionsFromCS(SOSDestinationOptions options) {
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

    private void setGetPidCommand() {
        if (objOptions.sshJobGetPidCommand.isDirty() && !objOptions.sshJobGetPidCommand.getValue().isEmpty()) {
            getPidCommand = objOptions.sshJobGetPidCommand.getValue();
        } else {
            if (isWindowsShell) {
                getPidCommand = DEFAULT_WINDOWS_GET_PID_COMMAND;
            } else {
                getPidCommand = DEFAULT_LINUX_GET_PID_COMMAND;
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[getPidCommand]%s", getPidCommand));
        }
    }

    private void checkOsAndShell() throws SOSSSHAutoDetectionException {
        LOGGER.info("*** Checking for remote Operating System and shell! ***");
        boolean forceAutoDetection = objOptions.autoDetectOS.value();
        if (!forceAutoDetection) {
            LOGGER.info("*** parameter 'auto_detect_os' was set to 'false', only checking without setting commands automatically! ***");
        }
        SOSShellInfo info = handler.getShellInfo();

        StringBuilder sb = new StringBuilder();
        sb.append("Can´t detect OS and shell automatically!\r\n");
        sb.append("Set parameter 'auto_os_detection' to false and specify the parameters ");
        sb.append("preCommand, postCommandRead and postCommandDelete according to your remote shell!\r\n");
        sb.append("For further details see knowledge base article https://kb.sos-berlin.com/x/EQaX");

        if (info.getCommandError() != null) {
            sb.append("\r\n").append(info.getCommandError().toString());
            if (forceAutoDetection) {
                throw new SOSSSHAutoDetectionException(sb.toString(), info.getCommandError());
            }
            info.setShell(Shell.UNIX);
        } else if (info.getShell().equals(Shell.UNKNOWN)) {
            if (forceAutoDetection) {
                throw new SOSSSHAutoDetectionException(sb.toString());
            } else {
                LOGGER.info(sb.toString());
            }
            info.setShell(Shell.UNIX);
        } else {
            LOGGER.info(info.toString());
        }

        isWindowsShell = info.getShell().equals(Shell.WINDOWS);
        delimiter = isWindowsShell ? DEFAULT_WINDOWS_DELIMITER : DEFAULT_LINUX_DELIMITER;
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

    public boolean isWindowsShell() {
        return isWindowsShell;
    }

    public String getPidFileName() {
        return pidFileName;
    }
}
