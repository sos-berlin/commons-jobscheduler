package sos.scheduler.job;

import static com.sos.scheduler.messages.JSMessages.JSJ_D_0010;
import static com.sos.scheduler.messages.JSMessages.JSJ_D_0070;
import static com.sos.scheduler.messages.JSMessages.JSJ_E_0009;
import static com.sos.scheduler.messages.JSMessages.JSJ_F_0050;
import static com.sos.scheduler.messages.JSMessages.JSJ_F_0060;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_0010;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_0020;
import static com.sos.scheduler.messages.JSMessages.LOG_D_0020;
import static com.sos.scheduler.messages.JSMessages.LOG_I_0010;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import sos.scheduler.interfaces.IJobSchedulerMonitor_impl;
import sos.scheduler.misc.ParameterSubstitutor;
import sos.spooler.Job;
import sos.spooler.Order;
import sos.spooler.Supervisor_client;
import sos.spooler.Variable_set;
import sos.util.SOSSchedulerLogger;

import com.sos.JSHelper.Basics.IJSCommands;
import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Basics.VersionInfo;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionElement;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.localization.Messages;
import com.sos.scheduler.JobSchedulerLog4JAppender;

@I18NResourceBundle(baseName = "com_sos_scheduler_messages", defaultLocale = "en")
public class JobSchedulerJobAdapter extends JobSchedulerJob implements JSJobUtilities, IJSCommands, IJobSchedulerMonitor_impl {

    protected Logger logger = Logger.getLogger(JobSchedulerJobAdapter.class);
    protected Messages Messages = null;
    protected HashMap<String, String> schedulerParameters = new HashMap<String, String>();
    protected HashMap<String, String> hsmParameters = null;
    protected final String EMPTY_STRING = "";
    protected final boolean continue_with_spooler_process = true;
    protected final boolean continue_with_task = true;
    private static final int MAX_LENGTH_OF_STATUSTEXT = 100;
    private JobSchedulerLog4JAppender objJSAppender = null;
    public static final boolean conJobSuccess = false;
    public static final boolean conJobFailure = false;
    public static final boolean conJobChainSuccess = true;
    public static final boolean conJobChainFailure = false;
    public final String conMessageFilePath = "com_sos_scheduler_messages";
    private ParameterSubstitutor parameterSubstitutor;

    public JobSchedulerJobAdapter() {
        Messages = new Messages(conMessageFilePath, Locale.getDefault());
        if (!Logger.getRootLogger().getAllAppenders().hasMoreElements()) {
            BasicConfigurator.configure();
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
        if (sosLogger == null && spooler_log != null) {
            try {
                this.setLogger(new SOSSchedulerLogger(spooler_log));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        String strJobName = this.getJobName();
        if (strJobName == null) {
            if (spooler_job != null) {
                this.setJobName(spooler_job.name());
            }
            strJobName = this.getJobName();
        }
        strJobName = strJobName.replace('/', '-');
        logger = Logger.getRootLogger();
        Appender objStdoutAppender = logger.getAppender("stdout");
        if (objStdoutAppender instanceof JobSchedulerLog4JAppender) {
            objJSAppender = (JobSchedulerLog4JAppender) objStdoutAppender;
            objJSAppender.setSchedulerLogger(sosLogger);
            LOG_D_0020.toLog();
            if (spooler_log.level() > 1) {
                logger.setLevel(Level.ERROR);
            } else if (spooler_log.level() == 1) {
                logger.setLevel(Level.WARN);
            } else if (spooler_log.level() == 0) {
                logger.setLevel(Level.INFO);
            } else if (spooler_log.level() < 0) {
                logger.setLevel(Level.DEBUG);
            } else if (spooler_log.level() == -9) {
                logger.setLevel(Level.TRACE);
            }
        }
        if (isNull(objJSAppender)) {
            SimpleLayout layout = new SimpleLayout();
            objJSAppender = new JobSchedulerLog4JAppender(layout);
            Appender consoleAppender = objJSAppender;
            logger.addAppender(consoleAppender);
            if (spooler_log.level() > 1) {
                logger.setLevel(Level.ERROR);
            } else if (spooler_log.level() == 1) {
                logger.setLevel(Level.WARN);
            } else if (spooler_log.level() == 0) {
                logger.setLevel(Level.INFO);
            } else if (spooler_log.level() < 0) {
                logger.setLevel(Level.DEBUG);
            } else if (spooler_log.level() == -9) {
                logger.setLevel(Level.TRACE);
            }
            LOG_I_0010.toLog();
        }
        objJSAppender.setSchedulerLogger(sosLogger);
    }

    protected HashMap<String, String> getSchedulerParameterAsProperties(final Variable_set pSchedulerParameterSet) {
        schedulerParameters = new HashMap<String, String>();
        try {
            if (isNotNull(pSchedulerParameterSet)) {
                schedulerParameters = convertVariableSet2HashMap(pSchedulerParameterSet);
                schedulerParameters.putAll(getSpecialParameters());
                for (String key : schedulerParameters.keySet()) {
                    String value = schedulerParameters.get(key);
                    if (value != null) {
                        String replacedValue = replaceSchedulerVars(value);
                        if (!replacedValue.equalsIgnoreCase(value)) {
                            schedulerParameters.put(key, replacedValue);
                            if (key.contains("password")) {
                                logger.trace(String.format("%1$s = *****", key));
                            } else {
                                logger.trace(String.format("%1$s = %2$s", key, replacedValue));
                            }
                        } else {
                            if (key.contains("password")) {
                                logger.trace(String.format("%1$s = *****", key));
                            } else {
                                logger.trace(String.format("%1$s = %2$s", key, value));
                            }
                        }
                    }
                }
            }
            schedulerParameters = deleteCurrentNodeNameFromKeys(schedulerParameters);
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
                    value = variableSet.var(key);
                    result.put(key, value);
                }
            }
            return result;
        } catch (Exception e) {
            throw new JobSchedulerException(JSJ_F_0060.params(stackTrace2String(e)), e);
        }
    }

    public HashMap<String, String> deleteCurrentNodeNameFromKeys(final HashMap<String, String> pSchedulerParameterSet) {
        String strCurrentNodeName = getCurrentNodeName(false) + "/";
        int intNNLen = strCurrentNodeName.length();
        HashMap<String, String> newSchedulerParameters = new HashMap<String, String>();
        newSchedulerParameters.putAll(pSchedulerParameterSet);
        Set<Map.Entry<String, String>> set = pSchedulerParameterSet.entrySet();
        for (Map.Entry<String, String> entry : set) {
            String key = entry.getKey();
            if (key.startsWith(strCurrentNodeName)) {
                String val = entry.getValue();
                key = key.substring(intNNLen);
                newSchedulerParameters.put(key, val);
                schedulerParameters.put(key, val);
            }
        }
        return newSchedulerParameters;
    }

    protected HashMap<String, String> getSchedulerParameterAsProperties(final HashMap<String, String> pSchedulerParameterSet) {
        schedulerParameters = new HashMap<String, String>();
        try {
            if (isNotNull(pSchedulerParameterSet)) {
                Set<Map.Entry<String, String>> set = pSchedulerParameterSet.entrySet();
                for (Map.Entry<String, String> entry : set) {
                    schedulerParameters.put(entry.getKey(), entry.getValue());
                    schedulerParameters.put(entry.getKey().replaceAll("_", EMPTY_STRING), entry.getValue());
                }
                set = schedulerParameters.entrySet();
                schedulerParameters.putAll(getSpecialParameters());
                for (Map.Entry<String, String> entry : set) {
                    String key = entry.getKey();
                    String val = entry.getValue();
                    if (val != null) {
                        String strR = replaceVars(schedulerParameters, key, val);
                        if (!strR.equalsIgnoreCase(val)) {
                            schedulerParameters.put(key, strR);
                        }
                    }
                }
            }
            schedulerParameters = deleteCurrentNodeNameFromKeys(schedulerParameters);
            return schedulerParameters;
        } catch (Exception e) {
            throw new JobSchedulerException(JSJ_F_0060.params(e.getMessage()), e);
        }
    }

    protected Variable_set getJobOrOrderParameters() {
        try {
            Variable_set objJobOrOrderParameters = spooler.create_variable_set();
            objJobOrOrderParameters.merge(getTaskParams());
            if (isJobchain() && hasOrderParameters()) {
                objJobOrOrderParameters.merge(getOrderParams());
            }
            JSJ_D_0070.toLog(objJobOrOrderParameters.count());
            return objJobOrOrderParameters;
        } catch (Exception e) {
            throw new JobSchedulerException(JSJ_F_0050.get(e), e);
        }
    }

    public Variable_set getParameters() {
        return getJobOrOrderParameters();
    }

    protected Variable_set getTaskParams() {
        return spooler_task.params();
    }

    protected Order getOrder() {
        return spooler_task.order();
    }

    protected Variable_set getOrderParams() {
        if (spooler_task.order() == null) {
            return null;
        } else {
            return spooler_task.order().params();
        }
    }

    public Variable_set getGlobalSchedulerParameters() {
        return spooler.variables();
    }

    @Override
    public void setJSParam(final String pstrKey, final String pstrValue) {
        if (isNotNull(spooler_task.params())) {
            spooler_task.params().set_var(pstrKey, pstrValue);
        }
        if (hasOrderParameters()) {
            spooler_task.order().params().set_var(pstrKey, pstrValue);
        }
        if (isNotNull(schedulerParameters)) {
            schedulerParameters.put(pstrKey, pstrValue);
        }
    }

    @Override
    public void setJSParam(final String pstrKey, final StringBuffer pstrValue) {
        setJSParam(pstrKey, pstrValue.toString());
    }

    @Override
    public String replaceSchedulerVars(final String string2Modify) {
        String resultString = string2Modify;
        if (isNotNull(schedulerParameters)) {
            if (string2Modify.matches("(?s).*\\$\\{[^{]+\\}.*")) {
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
                resultString = parameterSubstitutor.replace(string2Modify);
            }
        }
        return resultString;
    }

    @Deprecated
    public String replaceVars(final HashMap<String, String> params, final String name, String pstrReplaceIn) {
        if (pstrReplaceIn != null && pstrReplaceIn.matches(".*%[^%]+%.*")) {
            for (String param : params.keySet()) {
                String paramValue = params.get(param);
                if (isNotNull(paramValue)) {
                    String paramPattern = "%" + Pattern.quote(param) + "%";
                    paramValue = paramValue.replace('\\', '/');
                    paramValue = Matcher.quoteReplacement(paramValue);
                    pstrReplaceIn = pstrReplaceIn.replaceAll(paramPattern, paramValue);
                } 
            }
        }
        return pstrReplaceIn;
    }

    private HashMap<String, String> getSpecialParameters() {
        HashMap<String, String> specialParams = new HashMap<String, String>();
        if (spooler == null) {
            return specialParams;
        }
        specialParams.put("SCHEDULER_HOST", spooler.hostname());
        specialParams.put("SCHEDULER_TCP_PORT", "" + spooler.tcp_port());
        specialParams.put("SCHEDULER_UDP_PORT", "" + spooler.udp_port());
        specialParams.put("SCHEDULER_ID", spooler.id());
        specialParams.put("SCHEDULER_DIRECTORY", spooler.directory());
        specialParams.put("SCHEDULER_CONFIGURATION_DIRECTORY", spooler.configuration_directory());
        if (isJobchain()) {
            specialParams.put("SCHEDULER_JOB_CHAIN_NAME", spooler_task.order().job_chain().name());
            specialParams.put("SCHEDULER_JOB_CHAIN_TITLE", spooler_task.order().job_chain().title());
            specialParams.put("SCHEDULER_ORDER_ID", spooler_task.order().id());
            specialParams.put("SCHEDULER_NODE_NAME", getCurrentNodeName(false));
            specialParams.put("SCHEDULER_NEXT_NODE_NAME", spooler_task.order().job_chain_node().next_state());
            specialParams.put("SCHEDULER_NEXT_ERROR_NODE_NAME", spooler_task.order().job_chain_node().error_state());
        }
        specialParams.put("SCHEDULER_JOB_NAME", this.getJobName());
        specialParams.put("SCHEDULER_JOB_FOLDER", this.getJobFolder());
        specialParams.put("SCHEDULER_JOB_PATH", this.getJobFolder() + "/" + this.getJobName());
        specialParams.put("SCHEDULER_JOB_TITLE", this.getJobTitle());
        specialParams.put("SCHEDULER_TASK_ID", "" + spooler_task.id());
        Supervisor_client objRemoteConfigurationService;
        try {
            objRemoteConfigurationService = spooler.supervisor_client();
            if (objRemoteConfigurationService != null) {
                specialParams.put("SCHEDULER_SUPERVISOR_HOST", objRemoteConfigurationService.hostname());
                specialParams.put("SCHEDULER_SUPERVISOR_PORT", "" + objRemoteConfigurationService.tcp_port());
            }
        } catch (Exception e) {
            specialParams.put("SCHEDULER_SUPERVISOR_HOST", "n.a.");
            specialParams.put("SCHEDULER_SUPERVISOR_PORT", "n.a.");
        }
        return specialParams;
    }

    private HashMap<String, String> getAllParametersAsProperties() {
        HashMap<String, String> result = convertVariableSet2HashMap(getGlobalSchedulerParameters());
        result.putAll(getSpecialParameters());
        result.putAll(getSchedulerParameterAsProperties(getParameters()));
        return result;
    }

    public String stackTrace2String(final Exception e) {
        String strT = null;
        if (isNotNull(e)) {
            strT = e.getMessage() + "\n";
            final StackTraceElement arrStack[] = e.getStackTrace();
            for (final StackTraceElement objS : arrStack) {
                strT += objS.toString() + "\n";
            }
        }
        return strT;
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
            Order objCurrentOrder = spooler_task.order();
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
        }
        return lstrNodeName;
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

    public boolean isOrderJob() {
        return isJobchain();
    }

    @Override
    public void setNextNodeState(final String pstrNodeName) {
        if (isJobchain()) {
            spooler_task.order().set_state(pstrNodeName);
        }
    }

    public boolean isJobchain() {
        boolean flgIsJobChain = false;
        if (isNotNull(spooler_job)) {
            flgIsJobChain = isNotNull(spooler_job.order_queue());
        }
        if (!flgIsJobChain && isNotNull(spooler_task)) {
            flgIsJobChain = isNotNull(spooler_task.order());
        }
        return flgIsJobChain;
    }

    public String setOrderParameter(final String pstrParameterName, final String pstrParameterValue) {
        if (isJobchain()) {
            Variable_set objP = getOrderParams();
            if (isNotNull(objP)) {
                objP.set_var(pstrParameterName, pstrParameterValue);
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

    protected boolean isSetBackActive() {
        boolean flgRet = false;
        if (isJobchain()) {
            Order objOrder = spooler_task.order();
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
                logger.debug(String.format("copy script from script-tag of job '%2$s' to option '%1$s'", pobjOptionElement.getShortKey(),
                        getJob().name()));
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
                    spooler_task.order().set_state_text(stateText);
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

}