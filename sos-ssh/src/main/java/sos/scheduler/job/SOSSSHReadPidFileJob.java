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

    private final Logger logger = Logger.getLogger(this.getClass());

    private static final String PARAM_PIDS_TO_KILL = "PIDS_TO_KILL";
    private static final String PID_PLACEHOLDER = "${pid}";
    private String tempPidFileName;

    private List<Integer> pids = new ArrayList<Integer>();

    private void openSession() {
        try {
            if (!vfsHandler.isConnected()) {
                SOSConnection2OptionsAlternate postAlternateOptions = getAlternateOptions(objOptions);
                postAlternateOptions.raise_exception_on_error.value(false);
                vfsHandler.Connect(postAlternateOptions);
            }
            vfsHandler.Authenticate(objOptions);
            logger.debug("connection established");
        } catch (Exception e) {
            throw new SSHConnectionError("Error occured during connection/authentication: " + e.getLocalizedMessage(), e);
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
                String strCmd = String.format(objOptions.getPostCommandRead().Value(), getTempPidFileName());
                // see http://www.sos-berlin.com/jira/browse/JS-673
                logger.debug(String.format(objMsg.getMsg(SOS_SSH_D_110), strCmd));
                strCmd = objJSJobUtilities.replaceSchedulerVars(flgIsWindowsShell, strCmd);
                logger.debug(String.format(objMsg.getMsg(SOS_SSH_D_110), strCmd));
                logger.debug("***Execute read pid file command!***");
                vfsHandler.ExecuteCommand(strCmd);
                objJSJobUtilities.setJSParam(conExit_code, "0");
                String pid = null;
                BufferedReader reader = new BufferedReader(new StringReader(new String(vfsHandler.getStdOut())));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    // get the first line via a regex matcher,
                    // if first line is parseable to an Integer we have the pid
                    // for the execute channel [SP]
                    logger.debug(line);
                    Matcher regExMatcher = Pattern.compile("^([^\r\n]*)\r*\n*").matcher(line);
                    if (regExMatcher.find()) {
                        pid = regExMatcher.group(1).trim(); // key with leading
                                                            // and trailing
                                                            // whitespace
                                                            // removed
                        try {
                            pids.add(Integer.parseInt(pid));
                            logger.debug("PID: " + pid);
                            // break;
                            continue;
                        } catch (Exception e) {
                            logger.debug("no parseable pid received in line:\n" + pid);
                        }
                    }
                }
                checkStdOut();
                checkStdErr();
                checkExitCode();
                changeExitSignal();
            } catch (Exception e) {
                if (objOptions.raise_exception_on_error.value()) {
                    if (objOptions.ignore_error.value()) {
                        if (objOptions.ignore_stderr.value()) {
                            logger.debug(this.StackTrace2String(e));
                        } else {
                            logger.error(this.StackTrace2String(e));
                            throw new SSHExecutionError("Exception raised: " + e, e);
                        }
                    } else {
                        logger.error(this.StackTrace2String(e));
                        throw new SSHExecutionError("Exception raised: " + e, e);
                    }
                }
            } finally {
                if (pids.size() > 0) {
                    StringBuilder strb = new StringBuilder();
                    boolean first = true;
                    // create a String with the comma separated pids to put in
                    // one Param
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
            // http://www.sos-berlin.com/jira/browse/JITL-112
            processPostCommands(getTempPidFileName());
        } catch (Exception e) {
            if (objOptions.raise_exception_on_error.value()) {
                String strErrMsg = "SOS-SSH-E-120: error occurred processing ssh command: ";
                if (objOptions.ignore_error.value()) {
                    if (objOptions.ignore_stderr.value()) {
                        logger.debug(this.StackTrace2String(e));
                        logger.debug(strErrMsg, e);
                    } else {
                        logger.error(this.StackTrace2String(e));
                        logger.error(strErrMsg, e);
                        throw new SSHExecutionError(strErrMsg, e);
                    }
                } else {
                    logger.error(this.StackTrace2String(e));
                    logger.error(strErrMsg, e);
                    throw new SSHExecutionError(strErrMsg, e);
                }
            }
        } finally {
            if (keepConnected == false) {
                disconnect();
            }
        }
        return this;
    }

    @Override
    public void disconnect() {
        if (isConnected == true) {
            try {
                vfsHandler.CloseConnection();
            } catch (Exception e) {
                throw new SSHConnectionError("problems closing connection", e);
            }
            isConnected = false;
        }
    }

    // http://www.sos-berlin.com/jira/browse/JITL-123
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
        Options().CheckMandatory();

        try {
            SOSConnection2OptionsAlternate alternateOptions = getAlternateOptions(objOptions);
            vfsHandler.Connect(alternateOptions);
            vfsHandler.Authenticate(objOptions);
            logger.debug("connection established");
        } catch (Exception e) {
            throw new SSHConnectionError("Error occured during connection/authentication: " + e.getLocalizedMessage(), e);
        }
        isConnected = true;

        // http://www.sos-berlin.com/jira/browse/JITL-112:
        // preparePostCommandHandler() has to be called once to generate a
        // second instance for post processing of stored return values
        preparePostCommandHandler();
        return this;
    } // private SOSSSHJob2 Connect

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
                logger.debug(SOSVfsMessageCodes.SOSVfs_I_0113.params(tempFileName));
            }
        }
        tempFilesToDelete = null;
    }

    public SOSConnection2OptionsAlternate getAlternateOptions(SOSSSHJobOptions options) {
        SOSConnection2OptionsAlternate alternateOptions = new SOSConnection2OptionsAlternate();
        alternateOptions.setStrict_HostKey_Checking("no");
        alternateOptions.host.Value(options.getHost().Value());
        alternateOptions.port.value(options.getPort().value());
        alternateOptions.user.Value(options.getUser().Value());
        alternateOptions.password.Value(options.getPassword().Value());
        alternateOptions.proxy_protocol.Value(options.getproxy_protocol().Value());
        alternateOptions.proxy_host.Value(options.getProxy_host().Value());
        alternateOptions.proxy_port.value(options.getProxy_port().value());
        alternateOptions.proxy_user.Value(options.getProxy_user().Value());
        alternateOptions.proxy_password.Value(options.getProxy_password().Value());
        alternateOptions.raise_exception_on_error.value(options.getraise_exception_on_error().value());
        alternateOptions.ignore_error.value(options.getIgnore_error().value());
        return alternateOptions;
    }

    public String getTempPidFileName() {
        return tempPidFileName;
    }

    public void setTempPidFileName(String tempPidFileName) {
        this.tempPidFileName = tempPidFileName;
    }

}
