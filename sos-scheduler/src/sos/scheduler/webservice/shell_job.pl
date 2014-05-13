package main;

use strict;
use warnings FATAL => "all";
use vars qw($spooler $spooler_log $spooler_job $spooler_task);

use Config;


sub spooler_process() {

  my $rc = 0;
     $rc = 1 if $spooler_job->order_queue;
  
  eval {
    my $commandline = "";
    my $parameters  = "";
    my $subprocess  = $spooler_task->create_subprocess();
   
    if ($spooler_task->params->value("shell_command") && length($spooler_task->params->value("shell_command")) > 0) {
        $commandline = $spooler_task->params->value("shell_command");
    } else {
        $spooler_log->warn( "no value for shell command was given for parameter [shell_command]" );
        return 0;
    }

#   these are samples for shell commands:
#   if ($Config{osname} eq 'MSWin32')  { 
#      #  Windows: the command to be executed, modify this to an executable script for your platform
#      $commandline = "cmd.exe /V:ON /C " . $spooler->directory . "samples/shell.webservice.command/webservice_shell.cmd";
#   } else {
#      #  Unix: the command to be executed, modify this to an executable script for your platform
#      $commandline = $spooler->directory . "samples/shell.webservice.command/webservice_shell.sh";
#   }

    #  retrieve parameters from order payload or from task parameters
    if ($rc) {
      $parameters = $spooler_task->order->payload;
    } else {
      $parameters = $spooler_task->params;
    }

    if ($parameters) {
      my @parameter_names = split( /;/, $parameters->names() );
      for(my $i=0; $i<@parameter_names; $i++) {
      if ( lc($parameter_names[$i]) eq "shell_command" ) { next; }
        # .. either append parameter names and values to the comamnd line (this needs more checking for vulnerabilities)
        #  $spooler_log->debug3( "parameter " . $parameter_names[$i] . "=" . $parameters->value($parameter_names[$i]) );
        #  commandline .= " " . $parameter_names[$i] . "=\"" . $parameters->value($parameter_names[$i]) . "\"";

        # .. or copy parameters to environment variables which is more safe
        $subprocess->LetProperty( 'environment', $parameter_names[$i], $parameters->value($parameter_names[$i]) );
        # .. and for this sample append parameter names to the command line
        $commandline .= " \"" . $parameter_names[$i] . "\"";
      }
    }

    $spooler_log->info( "... executing command line: " . $commandline );
    $subprocess->start( $commandline );

    $subprocess->wait_for_termination();
    if ( $subprocess->terminated ) {
      $spooler_log->info( "exit code=" . $subprocess->exit_code );
      $spooler_log->info( "termination signal=" . $subprocess->termination_signal );
    }
  };
  if ( $@ ) {
    chomp $@; 
    $spooler_log->warn( $@ ); 
    return 0;
  }
  
  return $rc;
}
