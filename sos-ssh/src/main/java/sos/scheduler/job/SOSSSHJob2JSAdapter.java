package sos.scheduler.job;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

import sos.net.ssh.SOSSSHJobOptions;
import sos.scheduler.job.impl.SOSSSHJob;
import sos.spooler.Job_chain;
import sos.spooler.Order;
import sos.spooler.Variable_set;

public class SOSSSHJob2JSAdapter extends JobSchedulerJobAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSSHJob2JSAdapter.class);

    private static final String PARAM_SSH_JOB_TASK_ID = "SSH_JOB_TASK_ID";
    private static final String PARAM_SSH_JOB_NAME = "SSH_JOB_NAME";
    private static final String PARAM_PID_FILE_NAME_KEY = "job_ssh_pid_file_name";
    private static final String PARAM_SCHEDULER_VARIABLE_NAME_PREFIX = "scheduler.variable_name_prefix";
    private static final String PARAM_SCHEDULER_VARIABLE_PREFIX_NONE_VALUE = "*NONE";
    private static final String PARAM_SCHEDULER_VARIABLE_PREFIX_DEFAULT_VALUE = "SCHEDULER_PARAM_";
    private static final String PARAM_SCHEDULER_VARIABLE_PREFIX_MASTER = "SCHEDULER_MASTER_";
    private static final String PARAM_SCHEDULER_VARIABLE_STARTS_WITH = "SCHEDULER_";
    private static final String PARAM_CLEANUP_JOBCHAIN = "cleanupJobchain";
    private static final String EXIT_CODE = "exit_code";
    private static final String EXIT_SIGNAL = "exit_signal";

    private String envVarNamePrefix;

    @Override
    public boolean spooler_process() throws Exception {
        try {
            super.spooler_process();
            doProcessing();
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
            throw new JobSchedulerException(e);
        }
        return signalSuccess();
    }

    private void doProcessing() throws Exception {
        Variable_set taskParams = spooler_task.params();
        Variable_set orderParams = null;
        boolean isOrderJob = spooler_task.job().order_queue() != null;
        if (isOrderJob) {
            orderParams = spooler_task.order().params();
            orderParams.set_var(EXIT_SIGNAL, "");
            orderParams.set_var(EXIT_CODE, "");
        }
        taskParams.set_var(EXIT_SIGNAL, "");
        taskParams.set_var(EXIT_CODE, "");

        envVarNamePrefix = spooler.var(PARAM_SCHEDULER_VARIABLE_NAME_PREFIX);
        if (envVarNamePrefix == null || envVarNamePrefix.isEmpty()) {
            envVarNamePrefix = PARAM_SCHEDULER_VARIABLE_PREFIX_DEFAULT_VALUE;
        }

        SOSSSHJob job = new SOSSSHJob();
        SOSSSHJobOptions options = job.getOptions();

        options.setCurrentNodeName(this.getCurrentNodeName(true));
        HashMap<String, String> params = getSchedulerParameterAsProperties();
        if (!"false".equalsIgnoreCase(params.get("create_environment_variables"))) {
            Map<String, String> envVars = new HashMap<String, String>();
            Map<String, String> schedulerMasterEnvVars = getSchedulerEnvironmentVariables();
            if (schedulerMasterEnvVars != null) {
                envVars.putAll(schedulerMasterEnvVars);
            }
            envVars.putAll(prefixSchedulerEnvVars(params));
            job.setSchedulerEnvVars(envVars);
        }
        options.setAllOptions(options.deletePrefix(params, "ssh_"));
        job.setJSJobUtilites(this);
        if (!options.commandSpecified()) {
            setJobScript(options.commandScript);
        }
        options.checkMandatory();
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Run with watchdog set to: " + options.runWithWatchdog.getValue());
        }
        if (options.runWithWatchdog.value()) {
            createOrderForWatchdog(job);
        }
        job.execute();
        if (isOrderJob) {
            if (!job.getReturnValues().isEmpty()) {
                for (Entry<String, String> entry : job.getReturnValues().entrySet()) {
                    orderParams.set_var(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    private void createOrderForWatchdog(SOSSSHJob job) {
        LOGGER.trace("createOrderForWatchdog started");

        Order order = spooler.create_order();
        Variable_set orderParams = order.params();
        Variable_set taskParams = spooler_task.params();

        orderParams.merge(taskParams);
        if (spooler_task.order() != null) {
            orderParams.merge(spooler_task.order().params());
        }
        orderParams.set_var(PARAM_SSH_JOB_TASK_ID, String.valueOf(spooler_task.id()));
        orderParams.set_var(PARAM_PID_FILE_NAME_KEY, job.getPidFileName());
        orderParams.set_var(PARAM_SSH_JOB_NAME, spooler_job.name());
        order.set_at("now+15");
        Job_chain chain = null;

        String cleanupJobChain = taskParams.value(PARAM_CLEANUP_JOBCHAIN);
        if (cleanupJobChain != null) {
            chain = spooler.job_chain(cleanupJobChain);
            LOGGER.trace("uses JobChain from parameter \"cleanupJobchain\": " + cleanupJobChain);
        } else {
            LOGGER.error(
                    "No JobChain configured to received the order! Please configure the \"cleanupJobchain\" parameter in your SSH Job Configuration.");
        }
        chain.add_or_replace_order(order);

        LOGGER.trace("order send");
    }

    public Map<String, String> getSchedulerEnvironmentVariables() {
        Map<String, String> envVars = new HashMap<String, String>();
        Variable_set env;
        try {
            env = spooler_task.create_subprocess().env();
        } catch (Exception e) {
            return null;
        }
        StringTokenizer t = new StringTokenizer(env.names(), ";");
        while (t.hasMoreTokens()) {
            String name = t.nextToken();
            String value = env.value(name);
            envVars.put(PARAM_SCHEDULER_VARIABLE_PREFIX_MASTER + name, value);
        }
        return envVars;
    }

    private Map<String, String> prefixSchedulerEnvVars(Map<String, String> allEnvVars) {
        Map<String, String> envVars = new HashMap<String, String>();
        String currentNodeName = getCurrentNodeName(false);
        for (String key : allEnvVars.keySet()) {
            String value = allEnvVars.get(key);
            if (value.contains("\"")) {
                value = value.replaceAll("\"", "\\\"");
            }
            if (!"password".equalsIgnoreCase(key)) {
                if (!key.startsWith(PARAM_SCHEDULER_VARIABLE_STARTS_WITH)) {
                    if (!PARAM_SCHEDULER_VARIABLE_PREFIX_NONE_VALUE.equalsIgnoreCase(envVarNamePrefix)) {
                        if (isActiveNodeParam(key, currentNodeName)) {
                            String parameterName = key.substring(currentNodeName.length() + 1);
                            if (!"password".equalsIgnoreCase(parameterName)) {
                                envVars.put(envVarNamePrefix + parameterName, value);
                            }
                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace("node name [" + currentNodeName + "] stripped from parameter name!");
                            }
                        } else if (Pattern.compile("\\W").matcher(key).find()) {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Parameter [" + key + "] not exported! Belongs to different node OR has special characters!");
                            }
                        } else {
                            envVars.put(envVarNamePrefix + key, value);
                        }
                    } else {
                        envVars.put(key, value);
                    }
                } else if (key.startsWith(PARAM_SCHEDULER_VARIABLE_STARTS_WITH) && !key.startsWith(PARAM_SCHEDULER_VARIABLE_PREFIX_DEFAULT_VALUE)
                        && !key.startsWith(PARAM_SCHEDULER_VARIABLE_PREFIX_MASTER)) {
                    envVars.put(PARAM_SCHEDULER_VARIABLE_PREFIX_MASTER + key, value);
                } else {
                    envVars.put(key, value);
                }
            }
        }
        return envVars;
    }

    private boolean isActiveNodeParam(String key, String currentNodeName) {
        if (key.startsWith(currentNodeName + "/")) {
            return true;
        }
        return false;
    }

    // TODO remove if process is reviewed and fixed! Original in JobSchedulerJobAdapter puts duplicated key/Value pair
    @Override
    protected HashMap<String, String> convertVariableSet2HashMap(final Variable_set params) {
        HashMap<String, String> result = new HashMap<String, String>();
        try {
            if (isNotNull(params)) {
                String[] names = params.names().split(";");
                String value = EMPTY_STRING;
                for (String key : names) {
                    value = EMPTY_STRING;
                    Object val = params.var(key);
                    if (val instanceof String) {
                        value = params.var(key);
                    } else if (val instanceof Integer) {
                        value = ((Integer) val).toString();
                    }
                    result.put(key, value);
                }
            }
            return result;
        } catch (Exception e) {
            throw new JobSchedulerException(e.toString(), e);
        }
    }

}
