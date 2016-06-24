package sos.net.ssh;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import sos.net.ssh.exceptions.SSHConnectionError;
import sos.net.ssh.exceptions.SSHExecutionError;
import sos.net.ssh.exceptions.SSHMissingCommandError;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.SFTP.SOSVfsSFtpJCraft;
import com.sos.VirtualFileSystem.common.SOSVfsMessageCodes;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "com_sos_net_messages", defaultLocale = "en")
public class SOSSSHJobJSch extends SOSSSHJob2 {

    protected ISOSVFSHandler prePostCommandVFSHandler = null;
    private static final Logger logger = Logger.getLogger(SOSSSHJobJSch.class);
    private static final String SCHEDULER_RETURN_VALUES = "SCHEDULER_RETURN_VALUES";
    private String tempFileName;
    private String pidFileName;
    private static final String DEFAULT_LINUX_GET_PID_COMMAND = "echo $$";
    private static final String DEFAULT_WINDOWS_GET_PID_COMMAND = "echo Add command to get PID of active shell here!";
    private String ssh_job_get_pid_command = "echo $$";
    protected ISOSVFSHandler vfsHandler;
    private Map allParams = null;
    private Map<String, String> returnValues = new HashMap<String, String>();
    private Map schedulerEnvVars;

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
            logger.debug("connection established");
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
        String completeCommand = "";
        try {
            if (!isConnected) {
                this.connect();
            }
            // first check if windows is running on the remote host
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
            for (String strCmd : strCommands2Execute) {
                executedCommand = strCmd;
                logger.debug("createEnvironmentVariables (Options) = " + objOptions.getCreateEnvironmentVariables().value());
                if (objOptions.getCreateEnvironmentVariables().value()) {
                    completeCommand = getEnvCommand() + getPreCommand() + strCmd;
                } else {
                    completeCommand = getPreCommand() + strCmd;
                }
                try {
                    strCmd = objJSJobUtilities.replaceSchedulerVars(strCmd);
                    logger.debug(String.format(objMsg.getMsg(SOS_SSH_D_110), strCmd));
                    vfsHandler.setSimulateShell(objOptions.simulateShell.value());
                    vfsHandler.executeCommand(completeCommand);
                    objJSJobUtilities.setJSParam(conExit_code, "0");
                    checkStdOut();
                    checkStdErr();
                    checkExitCode();
                    changeExitSignal();
                } catch (Exception e) {
                    if (objOptions.raiseExceptionOnError.value()) {
                        if (objOptions.ignoreError.value()) {
                            if (objOptions.ignoreStderr.value()) {
                                logger.debug(this.stackTrace2String(e));
                            } else {
                                logger.error(this.stackTrace2String(e));
                                throw new SSHExecutionError("Exception raised: " + e, e);
                            }
                        } else {
                            logger.error(this.stackTrace2String(e));
                            throw new SSHExecutionError("Exception raised: " + e, e);
                        }
                    }
                }
            }
            processPostCommands(getTempFileName());
        } catch (Exception e) {
            if (objOptions.raiseExceptionOnError.value()) {
                String strErrMsg =
                        "SOS-SSH-E-120: error occurred processing ssh command: \"" + executedCommand + "\""
                                + "\nSOS-SSH-E-120: full command String: \"" + completeCommand + "\"";
                if (objOptions.ignoreError.value()) {
                    if (objOptions.ignoreStderr.value()) {
                        logger.debug(this.stackTrace2String(e));
                        logger.debug(strErrMsg, e);
                    } else {
                        logger.error(this.stackTrace2String(e));
                        logger.error(strErrMsg, e);
                        throw new SSHExecutionError(strErrMsg, e);
                    }
                } else {
                    logger.error(this.stackTrace2String(e));
                    logger.error(strErrMsg, e);
                    throw new SSHExecutionError(strErrMsg, e);
                }
            }
        } finally {
            vfsHandler.getStdOut().setLength(0);
            vfsHandler.getStdErr().setLength(0);
            if (keepConnected == false) {
                disconnect();
            }
        }
        return this;
    }

    @Override
    public void disconnect() {
        if (isConnected) {
            try {
                vfsHandler.closeConnection();
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
        logger.debug(String.format(SOSVfsMessageCodes.SOSVfs_D_254.params(fileNameToDelete)));
    }

    @Override
    public SOSSSHJob2 connect() {
        getVFS();
        getOptions().checkMandatory();
        try {
            SOSConnection2OptionsAlternate alternateOptions = getAlternateOptions(objOptions);
            vfsHandler.connect(alternateOptions);
            vfsHandler.authenticate(objOptions);
            logger.debug("connection established");
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
        StringBuilder strb = new StringBuilder();
        if (objOptions.runWithWatchdog.value()) {
            readGetPidCommandFromPropertiesFile();
            strb.append(ssh_job_get_pid_command).append(objOptions.commandDelimiter.getValue()).append(ssh_job_get_pid_command);
            strb.append(" >> ").append(pidFileName).append(objOptions.commandDelimiter.getValue());
            strb.append(String.format(objOptions.getPreCommand().getValue(), SCHEDULER_RETURN_VALUES, tempFileName));
            strb.append(objOptions.commandDelimiter.getValue());
            return strb.toString();
        }
        strb.append(ssh_job_get_pid_command).append(objOptions.commandDelimiter.getValue());
        strb.append(String.format(objOptions.getPreCommand().getValue(), SCHEDULER_RETURN_VALUES, tempFileName));
        strb.append(objOptions.commandDelimiter.getValue());
        return strb.toString();
    }

    private String getEnvCommand() {
        StringBuilder sb = new StringBuilder();
        for (Object key : schedulerEnvVars.keySet()) {
            if (!"SCHEDULER_PARAM_JOBSCHEDULEREVENTJOB.EVENTS".equals(key.toString())) {
                String envVarValue = schedulerEnvVars.get(key).toString();
                if(key.toString().contains("()")){
                    logger.debug("*******************************");
                    logger.debug("KEY BEFORE REPLACEMENT: " + key);
                    logger.debug("*******************************");
                }
                String keyVal = key.toString().replaceAll("\\.|\\(|\\)", "_");
                if(key.toString().contains("()")){
                    logger.debug("KEY AFTER REPLACEMENT: " + keyVal);
                    logger.debug("*******************************");
                }
                envVarValue = envVarValue.replaceAll("\"", "\\\"");
                envVarValue = "'" + envVarValue + "'";
                if (!flgIsWindowsShell) {
                    envVarValue = envVarValue.replaceAll("\\\\", "\\\\\\\\");
                }
                if (!"SCHEDULER_PARAM_std_out_output".equalsIgnoreCase(keyVal) && !"SCHEDULER_PARAM_std_err_output".equalsIgnoreCase(keyVal)) {
                    sb.append(String.format(objOptions.getPreCommand().getValue(), keyVal.toUpperCase(), envVarValue));
                    sb.append(objOptions.commandDelimiter.getValue());
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
        String postCommandRead = String.format(objOptions.getPostCommandRead().getValue(), tmpFileName);
        String stdErr = "";
        if (tempFilesToDelete != null && !tempFilesToDelete.isEmpty()) {
            for (String tempFileName : tempFilesToDelete) {
                ((SOSVfsSFtpJCraft) vfsHandler).delete(tempFileName);
                logger.debug(SOSVfsMessageCodes.SOSVfs_I_0113.params(tempFileName));
            }
        }
        tempFilesToDelete = null;
        try {
            prePostCommandVFSHandler.executeCommand(postCommandRead);
            if (prePostCommandVFSHandler.getExitCode() == 0) {
                if (!prePostCommandVFSHandler.getStdOut().toString().isEmpty()) {
                    BufferedReader reader = new BufferedReader(new StringReader(new String(prePostCommandVFSHandler.getStdOut())));
                    String line = null;
                    logger.debug(SOSVfsMessageCodes.SOSVfs_D_284.getFullMessage());
                    while ((line = reader.readLine()) != null) {
                        Matcher regExMatcher = Pattern.compile("^([^=]+)=(.*)").matcher(line);
                        if (regExMatcher.find()) {
                            String key = regExMatcher.group(1).trim();
                            String value = regExMatcher.group(2).trim();
                            returnValues.put(key, value);
                        }
                    }
                    String postCommandDelete = String.format(objOptions.getPostCommandDelete().getValue(), tmpFileName);
                    prePostCommandVFSHandler.executeCommand(postCommandDelete);
                    logger.debug(SOSVfsMessageCodes.SOSVfs_I_0113.params(tmpFileName));
                } else {
                    logger.debug(SOSVfsMessageCodes.SOSVfs_D_280.getFullMessage());
                }
            } else {
                logger.debug(SOSVfsMessageCodes.SOSVfs_D_281.getFullMessage());
                stdErr = prePostCommandVFSHandler.getStdErr().toString();
                if (stdErr.length() > 0) {
                    logger.debug(stdErr);
                }
            }
        } catch (Exception e) {
            logger.debug(SOSVfsMessageCodes.SOSVfs_D_282.getFullMessage());
        }
    }

    public SOSConnection2OptionsAlternate getAlternateOptions(SOSSSHJobOptions options) {
        SOSConnection2OptionsAlternate alternateOptions = new SOSConnection2OptionsAlternate();
        alternateOptions.strictHostKeyChecking.value(options.strictHostKeyChecking.value());
        alternateOptions.host.setValue(options.getHost().getValue());
        alternateOptions.port.value(options.getPort().value());
        alternateOptions.user.setValue(options.getUser().getValue());
        alternateOptions.password.setValue(options.getPassword().getValue());
        alternateOptions.proxyProtocol.setValue(options.getProxyProtocol().getValue());
        alternateOptions.proxyHost.setValue(options.getProxyHost().getValue());
        alternateOptions.proxyPort.value(options.getProxyPort().value());
        alternateOptions.proxyUser.setValue(options.getProxyUser().getValue());
        alternateOptions.proxyPassword.setValue(options.getProxyPassword().getValue());
        alternateOptions.raiseExceptionOnError.value(options.getRaiseExceptionOnError().value());
        alternateOptions.ignoreError.value(options.getIgnoreError().value());
        return alternateOptions;
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
            logger.debug("Command to receive PID of the active shell from Job Parameter used!");
        } else {
            if (flgIsWindowsShell) {
                ssh_job_get_pid_command = DEFAULT_WINDOWS_GET_PID_COMMAND;
                logger.debug("Default Windows command used to receive PID of the active shell!");
            } else {
                ssh_job_get_pid_command = DEFAULT_LINUX_GET_PID_COMMAND;
                logger.debug("Default Linux command used to receive PID of the active shell!");
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
