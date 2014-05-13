function spooler_process() {

  try {
    var commandline;
    var parameters;
    var subprocess = spooler_task.create_subprocess();
    
    if (spooler_task.params.value("shell_command") != null && spooler_task.params.value("shell_command").length > 0) {
        commandline = spooler_task.params.value("shell_command");
    } else {
        throw "no value for shell command was given for parameter [shell_command]";
    }

    /* these are samples for shell commands:
    if ( String(java.lang.System.getProperty("os.name")).toLowerCase().substring(0,3) === "win" ) {
      // Windows: the command to be executed, modify this to an executable script for your platform
      commandline = "cmd.exe /V:ON /C " + spooler.directory + "samples/shell.webservice.command/webservice_shell.cmd";
    } else {
      // Unix: the command to be executed, modify this to an executable script for your platform
      commandline = spooler.directory + "samples/shell.webservice.command/webservice_shell.sh";
    }
    */
    
    // retrieve parameters from order payload (<job order="yes"/>) or from task parameters
    if ( spooler_job.order_queue != null ) {
      parameters = spooler_task.order.payload;
    } else {
      parameters = spooler_task.params;
    }

    if (parameters != null) {
      var parameter_names = parameters.names().split(";");
      for(var i=0; i<parameter_names.length; i++) {
      	if (parameter_names[i].toLowerCase() == "shell_command") continue;
        // .. either append parameter names and values to the comamnd line (this needs more checking for vulnerabilities)
        //  spooler_log.debug3( "parameter " + parameter_names[i] + "=" + parameters.value(parameter_names[i]) );
        //  commandline += " " + parameter_names[i] + "=\"" + parameters.value(parameter_names[i]) + "\"";

        // .. or move parameters to environment variables which is more safe
        subprocess.environment( parameter_names[i] ) = parameters.value( parameter_names[i] );
        // .. and for this sample append parameter names to the command line
        commandline += " \"" + parameter_names[i] + "\"";
      }
    }

    spooler_log.info( ".. executing command line: " + commandline );
    subprocess.start( commandline );

    // wait until the subprocess terminates, timeout is restricted by <job timeout="..."/> or subprocess.wait_for_termination(60)
    subprocess.wait_for_termination();
    if (subprocess.terminated) {
      spooler_log.info( "exit code=" + subprocess.exit_code );
      spooler_log.info( "termination signal=" + subprocess.termination_signal );
    }

    return !(spooler_job.order_queue == null);
  } catch (e) {
    spooler_log.warn( String(e) );
    return false;
  }
}
