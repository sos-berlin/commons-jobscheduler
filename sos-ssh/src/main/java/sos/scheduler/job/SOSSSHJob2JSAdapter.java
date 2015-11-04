package sos.scheduler.job;

import static com.sos.scheduler.messages.JSMessages.JSJ_F_0060;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.regex.Pattern;

import sos.net.ssh.SOSSSHJob2;
import sos.net.ssh.SOSSSHJobJSch;
import sos.net.ssh.SOSSSHJobOptions;
import sos.net.ssh.SOSSSHJobTrilead;
import sos.spooler.Job_chain;
import sos.spooler.Order;
import sos.spooler.Variable_set;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class SOSSSHJob2JSAdapter extends SOSSSHJob2JSBaseAdapter {
    private final String conClassName = this.getClass().getSimpleName();
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
    private boolean useTrilead = true;
    private String pidFileName;
    private boolean envVarsCaseSensitive = true;
    private String envVarNamePrefix;

    @Override
    public boolean spooler_process() throws Exception {
        try {
            super.spooler_process();
            doProcessing();
        } catch (Exception e) {
            logger.fatal(StackTrace2String(e));
            throw new JobSchedulerException(e);
        }
        return signalSuccess();
    }

    private void doProcessing() throws Exception {
        SOSSSHJob2 objR;
        String useJSch = spooler.var(PARAM_JITL_SSH_USE_JSCH_IMPL);
        envVarNamePrefix = spooler.var(PARAM_SCHEDULER_VARIABLE_NAME_PREFIX);
        if (envVarNamePrefix == null || envVarNamePrefix.isEmpty()) {
            envVarNamePrefix = PARAM_SCHEDULER_VARIABLE_PREFIX_DEFAULT_VALUE;
        }
        SOSSSHJobOptions objO = null;
        if (!"true".equalsIgnoreCase(useJSch)) {
            // this is the default value for v1.9, will change to JSch with
            // v1.10 [SP]
            useTrilead = true;
            objR = new SOSSSHJobTrilead();
            objO = objR.Options();
            spooler_log.debug9("uses Trilead implementation of SSH");
        } else {
            useTrilead = false;
            objR = new SOSSSHJobJSch();
            objO = objR.Options();
            spooler_log.debug9("uses JSch implementation of SSH");
        }
        objO.CurrentNodeName(this.getCurrentNodeName());
        HashMap<String, String> hsmParameters1 = getSchedulerParameterAsProperties(getJobOrOrderParameters());
        if (!useTrilead) {
            ((SOSSSHJobJSch) objR).setAllParams(hsmParameters1);
            if(objO.getCreateEnvironmentVariables().isTrue()){
                Map allEnvVars = new HashMap();
                allEnvVars.putAll(getSchedulerEnvironmentVariables());
                allEnvVars.putAll(prefixSchedulerEnvVars(hsmParameters1));
                ((SOSSSHJobJSch) objR).setSchedulerEnvVars(allEnvVars);
            }
        }
        objO.setAllOptions(objO.DeletePrefix(hsmParameters1, "ssh_"));
        objR.setJSJobUtilites(this);
        if (!objO.commandSpecified()) {
            setJobScript(objO.command_script);
        }
        objO.CheckMandatory();
        if (!useTrilead) {
            // generate temporary file for remote pids for further usage
            spooler_log.debug9("Run with watchdog set to: " + objO.runWithWatchdog.Value());
            if (objO.runWithWatchdog.value()) {
                pidFileName = generateTempPidFileName();
                ((SOSSSHJobJSch) objR).setPidFileName(pidFileName);
                createOrderForWatchdog();
            }
        }
        // if command_delimiter is not set by customer then we override the
        // default value due to compatibility issues
        // the default command delimiter is used in the option class to split
        // the commands with a delimiter not known by the os
        // but here a command delimiter (known by the os) is needed to chain
        // commands together
        // TO DO: a solution which fits for both cases [SP]
        if (!useTrilead && objO.command_delimiter.isNotDirty()) {
            objO.command_delimiter.Value(";");
        }
        objR.Execute();
        if (!useTrilead && !((SOSSSHJobJSch) objR).getReturnValues().isEmpty()) {
            for (String key : ((SOSSSHJobJSch) objR).getReturnValues().keySet()) {
                spooler_task.order().params().set_var(key, ((SOSSSHJobJSch) objR).getReturnValues().get(key));
            }
        }

    }

    // creates a new order for the cleanup jobchain with all the options,
    // params, values and the TaskId of the task which created this
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
        // delayed start after 15 seconds when the order is created
        order.set_at("now+15");
        Job_chain chain = null;
        if (spooler_task.params().value(PARAM_CLEANUP_JOBCHAIN) != null) {
            chain = spooler.job_chain(spooler_task.params().value(PARAM_CLEANUP_JOBCHAIN));
            spooler_log.debug9("uses jobchainname from parameter");
            spooler_log.debug9("Jobchainname: " + spooler_task.params().value(PARAM_CLEANUP_JOBCHAIN));
        } else {
            logger.error("No jobchain configured to received the order! Please configure the cleanupJobchain Parameter in your SSH Job Configuration.");
        }
        chain.add_or_replace_order(order);
        spooler_log.debug9("order send");
    }

    private String generateTempPidFileName() {
        UUID uuid = UUID.randomUUID();
        return "sos-ssh-pid-" + uuid + ".txt";
    }

    public Map getSchedulerEnvironmentVariables() {
        String os = System.getProperty("os.name").toLowerCase();
        boolean win = false;
        Map envvars = new HashMap();
        if (os.indexOf("nt") > -1 || os.indexOf("windows") > -1) {
            win = true;
            envVarsCaseSensitive = false;
        }
        Variable_set env = spooler_task.create_subprocess().env();
        StringTokenizer t = new StringTokenizer(env.names(), ";");
        while (t.hasMoreTokens()) {
            String envname = t.nextToken();
            String envvalue = env.value(envname);
            if (envname != null && envname.startsWith("SCHEDULER_")) {
                if (win) {
                    envvars.put(PARAM_SCHEDULER_VARIABLE_PREFIX_MASTER + envname.toUpperCase(), envvalue);
                } else {
                    envvars.put(PARAM_SCHEDULER_VARIABLE_PREFIX_MASTER + envname, envvalue);
                }
            }
        }
        return envvars;
    }

    private Map<String, String> prefixSchedulerEnvVars(Map<String, String> allEnvVars) {
        Map<String, String> envVars = new HashMap<String, String>();
        // JTIL-224
        String currentNodeName = getCurrentNodeName();
        for (String key : allEnvVars.keySet()) {
            if (!"password".equalsIgnoreCase(key)) {
                if (!key.startsWith(PARAM_SCHEDULER_VARIABLE_STARTS_WITH)) {
                    if (!PARAM_SCHEDULER_VARIABLE_PREFIX_NONE_VALUE.equalsIgnoreCase(envVarNamePrefix)) {
                        if (isActiveNodeParam(key, currentNodeName)) {
                            String parameterName = key.substring(currentNodeName.length() + 1);
                            if (!"password".equalsIgnoreCase(parameterName)) {
                                envVars.put(envVarNamePrefix + parameterName, allEnvVars.get(key));
                            }
                            spooler_log.debug9("node name [" + currentNodeName + "] stripped from parameter name!");
                        } else if (Pattern.compile("\\W").matcher(key).find()) { 
                            // is not active AND contains special characters
                            spooler_log.debug6("Parameter [" + key + "] not exported! Belongs to different node OR has special characters!");
                        } else {
                            envVars.put(envVarNamePrefix + key, allEnvVars.get(key));
                        }
                    } else {
                        envVars.put(key, allEnvVars.get(key));
                    }
                } else if (key.startsWith(PARAM_SCHEDULER_VARIABLE_STARTS_WITH) && !key.startsWith(PARAM_SCHEDULER_VARIABLE_PREFIX_DEFAULT_VALUE)
                        && !key.startsWith(PARAM_SCHEDULER_VARIABLE_PREFIX_MASTER)) {
                    envVars.put(PARAM_SCHEDULER_VARIABLE_PREFIX_MASTER + key, allEnvVars.get(key));
                } else {
                    envVars.put(key, allEnvVars.get(key));
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
    protected HashMap<String, String> convertVariableSet2HashMap(final Variable_set variableSet) {
        HashMap<String, String> result = new HashMap<String, String>();
        try {
            if (isNotNull(variableSet)) {
                String[] names = variableSet.names().split(";");
                String value = EMPTY_STRING;
                for (String key : names) {
                    value = EMPTY_STRING;
                    /**
                     * the variable_set is able to handle the value of a
                     * variable as an Object. In Java this class (the HashMap)
                     * is defined as <String,String> but it is possible e.g. in
                     * JavaScript to set a value as any object.
                     *
                     * Values with other types than string and integer are
                     * ignored, e.g. the value of this parameter is set to
                     * space.
                     */
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
            throw new JobSchedulerException(JSJ_F_0060.params(StackTrace2String(e)), e);
        }
    }

}
