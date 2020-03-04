package sos.scheduler.job.impl;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

import sos.net.ssh.exceptions.SSHExecutionError;

public class SOSSSHReadPidFileJob extends SOSSSHJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSSHReadPidFileJob.class);

    private List<Integer> pids = new ArrayList<Integer>();
    private String tempPidFileName;

    public SOSSSHReadPidFileJob() {
        super();
        disableRaiseException(false);// ?
    }

    @Override
    public void execute() throws Exception {
        try {
            connect();

            add2Files2Delete(tempPidFileName);
            try {
                String cmd = String.format(objOptions.getPostCommandRead().getValue(), tempPidFileName);
                LOGGER.debug(String.format("executing remote command: %s", cmd));

                cmd = objJSJobUtilities.replaceSchedulerVars(cmd);
                LOGGER.debug(String.format("executing remote command: %s", cmd));
                LOGGER.debug("***Execute read pid file command!***");
                getHandler().executeCommand(cmd);

                objJSJobUtilities.setJSParam(PARAM_EXIT_CODE, "0");

                String pid = null;
                BufferedReader reader = new BufferedReader(new StringReader(new String(getHandler().getStdOut())));
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
                    objJSJobUtilities.setJSParam(PARAM_PIDS_TO_KILL, Joiner.on(",").join(pids));
                }
            }

            connect();
            deleteTempFiles();
        } catch (Exception e) {
            if (objOptions.raiseExceptionOnError.value()) {
                if (objOptions.ignoreError.value()) {
                    if (objOptions.ignoreStderr.value()) {
                        LOGGER.debug(e.toString(), e);
                    } else {
                        LOGGER.error(e.toString(), e);
                        throw new SSHExecutionError(e.toString(), e);
                    }
                } else {
                    LOGGER.error(e.toString(), e);
                    throw new SSHExecutionError(e.toString(), e);
                }
            }
        } finally {
            disconnect();
        }
    }

    public void setTempPidFileName(String val) {
        tempPidFileName = val;
    }

}