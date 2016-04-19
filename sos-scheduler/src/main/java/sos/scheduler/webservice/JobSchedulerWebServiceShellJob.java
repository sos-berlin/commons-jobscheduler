package sos.scheduler.webservice;

import sos.spooler.Job_impl;
import sos.spooler.Subprocess;
import sos.spooler.Variable_set;

public class JobSchedulerWebServiceShellJob extends Job_impl {

    public boolean spooler_process() {
        try {
            String commandline = "";
            Variable_set parameters = null;
            Subprocess subprocess = spooler_task.create_subprocess();
            if (spooler_task.params().value("shell_command") != null && !spooler_task.params().value("shell_command").isEmpty()) {
                commandline = spooler_task.params().value("shell_command");
            } else {
                throw new Exception("no value for shell command was given for parameter [shell_command]");
            }
            if (spooler_job.order_queue() != null) {
                parameters = spooler_task.order().params();
            } else {
                parameters = spooler_task.params();
            }
            if (parameters != null) {
                String[] parameter_names = parameters.names().split(";");
                for (int i = 0; i < parameter_names.length; i++) {
                    if ("shell_command".equalsIgnoreCase(parameter_names[i])) {
                        continue;
                    }
                    subprocess.set_environment(parameter_names[i], parameters.value(parameter_names[i]));
                    commandline += " \"" + parameter_names[i] + "\"";
                }
            }
            spooler_log.info(".. executing command line: " + commandline);
            subprocess.start(commandline);
            subprocess.wait_for_termination();
            if (subprocess.terminated()) {
                spooler_log.info("exit code=" + subprocess.exit_code());
                spooler_log.info("termination signal=" + subprocess.termination_signal());
            }
            return !(spooler_job.order_queue() == null);
        } catch (Exception e) {
            spooler_log.warn("error occurred processing web service request: " + e.getMessage());
            return false;
        }
    }

}