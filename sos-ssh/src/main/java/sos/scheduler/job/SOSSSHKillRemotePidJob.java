package sos.scheduler.job;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.net.ssh.SOSSSHJob2;
import sos.net.ssh.SOSSSHJobJSch;
import sos.net.ssh.exceptions.SSHConnectionError;
import sos.net.ssh.exceptions.SSHExecutionError;

import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "com_sos_net_messages", defaultLocale = "en")
public class SOSSSHKillRemotePidJob extends SOSSSHJobJSch {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSSHKillRemotePidJob.class);
    private static final String PARAM_PIDS_TO_KILL = "PIDS_TO_KILL";
    private static final String PID_PLACEHOLDER = "${pid}";
    private static final String USER_PLACEHOLDER = "${user}";
    private static final String COMMAND_PLACEHOLDER = "${command}";
    private static final String DEFAULT_LINUX_KILL_PID_COMMAND = "kill -9 " + PID_PLACEHOLDER;
    private static final String DEFAULT_WINDOWS_KILL_PID_COMMAND = "taskkill /f /pid " + PID_PLACEHOLDER + " /FI \"USERNAME eq " + USER_PLACEHOLDER
            + "\" /FI \"IMAGENAME eq " + COMMAND_PLACEHOLDER + "\"";
    private String ssh_job_kill_pid_command = "kill -9 " + PID_PLACEHOLDER;
    private List<Integer> allPids = new ArrayList<Integer>();

    private void openSession() {
        try {
            if (!vfsHandler.isConnected()) {
                SOSConnection2OptionsAlternate postAlternateOptions = getAlternateOptions(objOptions);
                postAlternateOptions.raiseExceptionOnError.value(false);
                vfsHandler.connect(postAlternateOptions);
            }
            vfsHandler.authenticate(objOptions);
            LOGGER.debug("connection for kill commands established");
        } catch (Exception e) {
            throw new SSHConnectionError("Error occured during connection/authentication: " + e.getMessage(), e);
        }
        vfsHandler.setJSJobUtilites(objJSJobUtilities);
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
        flgIsWindowsShell = vfsHandler.remoteIsWindowsShell();
        getKillCommandFromJobParameters();
        isConnected = true;
        return this;
    }

    @Override
    public SOSSSHJob2 execute() throws Exception {
        vfsHandler.setJSJobUtilites(objJSJobUtilities);
        boolean configuredRaiseExeptionOnError = objOptions.raiseExceptionOnError.value();
        boolean configuredIgnoreError = objOptions.ignoreError.value();
        objOptions.raiseExceptionOnError.value(false);
        objOptions.ignoreError.value(true);
        openSession();
        List<Integer> pidsToKillFromOrder = getPidsToKillFromOrder();
        try {
            if (isConnected == false) {
                this.connect();
            }
            LOGGER.debug("try to kill remote PIDs");
            for (Integer pid : pidsToKillFromOrder) {
                allPids.add(pid);
                executeGetAllChildProcesses(pid);
            }
            for (Integer pid : allPids) {
                processKillCommand(pid);
            }
        } catch (Exception e) {
            if (objOptions.raiseExceptionOnError.value()) {
                if (objOptions.ignoreError.value()) {
                    if (objOptions.ignoreStderr.value()) {
                        LOGGER.debug(this.stackTrace2String(e));
                    } else {
                        LOGGER.error(this.stackTrace2String(e));
                        throw new SSHExecutionError("Exception raised: " + e, e);
                    }
                } else {
                    LOGGER.error(this.stackTrace2String(e));
                    throw new SSHExecutionError("Exception raised: " + e, e);
                }
            }
        } finally {
            objOptions.raiseExceptionOnError.value(configuredRaiseExeptionOnError);
            objOptions.ignoreError.value(configuredIgnoreError);
        }
        return this;
    }

    private List<Integer> getPidsToKillFromOrder() {
        String[] pidsFromOrder = objOptions.getItem(PARAM_PIDS_TO_KILL).split(",");
        List<Integer> pidsToKill = new ArrayList<Integer>();
        for (String pid : pidsFromOrder) {
            pidsToKill.add(Integer.parseInt(pid));
        }
        return pidsToKill;
    }

    private void processKillCommand(Integer pid) {
        LOGGER.debug("Sending kill command: " + ssh_job_kill_pid_command + " with ${pid}=" + pid);
        String killCommand = null;
        if (ssh_job_kill_pid_command.contains(PID_PLACEHOLDER)) {
            killCommand = ssh_job_kill_pid_command.replace(PID_PLACEHOLDER, pid.toString());
        }
        if (ssh_job_kill_pid_command.contains(USER_PLACEHOLDER)) {
            killCommand = killCommand.replace(USER_PLACEHOLDER, objOptions.userName.getValue());
        }
        if (ssh_job_kill_pid_command.contains(COMMAND_PLACEHOLDER)) {
            killCommand = killCommand.replace(COMMAND_PLACEHOLDER, objOptions.command.getValue());
        }
        String stdErr = "";
        try {
            vfsHandler.executeCommand(killCommand);
        } catch (Exception e) {
            if (vfsHandler.getExitCode() != 0) {
                try {
                    stdErr = vfsHandler.getStdErr().toString();
                    if (stdErr.contains("No such process")) {
                        LOGGER.debug("meanwhile the remote process is not available anymore!");
                    } else {
                        if (objOptions.raiseExceptionOnError.value()) {
                            if (objOptions.ignoreError.value()) {
                                if (objOptions.ignoreStderr.value()) {
                                    LOGGER.debug("error occured while trying to execute command");
                                } else {
                                    LOGGER.error("error occured while trying to execute command");
                                    throw new SSHExecutionError("Exception raised: " + e, e);
                                }
                            } else {
                                LOGGER.error("error occured while trying to execute command");
                                throw new SSHExecutionError("Exception raised: " + e, e);
                            }
                        }
                    }
                } catch (Exception e1) {
                    LOGGER.debug("error occured while reading remote stderr");
                }
            }
        }
    }

    private void getKillCommandFromJobParameters() {
        if (objOptions.sshJobKillPidCommand.isDirty() && !objOptions.sshJobKillPidCommand.getValue().isEmpty()) {
            ssh_job_kill_pid_command = objOptions.sshJobKillPidCommand.getValue();
            LOGGER.debug("Command to kill from Job Parameter used!");
        } else {
            if (flgIsWindowsShell) {
                ssh_job_kill_pid_command = DEFAULT_WINDOWS_KILL_PID_COMMAND;
                LOGGER.debug("Default Windows commands used to kill PID!");
            } else {
                ssh_job_kill_pid_command = DEFAULT_LINUX_KILL_PID_COMMAND;
                LOGGER.debug("Default Linux commands used to kill PID!");
            }
        }
    }

    private boolean executeGetAllChildProcesses(Integer pPid) {
        boolean configuredRaiseExeptionOnError = objOptions.raiseExceptionOnError.value();
        boolean configuredIgnoreError = objOptions.ignoreError.value();
        objOptions.raiseExceptionOnError.value(false);
        objOptions.ignoreError.value(true);
        try {
            String command;
            if (objOptions.getSshJobGetChildProcessesCommand().getValue().contains(PID_PLACEHOLDER)) {
                command = objOptions.getSshJobGetChildProcessesCommand().getValue().replace(PID_PLACEHOLDER, pPid.toString());
            } else {
                command = objOptions.getSshJobGetChildProcessesCommand().getValue();
            }
            LOGGER.debug("***Execute read children of pid command!***");
            vfsHandler.executeCommand(command);
            BufferedReader reader = new BufferedReader(new StringReader(new String(vfsHandler.getStdOut())));
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
            LOGGER.error(e.getMessage(), e);
            return false;
        } finally {
            objOptions.raiseExceptionOnError.value(configuredRaiseExeptionOnError);
            objOptions.ignoreError.value(configuredIgnoreError);
        }
    }

}
