package sos.scheduler.job.impl;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSSSHTerminateRemotePidJob extends SOSSSHJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSSHTerminateRemotePidJob.class);

    private static final String PARAM_PIDS_TO_KILL = "PIDS_TO_KILL";
    private static final String PID_PLACEHOLDER = "${pid}";
    private static final String USER_PLACEHOLDER = "${user}";
    private static final String COMMAND_PLACEHOLDER = "${command}";
    private static final String DEFAULT_LINUX_TERMINATE_PID_COMMAND = "kill -15 " + PID_PLACEHOLDER;
    private static final String DEFAULT_WINDOWS_TERMINATE_PID_COMMAND = "taskkill /pid " + PID_PLACEHOLDER + " /FI \"USERNAME eq " + USER_PLACEHOLDER
            + "\" /FI \"IMAGENAME eq " + COMMAND_PLACEHOLDER + "\"";
    private String ssh_job_terminate_pid_command = "kill -15 " + PID_PLACEHOLDER;// default
    private List<Integer> allPids = new ArrayList<Integer>();

    @Override
    public void execute() {
        boolean configuredRaiseExeptionOnError = objOptions.raiseExceptionOnError.value();
        boolean configuredIgnoreError = objOptions.ignoreError.value();
        List<Integer> pidsToKillFromOrder = getPidsToKillFromOrder();
        try {
            connect(true);
            getTerminateCommandFromJobParameters();
            LOGGER.debug("try to kill remote PIDs");
            for (Integer pid : pidsToKillFromOrder) {
                allPids.add(pid);
                executeGetAllChildProcesses(pid);
            }
            for (Integer pid : allPids) {
                processTerminateCommand(pid);
            }
        } catch (Exception e) {
            // ignore due disableRaiseExceptionOnError=true
        } finally {
            disconnect();
            objOptions.raiseExceptionOnError.value(configuredRaiseExeptionOnError);
            objOptions.ignoreError.value(configuredIgnoreError);
        }
    }

    private List<Integer> getPidsToKillFromOrder() {
        String[] pidsFromOrder = objOptions.getItem(PARAM_PIDS_TO_KILL).split(",");
        List<Integer> pidsToKill = new ArrayList<Integer>();
        for (String pid : pidsFromOrder) {
            pidsToKill.add(Integer.parseInt(pid));
        }
        return pidsToKill;
    }

    private void processTerminateCommand(Integer pid) {
        boolean raiseExeptionOnError = objOptions.raiseExceptionOnError.value();
        boolean ignoreError = objOptions.ignoreError.value();

        LOGGER.debug("Sending terminate command: " + ssh_job_terminate_pid_command + " with ${pid}=" + pid);
        String terminateCommand = null;
        if (ssh_job_terminate_pid_command.contains(PID_PLACEHOLDER)) {
            terminateCommand = ssh_job_terminate_pid_command.replace(PID_PLACEHOLDER, pid.toString());
        }
        if (ssh_job_terminate_pid_command.contains(USER_PLACEHOLDER)) {
            terminateCommand = terminateCommand.replace(USER_PLACEHOLDER, objOptions.userName.getValue());
        }
        if (ssh_job_terminate_pid_command.contains(COMMAND_PLACEHOLDER)) {
            terminateCommand = terminateCommand.replace(COMMAND_PLACEHOLDER, objOptions.command.getValue());
        }
        try {
            getHandler().executeCommand(terminateCommand);
        } catch (Exception e) {
            // check if command was processed correctly
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
        } finally {
            objOptions.raiseExceptionOnError.value(raiseExeptionOnError);
            objOptions.ignoreError.value(ignoreError);
        }

    }

    private void getTerminateCommandFromJobParameters() {
        if (objOptions.sshJobTerminatePidCommand.isDirty() && !objOptions.sshJobTerminatePidCommand.getValue().isEmpty()) {
            ssh_job_terminate_pid_command = objOptions.sshJobTerminatePidCommand.getValue();
            LOGGER.debug("Commands to terminate from Job Parameter used!");
        } else {
            if (isWindowsShell()) {
                ssh_job_terminate_pid_command = DEFAULT_WINDOWS_TERMINATE_PID_COMMAND;
                LOGGER.debug("Default Windows commands used to terminate PID!");
            } else {
                ssh_job_terminate_pid_command = DEFAULT_LINUX_TERMINATE_PID_COMMAND;
                LOGGER.debug("Default Linux commands used to terminate PID!");
            }
        }
    }

    private boolean executeGetAllChildProcesses(Integer pPid) {
        boolean raiseExeptionOnError = objOptions.raiseExceptionOnError.value();
        boolean ignoreError = objOptions.ignoreError.value();
        try {
            String command;
            if (objOptions.getSshJobGetChildProcessesCommand().getValue().contains(PID_PLACEHOLDER)) {
                command = objOptions.getSshJobGetChildProcessesCommand().getValue().replace(PID_PLACEHOLDER, pPid.toString());
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
