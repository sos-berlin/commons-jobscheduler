package sos.scheduler.job.impl;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Options.SOSOptionTransferType.TransferTypes;
import com.sos.vfs.common.SOSCommandResult;

public class SOSSSHKillRemotePidJob extends SOSSSHJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSSHKillRemotePidJob.class);

    private static final String PID_PLACEHOLDER = "${pid}";
    private static final String USER_PLACEHOLDER = "${user}";
    private static final String COMMAND_PLACEHOLDER = "${command}";
    private static final String DEFAULT_LINUX_KILL_PID_COMMAND = "kill -9 " + PID_PLACEHOLDER;
    private static final String DEFAULT_WINDOWS_KILL_PID_COMMAND = "taskkill /f /pid " + PID_PLACEHOLDER + " /FI \"USERNAME eq " + USER_PLACEHOLDER
            + "\" /FI \"IMAGENAME eq " + COMMAND_PLACEHOLDER + "\"";

    private List<Integer> allPids = new ArrayList<Integer>();
    private String killPidCommand = "kill -9 " + PID_PLACEHOLDER;

    @Override
    public void execute() throws Exception {
        List<Integer> pids = getParamPids();
        try {
            connect(TransferTypes.ssh);
            getKillCommandFromJobParameters();

            LOGGER.debug("try to kill remote PIDs");
            for (Integer pid : pids) {
                allPids.add(pid);
                executeGetAllChildProcesses(pid);
            }
            for (Integer pid : allPids) {
                processKillCommand(pid);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            disconnect();
        }
    }

    private void processKillCommand(Integer pid) {
        LOGGER.debug("Sending kill command: " + killPidCommand + " with ${pid}=" + pid);
        String cmd = null;
        if (killPidCommand.contains(PID_PLACEHOLDER)) {
            cmd = killPidCommand.replace(PID_PLACEHOLDER, pid.toString());
        }
        if (killPidCommand.contains(USER_PLACEHOLDER)) {
            cmd = cmd.replace(USER_PLACEHOLDER, objOptions.userName.getValue());
        }
        if (killPidCommand.contains(COMMAND_PLACEHOLDER)) {
            cmd = cmd.replace(COMMAND_PLACEHOLDER, objOptions.command.getValue());
        }

        SOSCommandResult result = null;
        try {
            result = getHandler().executeResultCommand(cmd);
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        } finally {
            if (result != null && result.getExitCode() != 0) {
                if (result.getStdErr().toString().contains("No such process")) {
                    LOGGER.debug("meanwhile the remote process is not available anymore!");
                }
            }
        }
    }

    private void getKillCommandFromJobParameters() {
        if (objOptions.sshJobKillPidCommand.isDirty() && !objOptions.sshJobKillPidCommand.getValue().isEmpty()) {
            killPidCommand = objOptions.sshJobKillPidCommand.getValue();
            LOGGER.debug("Command to kill from Job Parameter used!");
        } else {
            if (isWindowsShell()) {
                killPidCommand = DEFAULT_WINDOWS_KILL_PID_COMMAND;
                LOGGER.debug("Default Windows commands used to kill PID!");
            } else {
                killPidCommand = DEFAULT_LINUX_KILL_PID_COMMAND;
                LOGGER.debug("Default Linux commands used to kill PID!");
            }
        }
    }

    private boolean executeGetAllChildProcesses(Integer processId) {
        try {
            String cmd;
            if (objOptions.getSshJobGetChildProcessesCommand().getValue().contains(PID_PLACEHOLDER)) {
                cmd = objOptions.getSshJobGetChildProcessesCommand().getValue().replace(PID_PLACEHOLDER, processId.toString());
            } else {
                cmd = objOptions.getSshJobGetChildProcessesCommand().getValue();
            }
            LOGGER.debug("***Execute read children of pid command!***");
            SOSCommandResult result = getHandler().executeResultCommand(cmd);
            BufferedReader reader = new BufferedReader(new StringReader(new String(result.getStdOut())));
            String line = null;
            while ((line = reader.readLine()) != null) {
                // get the first line via a regex matcher,
                // if first line is parseable to an Integer we have the pid for
                // the execute channel [SP]
                LOGGER.debug(line);
                Matcher regExMatcher = Pattern.compile("^([^\r\n]*)\r*\n*").matcher(line);
                if (regExMatcher.find()) {
                    // key with leading and trailing whitespace removed
                    String pid = regExMatcher.group(1).trim();
                    try {
                        LOGGER.debug("PID: " + pid);
                        allPids.add(Integer.parseInt(pid));
                        executeGetAllChildProcesses(Integer.parseInt(pid));
                        continue;
                    } catch (Exception e) {
                        LOGGER.debug("no parseable pid received in line: \"" + pid + "\"");
                    }
                }
            }
            return true;
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
            return false;
        }
    }

}
