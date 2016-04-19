package sos.scheduler.managed;

import java.io.BufferedReader;
import java.io.FileReader;

import sos.spooler.Order;
import sos.spooler.Subprocess;
import sos.spooler.Variable_set;

/** @author andreas pueschel */
public class JobSchedulerManagedExecutableJob extends JobSchedulerManagedJob {

    private BufferedReader stdoutStream;
    private BufferedReader stderrStream;
    private final String strOrderParamPrefix = "scheduler_order_";
    private String[][] inputParameterAliases = { { strOrderParamPrefix + "ignore_stderr", "ignore_stderr" },
            { strOrderParamPrefix + "ignore_error", "ignore_error" }, { strOrderParamPrefix + "ignore_signal", "ignore_signal" },
            { strOrderParamPrefix + "timeout", "timeout" }, { strOrderParamPrefix + "priority_class", "priority_class" } };
    private final String conStd_err_output = "std_err_output";
    private final String conStd_out_output = "std_out_output";
    private final String conExit_code = "exit_code";
    private final String conClassName = "JobSchedulerManagedExecutableJob";
    private final String[][] outputParameterAliases = { { conStd_err_output, strOrderParamPrefix + "stderr_output" },
            { conStd_out_output, strOrderParamPrefix + "stdout_output" }, { conExit_code, strOrderParamPrefix + "exit_code" } };

    public boolean spooler_init() {
        try {
            FileReader fisOut = new FileReader(spooler_task.stdout_path());
            FileReader fisErr = new FileReader(spooler_task.stderr_path());
            stdoutStream = new BufferedReader(fisOut);
            stderrStream = new BufferedReader(fisErr);
        } catch (Exception e) {
            spooler_log.warn("failed to initialize stdout and stderr streams. " + e);
        }
        if (!super.spooler_init()) {
            return false;
        }
        return true;
    }

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
                    command = JobSchedulerManagedObject.getOrderCommand(this.getConnection(), this, commandScript);
                    order = spooler_task.order();
                }
                if (command == null || command.trim().isEmpty()) {
                    command = JobSchedulerManagedObject.getJobCommand(this.getConnection(), this);
                }
                command = modifyCommand(command);
                if (command == null || command.trim().isEmpty()) {
                    throw new Exception("command is empty");
                }
            } catch (Exception e) {
                throw (new Exception("no executable command found for order:" + e));
            }
            getLogger().debug3("command before replacements:\n" + command);
            boolean ignoreError = false;
            boolean ignoreSignal = false;
            boolean ignoreStderr = false;
            boolean ignoreTimeout = false;
            boolean timedOut = false;
            boolean ownProcessGroup = false;
            if (orderPayload != null) {
                replaceAliases(orderPayload, inputParameterAliases);
                if (orderPayload.var("ignore_error") != null
                        && ("true".equalsIgnoreCase(orderPayload.var("ignore_error").toString())
                                || "1".equalsIgnoreCase(orderPayload.var("ignore_error").toString()) || "yes".equalsIgnoreCase(orderPayload.var("ignore_error")
                                .toString()))) {
                    ignoreError = true;
                }
                if (orderPayload.var("ignore_signal") != null
                        && ("true".equalsIgnoreCase(orderPayload.var("ignore_signal").toString())
                                || "1".equalsIgnoreCase(orderPayload.var("ignore_signal").toString()) || "yes".equalsIgnoreCase(orderPayload.var(
                                "ignore_signal").toString()))) {
                    ignoreSignal = true;
                }
                if (orderPayload.var("ignore_stderr") != null
                        && ("true".equalsIgnoreCase(orderPayload.var("ignore_stderr").toString())
                                || "1".equalsIgnoreCase(orderPayload.var("ignore_stderr").toString()) || "yes".equalsIgnoreCase(orderPayload.var(
                                "ignore_stderr").toString()))) {
                    ignoreStderr = true;
                }
                if (orderPayload.var("ignore_timeout") != null
                        && ("true".equalsIgnoreCase(orderPayload.var("ignore_timeout").toString())
                                || "1".equalsIgnoreCase(orderPayload.var("ignore_timeout").toString()) || "yes".equalsIgnoreCase(orderPayload.var(
                                "ignore_timeout").toString()))) {
                    ignoreTimeout = true;
                }
                if (orderPayload.var("own_process_group") != null
                        && ("true".equalsIgnoreCase(orderPayload.var("own_process_group").toString())
                                || "1".equalsIgnoreCase(orderPayload.var("own_process_group").toString()) || "yes".equalsIgnoreCase(orderPayload.var(
                                "own_process_group").toString()))) {
                    ownProcessGroup = true;
                }
                if (orderPayload.var("log_file") != null && !orderPayload.var("log_file").toString().isEmpty()) {
                    logFile = orderPayload.var("log_file").toString();
                }
                if (orderPayload.var("priority_class") != null && !orderPayload.var("priority_class").toString().isEmpty()) {
                    priorityClass = orderPayload.var("priority_class").toString();
                }
                if (orderPayload.var("scheduler_order_command_parameters") != null
                        && !orderPayload.var("scheduler_order_command_parameters").toString().isEmpty()) {
                    command += " " + orderPayload.var("scheduler_order_command_parameters").toString();
                }
            }
            if (spooler_task.params().var("interpreter") != null && !spooler_task.params().var("interpreter").isEmpty()) {
                program = spooler_task.params().var("interpreter");
                spooler_log.debug3("Using interpreter: " + program);
            }
            spooler_log.debug3("current setting ignore_error: " + ignoreError);
            spooler_log.debug3("current setting ignore_signal: " + ignoreSignal);
            spooler_log.debug3("current setting own_process_group: " + ownProcessGroup);
            spooler_log.debug9("logFile.lentgh:" + logFile.length());
            if (!logFile.isEmpty()) {
                spooler_log.debug3("current setting log_file: " + logFile);
            }
            command = command.replaceAll("(\\$|§)\\{scheduler_order_job_name\\}", this.getJobName());
            command = command.replaceAll("(\\$|§)\\{scheduler_order_job_id\\}", Integer.toString(this.getJobId()));
            command = command.replaceAll("(\\$|§)\\{scheduler_id\\}", spooler.id());
            if (orderJob) {
                if (order != null) {
                    command = command.replaceAll("(\\$|§)\\{scheduler_order_id\\}", order.id());
                }
            }
            if (orderPayload != null) {
                command = JobSchedulerManagedObject.replaceVariablesInCommand(command, orderPayload, getLogger());
            }
            command = command.replaceAll("\r\n", "\n");
            getLogger().debug3("Command after replacements:\n" + command);
            String[] commands = command.split("\n");
            getLogger().debug6("Found " + commands.length + " commands.");
            for (int i = 0; i < commands.length && !timedOut; i++) {
                Subprocess subProc = spooler_task.create_subprocess();
                subProc.set_own_process_group(ownProcessGroup);
                subProc.set_ignore_error(ignoreError);
                subProc.set_ignore_signal(ignoreSignal);
                subProc.set_priority_class(priorityClass);
                try {
                    setEnvironment(orderPayload, subProc);
                } catch (Exception e) {
                    throw new Exception("Error occured setting environment variables: " + e);
                }
                if (program != null && !program.isEmpty()) {
                    subProc.start(program + " " + commands[i]);
                    spooler_log.info("executing \"" + program + " " + commands[i] + "\"");
                } else {
                    subProc.start(commands[i]);
                    spooler_log.info("executing \"" + commands[i] + "\"");
                }
                if (orderPayload != null && orderPayload.var("timeout") != null && !orderPayload.var("timeout").toString().isEmpty()
                        && !"0".equals(orderPayload.var("timeout").toString())) {
                    spooler_log.info("executable file is launched with process id " + subProc.pid() + " for timeout in "
                            + orderPayload.var("timeout").toString() + "s");
                    boolean terminated = subProc.wait_for_termination(Double.parseDouble(orderPayload.var("timeout").toString()));
                    if (!terminated) {
                        spooler_log.info("timeout reached, process will be terminated.");
                        subProc.kill();
                        subProc.wait_for_termination();
                        timedOut = true;
                    }
                } else {
                    spooler_log.info("executable file is launched with process id " + subProc.pid());
                    subProc.wait_for_termination();
                }
                if (!timedOut) {
                    spooler_log.info("file executed");
                }
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
                    if (!stdErrLine.trim().isEmpty()) {
                        stdErrEmpty = false;
                    }
                    stdErrString += stdErrLine + "\n";
                }
                if (orderJob && order != null) {
                    Variable_set realOrderPayload = order.params();
                    SetVar(realOrderPayload, conStd_err_output, stdErrString);
                    SetVar(realOrderPayload, conStd_out_output, stdOutString);
                    SetVar(realOrderPayload, conExit_code, "" + subProc.exit_code());
                    SetVar(realOrderPayload, "timed_out", "" + timedOut);
                    SetVar(realOrderPayload, "scheduler_order_terminated", (!timedOut ? "true" : "false"));
                    replaceAliases(realOrderPayload, outputParameterAliases);
                }
                Variable_set taskParams = spooler_task.params();
                SetVar(taskParams, conStd_err_output, stdErrString);
                SetVar(taskParams, conStd_out_output, stdOutString);
                SetVar(taskParams, conExit_code, "" + subProc.exit_code());
                SetVar(taskParams, "timed_out", "" + timedOut);
                replaceAliases(taskParams, outputParameterAliases);
                if (timedOut && !ignoreTimeout) {
                    throw new Exception("Process had to be killed because of timeout");
                }
                if (subProc.exit_code() != 0) {
                    if (ignoreError) {
                        spooler_log.info("Command terminated with exit code: " + subProc.exit_code());
                    } else {
                        throw new Exception("Command terminated with exit code: " + subProc.exit_code());
                    }
                }
                if (subProc.termination_signal() != 0) {
                    if (ignoreSignal) {
                        spooler_log.info("Command terminated with signal: " + subProc.termination_signal());
                    } else {
                        throw new Exception("Command terminated with signal: " + subProc.termination_signal());
                    }
                }
                if (!ignoreStderr && !stdErrEmpty) {
                    throw new Exception("Command terminated with text in stderr:\n" + stdErrString);
                }
            }
            return orderJob;
        } catch (Exception e) {
            if (orderJob) {
                spooler_log.warn("error occurred processing managed order ["
                        + ((order != null) ? "Job Chain: " + order.job_chain().name() + ", ID:" + order.id() : "(none)") + "] : " + e);
            } else {
                spooler_log.warn("error occurred processing executable file: " + e);
            }
            spooler_task.end();
            return false;
        } finally {
            if (!logFile.isEmpty()) {
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
                if (aliasParamValue != null) {
                    SetVar(pOrderPayload, replacedParam, aliasParamValue);
                } else {
                    spooler_log.info("Variable not found: '" + aliasParam + "'.");
                }
            }
        }
    }

    private void SetVar(final Variable_set objVars, final String strVarName, final String strVarValue) {
        final String conMethodName = conClassName + "::SetVar";
        objVars.set_var(strVarName, strVarValue);
        spooler_log.info(conMethodName + "Variable '" + strVarName + "' set to value '" + strVarValue + "'.");
    }

    protected String modifyCommand(String command) {
        // TO DO Auto-generated method stub
        return command;
    }

    public void spooler_exit() {
        super.spooler_exit();
    }

    private void setEnvironment(Variable_set vars, Subprocess proc) throws Exception {
        if (vars == null || vars.xml() == null || vars.xml().isEmpty()) {
            return;
        }
        String[] keys = vars.names().split(";");
        for (int i = 0; i < keys.length; i++) {
            String parameterName = keys[i];
            String parameterValue = vars.var(keys[i]);
            if (parameterName.startsWith("env.")) {
                proc.set_environment(parameterName.substring(4), parameterValue);
            }
        }
        if (vars.value("scheduler_file_path") != null && !vars.value("scheduler_file_path").isEmpty()) {
            proc.set_environment("SCHEDULER_TRIGGER_FILE", vars.value("scheduler_file_path"));
        }
        if (vars.value("scheduler_order_additional_envvars") != null && !vars.value("scheduler_order_additional_envvars").isEmpty()) {
            getLogger().debug3("Setting additional envvars.");
            String[] envVarKeys = vars.value("scheduler_order_additional_envvars").split(";");
            for (int i = 0; i < envVarKeys.length; i++) {
                String key = envVarKeys[i];
                String value = vars.value(key);
                if (value != null && !value.isEmpty()) {
                    proc.set_environment(key, value);
                }
            }
        }
    }

}