package sos.scheduler.job;

import static com.sos.scheduler.messages.JSMessages.JSJ_D_0010;
import static com.sos.scheduler.messages.JSMessages.JSJ_D_0070;
import static com.sos.scheduler.messages.JSMessages.JSJ_E_0009;
import static com.sos.scheduler.messages.JSMessages.JSJ_F_0050;
import static com.sos.scheduler.messages.JSMessages.JSJ_F_0060;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_0010;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_0020;
import static com.sos.scheduler.messages.JSMessages.LOG_D_0020;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;

import com.sos.JSHelper.Basics.IJSCommands;
import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Basics.VersionInfo;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionElement;
import com.sos.JSHelper.interfaces.IJobSchedulerEventHandler;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.localization.Messages;

import sos.spooler.IMonitor_impl;
import sos.spooler.Job;
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.util.ParameterSubstitutor;

@I18NResourceBundle(baseName = "com_sos_scheduler_messages", defaultLocale = "en")
public class JobSchedulerJobAdapter extends JobSchedulerJob implements JSJobUtilities, IJSCommands, IMonitor_impl, IJobSchedulerEventHandler {

    protected Logger logger = LogManager.getLogger(JobSchedulerJobAdapter.class);
    protected Messages Messages = null;
    protected HashMap<String, String> schedulerParameters = new HashMap<String, String>();
    protected HashMap<String, String> hsmParameters = null;
    protected final String EMPTY_STRING = "";
    protected final boolean continue_with_spooler_process = true;
    protected final boolean continue_with_task = true;
    private static final int MAX_LENGTH_OF_STATUSTEXT = 100;
    public static final boolean conJobSuccess = false;
    public static final boolean conJobFailure = false;
    public static final boolean conJobChainSuccess = true;
    public static final boolean conJobChainFailure = false;
    public static final boolean continueWithProcess = true;
    public static final boolean continueWithProcessBefore = true;
    public static final boolean continueWithTaskAfter = false;
    public final String conMessageFilePath = "com_sos_scheduler_messages";
    private ParameterSubstitutor parameterSubstitutor;
    private String jobNameForTest = "job_name_for_test";
    private String orderStateForTest = "order_state_for_test";
    private boolean loggerConfigured = false;
    private IJobSchedulerEventHandler eventHandler = null;
    private HashMap<String, String> taskParams = null;
    private HashMap<String, String> globalSchedulerParams = null;
    private Integer schedulerLogLevel = null;

    public JobSchedulerJobAdapter() {
        Messages = new Messages(conMessageFilePath, Locale.getDefault());
        if (LogManager.getRootLogger() == null) {
            Configurator.initialize(new DefaultConfiguration());
        }
    }

    @Override
    public boolean spooler_init() {
        Messages = new Messages(conMessageFilePath, Locale.getDefault());
        return super.spooler_init();
    }

    @Override
    public boolean spooler_process() throws Exception {
        try {
            super.spooler_process();
            initializeLog4jAppenderClass();
            logger.info(VersionInfo.VERSION_STRING);
        } catch (JobSchedulerException e) {
            return signalFailure();
        } catch (RuntimeException e) {
            return signalFailure();
        } catch (Exception e) {
            return signalFailure();
        }
        return signalSuccess();
    }

    protected void initializeLog4jAppenderClass() {
        if (!loggerConfigured) {
            LOG_D_0020.toLog();

            logger = LogManager.getRootLogger();
            LoggerContext logContext = (LoggerContext) LogManager.getContext(false);
            Configuration configuration = logContext.getConfiguration();

            if (schedulerLogLevel == null) {
                schedulerLogLevel = spooler_log.level();
            }
            if (schedulerLogLevel > 1) {
                configuration.getRootLogger().setLevel(Level.ERROR);
            } else if (schedulerLogLevel == 1) {
                configuration.getRootLogger().setLevel(Level.WARN);
            } else if (schedulerLogLevel == 0) {
                configuration.getRootLogger().setLevel(Level.INFO);
            } else if (schedulerLogLevel == -9) {
                configuration.getRootLogger().setLevel(Level.TRACE);
            } else if (schedulerLogLevel < 0) {
                configuration.getRootLogger().setLevel(Level.DEBUG);
            }
            loggerConfigured = true;
            logContext.updateLoggers();
        }
    }

    protected HashMap<String, String> getSchedulerParameterAsProperties(final HashMap<String, String> pSchedulerParameterSet) {
        schedulerParameters = new HashMap<String, String>();
        try {
            if (isNotNull(pSchedulerParameterSet)) {
                schedulerParameters = pSchedulerParameterSet;
                for (String key : schedulerParameters.keySet()) {
                    String value = schedulerParameters.get(key);
                    if (value != null) {
                        String replacedValue = replaceSchedulerVars(value);
                        if (!replacedValue.equalsIgnoreCase(value)) {
                            schedulerParameters.put(key, replacedValue);
                            if (key.contains("password") || key.contains("passphrase")) {
                                logger.trace(String.format("%1$s = *****", key));
                            } else {
                                logger.trace(String.format("%1$s = %2$s", key, replacedValue));
                            }
                        } else {
                            if (key.contains("password") || key.contains("passphrase")) {
                                logger.trace(String.format("%1$s = *****", key));
                            } else {
                                logger.trace(String.format("%1$s = %2$s", key, value));
                            }
                        }
                    }
                }
            }
            schedulerParameters = deleteCurrentNodeNameFromKeys(schedulerParameters);
            parameterSubstitutor = null;
        } catch (Exception e) {
            throw new JobSchedulerException(JSJ_F_0060.params(stackTrace2String(e)), e);
        }
        return schedulerParameters;
    }

    protected HashMap<String, String> getSchedulerParameterAsProperties() {
        return getSchedulerParameterAsProperties(getJobOrOrderParameters());
    }

    protected HashMap<String, String> convertVariableSet2HashMap(final Variable_set variableSet) {
        HashMap<String, String> result = new HashMap<String, String>();
        try {
            if (isNotNull(variableSet)) {
                String[] names = variableSet.names().split(";");
                String value = EMPTY_STRING;
                for (String key : names) {
                    if (!"".equals(key)) {
                        value = variableSet.var(key);
                        result.put(key, value);
                    }
                }
            }
            return result;
        } catch (Exception e) {
            throw new JobSchedulerException(JSJ_F_0060.params(stackTrace2String(e)), e);
        }
    }

    private String getParameterName(String parameterName) {
        String currentNodeName = getCurrentNodeName(false);
        String currentJob = getCurrentJob();

        String pattern1 = "^" + currentNodeName + "/(.*)";
        String pattern2 = "^job::" + currentJob + "/(.*)";
        String pattern3 = "^job::" + currentJob + "\\." + currentNodeName + "/(.*)";

        String newParameter = parameterName.replaceAll(pattern3, "$1");
        newParameter = newParameter.replaceAll(pattern2, "$1");
        newParameter = newParameter.replaceAll(pattern1, "$1");
        return newParameter;
    }

    private HashMap<String, String> deleteCurrentNodeNameFromKeys(final HashMap<String, String> pSchedulerParameterSet) {

        HashMap<String, String> newSchedulerParameters = new HashMap<String, String>();
        newSchedulerParameters.putAll(pSchedulerParameterSet);
        Set<Map.Entry<String, String>> set = pSchedulerParameterSet.entrySet();
        for (Map.Entry<String, String> entry : set) {
            String key = entry.getKey();
            String newParameterName = getParameterName(key);
            if (!key.equals(newParameterName)) {
                String val = entry.getValue();
                newSchedulerParameters.put(newParameterName, val);
            }
        }
        return newSchedulerParameters;
    }

    public HashMap<String, String> testDeleteCurrentNodeNameFromKeys(final HashMap<String, String> pSchedulerParameterSet) {
        return deleteCurrentNodeNameFromKeys(pSchedulerParameterSet);
    }

    protected HashMap<String, String> getJobOrOrderParameters() {
        try {
            HashMap<String, String> jobOrOrderParameters = new HashMap<String, String>();
            jobOrOrderParameters.putAll(getTaskParams());
            if (isJobchain() && hasOrderParameters()) {
                jobOrOrderParameters.putAll(convertVariableSet2HashMap(getOrderParams()));
            }
            JSJ_D_0070.toLog(jobOrOrderParameters.size());
            return jobOrOrderParameters;

        } catch (Exception e) {
            throw new JobSchedulerException(JSJ_F_0050.get(e), e);
        }
    }

    @Deprecated
    public HashMap<String, String> getParameters() {
        return getJobOrOrderParameters();
    }

    protected HashMap<String, String> getTaskParams() {
        if (taskParams == null) {
            taskParams = convertVariableSet2HashMap(spooler_task.params());
        }
        return taskParams;
    }

    @Deprecated
    private Order getOrder() {
        return spooler_task.order();
    }

    protected Variable_set getOrderParams() {
        Order o = getOrder();
        if (o == null) {
            return null;
        } else {
            return o.params();
        }
    }

    public HashMap<String, String> getGlobalSchedulerParameters() {
        if (globalSchedulerParams == null) {
            globalSchedulerParams = convertVariableSet2HashMap(spooler.variables());
        }
        return globalSchedulerParams;
    }

    @Override
    public void setJSParam(final String pstrKey, final String pstrValue) {
        if (isNotNull(getTaskParams())) {
            getTaskParams().put(pstrKey, pstrValue);
        }

        Variable_set taskParams = spooler_task.params();
        if (taskParams != null) {
            taskParams.set_var(pstrKey, pstrValue);
        }
        if (hasOrderParameters()) {
            if (isNotNull(getOrderParams())) {
                getOrderParams().set_var(pstrKey, pstrValue);
            }
        }
        if (isNotNull(schedulerParameters)) {
            schedulerParameters.put(pstrKey, pstrValue);
        }
    }

    @Override
    public void setJSParam(final String pstrKey, final StringBuffer pstrValue) {
        setJSParam(pstrKey, pstrValue.toString());
    }

    private void fillParameterSubstitutor() {
        if (parameterSubstitutor == null) {
            parameterSubstitutor = new ParameterSubstitutor();
            for (Entry<String, String> entry : schedulerParameters.entrySet()) {
                String value = entry.getValue();
                String paramName = entry.getKey();

                if (value != null && !value.isEmpty()) {
                    parameterSubstitutor.addKey(paramName, value);
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

            if (string2Modify.matches("(.*)%(.+)%(.*)")) {
                parameterSubstitutor.setOpenTag("%");
                parameterSubstitutor.setCloseTag("%");
                resultString = parameterSubstitutor.replace(string2Modify);
            }

        }
        return resultString;
    }

    public String stackTrace2String(final Exception e) {
        if (isNotNull(e)) {
            StringBuilder sb = new StringBuilder(e.getMessage()).append("\n");
            final StackTraceElement stacks[] = e.getStackTrace();
            for (final StackTraceElement stack : stacks) {
                sb.append(stack.toString()).append("\n");
            }
            return sb.toString();
        }
        return null;
    }

    protected JSJobUtilities getJSJobUtilities() {
        return this;
    }

    @Override
    public void setJSJobUtilites(final JSJobUtilities pobjJSJobUtilities) {
        //
    }

    @Override
    public String getCurrentNodeName() {
        return getCurrentNodeName(true);
    }

    public String getCurrentNodeName(boolean verbose) {
        final String conMethodName = "JobSchedulerJobAdapter::getNodeName";
        String lstrNodeName = "node1";
        if (spooler_task != null) {
            Order objCurrentOrder = getOrder();
            if (isNotNull(objCurrentOrder)) {
                lstrNodeName = objCurrentOrder.state();
                if (verbose) {
                    JSJ_I_0020.toLog(conMethodName, lstrNodeName);
                }
            } else {
                Job objCurrentJob = getJob();
                lstrNodeName = objCurrentJob.name();
                if (verbose) {
                    JSJ_I_0010.toLog(conMethodName, lstrNodeName);
                }
            }
        } else {
            return orderStateForTest;
        }
        return lstrNodeName;
    }

    public String getCurrentJob() {
        if (spooler_task == null) {
            return jobNameForTest;
        } else {
            return spooler_task.job().name();
        }
    }

    public Job getJob() {
        return spooler_task.job();
    }

    @Override
    public Object getSpoolerObject() {
        return spooler;
    }

    @Override
    public String executeXML(final String pstrJSXmlCommand) {
        return spooler.execute_xml(pstrJSXmlCommand);
    }

    @Override
    public void setNextNodeState(final String pstrNodeName) {
        if (isJobchain()) {
            getOrder().set_state(pstrNodeName);
        }
    }

    @Deprecated
    private boolean isJobchain() {
        return isNotNull(getOrder());
    }

    public String setOrderParameter(final String pstrParameterName, final String pstrParameterValue) {
        if (isJobchain()) {
            Variable_set params = getOrderParams();
            if (isNotNull(params)) {
                params.set_var(pstrParameterName, pstrParameterValue);
                JSJ_D_0010.toLog(pstrParameterName, pstrParameterValue);
            }
        }
        return pstrParameterValue;
    }

    public boolean isNotNull(final Object pobjObject) {
        return pobjObject != null;
    }

    public boolean isNull(final Object pobjObject) {
        return pobjObject == null;
    }

    public boolean hasOrderParameters() {
        boolean flgResult = false;
        if (isJobchain()) {
            flgResult = isNotNull(getOrderParams());
        }
        return flgResult;
    }

    public boolean signalSuccess() {
        if (isJobchain()) {
            return conJobChainSuccess;
        }
        return conJobSuccess;
    }

    public boolean signalFailure() {
        boolean RaiseErrorOnSetback = false;
        String strMsg = JSJ_E_0009.get(this.getJobName());
        if (isJobchain()) {
            if (!isSetBackActive() || RaiseErrorOnSetback) {
                logger.error(strMsg);
            }
            return conJobChainFailure;
        }
        return conJobFailure;
    }

    public boolean signalFailureNoLog() {
        if (isJobchain()) {
            return conJobChainFailure;
        } else {
            return conJobFailure;
        }
    }

    @SuppressWarnings("deprecation")
    protected boolean isSetBackActive() {
        boolean flgRet = false;
        if (isJobchain()) {
            Order objOrder = getOrder();
            if (objOrder.setback_count() > 0 && objOrder.setback_count() >= getJob().setback_max()) {
                flgRet = true;
            }
        }
        return flgRet;
    }

    protected boolean isNotEmpty(final String pstrValue) {
        return isNotNull(pstrValue) && !pstrValue.trim().isEmpty();
    }

    protected boolean isEmpty(final String pstrValue) {
        return isNull(pstrValue) || pstrValue.trim().isEmpty();
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
        String strS = "";
        Job objJob = getJob();
        if (isNotNull(objJob)) {
            try {
                strS = objJob.script_code();
            } catch (Exception e) {
                logger.info("JobScheduler doesn't support reading the script tag.");
            }
            if (isEmpty(strS)) {
                strS = "";
            }
        }
        return strS;
    }

    public void setJobScript(final SOSOptionElement pobjOptionElement) {
        if (pobjOptionElement.isNotDirty()) {
            String strS = getJobScript();
            if (isNotEmpty(strS)) {
                pobjOptionElement.setValue(strS);
                logger.debug(String.format("copy script from script-tag of job '%2$s' to option '%1$s'", pobjOptionElement.getShortKey(), getJob()
                        .name()));
            }
        }
    }

    @Override
    public boolean spooler_task_before() throws Exception {
        initializeLog4jAppenderClass();
        getSchedulerParameterAsProperties();
        return continueWithProcessBefore;
    }

    @Override
    public void spooler_task_after() throws Exception {
        initializeLog4jAppenderClass();
        getSchedulerParameterAsProperties();
    }

    @Override
    public boolean spooler_process_before() throws Exception {
        initializeLog4jAppenderClass();
        getSchedulerParameterAsProperties();
        return continueWithProcess;
    }

    @Override
    public boolean spooler_process_after(final boolean spooler_process_result) throws Exception {
        initializeLog4jAppenderClass();
        getSchedulerParameterAsProperties();
        return spooler_process_result;
    }

    @Override
    public void setStateText(final String pstrStateText) {
        if (pstrStateText != null) {
            String stateText = pstrStateText;
            if (stateText.length() > MAX_LENGTH_OF_STATUSTEXT) {
                stateText = stateText.substring(0, MAX_LENGTH_OF_STATUSTEXT - 3) + "...";
            }
            if (isJobchain()) {
                try {
                    getOrder().set_state_text(stateText);
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
    public void setCC(final int pintCC) {
        if (spooler_task != null) {
            logger.debug(String.format("CC set to %1$d", pintCC));
            spooler_task.set_exit_code(pintCC);
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

}