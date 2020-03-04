package sos.scheduler.job.impl;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public SOSSSHKillRemotePidJob() {
        super();
        disableRaiseException(true);
    }

    @Override
    public void execute() {
        boolean raiseExeptionOnError = objOptions.raiseExceptionOnError.value();
        boolean ignoreError = objOptions.ignoreError.value();
        List<Integer> pids = getParamPids();
        try {
            connect();
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
            // ignore due raiseExceptionOnError=true
        } finally {
            disconnect();
            objOptions.raiseExceptionOnError.value(raiseExeptionOnError);
            objOptions.ignoreError.value(ignoreError);
        }
    }

    private void processKillCommand(Integer pid) {
        LOGGER.debug("Sending kill command: " + killPidCommand + " with ${pid}=" + pid);
        String killCommand = null;
        if (killPidCommand.contains(PID_PLACEHOLDER)) {
            killCommand = killPidCommand.replace(PID_PLACEHOLDER, pid.toString());
        }
        if (killPidCommand.contains(USER_PLACEHOLDER)) {
            killCommand = killCommand.replace(USER_PLACEHOLDER, objOptions.userName.getValue());
        }
        if (killPidCommand.contains(COMMAND_PLACEHOLDER)) {
            killCommand = killCommand.replace(COMMAND_PLACEHOLDER, objOptions.command.getValue());
        }
        try {
            getHandler().executeCommand(killCommand);
        } catch (Exception e) {
            if (getHandler().getExitCode() != 0) {
                try {
                    String stdErr = getHandler().getStdErr().toString();
                    if (stdErr.contains("No such process")) {
                        LOGGER.debug("meanwhile the remote process is not available anymore!");
                    }
                } catch (Exception ex) {
                    LOGGER.debug("error occured while reading remote stderr" + ex.toString(), ex);
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
        boolean raiseExeptionOnError = objOptions.raiseExceptionOnError.value();
        boolean ignoreError = objOptions.ignoreError.value();

        try {
            String command;
            if (objOptions.getSshJobGetChildProcessesCommand().getValue().contains(PID_PLACEHOLDER)) {
                command = objOptions.getSshJobGetChildProcessesCommand().getValue().replace(PID_PLACEHOLDER, processId.toString());
            } else {
                command = objOptions.getSshJobGetChildProcessesCommand().getValue();
            }
            LOGGER.debug("***Execute read children of pid command!***");
            getHandler().executeCommand(command);
            BufferedReader reader = new BufferedReader(new StringReader(new String(getHandler().getStdOut())));
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
        } finally {
            objOptions.raiseExceptionOnError.value(raiseExeptionOnError);
            objOptions.ignoreError.value(ignoreError);
        }
    }

}
