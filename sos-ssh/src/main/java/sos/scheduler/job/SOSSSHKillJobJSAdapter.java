package sos.scheduler.job;

import java.util.HashMap;

import org.apache.log4j.Logger;

import sos.net.ssh.SOSSSHJob2;
import sos.net.ssh.SOSSSHJobOptions;
import sos.net.ssh.exceptions.SSHExecutionError;
import sos.spooler.Variable_set;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class SOSSSHKillJobJSAdapter extends SOSSSHJob2JSBaseAdapter {

    private static final String PARAM_PIDS_TO_KILL = "PIDS_TO_KILL";
    private static final String PARAM_SSH_JOB_TASK_ID = "SSH_JOB_TASK_ID";
    private static final String PARAM_SSH_JOB_NAME = "SSH_JOB_NAME";
    private static final String PARAM_SSH_JOB_TIMEOUT_KILL_AFTER = "ssh_job_timeout_kill_after";
    private static final Logger LOGGER = Logger.getLogger(SOSSSHKillJobJSAdapter.class);
    private HashMap<String, String> allParams;

    @Override
    public boolean spooler_process() throws Exception {
        boolean successfull = true;
        try {
            super.spooler_process();
            successfull = doProcessing();
        } catch (Exception e) {
            LOGGER.fatal(stackTrace2String(e));
            throw new JobSchedulerException(e);
        }
        if (successfull) {
            return signalSuccess();
        } else {
            return signalFailure();
        }
    }

    private boolean doProcessing() throws Exception {
        SOSSSHJob2 sshJob;
        allParams = getGlobalSchedulerParameters();
        allParams.putAll(getParameters());
        boolean taskIsActive = true;
        boolean timeoutAfterKillIsSet = false;
        if (allParams.get(PARAM_SSH_JOB_TASK_ID) != null && !allParams.get(PARAM_SSH_JOB_TASK_ID).isEmpty()) {
            taskIsActive = isTaskActive(allParams.get(PARAM_SSH_JOB_TASK_ID));
        } else {
            taskIsActive = false;
        }
        if (allParams.get(PARAM_SSH_JOB_TIMEOUT_KILL_AFTER) != null && !allParams.get(PARAM_SSH_JOB_TIMEOUT_KILL_AFTER).isEmpty()) {
            timeoutAfterKillIsSet = true;
        }
        LOGGER.info("Task is still active: " + taskIsActive);
        sshJob = executeCheckPids();
        if (((SOSSSHCheckRemotePidJob) sshJob).getPids() != null) {
            spooler_log.debug9(((SOSSSHCheckRemotePidJob) sshJob).getPids());
            getOrderParams().set_var(PARAM_PIDS_TO_KILL, ((SOSSSHCheckRemotePidJob) sshJob).getPids());
        }
        String runningPids = spooler_task.order().params().value(PARAM_PIDS_TO_KILL);
        if (taskIsActive && runningPids != null && !runningPids.isEmpty()) {
            // if task is still running and remote pids are still available -->
            // do nothing check again after some delay
            LOGGER.info("Task and remote processes are still active, do nothing!");
            return false;
        } else if (taskIsActive && (runningPids == null || runningPids.isEmpty())) {
            // if task is still running but remote pid is not available anymore (finished) --> kill task
            LOGGER.info("Task is still active, try to end task!");
            String killTaskXml = new String("<kill_task job=\"" + allParams.get(PARAM_SSH_JOB_NAME) + "\" id=\""
                    + allParams.get(PARAM_SSH_JOB_TASK_ID) + "\" immediately=\"yes\"/>");
            String killTaskXmlAnswer = spooler.execute_xml(killTaskXml);
            LOGGER.debug("killTaskXmlAnswer:\n" + killTaskXmlAnswer);
            return true;
        } else if (!taskIsActive && runningPids != null && !runningPids.isEmpty() && !timeoutAfterKillIsSet) {
            LOGGER.info("Task is not active anymore, processing kill remote pids!");
            // if task is not running anymore but remote pid is still available
            // and a timeout_kill_after is not set --> kill remote pid immediately
            sshJob = executeKillPids();
            return true;
        } else if (!taskIsActive && runningPids != null && !runningPids.isEmpty() && timeoutAfterKillIsSet) {
            LOGGER.info("Task is not active anymore, processing kill remote pids!");
            // if task is not running anymore but remote pid is still available
            // and a timeout_kill_after is set --> terminate remote pid
            // if timeout_kill_after is set, try terminate first and kill after
            // timeout
            allParams.put(PARAM_SSH_JOB_TIMEOUT_KILL_AFTER, "");
            sshJob = executeTerminatePids();
            return true;
        } else if (!taskIsActive && (runningPids == null || runningPids.isEmpty())) {
            // if task is not running anymore AND remote pid is not available anymore --> do nothing
            LOGGER.info("Task is not active anymore, remote pids not available anymore. Nothing to do!");
            return true;
        }
        return true;
    }

    private boolean isTaskActive(String taskId) {
        String showTaskXml = new String("<show_task id=\"" + taskId + "\"/>");
        String showTaskAnswerXml = spooler.execute_xml(showTaskXml);
        LOGGER.debug("showTaskAnswer:\n" + showTaskAnswerXml);
        return showTaskAnswerXml.contains("state=\"running");
    }

    private SOSSSHJob2 executeCheckPids() {
        SOSSSHJob2 sshJob = null;
        SOSSSHJobOptions options = null;
        try {
            sshJob = new SOSSSHCheckRemotePidJob();
            LOGGER.debug("SOSSSHCheckRemotePidJob instantiated!");
            options = sshJob.getOptions();
            options.setCurrentNodeName(this.getCurrentNodeName(false));
            HashMap<String, String> hsmParameters1 = getSchedulerParameterAsProperties(allParams);
            options.setAllOptions(options.deletePrefix(hsmParameters1, "ssh_"));
            sshJob.setJSJobUtilites(this);
            options.checkMandatory();
            sshJob.execute();
        } catch (Exception e) {
            if (options.raiseExceptionOnError.value()) {
                if (options.ignoreError.value()) {
                    if (options.ignoreStderr.value()) {
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
        }
        return sshJob;
    }

    private SOSSSHJob2 executeKillPids() {
        SOSSSHJob2 sshJob = null;
        SOSSSHJobOptions options = null;
        try {
            sshJob = new SOSSSHKillRemotePidJob();
            LOGGER.debug("SOSSSHKillRemotePidJob instantiated!");
            options = sshJob.getOptions();
            options.setCurrentNodeName(this.getCurrentNodeName(false));
            HashMap<String, String> hsmParameters1 = getSchedulerParameterAsProperties(allParams);
            options.setAllOptions(options.deletePrefix(hsmParameters1, "ssh_"));
            sshJob.setJSJobUtilites(this);
            options.checkMandatory();
            sshJob.execute();
        } catch (Exception e) {
            if (options.raiseExceptionOnError.value()) {
                if (options.ignoreError.value()) {
                    if (options.ignoreStderr.value()) {
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
        }
        return sshJob;
    }

    private SOSSSHJob2 executeTerminatePids() {
        SOSSSHJob2 sshJob = null;
        SOSSSHJobOptions options = null;
        try {
            sshJob = new SOSSSHTerminateRemotePidJob();
            LOGGER.debug("SOSSSHTerminateRemotePidJob instantiated!");
            options = sshJob.getOptions();
            options.setCurrentNodeName(this.getCurrentNodeName(false));
            HashMap<String, String> hsmParameters1 = getSchedulerParameterAsProperties(allParams);
            options.setAllOptions(options.deletePrefix(hsmParameters1, "ssh_"));
            sshJob.setJSJobUtilites(this);
            options.checkMandatory();
            sshJob.execute();
        } catch (Exception e) {
            if (options.raiseExceptionOnError.value()) {
                if (options.ignoreError.value()) {
                    if (options.ignoreStderr.value()) {
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
        }
        return sshJob;
    }

}