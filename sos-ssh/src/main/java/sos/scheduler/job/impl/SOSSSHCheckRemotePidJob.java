package sos.scheduler.job.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionTransferType.TransferTypes;
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
            connect(TransferTypes.ssh);
            setActiveProcessesCommand();

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
            if (pidsStillRunning.isEmpty()) {
                pids = null;
                LOGGER.debug("no still running PIDs found");
            } else {
                pids = Joiner.on(",").join(pidsStillRunning);
                LOGGER.debug("all still running PIDs to kill: " + pids);
            }
        } catch (JobSchedulerException ex) {
            if (pidsStillRunning.isEmpty()) {
                LOGGER.debug("Overriding " + PARAM_PIDS_TO_KILL + " param with empty String");
                objJSJobUtilities.setJSParam(PARAM_PIDS_TO_KILL, "");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            disconnect();
        }
    }

    private void setActiveProcessesCommand() {
        if (objOptions.sshJobGetActiveProcessesCommand.isDirty() && !objOptions.sshJobGetActiveProcessesCommand.getValue().isEmpty()) {
            activeProcessesCommand = objOptions.sshJobGetActiveProcessesCommand.getValue();
        } else {
            if (isWindowsShell()) {
                activeProcessesCommand = DEFAULT_WINDOWS_GET_ACTIVE_PROCESSES_COMMAND;
            } else {
                activeProcessesCommand = DEFAULT_LINUX_GET_ACTIVE_PROCESSES_COMMAND;
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[activeProcessesCommand]%s", activeProcessesCommand));
        }
    }

    public String getPids() {
        return pids;
    }

}
