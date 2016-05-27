package sos.scheduler.job;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import sos.net.ssh.SOSSSHJob2;
import sos.net.ssh.SOSSSHJobJSch;
import sos.net.ssh.SOSSSHJobOptions;
import sos.net.ssh.exceptions.SSHConnectionError;
import sos.net.ssh.exceptions.SSHExecutionError;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.SFTP.SOSVfsSFtpJCraft;
import com.sos.VirtualFileSystem.common.SOSVfsMessageCodes;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "com_sos_net_messages", defaultLocale = "en")
public class SOSSSHReadPidFileJob extends SOSSSHJobJSch {

    private static final Logger LOGGER = Logger.getLogger(SOSSSHReadPidFileJob.class);
    private static final String PARAM_PIDS_TO_KILL = "PIDS_TO_KILL";
    private static final String PID_PLACEHOLDER = "${pid}";
    private String tempPidFileName;
    private List<Integer> pids = new ArrayList<Integer>();

    private void openSession() {
        try {
            if (!vfsHandler.isConnected()) {
                SOSConnection2OptionsAlternate postAlternateOptions = getAlternateOptions(objOptions);
                postAlternateOptions.raiseExceptionOnError.value(false);
                vfsHandler.connect(postAlternateOptions);
            }
            vfsHandler.authenticate(objOptions);
            LOGGER.debug("connection established");
        } catch (Exception e) {
            throw new SSHConnectionError("Error occured during connection/authentication: " + e.getMessage(), e);
        }
        prePostCommandVFSHandler.setJSJobUtilites(objJSJobUtilities);
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
        vfsHandler.setJSJobUtilites(objJSJobUtilities);
        try {
            if (isConnected == false) {
                this.connect();
            }
            add2Files2Delete(getTempPidFileName());
            try {
                String strCmd = String.format(objOptions.getPostCommandRead().getValue(), getTempPidFileName());
                LOGGER.debug(String.format(objMsg.getMsg(SOS_SSH_D_110), strCmd));
                strCmd = objJSJobUtilities.replaceSchedulerVars(strCmd);
                LOGGER.debug(String.format(objMsg.getMsg(SOS_SSH_D_110), strCmd));
                LOGGER.debug("***Execute read pid file command!***");
                vfsHandler.executeCommand(strCmd);
                objJSJobUtilities.setJSParam(conExit_code, "0");
                String pid = null;
                BufferedReader reader = new BufferedReader(new StringReader(new String(vfsHandler.getStdOut())));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    LOGGER.debug(line);
                    Matcher regExMatcher = Pattern.compile("^([^\r\n]*)\r*\n*").matcher(line);
                    if (regExMatcher.find()) {
                        pid = regExMatcher.group(1).trim();
                        try {
                            pids.add(Integer.parseInt(pid));
                            LOGGER.debug("PID: " + pid);
                            continue;
                        } catch (Exception e) {
                            LOGGER.debug("no parseable pid received in line:\n" + pid);
                        }
                    }
                }
                checkStdOut();
                checkStdErr();
                checkExitCode();
                changeExitSignal();
            } catch (Exception e) {
                if (objOptions.raiseExceptionOnError.value()) {
                    if (objOptions.ignoreError.value()) {
                        if (objOptions.ignoreStderr.value()) {
                            LOGGER.debug(this.stackTrace2String(e));
                        } else {
                            LOGGER.error(this.stackTrace2String(e));
                            throw new SSHExecutionError("Exception raised: " + e.getMessage(), e);
                        }
                    } else {
                        LOGGER.error(this.stackTrace2String(e));
                        throw new SSHExecutionError("Exception raised: " + e.getMessage(), e);
                    }
                }
            } finally {
                if (!pids.isEmpty()) {
                    StringBuilder strb = new StringBuilder();
                    boolean first = true;
                    for (Integer pid : pids) {
                        if (first) {
                            strb.append(pid.toString());
                            first = false;
                        } else {
                            strb.append(",").append(pid.toString());
                        }
                    }
                    objJSJobUtilities.setJSParam(PARAM_PIDS_TO_KILL, strb.toString());
                }
            }
            processPostCommands(getTempPidFileName());
        } catch (Exception e) {
            if (objOptions.raiseExceptionOnError.value()) {
                String strErrMsg = "SOS-SSH-E-120: error occurred processing ssh command: ";
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
            if (!keepConnected) {
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
        LOGGER.debug(String.format(SOSVfsMessageCodes.SOSVfs_D_254.params(fileNameToDelete)));
    }

    @Override
    public SOSSSHJob2 connect() {
        getVFS();
        getOptions().checkMandatory();
        try {
            SOSConnection2OptionsAlternate alternateOptions = getAlternateOptions(objOptions);
            vfsHandler.connect(alternateOptions);
            vfsHandler.authenticate(objOptions);
            LOGGER.debug("connection established");
        } catch (Exception e) {
            throw new SSHConnectionError("Error occured during connection/authentication: " + e.getMessage(), e);
        }
        isConnected = true;
        preparePostCommandHandler();
        return this;
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
        openSession();
        if (tempFilesToDelete != null && !tempFilesToDelete.isEmpty()) {
            for (String tempFileName : tempFilesToDelete) {
                ((SOSVfsSFtpJCraft) vfsHandler).delete(tempFileName);
                LOGGER.debug(SOSVfsMessageCodes.SOSVfs_I_0113.params(tempFileName));
            }
        }
        tempFilesToDelete = null;
    }

    public SOSConnection2OptionsAlternate getAlternateOptions(SOSSSHJobOptions options) {
        SOSConnection2OptionsAlternate alternateOptions = new SOSConnection2OptionsAlternate();
        alternateOptions.setStrictHostKeyChecking("no");
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

    public String getTempPidFileName() {
        return tempPidFileName;
    }

    public void setTempPidFileName(String tempPidFileName) {
        this.tempPidFileName = tempPidFileName;
    }

}