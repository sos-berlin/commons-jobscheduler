package sos.scheduler.job;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import sos.net.ssh.SOSSSHJob2;
import sos.net.ssh.SOSSSHJobJSch;
import sos.net.ssh.exceptions.SSHConnectionError;
import sos.net.ssh.exceptions.SSHExecutionError;

import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "com_sos_net_messages", defaultLocale = "en")
public class SOSSSHKillRemotePidJob extends SOSSSHJobJSch{
  private final Logger logger = Logger.getLogger(this.getClass());
  private static final String PARAM_PIDS_TO_KILL = "PIDS_TO_KILL";
  private static final String DEFAULT_LINUX_KILL_PID_COMMAND = "kill -9";
  private static final String DEFAULT_LINUX_TERMINATE_PID_COMMAND = "kill -15";
  private static final String DEFAULT_WINDOWS_KILL_PID_COMMAND = "echo Add command to kill PID here!";
  private static final String DEFAULT_WINDOWS_TERMINATE_PID_COMMAND = "echo Add command to terminate PID here!";
  private String ssh_job_kill_pid_command = "kill -9";//default
  private String ssh_job_terminate_pid_command = "kill -15";//default
  
  private void openSession() {
    try {
      if (!vfsHandler.isConnected()) {
        SOSConnection2OptionsAlternate postAlternateOptions = getAlternateOptions(objOptions);
        postAlternateOptions.raise_exception_on_error.value(false);
        vfsHandler.Connect(postAlternateOptions);
      }
      vfsHandler.Authenticate(objOptions);
      logger.debug("connection for kill commands established");
    } catch (Exception e) {
      throw new SSHConnectionError("Error occured during connection/authentication: " + e.getLocalizedMessage(), e);
    }
    vfsHandler.setJSJobUtilites(objJSJobUtilities);
  }

  @Override
  public SOSSSHJob2 Connect() {
    getVFS();
    Options().CheckMandatory();
    try {
      SOSConnection2OptionsAlternate alternateOptions = getAlternateOptions(objOptions);
      vfsHandler.Connect(alternateOptions);
      vfsHandler.Authenticate(objOptions);
      logger.debug("connection established");
    } catch (Exception e) {
      throw new SSHConnectionError("Error occured during connection/authentication: " + e.getLocalizedMessage(), e);
    }
    flgIsWindowsShell = vfsHandler.remoteIsWindowsShell();
    readKillAndTerminateCommandsFromPropertiesFile();
    isConnected = true;
    return this;
  }

  @Override
  public SOSSSHJob2 Execute() throws Exception {
    vfsHandler.setJSJobUtilites(objJSJobUtilities);
    openSession();
    List<Integer> pidsToKillFromOrder = getPidsToKillFromOrder(); 
    try {
      if (isConnected == false) {
        this.Connect();
      }
      logger.debug("try to kill remote PIDs");
      for(Integer pid : pidsToKillFromOrder){
        processKillCommand(pid);
      }
    } catch (Exception e) {
      if(objOptions.raise_exception_on_error.value()){
        if(objOptions.ignore_error.value()){
          if(objOptions.ignore_stderr.value()){
            logger.debug(this.StackTrace2String(e));
          }else{
            logger.error(this.StackTrace2String(e));
            throw new SSHExecutionError("Exception raised: " + e, e);
          }
        }else{
          logger.error(this.StackTrace2String(e));
          throw new SSHExecutionError("Exception raised: " + e, e);
        }
      }
    }
    return this;
  }
  
  private List<Integer> getPidsToKillFromOrder(){
    String[] pidsFromOrder = objOptions.getItem(PARAM_PIDS_TO_KILL).split(",");
    List<Integer> pidsToKill = new ArrayList<Integer>();
    for(String pid : pidsFromOrder){
      pidsToKill.add(Integer.parseInt(pid));
    }
    return pidsToKill;
  }

  private void processKillCommand(Integer pid){
    logger.debug("Sending kill command: " + ssh_job_kill_pid_command + " " + pid);
    String killCommand = ssh_job_kill_pid_command + " " + pid;
    String stdErr = "";
    try {
      vfsHandler.ExecuteCommand(killCommand);
    } catch (Exception e) {
      // check if command was processed correctly
      if (vfsHandler.getExitCode() != 0) {
        try {
          stdErr = vfsHandler.getStdErr().toString();
          if(stdErr.contains("No such process")){
            logger.debug("meanwhile the remote process is not available anymore!");
          } else {
            if (objOptions.raise_exception_on_error.value()) {
              if (objOptions.ignore_error.value()) {
                if (objOptions.ignore_stderr.value()) {
                  logger.debug("error occured while trying to execute command");
                } else {
                  logger.error("error occured while trying to execute command");
                  throw new SSHExecutionError("Exception raised: " + e, e);
                }
              } else {
                logger.error("error occured while trying to execute command");
                throw new SSHExecutionError("Exception raised: " + e, e);
              }
            }
          }
        } catch (Exception e1) {
          logger.debug("error occured while reading remote stderr");
        }
      }
    }
  }

   private void readKillAndTerminateCommandsFromPropertiesFile(){
     if(objOptions.ssh_job_kill_pid_command.isDirty() && !objOptions.ssh_job_kill_pid_command.Value().isEmpty()){
       ssh_job_kill_pid_command = objOptions.ssh_job_kill_pid_command.Value();
       logger.debug("Command to kill from Job Parameter used!");
     } else {
       if(flgIsWindowsShell){
         ssh_job_kill_pid_command = DEFAULT_WINDOWS_KILL_PID_COMMAND;
         logger.debug("Default Windows commands used to kill PID!");
       }else{
         ssh_job_kill_pid_command = DEFAULT_LINUX_KILL_PID_COMMAND;
         logger.debug("Default Linux commands used to kill PID!");
       }
     }
     if(objOptions.ssh_job_terminate_pid_command.isDirty() && !objOptions.ssh_job_terminate_pid_command.Value().isEmpty()){
       ssh_job_terminate_pid_command = objOptions.ssh_job_terminate_pid_command.Value();
       logger.debug("Commands to terminate from Job Parameter used!");
     } else {
       if(flgIsWindowsShell){
         ssh_job_terminate_pid_command = DEFAULT_WINDOWS_TERMINATE_PID_COMMAND;
         logger.debug("Default Windows commands used to terminate PID!");
       }else{
         ssh_job_terminate_pid_command = DEFAULT_LINUX_TERMINATE_PID_COMMAND;
         logger.debug("Default Linux commands used to terminate PID!");
       }
     }
   }
   
}
