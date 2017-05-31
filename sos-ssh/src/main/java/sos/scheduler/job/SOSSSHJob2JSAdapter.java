package sos.scheduler.job;

import static com.sos.scheduler.messages.JSMessages.JSJ_F_0060;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import sos.net.ssh.SOSSSHJob2;
import sos.net.ssh.SOSSSHJobJSch;
import sos.net.ssh.SOSSSHJobOptions;
import sos.net.ssh.SOSSSHJobTrilead;
import sos.spooler.Job_chain;
import sos.spooler.Order;
import sos.spooler.Variable_set;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class SOSSSHJob2JSAdapter extends SOSSSHJob2JSBaseAdapter {

    private static final String PARAM_SSH_JOB_TASK_ID = "SSH_JOB_TASK_ID";
    private static final String PARAM_SSH_JOB_NAME = "SSH_JOB_NAME";
    private static final String PARAM_JITL_SSH_USE_JSCH_IMPL = "jitl.ssh.use_jsch_impl";
    private static final String PARAM_PID_FILE_NAME_KEY = "job_ssh_pid_file_name";
    private static final String PARAM_SCHEDULER_VARIABLE_NAME_PREFIX = "scheduler.variable_name_prefix";
    private static final String PARAM_SCHEDULER_VARIABLE_PREFIX_NONE_VALUE = "*NONE";
    private static final String PARAM_SCHEDULER_VARIABLE_PREFIX_DEFAULT_VALUE = "SCHEDULER_PARAM_";
    private static final String PARAM_SCHEDULER_VARIABLE_PREFIX_MASTER = "SCHEDULER_MASTER_";
    private static final String PARAM_SCHEDULER_VARIABLE_STARTS_WITH = "SCHEDULER_";
    private static final String PARAM_CLEANUP_JOBCHAIN = "cleanupJobchain";
    private static final String STD_ERR_OUTPUT = "std_err_output";
    private static final String STD_OUT_OUTPUT = "std_out_output";
    private static final String EXIT_CODE = "exit_code";
    private static final String EXIT_SIGNAL = "exit_signal";
    private static final Logger LOGGER = Logger.getLogger(SOSSSHJob2JSAdapter.class);
    private boolean useTrilead = true;
    private String pidFileName;
    private String envVarNamePrefix;

    @Override
    public boolean spooler_process() throws Exception {
        try {
            super.spooler_process();
            doProcessing();
        } catch (Exception e) {
            LOGGER.fatal(stackTrace2String(e));
            throw new JobSchedulerException(e);
        }
        return signalSuccess();
    }

    private void doProcessing() throws Exception {
        if (this.isOrderJob()) {
            spooler_task.order().params().set_var(EXIT_SIGNAL, "");
            spooler_task.order().params().set_var(EXIT_CODE, "");
            spooler_task.order().params().set_var(STD_ERR_OUTPUT, "");
            spooler_task.order().params().set_var(STD_OUT_OUTPUT, "");
        }
        spooler_task.params().set_var(EXIT_SIGNAL, "");
        spooler_task.params().set_var(EXIT_CODE, "");
        spooler_task.params().set_var(STD_ERR_OUTPUT, "");
        spooler_task.params().set_var(STD_OUT_OUTPUT, "");
        SOSSSHJob2 objR;
        String useJSch = spooler.var(PARAM_JITL_SSH_USE_JSCH_IMPL);
        envVarNamePrefix = spooler.var(PARAM_SCHEDULER_VARIABLE_NAME_PREFIX);
        if (envVarNamePrefix == null || envVarNamePrefix.isEmpty()) {
            envVarNamePrefix = PARAM_SCHEDULER_VARIABLE_PREFIX_DEFAULT_VALUE;
        }
        SOSSSHJobOptions objO = null;
        if ("false".equalsIgnoreCase(useJSch)) {
            // this is the default value since v1.10 [SP]
            useTrilead = true;
            objR = new SOSSSHJobTrilead();
            objO = objR.getOptions();
            spooler_log.debug9("uses Trilead implementation of SSH");
        } else {
            useTrilead = false;
            objR = new SOSSSHJobJSch();
            objO = objR.getOptions();
            spooler_log.debug9("uses JSch implementation of SSH");
        }
        objO.setCurrentNodeName(this.getCurrentNodeName(true));
        HashMap<String, String> hsmParameters1 = getSchedulerParameterAsProperties(getJobOrOrderParameters());
        if (!useTrilead && !"false".equalsIgnoreCase(hsmParameters1.get("create_environment_variables"))) {
            Map<String, String> allEnvVars = new HashMap<String, String>();
            Map<String, String> schedulerMasterEnvVars = getSchedulerEnvironmentVariables();
            if (schedulerMasterEnvVars != null) {
                allEnvVars.putAll(schedulerMasterEnvVars);
            }
            allEnvVars.putAll(prefixSchedulerEnvVars(hsmParameters1));
            ((SOSSSHJobJSch) objR).setSchedulerEnvVars(allEnvVars);
        }
        objO.setAllOptions(objO.deletePrefix(hsmParameters1, "ssh_"));
        objR.setJSJobUtilites(this);
        if (!objO.commandSpecified()) {
            setJobScript(objO.commandScript);
        }
        objO.checkMandatory();
        if (!useTrilead) {
            spooler_log.debug9("Run with watchdog set to: " + objO.runWithWatchdog.getValue());
            if (objO.runWithWatchdog.value()) {
                pidFileName = generateTempPidFileName();
                ((SOSSSHJobJSch) objR).setPidFileName(pidFileName);
                createOrderForWatchdog();
            }
        }
        objR.execute();
        if (!useTrilead && !((SOSSSHJobJSch) objR).getReturnValues().isEmpty()) {
            for (String key : ((SOSSSHJobJSch) objR).getReturnValues().keySet()) {
                spooler_task.order().params().set_var(key, ((SOSSSHJobJSch) objR).getReturnValues().get(key));
            }
        }
    }

    private void createOrderForWatchdog() {
        spooler_log.debug9("createOrderForWatchdog started");
        Order order = spooler.create_order();
        order.params().merge(spooler_task.params());
        if (spooler_task.order() != null) {
            order.params().merge(spooler_task.order().params());
        }
        order.params().set_var(PARAM_SSH_JOB_TASK_ID, String.valueOf(spooler_task.id()));
        order.params().set_var(PARAM_PID_FILE_NAME_KEY, pidFileName);
        order.params().set_var(PARAM_SSH_JOB_NAME, spooler_job.name());
        order.set_at("now+15");
        Job_chain chain = null;
        if (spooler_task.params().value(PARAM_CLEANUP_JOBCHAIN) != null) {
            chain = spooler.job_chain(spooler_task.params().value(PARAM_CLEANUP_JOBCHAIN));
            spooler_log.debug9("uses jobchainname from parameter");
            spooler_log.debug9("Jobchainname: " + spooler_task.params().value(PARAM_CLEANUP_JOBCHAIN));
        } else {
            LOGGER.error("No jobchain configured to received the order! Please configure the cleanupJobchain Parameter in your SSH Job Configuration.");
        }
        chain.add_or_replace_order(order);
        spooler_log.debug9("order send");
    }

    private String generateTempPidFileName() {
        UUID uuid = UUID.randomUUID();
        return "sos-ssh-pid-" + uuid + ".txt";
    }

    public Map<String, String> getSchedulerEnvironmentVariables() {
        String os = System.getProperty("os.name").toLowerCase();
        boolean win = false;
        Map<String, String> envvars = new HashMap<String, String>();
        if (os.indexOf("nt") > -1 || os.indexOf("windows") > -1) {
            win = true;
        }
        Variable_set env;
        try {
            env = spooler_task.create_subprocess().env();
        } catch (Exception e) {
            return null;
        }
        StringTokenizer t = new StringTokenizer(env.names(), ";");
        while (t.hasMoreTokens()) {
            String envname = t.nextToken();
            String envvalue = env.value(envname);
            envvars.put(PARAM_SCHEDULER_VARIABLE_PREFIX_MASTER + envname, envvalue);
        }
        return envvars;
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
                            spooler_log.debug9("node name [" + currentNodeName + "] stripped from parameter name!");
                        } else if (Pattern.compile("\\W").matcher(key).find()) {
                            spooler_log.debug6("Parameter [" + key + "] not exported! Belongs to different node OR has special characters!");
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

    @Override
    // TO DO remove if process is reviewed and fixed! Original in JobSchedulerJobAdapter puts duplicated key/Value pair
        protected
        HashMap<String, String> convertVariableSet2HashMap(final Variable_set variableSet) {
        HashMap<String, String> result = new HashMap<String, String>();
        try {
            if (isNotNull(variableSet)) {
                String[] names = variableSet.names().split(";");
                String value = EMPTY_STRING;
                for (String key : names) {
                    value = EMPTY_STRING;
                    Object objO = variableSet.var(key);
                    if (objO instanceof String) {
                        value = variableSet.var(key);
                    } else if (objO instanceof Integer) {
                        Integer intI = (Integer) objO;
                        value = intI.toString();
                    }
                    result.put(key, value);
                }
            }
            return result;
        } catch (Exception e) {
            throw new JobSchedulerException(JSJ_F_0060.params(stackTrace2String(e)), e);
        }
    }

}
