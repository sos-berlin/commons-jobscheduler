package sos.scheduler.job.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.common.SOSCommandResult;

public class SOSSSHCheckRemotePidJob extends SOSSSHJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSSHCheckRemotePidJob.class);

    private static final String DEFAULT_LINUX_GET_ACTIVE_PROCESSES_COMMAND = "/bin/ps -ef | grep ${pid} | grep ${user} | grep -v grep";
    private static final String DEFAULT_WINDOWS_GET_ACTIVE_PROCESSES_COMMAND = "Qprocess ${pid}";

    private String activeProcessesCommand = "/bin/ps -ef | grep ${pid} | grep ${user} | grep -v grep";
    private String pids = null;

    @Override
    public void execute() throws Exception {
        List<Integer> paramPids = getParamPids();
        List<Integer> pidsStillRunning = new ArrayList<Integer>();
        try {
            connect();
            readCheckIfProcessesIsStillActiveCommandFromPropertiesFile();

            for (Integer pid : paramPids) {
                String checkPidCommand = null;
                if (activeProcessesCommand.contains("${user}")) {
                    checkPidCommand = activeProcessesCommand.replace("${user}", objOptions.user.getValue());
                }
                if (activeProcessesCommand.contains("${pid}")) {
                    checkPidCommand = checkPidCommand.replace("${pid}", pid.toString());
                }
                SOSCommandResult result = getHandler().executeResultCommand(checkPidCommand);
                if (result.getExitCode() == 0) {
                    pidsStillRunning.add(pid);
                    LOGGER.debug("PID " + pid + " is still running");
                } else {
                    LOGGER.debug("PID " + pid + " is not running anymore");
                }
            }
            if (!pidsStillRunning.isEmpty()) {
                LOGGER.debug("Overriding param " + PARAM_PIDS_TO_KILL);

                StringBuilder sb = new StringBuilder();
                boolean first = true;
                for (Integer pid : pidsStillRunning) {
                    if (first) {
                        sb.append(pid.toString());
                        first = false;
                    } else {
                        sb.append(",").append(pid.toString());
                    }
                }
                LOGGER.debug("still running PIDs to kill: " + sb.toString());
                pids = sb.toString();
            } else {
                pids = null;
            }
        } catch (JobSchedulerException ex) {
            if (pidsStillRunning.isEmpty()) {
                LOGGER.debug("Overriding PARAM_PIDS_TO_KILL with empty String");
                objJSJobUtilities.setJSParam(PARAM_PIDS_TO_KILL, "");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            disconnect();
        }
    }

    private void readCheckIfProcessesIsStillActiveCommandFromPropertiesFile() {
        if (objOptions.sshJobGetActiveProcessesCommand.isDirty() && !objOptions.sshJobGetActiveProcessesCommand.getValue().isEmpty()) {
            activeProcessesCommand = objOptions.sshJobGetActiveProcessesCommand.getValue();
            LOGGER.debug("Command to check if PID is still running from Job Parameter used!");
        } else {
            if (isWindowsShell()) {
                activeProcessesCommand = DEFAULT_WINDOWS_GET_ACTIVE_PROCESSES_COMMAND;
                LOGGER.debug("Default Windows command used to check if PID is still running!");
            } else {
                activeProcessesCommand = DEFAULT_LINUX_GET_ACTIVE_PROCESSES_COMMAND;
                LOGGER.debug("Default Linux command used to check if PID is still running!");
            }
        }
    }

    public String getPids() {
        return pids;
    }

}
