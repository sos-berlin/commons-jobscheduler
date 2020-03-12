package sos.scheduler.job;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;


import sos.net.ssh.SOSSSHJobOptions;
import sos.scheduler.job.impl.SOSSSHCheckRemotePidJob;
import sos.scheduler.job.impl.SOSSSHKillRemotePidJob;
import sos.scheduler.job.impl.SOSSSHTerminateRemotePidJob;
import sos.spooler.Order;
import sos.spooler.Variable_set;

public class SOSSSHKillJobJSAdapter extends JobSchedulerJobAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSSHKillJobJSAdapter.class);

    private static final String PARAM_PIDS_TO_KILL = "PIDS_TO_KILL";
    private static final String PARAM_SSH_JOB_TASK_ID = "SSH_JOB_TASK_ID";
    private static final String PARAM_SSH_JOB_NAME = "SSH_JOB_NAME";
    private static final String PARAM_SSH_JOB_TIMEOUT_KILL_AFTER = "ssh_job_timeout_kill_after";
    private HashMap<String, String> allParams;

    @Override
    public boolean spooler_process() throws Exception {
        try {
            super.spooler_process();
            doProcessing();

            return getSpoolerProcess().getSuccess();
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
            throw new JobSchedulerException(e);
        }
    }

    private boolean doProcessing() throws Exception {
        allParams = getGlobalSchedulerParameters();
        allParams.putAll(getJobOrOrderParameters(getSpoolerProcess().getOrder()));
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

        String currentNodeName = getCurrentNodeName(getSpoolerProcess().getOrder(), false);
        SOSSSHCheckRemotePidJob job = executeCheckPids(currentNodeName);
        if (job.getPids() != null) {
            LOGGER.trace(job.getPids());
            if (getSpoolerProcess().getOrder() != null) {
                Variable_set orderParams = getSpoolerProcess().getOrder().params();
                if (orderParams != null) {
                    orderParams.set_var(PARAM_PIDS_TO_KILL, job.getPids());
                }
            }
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
            String killTaskXml = new String("<kill_task job=\"" + allParams.get(PARAM_SSH_JOB_NAME) + "\" id=\"" + allParams.get(
                    PARAM_SSH_JOB_TASK_ID) + "\" immediately=\"yes\"/>");
            String killTaskXmlAnswer = spooler.execute_xml(killTaskXml);
            LOGGER.debug("killTaskXmlAnswer:\n" + killTaskXmlAnswer);
            return true;
        } else if (!taskIsActive && runningPids != null && !runningPids.isEmpty() && !timeoutAfterKillIsSet) {
            LOGGER.info("Task is not active anymore, processing kill remote pids!");
            // if task is not running anymore but remote pid is still available
            // and a timeout_kill_after is not set --> kill remote pid immediately
            executeKillPids(currentNodeName);
            return true;
        } else if (!taskIsActive && runningPids != null && !runningPids.isEmpty() && timeoutAfterKillIsSet) {
            LOGGER.info("Task is not active anymore, processing kill remote pids!");
            // if task is not running anymore but remote pid is still available
            // and a timeout_kill_after is set --> terminate remote pid
            // if timeout_kill_after is set, try terminate first and kill after
            // timeout
            allParams.put(PARAM_SSH_JOB_TIMEOUT_KILL_AFTER, "");
            executeTerminatePids(currentNodeName);
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

    private SOSSSHCheckRemotePidJob executeCheckPids(String nodeName) throws Exception {
        SOSSSHCheckRemotePidJob job = new SOSSSHCheckRemotePidJob();
        job.setJSJobUtilites(this);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(job.getClass().getSimpleName() + " instantiated!");
        }
        SOSSSHJobOptions options = job.getOptions();
        options.setCurrentNodeName(nodeName);
        HashMap<String, String> params = getSchedulerParameterAsProperties(allParams);
        options.setAllOptions(options.deletePrefix(params, "ssh_"));
        options.checkMandatory();

        job.execute();
        return job;
    }

    private SOSSSHKillRemotePidJob executeKillPids(String nodeName) throws Exception {
        SOSSSHKillRemotePidJob job = new SOSSSHKillRemotePidJob();
        job.setJSJobUtilites(this);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(job.getClass().getSimpleName() + " instantiated!");
        }
        SOSSSHJobOptions options = job.getOptions();
        options.setCurrentNodeName(nodeName);
        HashMap<String, String> params = getSchedulerParameterAsProperties(allParams);
        options.setAllOptions(options.deletePrefix(params, "ssh_"));
        options.checkMandatory();

        job.execute();
        return job;
    }

    private SOSSSHTerminateRemotePidJob executeTerminatePids(String nodeName) throws Exception {
        SOSSSHTerminateRemotePidJob job = new SOSSSHTerminateRemotePidJob();
        job.setJSJobUtilites(this);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(job.getClass().getSimpleName() + " instantiated!");
        }
        SOSSSHJobOptions options = job.getOptions();
        options.setCurrentNodeName(nodeName);
        HashMap<String, String> params = getSchedulerParameterAsProperties(allParams);
        options.setAllOptions(options.deletePrefix(params, "ssh_"));
        options.checkMandatory();

        job.execute();
        return job;
    }

}