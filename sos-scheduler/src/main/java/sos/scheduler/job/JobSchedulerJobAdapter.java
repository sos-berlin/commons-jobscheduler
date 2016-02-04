package sos.scheduler.job;
import static com.sos.scheduler.messages.JSMessages.JSJ_D_0010;
import static com.sos.scheduler.messages.JSMessages.JSJ_D_0030;
import static com.sos.scheduler.messages.JSMessages.JSJ_D_0032;
import static com.sos.scheduler.messages.JSMessages.JSJ_D_0040;
import static com.sos.scheduler.messages.JSMessages.JSJ_D_0070;
import static com.sos.scheduler.messages.JSMessages.JSJ_D_0080;
import static com.sos.scheduler.messages.JSMessages.JSJ_E_0009;
import static com.sos.scheduler.messages.JSMessages.JSJ_F_0010;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import sos.scheduler.interfaces.IJobSchedulerMonitor_impl;
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


@I18NResourceBundle(
					baseName = "com_sos_scheduler_messages",
					defaultLocale = "en")
public class JobSchedulerJobAdapter extends JobSchedulerJob implements JSJobUtilities, IJSCommands, IJobSchedulerMonitor_impl {
	@SuppressWarnings("unused")
	private final String				conSVNVersion					= "$Id$";
	private final String				conClassName					= "JobSchedulerJobAdapter";
	public final String					conMessageFilePath				= "com_sos_scheduler_messages";
	protected Variable_set				objJobOrOrderParams				= null;
	protected Logger					logger							= Logger.getLogger(JobSchedulerJobAdapter.class);
	protected Messages					Messages						= null;
	private JobSchedulerLog4JAppender	objJSAppender					= null;
	private final static int			maxLengthOfStatusText			= 100;																	// maximum of state text in JobScheduler DB is 100
	protected HashMap<String, String>	SchedulerParameters				= new HashMap<String, String>();
	protected HashMap<String, String>	hsmParameters					= null;
	protected final String				EMPTY_STRING					= "";
	protected final boolean				continue_with_spooler_process	= true;
	protected final boolean				continue_with_task				= true;
	public JobSchedulerJobAdapter() {
		Messages = new Messages(conMessageFilePath, Locale.getDefault());
		if( !Logger.getRootLogger().getAllAppenders().hasMoreElements() ) {
			BasicConfigurator.configure();
		}
	}

	@Override public boolean spooler_init() {
		Messages = new Messages(conMessageFilePath, Locale.getDefault());
		return super.spooler_init();
	}

	@Override public boolean spooler_process() throws Exception  {
		try {
			super.spooler_process();
			initializeLog4jAppenderClass();
			logger.info(VersionInfo.VERSION_STRING);
		}
		catch (JobSchedulerException e) {
			return signalFailure();
		}
		catch (RuntimeException e) {
			return signalFailure();
		}
		catch (Exception e) {
			return signalFailure();
		}
		finally {
		}
		return signalSuccess();
	}

	protected void initializeLog4jAppenderClass() {
		if (sosLogger == null) {
			if (spooler_log != null) {
				try {
					this.setLogger(new SOSSchedulerLogger(spooler_log));
				}
				catch (Exception e) {
					e.printStackTrace();
				}
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
		/*
		 * the JobSchedulerLog4JAppender is used as the stdout-appender
		 * Therefore the code-snippet below asked log4j what the stdout-appender
		 * is and if it is the JobSchedulerLog4JAppender, the Instance of the
		 * sosJobSchedulerLogger is set.
		 *
		 * All Log4J-Messages are redirected to the sosLogger.
		 */
		Appender objStdoutAppender = logger.getAppender("stdout"); //$NON-NLS-1$
		if (objStdoutAppender instanceof JobSchedulerLog4JAppender) {
			objJSAppender = (JobSchedulerLog4JAppender) objStdoutAppender;
			objJSAppender.setSchedulerLogger(sosLogger);
			LOG_D_0020.toLog();
			if (spooler_log.level() > 1) {
				logger.setLevel(Level.ERROR);
			}
			if (spooler_log.level() == 1) {
				logger.setLevel(Level.WARN);
			}
			if (spooler_log.level() == 0) {
				logger.setLevel(Level.INFO);
			}
			if (spooler_log.level() < 0) {
				logger.setLevel(Level.DEBUG);
			}
			if (spooler_log.level() == -9) {
				logger.setLevel(Level.TRACE);
			}
		}
		if (isNull(objJSAppender)) {
			SimpleLayout layout = new SimpleLayout();
			objJSAppender = new JobSchedulerLog4JAppender(layout);
			Appender consoleAppender = objJSAppender; // JobSchedulerLog4JAppender(layout);
			logger.addAppender(consoleAppender);
			// ALL | DEBUG | INFO | WARN | ERROR | FATAL | OFF:
 			if (spooler_log.level() > 1) {
				logger.setLevel(Level.ERROR);
			}
			if (spooler_log.level() == 1) {
				logger.setLevel(Level.WARN);
			}
			if (spooler_log.level() == 0) {
				logger.setLevel(Level.INFO);
			}
			if (spooler_log.level() < 0) {
				logger.setLevel(Level.DEBUG);
			}
			if (spooler_log.level() == -9) {
				logger.setLevel(Level.TRACE);
			}
			LOG_I_0010.toLog();
		}
		objJSAppender.setSchedulerLogger(sosLogger);
	}

	 
	protected HashMap<String, String> getSchedulerParameterAsProperties(final Variable_set pSchedulerParameterSet) {
		SchedulerParameters = new HashMap<String, String>();
		try {
			if (isNotNull(pSchedulerParameterSet)) {
				SchedulerParameters = convertVariableSet2HashMap(pSchedulerParameterSet);
				SchedulerParameters.putAll(getSpecialParameters());
				for (String key : SchedulerParameters.keySet()) {
					String value = SchedulerParameters.get(key);
					if (value != null) {
						// TODO allow different types of specifying a variable substitution in all JITL jobs
						String replacedValue = replaceSchedulerVars(false, value);
						//						String replacedValue = replaceVars(SchedulerParameters, key, value);
						if (replacedValue.equalsIgnoreCase(value) == false) {
							SchedulerParameters.put(key, replacedValue);
							if(key.contains("password")){
								logger.trace(String.format("%1$s = *****", key));
							}else{
								logger.trace(String.format("%1$s = %2$s", key, replacedValue));
							}
						}
						else {
							if(key.contains("password")){
								logger.trace(String.format("%1$s = *****", key));
							}else{
								logger.trace(String.format("%1$s = %2$s", key, value));
							}
						}
					}
				}
			}
			SchedulerParameters = DeleteCurrentNodeNameFromKeys(SchedulerParameters);
		}
		catch (Exception e) {
			throw new JobSchedulerException(JSJ_F_0060.params(StackTrace2String(e)), e);
		}
		return SchedulerParameters;
	}

	protected HashMap<String, String> convertVariableSet2HashMap(final Variable_set variableSet) {
		HashMap<String, String> result = new HashMap<String, String>();
		try {
			if (isNotNull(variableSet)) {
				String[] names = variableSet.names().split(";");
				String value = EMPTY_STRING;
				for (String key : names) {
					value = EMPTY_STRING;
					/*
					 * the variable_set is able to handle the value of a variable as an
					 * Object. In Java this class (the HashMap) is defined as <String,String> but it is possible
					 * e.g. in JavaScript to set a value as any object.
					 *
					 * Values with other types than string and integer are ignored, e.g. the
					 * value of this parameter is set to space.
					 */
					Object objO = variableSet.var(key);
					if (objO instanceof String) {
						value = variableSet.var(key);
					}
					else {
						if (objO instanceof Integer) {
							Integer intI = (Integer) objO;
							value = intI.toString();
						}
					}
					result.put(key, value);
					result.put(key.replaceAll("_", EMPTY_STRING).toLowerCase(), value);
				}
			}
			return result;
		}
		catch (Exception e) {
			throw new JobSchedulerException(JSJ_F_0060.params(StackTrace2String(e)), e);
		}
	}

	public HashMap<String, String> DeleteCurrentNodeNameFromKeys(final HashMap<String, String> pSchedulerParameterSet) {
		/*
		 * Delete the NodeName (StepName) for all parameters dedicated to the current step
		 * The name of the current step is the return-value of "getCurrentNodeName".
		 */
		String strCurrentNodeName = getCurrentNodeName(false) + "/";
		int intNNLen = strCurrentNodeName.length();
		HashMap<String, String> newSchedulerParameters = new HashMap<String, String>();
		newSchedulerParameters.putAll(pSchedulerParameterSet);
		Set<Map.Entry<String, String>> set = pSchedulerParameterSet.entrySet();
		for (Map.Entry<String, String> entry : set) {
			String key = entry.getKey();
			if (key.startsWith(strCurrentNodeName) == true) {
				String val = entry.getValue();
				key = key.substring(intNNLen);
				newSchedulerParameters.put(key, val);
				objJobOrOrderParams.set_value(key, val);
			}
		}
		return newSchedulerParameters;
	}

	protected HashMap<String, String> getSchedulerParameterAsProperties(final HashMap<String, String> pSchedulerParameterSet) {
		SchedulerParameters = new HashMap<String, String>();
		try {
			if (isNotNull(pSchedulerParameterSet)) {

				Set<Map.Entry<String, String>> set = pSchedulerParameterSet.entrySet();
				for (Map.Entry<String, String> entry : set) {
					SchedulerParameters.put(entry.getKey(), entry.getValue());
					SchedulerParameters.put(entry.getKey().replaceAll("_", EMPTY_STRING), entry.getValue());
				}
				set = SchedulerParameters.entrySet();
				SchedulerParameters.putAll(getSpecialParameters());
				for (Map.Entry<String, String> entry : set) {
					String key = entry.getKey();
					String val = entry.getValue();
					if (val != null) {
						String strR = replaceVars(SchedulerParameters, key, val);
						if (strR.equalsIgnoreCase(val) == false) {
							SchedulerParameters.put(key, strR);
						}
					}
				}
			}
			SchedulerParameters = DeleteCurrentNodeNameFromKeys(SchedulerParameters);
			return SchedulerParameters;
		}
		catch (Exception e) {
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
			objJobOrOrderParams = objJobOrOrderParameters;

			JSJ_D_0070.toLog(objJobOrOrderParameters.count());
			return objJobOrOrderParameters;
		}
		catch (Exception e) {
			throw new JobSchedulerException(JSJ_F_0050.get(e), e);
		}
	}

	protected Variable_set getTaskParams() {
		return spooler_task.params();
	}

	
	protected Order getOrder() {
		if (spooler_task.order() == null) {
			return null;
		} else {
			return spooler_task.order();
		}
	}

	protected Variable_set getOrderParams() {
		// in spooler_task_before spooler_task_order is null
		if (spooler_task.order() == null) {
			return null;
		}
		else {
			return spooler_task.order().params();
		}
	}


	public Variable_set getParameters() {
		Order order = null;
		try {
			/*
			 * it is important to get a *copy* of the params only instead of a reference to the js-object
			 * if we are working with a reference the params will be in effect until the task comes to an end
			 * all orders/jobs etc which are using the task until it is ended will have the possibly wrong (or too much) parameters
			 */
			Variable_set params = spooler.create_variable_set();
			if (isNotNull(spooler_task.params())) {
				params.merge(spooler_task.params());
			}
			if (isJobchain()) {
				order = spooler_task.order();
				if (isNotNull(order.params())) {
					/*
					 * the order-parameter have higher priority over the task params
					 * sometime a job is using task *and* order-parameter
					 */
					params.merge(order.params());
				}
			}
			objJobOrOrderParams = params;
			return params;
		}
		catch (Exception e) {
			e.printStackTrace();
			String strM = JSJ_F_0010.params(e.getMessage());
			logger.error(strM, e);
			throw new JobSchedulerException(strM, e);
		}
	}

	
	public Variable_set getGlobalSchedulerParameters() {
		return spooler.variables();
	}

	
	public void setParameters(final Variable_set pVariableSet) {
		objJobOrOrderParams = pVariableSet;
	}

	
	@Override 
	public void setJSParam(final String pstrKey, final String pstrValue) {
		if (isNotNull(spooler_task.params())) {
			spooler_task.params().set_var(pstrKey, pstrValue);
		}
		if (hasOrderParameters()) {
			spooler_task.order().params().set_var(pstrKey, pstrValue);
		}
		if (isNotNull(objJobOrOrderParams)) {
			objJobOrOrderParams.set_var(pstrKey, pstrValue);
		}
	}

	
	@Override public void setJSParam(final String pstrKey, final StringBuffer pstrValue) {
		setJSParam(pstrKey, pstrValue.toString());
	}

	@Deprecated // use replaceSchedulerVars instead
	public String replaceVars(final HashMap<String, String> params, final String name, String pstrReplaceIn) {
		if (pstrReplaceIn != null) {
			if (pstrReplaceIn.matches(".*%[^%]+%.*")) {
				for (String param : params.keySet()) {
					String paramValue = params.get(param);
					if (isNotNull(paramValue)) {
						String paramPattern = "%" + Pattern.quote(param) + "%";
						paramValue = paramValue.replace('\\', '/');
						// avoid "invalid group reference" error when using $ in param values  http://www.sos-berlin.com/jira/browse/JITL-74
						paramValue = Matcher.quoteReplacement(paramValue);
						//	logger.debug(Messages.getMsg(JSJ_D_0031, name, paramPattern, paramValue)); // "processing job parameter '%1$s': substitute '%2$s' with '%3$s'."
						pstrReplaceIn = pstrReplaceIn.replaceAll(paramPattern, paramValue);
					}
					else {
						JSJ_D_0032.toLog(param); // "variable '%1$s' not found. no substitution done"
					}
				}
			}
		}
		return pstrReplaceIn;
	}
	
	@Override
	public String replaceSchedulerVars(final boolean isWindows, final String pstrString2Modify) {
		String strTemp = pstrString2Modify;
		if (isNotNull(objJobOrOrderParams)) {
			strTemp = replaceSchedulerVarsInString(objJobOrOrderParams,pstrString2Modify);
		}
		return strTemp; 
	}
	
	
	public String replaceSchedulerVarsInString(Variable_set params, final String pstrString2Modify) {
        String strTemp = pstrString2Modify;

        JSJ_D_0080.toLog();

        if (pstrString2Modify.matches("(?s).*%[^%]+%.*") || pstrString2Modify.matches("(?s).*(\\$|§)\\{[^{]+\\}.*")) {
            if (isNotNull(params)) {

                // Wenn String.format verwendet werden soll
//                String[] strPatterns2 = new String[] { "%%SCHEDULER_PARAM_%1$s%%", "%%%1$s%%", "(\\$|§)\\{?SCHEDULER_PARAM_%1$s\\}?",
//                        "(\\$|§)\\{?%1$s\\}?" };

                String[] strPatterns = new String[] { "%SCHEDULER_PARAM_%1$s%", "%%1$s%", "(\\$|§)\\{?SCHEDULER_PARAM_%1$s\\}?",
                        "(\\$|§)\\{?%1$s\\}?" };

                //
                /* beides zulassen, % und $ moegliche Kombinationen sind:
                 * 
                 * %SCHEDULER_PARAM_name% %name% ${SCHEDULER_PARAM_name}
                 * $SCHEDULER_PARAM_name §{SCHEDULER_PARAM_name}
                 * §SCHEDULER_PARAM_name ${name} §{name} $name §name
                 * 
                 * Managed-DB: §{...} */
                
                String[] names = params.names().split(";");
                for (String strPattern : strPatterns) {
                    String regExPattern = strPattern;

                    for (String name : names) {
                        String strParamValue = params.value(name);
                        if (strParamValue == null) {
                            continue;
                        }
                        // too verbose
                        // if (name.contains("password") == false &&
                        // name.trim().length() > 0) {
                        // logger.debug("name = " + name + ", value = " +
                        // strParamValue);
                        // }
                        // String.format ist ca. 10% langsamer.
                        // String regex = String.format(regExPattern, name);
                        String regex = regExPattern.replaceAll("\\%1\\$s", name);

                        // avoid "invalid group reference" error when using $ in
                        // param values
                        // http://www.sos-berlin.com/jira/browse/JITL-74
                        strParamValue = Matcher.quoteReplacement(strParamValue);
                        strTemp = myReplaceAll(strTemp, regex, strParamValue);

                        // End if no more variables in string for substitution
                        if (!(strTemp.matches("(?s).*%[^%]+%.*") || strTemp.matches("(?s).*(\\$|§)\\{[^{]+\\}.*"))) {
                            break;
                        }
                    }
                }
                JSJ_D_0030.toLog(strTemp);
            } else {
                JSJ_D_0040.toLog();
            }
        }

        return strTemp;
    }

	
    public String replaceSchedulerVarsInString(HashMap<String, String> params, final String pstrString2Modify) {
        String strTemp = pstrString2Modify;

        JSJ_D_0080.toLog();

        if (pstrString2Modify.matches("(?s).*%[^%]+%.*") || pstrString2Modify.matches("(?s).*(\\$|§)\\{[^{]+\\}.*")) {
            if (isNotNull(params)) {

                // Wenn String.format verwendet werden soll
//                String[] strPatterns2 = new String[] { "%%SCHEDULER_PARAM_%1$s%%", "%%%1$s%%", "(\\$|§)\\{?SCHEDULER_PARAM_%1$s\\}?",
//                        "(\\$|§)\\{?%1$s\\}?" };

                String[] strPatterns = new String[] { "%SCHEDULER_PARAM_%1$s%", "%%1$s%", "(\\$|§)\\{?SCHEDULER_PARAM_%1$s\\}?",
                        "(\\$|§)\\{?%1$s\\}?" };

                //
                /* beides zulassen, % und $ moegliche Kombinationen sind:
                 * 
                 * %SCHEDULER_PARAM_name% %name% ${SCHEDULER_PARAM_name}
                 * $SCHEDULER_PARAM_name §{SCHEDULER_PARAM_name}
                 * §SCHEDULER_PARAM_name ${name} §{name} $name §name
                 * 
                 * Managed-DB: §{...} */
                for (String strPattern : strPatterns) {
                    String regExPattern = strPattern;

                    for (String name : params.keySet()) {
                        String strParamValue = params.get(name);
                        // too verbose
                        // if (name.contains("password") == false &&
                        // name.trim().length() > 0) {
                        // logger.debug("name = " + name + ", value = " +
                        // strParamValue);
                        // }
                        // String.format ist ca. 10% langsamer.
                        // String regex = String.format(regExPattern, name);
                        String regex = regExPattern.replaceAll("\\%1\\$s", name);

                        // avoid "invalid group reference" error when using $ in
                        // param values
                        // http://www.sos-berlin.com/jira/browse/JITL-74
                        strParamValue = Matcher.quoteReplacement(strParamValue);
                        strTemp = myReplaceAll(strTemp, regex, strParamValue);

                        // End if no more variables in string for substitution
                        if (!(strTemp.matches("(?s).*%[^%]+%.*") || strTemp.matches("(?s).*(\\$|§)\\{[^{]+\\}.*"))) {
                            break;
                        }

                    }

                }
                JSJ_D_0030.toLog(strTemp);
            } else {
                JSJ_D_0040.toLog();
            }
        }

        return strTemp;
    }
	      
	public HashMap<String, String> getSpecialParameters() {
		HashMap<String, String> specialParams = new HashMap<String, String>();
		if (spooler == null) { // junit test specific
			return specialParams;
		}
		// specialParams.put("SCHEDULER_RETURN_VALUES", remoteCommandScriptOutputParamsFileName);
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
		}
		catch (Exception e) {
			specialParams.put("SCHEDULER_SUPERVISOR_HOST", "n.a.");
			specialParams.put("SCHEDULER_SUPERVISOR_PORT", "n.a.");
		}
		return specialParams;
	}

	
	public HashMap<String, String> getAllParametersAsProperties() {
		HashMap<String, String> result = convertVariableSet2HashMap(getGlobalSchedulerParameters());
		result.putAll(getSpecialParameters());
		result.putAll(getSchedulerParameterAsProperties(getParameters()));
		return result;
	}

	
	@Override
	public String myReplaceAll(final String source, final String what, final String replacement) {
		String newReplacement = replacement.replaceAll("\\$", "\\\\\\$");
		return source.replaceAll("(?im)" + what, newReplacement);
	}

	
	public String StackTrace2String(final Exception e) {
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
	}

	
	@Override
	public String getCurrentNodeName() {
		return getCurrentNodeName(true);
	}
	
	public String getCurrentNodeName(boolean verbose) {
		final String conMethodName = conClassName + "::getNodeName";
		String lstrNodeName = "node1"; // Test, needed for JUnit-Test
		if (spooler_task != null) {
			Order objCurrentOrder = spooler_task.order();
			if (isNotNull(objCurrentOrder)) {
				lstrNodeName = objCurrentOrder.state();
				if (verbose) {
					JSJ_I_0020.toLog(conMethodName, lstrNodeName);
				}
			}
			else {
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
		String strX = spooler.execute_xml(pstrJSXmlCommand);
		return strX;
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
		if (isNotNull(spooler_job)) { // && isNotNull(spooler_task)) {
			flgIsJobChain = isNotNull(spooler_job.order_queue()); // || (isNotNull(spooler_task.order()));
		}
		if (flgIsJobChain == false) {
			if (isNotNull(spooler_task)) {
				flgIsJobChain = isNotNull(spooler_task.order());
			}
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
	/*
	 * 6.3 spooler_process()
	 * The return value is interpreted as with spooler_init().
	 * Should False be returned, then the task will be continued
	 * using spooler_close(); after True the task will be continued with a further call of spooler_process().
	 */
	public final static boolean	conJobSuccess		= false;
	public final static boolean	conJobFailure		= false;
	/*
	 * 6.8.2 Return value of spooler_process()
	 * The return value of order jobs determines if an order was successful or not.
	 *
	 * True: The order will be continued in the next_state of the current job chain node.
	 *
	 * False: The order will be continued in the error_state of the current job chain node.
	 */
	public final static boolean	conJobChainSuccess	= true;
	public final static boolean	conJobChainFailure	= false;

	public boolean signalSuccess() {
		if (isJobchain()) {
			return conJobChainSuccess;
		}
		//setStateText("completed without error");
		return conJobSuccess;
	}

	public boolean signalFailure() {
		boolean RaiseErrorOnSetback = false;
		String strMsg = JSJ_E_0009.get(this.getJobName());
		if (isJobchain()) {
			if (isSetBackActive() == false || RaiseErrorOnSetback) {
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
		return isNotNull(pstrValue) && pstrValue.trim().length() > 0;
	}

	protected boolean isEmpty(final String pstrValue) {
		return isNull(pstrValue) || pstrValue.trim().length() <= 0;
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
			}
			catch (Exception e) {
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
				pobjOptionElement.Value(strS);
				logger.debug(String.format("copy script from script-tag of job '%2$s' to option '%1$s'", pobjOptionElement.getShortKey(), getJob().name()));
			}
		}
	}

	@Override
	public boolean spooler_task_before() throws Exception {
		final String conMethodName = conClassName + "::spooler_task_before";
		initializeLog4jAppenderClass();
		getJobOrOrderParameters();
		boolean ret = continueWithProcessBefore;
		//boolean ret = continueWithTaskAfter;
		logger.info(String.format("%1$s is running and returns %2$s", conMethodName, ret));
		return ret;
	}

	@Override
	public void spooler_task_after() throws Exception {
		final String conMethodName = conClassName + "::spooler_task_after";
		initializeLog4jAppenderClass();
		getJobOrOrderParameters();
		logger.info(String.format("%1$s is running", conMethodName));
	}

	@Override
	public boolean spooler_process_before() throws Exception {
		final String conMethodName = conClassName + "::spooler_process_before";
		initializeLog4jAppenderClass();
		getJobOrOrderParameters();
		boolean ret = continueWithProcess;
		//boolean ret = continueWithTaskAfter;
		logger.info(String.format("%1$s is running and returns %2$s", conMethodName, ret));
		return ret;
	}

	@Override
	public boolean spooler_process_after(final boolean spooler_process_result) throws Exception {
		final String conMethodName = conClassName + "::spooler_process_after";
		initializeLog4jAppenderClass();
		logger.info(String.format("%1$s is running and returns %2$s", conMethodName, spooler_process_result));
		getJobOrOrderParameters();
		return spooler_process_result;
	}

	@Override
	public void setStateText(final String pstrStateText) {
		if (pstrStateText != null) {
			String stateText = pstrStateText;
			if (stateText.length() > maxLengthOfStatusText) {
				stateText = stateText.substring(0, maxLengthOfStatusText - 3) + "...";
			}
			if (isJobchain()) {
				try {
					spooler_task.order().set_state_text(stateText);
				}
				catch (Exception e) {
					// i.e. in spooler_on_error and spooler_on_success is spooler_task.order() == null
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
