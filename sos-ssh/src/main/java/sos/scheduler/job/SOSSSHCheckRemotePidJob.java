package sos.scheduler.job;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.net.ssh.SOSSSHJob2;
import sos.net.ssh.SOSSSHJobJSch;
import sos.net.ssh.exceptions.SSHConnectionError;
import sos.net.ssh.exceptions.SSHExecutionError;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "com_sos_net_messages", defaultLocale = "en")
public class SOSSSHCheckRemotePidJob extends SOSSSHJobJSch {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSSHCheckRemotePidJob.class);
    private static final String PARAM_PIDS_TO_KILL = "PIDS_TO_KILL";
    private static final String DEFAULT_LINUX_GET_ACTIVE_PROCESSES_COMMAND = "/bin/ps -ef | grep ${pid} | grep ${user} | grep -v grep";
    private static final String DEFAULT_WINDOWS_GET_ACTIVE_PROCESSES_COMMAND = "Qprocess ${pid}";
    private String ssh_job_get_active_processes_command = "/bin/ps -ef | grep ${pid} | grep ${user} | grep -v grep";
    private String pids = null;

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
        readCheckIfProcessesIsStillActiveCommandFromPropertiesFile();
        isConnected = true;
        return this;
    }

    @Override
    public SOSSSHJob2 execute() {
        vfsHandler.setJSJobUtilites(objJSJobUtilities);
        openSession();
        boolean configuredRaiseExeptionOnError = objOptions.raiseExceptionOnError.value();
        boolean configuredIgnoreError = objOptions.ignoreError.value();
        List<Integer> pidsToKillFromOrder = getPidsToKill();
        List<Integer> pidsStillRunning = new ArrayList<Integer>();
        try {
            if (!isConnected) {
                this.connect();
            }
            objOptions.raiseExceptionOnError.value(false);
            objOptions.ignoreError.value(true);
            for (Integer pid : pidsToKillFromOrder) {
                String checkPidCommand = null;
                if (ssh_job_get_active_processes_command.contains("${user}")) {
                    checkPidCommand = ssh_job_get_active_processes_command.replace("${user}", objOptions.user.getValue());
                }
                if (ssh_job_get_active_processes_command.contains("${pid}")) {
                    checkPidCommand = checkPidCommand.replace("${pid}", pid.toString());
                }
                vfsHandler.executeCommand(checkPidCommand);
                if (vfsHandler.getExitCode() == 0) {
                    pidsStillRunning.add(pid);
                    LOGGER.debug("PID " + pid + " is still running");
                } else {
                    LOGGER.debug("PID " + pid + " is not running anymore");
                }
            }
            if (!pidsStillRunning.isEmpty()) {
                StringBuilder strb = new StringBuilder();
                LOGGER.debug("Overriding param " + PARAM_PIDS_TO_KILL);
                boolean first = true;
                for (Integer pid : pidsStillRunning) {
                    if (first) {
                        strb.append(pid.toString());
                        first = false;
                    } else {
                        strb.append(",").append(pid.toString());
                    }
                }
                LOGGER.debug("still running PIDs to kill: " + strb.toString());
                pids = strb.toString();
            } else {
                pids = null;
            }
        } catch (JobSchedulerException ex) {
            if (pidsStillRunning.isEmpty()) {
                LOGGER.debug("Overriding PARAM_PIDS_TO_KILL with empty String");
                objJSJobUtilities.setJSParam(PARAM_PIDS_TO_KILL, "");
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

    private List<Integer> getPidsToKill() {
        LOGGER.debug("PIDs to kill From Order: " + objOptions.getItem(PARAM_PIDS_TO_KILL));
        String[] pidsFromOrder = null;
        if (objOptions.getItem(PARAM_PIDS_TO_KILL) != null && objOptions.getItem(PARAM_PIDS_TO_KILL).length() > 0) {
            pidsFromOrder = objOptions.getItem(PARAM_PIDS_TO_KILL).split(",");
        }
        List<Integer> pidsToKill = new ArrayList<Integer>();
        if (pidsFromOrder != null) {
            for (String pid : pidsFromOrder) {
                if (pid != null && !pid.isEmpty()) {
                    pidsToKill.add(Integer.parseInt(pid));
                } else {
                    LOGGER.debug("PID is empty!");
                }
            }
        }
        return pidsToKill;
    }

    private void readCheckIfProcessesIsStillActiveCommandFromPropertiesFile() {
        if (objOptions.sshJobGetActiveProcessesCommand.isDirty() && !objOptions.sshJobGetActiveProcessesCommand.getValue().isEmpty()) {
            ssh_job_get_active_processes_command = objOptions.sshJobGetActiveProcessesCommand.getValue();
            LOGGER.debug("Command to check if PID is still running from Job Parameter used!");
        } else {
            if (flgIsWindowsShell) {
                ssh_job_get_active_processes_command = DEFAULT_WINDOWS_GET_ACTIVE_PROCESSES_COMMAND;
                LOGGER.debug("Default Windows command used to check if PID is still running!");
            } else {
                ssh_job_get_active_processes_command = DEFAULT_LINUX_GET_ACTIVE_PROCESSES_COMMAND;
                LOGGER.debug("Default Linux command used to check if PID is still running!");
            }
        }
    }

    public String getPids() {
        return pids;
    }

}
