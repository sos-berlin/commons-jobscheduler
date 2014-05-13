
package sos.scheduler.webservice;


import sos.spooler.Job_impl;
import sos.spooler.Subprocess;
import sos.spooler.Variable_set;


public class JobSchedulerWebServiceShellJob extends Job_impl {

    
    public boolean spooler_process() {

        try {
            String commandline = "";
            Variable_set parameters = null;;
            Subprocess subprocess = spooler_task.create_subprocess();
            
            if (spooler_task.params().value("shell_command") != null && spooler_task.params().value("shell_command").length() > 0) {
                commandline = spooler_task.params().value("shell_command");
            } else {
                throw new Exception( "no value for shell command was given for parameter [shell_command]" );
            }

            /* these are samples for shell commands:
            if ( System.getProperty("os.name").toLowerCase().startsWith("win") ) {
              // Windows: the command to be executed, modify this to an executable script for your platform
              commandline = "cmd.exe /V:ON /C " + spooler.directory() + "samples/shell.webservice.command/webservice_shell.cmd";
            } else {
              // Unix: the command to be executed, modify this to an executable script for your platform
              commandline = spooler.directory() + "samples/shell.webservice.command/webservice_shell.sh";
            }
            */

            // retrieve parameters from order payload (<job order="yes"/>) or from task parameters
            if ( spooler_job.order_queue() != null ) {
              parameters = spooler_task.order().params();
            } else {
              parameters = spooler_task.params();
            }

            if (parameters != null) {
              String[] parameter_names = parameters.names().split(";");
              for(int i=0; i<parameter_names.length; i++) {
                if ( parameter_names[i].equalsIgnoreCase("shell_command") ) continue;
                // .. either append parameter names and values to the comamnd line (this needs more checking for vulnerabilities)
                //  spooler_log.debug3( "parameter " + parameter_names[i] + "=" + parameters.value(parameter_names[i]) );
                //  commandline += " " + parameter_names[i] + "=\"" + parameters.value(parameter_names[i]) + "\"";

                // .. or move parameters to environment variables which is more safe
                subprocess.set_environment( parameter_names[i], parameters.value(parameter_names[i]) );
                // .. and for this sample append parameter names to the command line
                commandline += " \"" + parameter_names[i] + "\"";
              }
            }

            spooler_log.info( ".. executing command line: " + commandline );
            subprocess.start( commandline );

            // wait until the subprocess terminates, timeout is restricted by <job timeout="..."/> or subprocess.wait_for_termination(60)
            subprocess.wait_for_termination();
            if (subprocess.terminated()) {
              spooler_log.info( "exit code=" + subprocess.exit_code() );
              spooler_log.info( "termination signal=" + subprocess.termination_signal() );
            }

            return !(spooler_job.order_queue() == null);
        } catch (Exception e) {
            spooler_log.warn("error occurred processing web service request: " + e.getMessage());
            return false;
        }
    }

}
