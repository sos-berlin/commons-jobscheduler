package sos.scheduler.job;

import static com.sos.scheduler.messages.JSMessages.JSJ_D_0070;
import static com.sos.scheduler.messages.JSMessages.JSJ_F_0050;
import static com.sos.scheduler.messages.JSMessages.JSJ_F_0060;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_0010;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_0020;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.IJSCommands;
import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Basics.VersionInfo;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionElement;
import com.sos.JSHelper.interfaces.IJobSchedulerEventHandler;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.localization.Messages;

import sos.scheduler.misc.SpoolerProcess;
import sos.spooler.IMonitor_impl;
import sos.spooler.Job;
import sos.spooler.Job_chain;
import sos.spooler.Job_chain_node;
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.util.ParameterSubstitutor;

@I18NResourceBundle(baseName = "com_sos_scheduler_messages", defaultLocale = "en")
public class JobSchedulerJobAdapter extends JobSchedulerJob implements JSJobUtilities, IJSCommands, IMonitor_impl, IJobSchedulerEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerJobAdapter.class);
    protected final String EMPTY_STRING = "";

    private static final String MESSAGES_BUNDLE_NAME = "com_sos_scheduler_messages";
    private static final int MAX_LENGTH_OF_STATUSTEXT = 100;

    private Messages messages = null;
    private IJobSchedulerEventHandler eventHandler = null;
    private SpoolerProcess spoolerProcess = null;
    private HashMap<String, String> taskParams = null;
    private HashMap<String, String> globalSchedulerParams = null;
    private HashMap<String, String> schedulerParameters = new HashMap<String, String>();
    private ParameterSubstitutor parameterSubstitutor;

    public JobSchedulerJobAdapter() {
        messages = new Messages(MESSAGES_BUNDLE_NAME, Locale.getDefault());
    }

    @Override
    public boolean spooler_process() throws Exception {
        try {
            LOGGER.info(VersionInfo.VERSION_STRING);

            if (spoolerProcess == null) {
                spoolerProcess = new SpoolerProcess(spooler_job);
            }
            spoolerProcess.setOrder(spooler_task.order());
            return spoolerProcess.isOrderJob();
        } catch (Throwable e) {
            return false;
        }
    }

    private Map<String, String> getSpecialVariables() {

        Map<String, String> specialVariables = new HashMap<String, String>();
        if (spooler_task != null) {
            setJobId(spooler_task.id());
        }
        if (spooler_job != null) {
            String jobName = spooler_job.name();

            setJobName(jobName);
            setJobFolder(spooler_job.folder_path());
            setJobTitle(spooler_job.title());
        }

        specialVariables.put("SCHEDULER_JOB_FOLDER", this.getJobFolder());
        specialVariables.put("SCHEDULER_JOB_PATH", this.getJobFolder() + "/" + this.getJobName());

        specialVariables.put("SCHEDULER_JOB_NAME", spooler_job.name());
        specialVariables.put("SCHEDULER_JOB_TITLE", spooler_job.title());
        specialVariables.put("SCHEDULER_TASK_ID", String.valueOf(spooler_task.id()));

        specialVariables.put("SCHEDULER_ID", String.valueOf(spooler.id()));
        if (spooler_task.order() != null) {
            specialVariables.put("SCHEDULER_JOB_CHAIN", String.valueOf(spooler_task.order().job_chain().name()));
            specialVariables.put("SCHEDULER_ORDER_ID", String.valueOf(spooler_task.order().id()));

            Job_chain jobChain = spooler_task.order().job_chain();
            if (jobChain != null) {
                Job_chain_node jobChainNode = spooler_task.order().job_chain_node();
                specialVariables.put("SCHEDULER_JOB_CHAIN_NAME", jobChain.name());
                specialVariables.put("SCHEDULER_JOB_CHAIN_TITLE", jobChain.title());
                specialVariables.put("SCHEDULER_JOB_CHAIN_PATH", jobChain.path());
                specialVariables.put("SCHEDULER_ORDER_ID", spooler_task.order().id());
                specialVariables.put("SCHEDULER_NODE_NAME", getCurrentNodeName(spooler_task.order(), false));
                specialVariables.put("SCHEDULER_NEXT_NODE_NAME", jobChainNode.next_state());
                specialVariables.put("SCHEDULER_NEXT_ERROR_NODE_NAME", jobChainNode.error_state());
            }

        }
        specialVariables.put("SCHEDULER_CONFIGURATION_DIRECTORY", String.valueOf(spooler.configuration_directory()));
        specialVariables.put("SCHEDULER_DIRECTORY", String.valueOf(spooler.directory()));
        specialVariables.put("SCHEDULER_HOST", String.valueOf(spooler.hostname()));
        specialVariables.put("SCHEDULER_TCP_PORT", String.valueOf(spooler.tcp_port()));
        specialVariables.put("SCHEDULER_UDP_PORT", String.valueOf(spooler.udp_port()));
        if (spooler.supervisor_client() != null) {
            specialVariables.put("SCHEDULER_SUPERVISOR_HOST", String.valueOf(spooler.supervisor_client().hostname()));
            specialVariables.put("SCHEDULER_SUPERVISOR_PORT", String.valueOf(spooler.supervisor_client().tcp_port()));
        } else {
            specialVariables.put("SCHEDULER_SUPERVISOR_HOST", "");
            specialVariables.put("SCHEDULER_SUPERVISOR_PORT", "");
        }
        return specialVariables;
    }

    protected HashMap<String, String> getSchedulerParameterAsProperties(final HashMap<String, String> params) {
        schedulerParameters = new HashMap<String, String>();
        try {
            if (isNotNull(params)) {
                schedulerParameters = params;

                for (String key : schedulerParameters.keySet()) {
                    String value = schedulerParameters.get(key);
                    if (value != null) {
                        String replacedValue = replaceSchedulerVars(value);
                        if (!replacedValue.equalsIgnoreCase(value)) {
                            schedulerParameters.put(key, replacedValue);
                            if (key.contains("password") || key.contains("passphrase")) {
                                LOGGER.trace(String.format("%1$s = *****", key));
                            } else {
                                LOGGER.trace(String.format("%1$s = %2$s", key, replacedValue));
                            }
                        } else {
                            if (key.contains("password") || key.contains("passphrase")) {
                                LOGGER.trace(String.format("%1$s = *****", key));
                            } else {
                                LOGGER.trace(String.format("%1$s = %2$s", key, value));
                            }
                        }
                    }
                }
            }
            schedulerParameters = deleteCurrentNodeNameFromKeys(schedulerParameters, getTaskJobName(), getCurrentNodeName(false));
            parameterSubstitutor = null;
        } catch (Exception e) {
            throw new JobSchedulerException(JSJ_F_0060.params(e.toString()), e);
        }

        return schedulerParameters;
    }

    protected HashMap<String, String> convertVariableSet2HashMap(final Variable_set params) {
        HashMap<String, String> result = new HashMap<String, String>();
        try {
            if (isNotNull(params)) {
                String[] names = params.names().split(";");
                String value = EMPTY_STRING;
                for (String key : names) {
                    if (!"".equals(key)) {
                        value = params.var(key);
                        result.put(key, value);
                    }
                }
            }
            return result;
        } catch (Exception e) {
            throw new JobSchedulerException(JSJ_F_0060.params(e.toString()), e);
        }
    }

    private String getParameterName(String currentJob, String currentNodeName, String parameterName) {
        String pattern1 = "^" + currentNodeName + "/(.*)";
        String pattern2 = "^job::" + currentJob + "/(.*)";
        String pattern3 = "^job::" + currentJob + "\\." + currentNodeName + "/(.*)";

        String newParameter = parameterName.replaceAll(pattern3, "$1");
        newParameter = newParameter.replaceAll(pattern2, "$1");
        newParameter = newParameter.replaceAll(pattern1, "$1");
        return newParameter;
    }

    private HashMap<String, String> deleteCurrentNodeNameFromKeys(final HashMap<String, String> params, String currentJob, String currentNodeName) {

        HashMap<String, String> result = new HashMap<String, String>();
        result.putAll(params);
        Set<Map.Entry<String, String>> set = params.entrySet();
        for (Map.Entry<String, String> entry : set) {
            String key = entry.getKey();
            String name = getParameterName(currentJob, currentNodeName, key);
            if (!key.equals(name)) {
                String val = entry.getValue();
                result.put(name, val);
            }
        }
        return result;
    }

    protected HashMap<String, String> getSchedulerParameterAsProperties(Order order) {
        return getSchedulerParameterAsProperties(getJobOrOrderParameters(order));
    }

    protected HashMap<String, String> getJobOrOrderParameters(Order order) {
        try {
            HashMap<String, String> params = new HashMap<String, String>();
            if (order == null) {
                taskParams = null;
            }
            params.putAll(getTaskParams(spooler_task.params()));
            if (order != null) {
                Variable_set orderParams = order.params();
                if (orderParams != null) {
                    params.putAll(convertVariableSet2HashMap(orderParams));
                }
            }
            JSJ_D_0070.toLog(params.size());
            return params;

        } catch (Exception e) {
            throw new JobSchedulerException(JSJ_F_0050.get(e), e);
        }
    }

    public HashMap<String, String> getGlobalSchedulerParameters() {
        if (globalSchedulerParams == null) {
            globalSchedulerParams = convertVariableSet2HashMap(spooler.variables());
        }
        return globalSchedulerParams;
    }

    @Override
    @Deprecated
    public void setJSParam(final String name, final String value) {
        Variable_set taskParams = spooler_task.params();
        HashMap<String, String> params = getTaskParams(taskParams);
        if (params != null) {
            params.put(name, value);
        }
        if (taskParams != null) {
            taskParams.set_var(name, value);
        }

        Order order = spoolerProcess == null ? spooler_task.order() : spoolerProcess.getOrder();
        if (order != null) {
            Variable_set orderParams = order.params();
            if (orderParams != null) {
                orderParams.set_var(name, value);
            }
        }
        if (isNotNull(schedulerParameters)) {
            schedulerParameters.put(name, value);
        }
    }

    @Override
    @Deprecated
    public void setJSParam(final String name, final StringBuilder value) {
        setJSParam(name, value.toString());
    }

    public HashMap<String, String> getTaskParams(Variable_set params) {
        if (taskParams == null) {
            taskParams = convertVariableSet2HashMap(params);
        }
        return taskParams;
    }

    private void fillParameterSubstitutor() {
        if (parameterSubstitutor == null) {

            Map<String, String> variables = new HashMap<String, String>();
            variables.putAll(schedulerParameters);
            variables.putAll(this.getSpecialVariables());

            parameterSubstitutor = new ParameterSubstitutor();
            for (Entry<String, String> entry : variables.entrySet()) {
                String name = entry.getKey();
                String value = entry.getValue();
                if (value != null && !value.isEmpty()) {
                    parameterSubstitutor.addKey(name, value);
                }
            }
        }
    }

    @Override
    public String replaceSchedulerVars(final String string2Modify) {
        String resultString = string2Modify;

        if (isNotNull(schedulerParameters)) {
            fillParameterSubstitutor();
            if (string2Modify.matches("(?s).*\\$\\{[^{]+\\}.*")) {
                parameterSubstitutor.setOpenTag("${");
                parameterSubstitutor.setCloseTag("}");
                resultString = parameterSubstitutor.replace(string2Modify);
            }

            if (resultString.contains("%")) {
                parameterSubstitutor.setOpenTag("%");
                parameterSubstitutor.setCloseTag("%");
                resultString = parameterSubstitutor.replace(resultString);
            }
        }
        return resultString;
    }

    protected JSJobUtilities getJSJobUtilities() {
        return this;
    }

    @Override
    public void setJSJobUtilites(final JSJobUtilities val) {
        //
    }

    private String getCurrentNodeName(boolean verbose) {
        return getCurrentNodeName(spoolerProcess != null ? spoolerProcess.getOrder() : spooler_task.order(), verbose);
    }

    public String getCurrentNodeName(Order order, boolean verbose) {
        final String method = "JobSchedulerJobAdapter::getCurrentNodeName";
        String name = "unknownNode";

        if (spooler_task != null) {
            if (order != null) {
                if (spoolerProcess != null) {
                    if (spoolerProcess.getCurrentOrderState() == null) {
                        spoolerProcess.setCurrentOrderState(order.state());
                    }
                    name = spoolerProcess.getCurrentOrderState();
                } else {
                    name = order.state();
                }
                if (verbose) {
                    JSJ_I_0020.toLog(method, name);
                }
            } else {
                name = getTaskJobName();
                if (verbose) {
                    JSJ_I_0010.toLog(method, name);
                }
            }
        }
        return name;
    }

    @Override
    public Object getSpoolerObject() {
        return spooler;
    }

    @Override
    public String executeXML(final String xml) {
        return spooler.execute_xml(xml);
    }

    @Override
    public void setNextNodeState(final String state) {
        Order order = spoolerProcess == null ? spooler_task.order() : spoolerProcess.getOrder();
        if (order != null) {
            order.set_state(state);
        }
    }

    public boolean isNotNull(final Object val) {
        return val != null;
    }

    public boolean isNull(final Object val) {
        return val == null;
    }

    public boolean signalSuccess(Order order) {
        if (order != null) {
            return true;
        }
        return false;
    }

    protected boolean isNotEmpty(final String val) {
        return isNotNull(val) && !val.trim().isEmpty();
    }

    protected boolean isEmpty(final String val) {
        return isNull(val) || val.trim().isEmpty();
    }

    public Properties mapToProperties(final Map<String, String> map) {
        Properties p = new Properties();
        Set<Map.Entry<String, String>> set = map.entrySet();
        for (Map.Entry<String, String> entry : set) {
            p.put(entry.getKey(), entry.getValue());
        }
        return p;
    }

    public String getJobScript() {
        String script = "";
        Job job = spooler_task.job();
        if (job != null) {
            try {
                script = job.script_code();
            } catch (Exception e) {
                LOGGER.info("JobScheduler doesn't support reading the script tag.");
            }
            if (isEmpty(script)) {
                script = "";
            }
        }
        return script;
    }

    public void setJobScript(final SOSOptionElement option) {
        if (option.isNotDirty()) {
            String script = getJobScript();
            if (isNotEmpty(script)) {
                option.setValue(script);
                if (LOGGER.isDebugEnabled()) {
                    try {
                        LOGGER.debug(String.format("copy script from script-tag of job '%2$s' to option '%1$s'", option.getShortKey(), spooler_task
                                .job().name()));
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    @Override
    public boolean spooler_task_before() throws Exception {
        setLogger();
        return true;
    }

    @Override
    public void spooler_task_after() throws Exception {
        setLogger();
    }

    @Override
    public boolean spooler_process_before() throws Exception {
        setLogger();
        return true;
    }

    @Override
    public boolean spooler_process_after(final boolean result) throws Exception {
        setLogger();
        return result;
    }

    @Override
    public void setStateText(final String state) {
        if (state != null) {
            String stateText = state;
            if (stateText.length() > MAX_LENGTH_OF_STATUSTEXT) {
                stateText = stateText.substring(0, MAX_LENGTH_OF_STATUSTEXT - 3) + "...";
            }
            Order order = spoolerProcess == null ? spooler_task.order() : spoolerProcess.getOrder();
            if (order != null) {
                try {
                    order.set_state_text(stateText);
                } catch (Exception e) {
                    //
                }
            }
            if (spooler_job != null) {
                spooler_job.set_state_text(stateText);
            }
        }
    }

    @Override
    public void setExitCode(final int exitCode) {
        if (spooler_task != null) {
            LOGGER.debug(String.format("exit code set to %1$d", exitCode));
            spooler_task.set_exit_code(exitCode);
        }
    }

    @Override
    public void spooler_on_error() {
        setStateText("! ended with Error !");
    }

    @Override
    public void spooler_on_success() {
        setStateText("*** ended without Errors ***");
    }

    @Override
    public void sendEvent(String key, Map<String, String> values) {
        // TODO Auto-generated method stub
        // nothing to do, should be implemented in Job classes extending this one
    }

    public IJobSchedulerEventHandler getEventHandler() {
        return eventHandler;
    }

    public void setEventHandler(IJobSchedulerEventHandler val) {
        eventHandler = val;
    }

    @Override
    public void updateDb(Long id, String type, Map<String, String> values) {
        // TODO Auto-generated method stub
        // nothing to do, should be implemented in Job classes extending this one
    }

    public HashMap<String, String> getSchedulerParameters() {
        return schedulerParameters;
    }

    public Messages getMessages() {
        return messages;
    }

    public SpoolerProcess getSpoolerProcess() {
        return spoolerProcess;
    }

}