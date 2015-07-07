package sos.scheduler.managed;

import java.io.BufferedReader;
import java.io.FileReader;

import sos.spooler.Order;
import sos.spooler.Subprocess;
import sos.spooler.Variable_set;

/**
 * execute command files for managed orders
 * 
 * order parameters:
 * ignore_error (optional): bei true werden Fehler des executables ignoriert
 * ignore_signal (optional): bei true werden Rückgabesignale des executables ignoriert
 * ignore_stderr (optional): bei true werden Zeichen in stdout nicht als Fehler behandelt
 * timeout (optional): timeout für die Ausführung des executables in Sekunden
 * log_file (optional): Der Scheduler nimmt nach Beenden des Subprozesses den Inhalt
 * dieser Datei in sein Protokoll
 * 
 * environment variablen können über parameter mit dem Präfix env. gesetzt werden, z.B.:
 * env.LD_LIBRARY_PATH
 * 
 * @author andreas.pueschel@sos-berlin.com
 * @since 1.0 2005-03-05 
 */
public class JobSchedulerManagedExecutableJob extends JobSchedulerManagedJob {

	private BufferedReader		stdoutStream;
	private BufferedReader		stderrStream;

	private final String strOrderParamPrefix = "scheduler_order_";
	// aliases for parameters from ProcessSubprocessJob
	private  String[][]	inputParameterAliases	= { { strOrderParamPrefix + "ignore_stderr", "ignore_stderr" },
			{ strOrderParamPrefix + "ignore_error", "ignore_error" }, 
			{ strOrderParamPrefix + "ignore_signal", "ignore_signal" },
			{ strOrderParamPrefix + "timeout", "timeout" },
			{ strOrderParamPrefix + "priority_class", "priority_class" } };

	private final  String	conStd_err_output		= "std_err_output";
	private final  String	conStd_out_output		= "std_out_output";
	private final  String	conExit_code			= "exit_code";
	
	private  final String	conClassName	= "JobSchedulerManagedExecutableJob";

	private final String[][]	outputParameterAliases	= { { conStd_err_output, strOrderParamPrefix + "stderr_output" },
			{ conStd_out_output, strOrderParamPrefix + "stdout_output" }, 
			{ conExit_code, strOrderParamPrefix + "exit_code" } };

	/**
	 * Initialize input and output streams
	 */
	public boolean spooler_init() {
		try {
			FileReader fisOut = new FileReader(spooler_task.stdout_path());
			FileReader fisErr = new FileReader(spooler_task.stderr_path());
			stdoutStream = new BufferedReader(fisOut);
			stderrStream = new BufferedReader(fisErr);
		}
		catch (Exception e) {
			spooler_log.warn("failed to initialize stdout and stderr streams. " + e);
		}

		if (!super.spooler_init())
			return false;
		return true;
	}

	/**
	 * process order
	 */
	public boolean spooler_process() {

		Order order = null;
		orderPayload = null;
		String program = "";
		String logFile = "";
		String priorityClass = "normal";

		try {
			super.prepareParams();

			String command = "";
			try {
				
				String commandScript = getJobScript();
				getLogger().debug9("setting 'command_script' value from script tag of job: " + commandScript);
				
				if (orderJob) {
					command = JobSchedulerManagedObject.getOrderCommand(this, commandScript);
					order = spooler_task.order();
				}
				if (command == null || command.trim().length() == 0) {
					command = JobSchedulerManagedObject.getJobCommand(this);
				}

				// overidden for custom implementation
				command = modifyCommand(command);

				if (command == null || command.trim().length() == 0)
					throw new Exception("command is empty");

			}
			catch (Exception e) {
				throw (new Exception("no executable command found for order:" + e));
			}
			getLogger().debug3("command before replacements:\n" + command);
			// if ( orderData == null || orderData.var("command") == null || orderData.var("command").toString().length() == 0)
			// throw (new Exception("no executable command found in order payload"));
			boolean ignoreError = false;
			boolean ignoreSignal = false;
			boolean ignoreStderr = false;
			boolean ignoreTimeout = false;

			boolean timedOut = false;
			boolean ownProcessGroup = false;

			if (orderPayload != null) {
				replaceAliases(orderPayload, inputParameterAliases);

				if (orderPayload.var("ignore_error") != null
						&& (orderPayload.var("ignore_error").toString().equalsIgnoreCase("true") || orderPayload.var("ignore_error").equalsIgnoreCase("1") || orderPayload.var(
								"ignore_error")
								.equalsIgnoreCase("yes"))) {
					ignoreError = true;
				}
				if (orderPayload.var("ignore_signal") != null
						&& (orderPayload.var("ignore_signal").toString().equalsIgnoreCase("true") || orderPayload.var("ignore_signal").equalsIgnoreCase("1") || orderPayload.var(
								"ignore_signal")
								.equalsIgnoreCase("yes"))) {
					ignoreSignal = true;
				}
				if (orderPayload.var("ignore_stderr") != null
						&& (orderPayload.var("ignore_stderr").toString().equalsIgnoreCase("true") || orderPayload.var("ignore_stderr").equalsIgnoreCase("1") || orderPayload.var(
								"ignore_stderr")
								.equalsIgnoreCase("yes"))) {
					ignoreStderr = true;
				}
				if (orderPayload.var("ignore_timeout") != null
						&& (orderPayload.var("ignore_timeout").toString().equalsIgnoreCase("true") || orderPayload.var("ignore_timeout").equalsIgnoreCase("1") || orderPayload.var(
								"ignore_timeout")
								.equalsIgnoreCase("yes"))) {
					ignoreTimeout = true;
				}
				if (orderPayload.var("own_process_group") != null
						&& (orderPayload.var("own_process_group").toString().equalsIgnoreCase("true")
								|| orderPayload.var("own_process_group").equalsIgnoreCase("1") || orderPayload.var("own_process_group").equalsIgnoreCase("yes"))) {
					ownProcessGroup = true;
				}
				if (orderPayload.var("log_file") != null && orderPayload.var("log_file").toString().length() > 0) {
					logFile = orderPayload.var("log_file").toString();
				}
				if (orderPayload.var("priority_class") != null && orderPayload.var("priority_class").toString().length() > 0) {
					priorityClass = orderPayload.var("priority_class").toString();
				}
				// for compatibility with ProcessSubprocessJob
				if (orderPayload.var("scheduler_order_command_parameters") != null
						&& orderPayload.var("scheduler_order_command_parameters").toString().length() > 0) {
					command += " " + orderPayload.var("scheduler_order_command_parameters").toString();
				}
			}
			// the path of the interpreter is given with this parameter
			if (spooler_task.params().var("interpreter") != null && spooler_task.params().var("interpreter").length() > 0) {
				program = spooler_task.params().var("interpreter");
				spooler_log.debug3("Using interpreter: " + program);
			}

			spooler_log.debug3("current setting ignore_error: " + ignoreError);
			spooler_log.debug3("current setting ignore_signal: " + ignoreSignal);
			spooler_log.debug3("current setting own_process_group: " + ownProcessGroup);

			spooler_log.debug9("logFile.lentgh:" + logFile.length());
			if (logFile.length() > 0) {
				spooler_log.debug3("current setting log_file: " + logFile);
			}
			// parse environment variables

			/*if(orderData!=null && orderData.var("environment")!=null && orderData.var("environment").toString().length() > 0){
				String environment=orderData.var("environment").toString();
			 	Pattern envPattern = Pattern.compile("\\{[^\\}]*\\}");
			    Matcher envMatcher = envPattern.matcher(environment);
			    spooler_log.debug3("environment variables:");
			    while(envMatcher.find()){
			    	String nameValueBr=envMatcher.group();
			    	String nameValue=nameValueBr.substring(1,nameValueBr.length()-1);
			    	spooler_log.debug3("  "+nameValue);
			    	String[] nameValueArr=nameValue.split("=");
			    	if (nameValueArr.length!=2){
			    		spooler_log.warn("Incorrect Syntax for Name=Value Pair.");
			    		continue;
			    	}
			    	subProc.set_environment(nameValueArr[0], nameValueArr[1]);
			    }

			}*/

			// replace job-specific placeholders
			command = command.replaceAll("(\\$|§)\\{scheduler_order_job_name\\}", this.getJobName());
			command = command.replaceAll("(\\$|§)\\{scheduler_order_job_id\\}", Integer.toString(this.getJobId()));
			command = command.replaceAll("(\\$|§)\\{scheduler_id\\}", spooler.id());
			if (orderJob)
				if (order != null) {
				    command = command.replaceAll("(\\$|§)\\{scheduler_order_id\\}", order.id());
				}

			// replace parameters
			if (orderPayload != null) {
				command = JobSchedulerManagedObject.replaceVariablesInCommand(command, orderPayload, getLogger());
			}

			// replace newlines
			command = command.replaceAll("\r\n", "\n");
			getLogger().debug3("Command after replacements:\n" + command);

			String[] commands = command.split("\n");
			getLogger().debug6("Found " + commands.length + " commands.");
			// neu: mit subprocess

			for (int i = 0; i < commands.length && !timedOut; i++) {
				Subprocess subProc = spooler_task.create_subprocess();
				subProc.set_own_process_group(ownProcessGroup);
				subProc.set_ignore_error(ignoreError);
				subProc.set_ignore_signal(ignoreSignal);
				subProc.set_priority_class(priorityClass);
				try {
					setEnvironment(orderPayload, subProc);
				}
				catch (Exception e) {
					throw new Exception("Error occured setting environment variables: " + e);
				}
				// execute interpreter
				if (program != null && program.length() > 0) {
					subProc.start(program + " " + commands[i]);
					spooler_log.info("executing \"" + program + " " + commands[i] + "\"");
				}
				else {
					subProc.start(commands[i]);
					spooler_log.info("executing \"" + commands[i] + "\"");
				}
				if (orderPayload != null && orderPayload.var("timeout") != null && orderPayload.var("timeout").toString().length() > 0
						&& !orderPayload.var("timeout").toString().equals("0")) {
					spooler_log.info("executable file is launched with process id " + subProc.pid() + " for timeout in "
							+ orderPayload.var("timeout").toString() + "s");
					boolean terminated = subProc.wait_for_termination(Double.parseDouble(orderPayload.var("timeout").toString()));
					if (!terminated) {
						spooler_log.info("timeout reached, process will be terminated.");
						subProc.kill();
						subProc.wait_for_termination();
						timedOut = true;
					}

				}
				else {
					spooler_log.info("executable file is launched with process id " + subProc.pid());
					subProc.wait_for_termination();

				}
				if (!timedOut)
					spooler_log.info("file executed");
				spooler_log.debug9("Exit code: " + subProc.exit_code());

				boolean stdErrEmpty = true;
				String stdErrString = "";
				String stdOutString = "";
				spooler_log.info("std_out for " + commands[i] + ":");
				while (stdoutStream != null && stdoutStream.ready()) {
					String stdOutLine = stdoutStream.readLine();
					spooler_log.info(stdOutLine);
					stdOutString += stdOutLine + "\n";
				}
				spooler_log.info("std_err for " + commands[i] + ":");

				while (stderrStream != null && stderrStream.ready()) {
					String stdErrLine = stderrStream.readLine();
					spooler_log.info(stdErrLine);
					if (stdErrLine.trim().length() > 0)
						stdErrEmpty = false;
					stdErrString += stdErrLine + "\n";
				}
				if (orderJob && order != null) {
					Variable_set realOrderPayload = order.params();
					SetVar(realOrderPayload, conStd_err_output, stdErrString);
					SetVar(realOrderPayload, conStd_out_output, stdOutString);
					SetVar(realOrderPayload, conExit_code, "" + subProc.exit_code());
					SetVar(realOrderPayload, "timed_out", "" + timedOut);
					// for compatibility with SubProcessJob
					SetVar(realOrderPayload, "scheduler_order_terminated", (!timedOut ? "true" : "false"));
					replaceAliases(realOrderPayload, outputParameterAliases);
				} // additionally set task parameters for use with copy-from:
				Variable_set taskParams = spooler_task.params();
				SetVar(taskParams, conStd_err_output, stdErrString);
				SetVar(taskParams, conStd_out_output, stdOutString);
				SetVar(taskParams, conExit_code, "" + subProc.exit_code());
				SetVar(taskParams, "timed_out", "" + timedOut);
				replaceAliases(taskParams, outputParameterAliases);

				if (timedOut && !ignoreTimeout) {
					throw new Exception("Process had to be killed because of timeout");
				}
				if ((subProc.exit_code() != 0)) {
					if (ignoreError)
						spooler_log.info("Command terminated with exit code: " + subProc.exit_code());
					else
						throw new Exception("Command terminated with exit code: " + subProc.exit_code());
				}
				if ((subProc.termination_signal() != 0)) {
					if (ignoreSignal)
						spooler_log.info("Command terminated with signal: " + subProc.termination_signal());
					else
						throw new Exception("Command terminated with signal: " + subProc.termination_signal());
				}
				if (!ignoreStderr && !stdErrEmpty) {
					throw new Exception("Command terminated with text in stderr:\n" + stdErrString);
				}

			}

			return orderJob;
		}
		catch (Exception e) {
			if (orderJob)
				spooler_log.warn("error occurred processing managed order ["
						+ ((order != null) ? "Job Chain: " + order.job_chain().name() + ", ID:" + order.id() : "(none)") + "] : " + e);
			else
				spooler_log.warn("error occurred processing executable file: " + e);
			spooler_task.end();
			return false;
		}
		finally {
			if (logFile.length() > 0) {
				spooler_log.log_file(logFile);
			}
		}
	}

	private void replaceAliases(Variable_set pOrderPayload, String[][] aliases) {
		if (pOrderPayload != null && aliases != null) {
			for (int i = 0; i < aliases.length; i++) {
				String aliasParam = aliases[i][0];
				String replacedParam = aliases[i][1];
				String aliasParamValue = pOrderPayload.value(aliasParam);
//				if (aliasParamValue != null && aliasParamValue.length() > 0) {
					if (aliasParamValue != null) {
					SetVar(pOrderPayload, replacedParam, aliasParamValue);
				}
				else {
					spooler_log.info("Variable not found: '" + aliasParam + "'.");
				}
			}
		}
	}

	/**
	 * 
	 * \brief SetVar
	 * 
	 * \details
	 *
	 * \return void
	 *
	 * @param objVars
	 * @param strVarName
	 * @param strVarValue
	 */
	private void SetVar(final Variable_set objVars, final String strVarName, final String strVarValue) {
		
		final String	conMethodName	= conClassName + "::SetVar";
		
		objVars.set_var(strVarName, strVarValue);
		spooler_log.info(conMethodName + "Variable '" + strVarName + "' set to value '" + strVarValue + "'.");
		
	} // private void SetVar
	
	/**
	 * 
	 * \brief modifyCommand
	 * 
	 * \details
	 *
	 * \return String
	 *
	 * @param command
	 * @return
	 */
	protected String modifyCommand(String command) {
		// TODO Auto-generated method stub
		return command;
	}

	/**
	 * Cleanup
	 */
	public void spooler_exit() {

		super.spooler_exit();
	}

	private void setEnvironment(Variable_set vars, Subprocess proc) throws Exception {
		if (vars == null || vars.xml() == null || vars.xml().length() == 0)
			return;
		String[] keys = vars.names().split(";");
		for (int i = 0; i < keys.length; i++) {
			String parameterName = keys[i];
			String parameterValue = vars.var(keys[i]);
			if (parameterName.startsWith("env.")) {
				proc.set_environment(parameterName.substring(4), parameterValue);
			}
		}
		if (vars.value("scheduler_file_path") != null && vars.value("scheduler_file_path").length() > 0) {
			proc.set_environment("SCHEDULER_TRIGGER_FILE", vars.value("scheduler_file_path"));
		}
		if (vars.value("scheduler_order_additional_envvars") != null && vars.value("scheduler_order_additional_envvars").length() > 0) {
			getLogger().debug3("Setting additional envvars.");
			String[] envVarKeys = vars.value("scheduler_order_additional_envvars").split(";");
			for (int i = 0; i < envVarKeys.length; i++) {
				String key = envVarKeys[i];
				String value = vars.value(key);
				if (value != null && value.length() > 0)
					proc.set_environment(key, value);
			}
		}
	}

}