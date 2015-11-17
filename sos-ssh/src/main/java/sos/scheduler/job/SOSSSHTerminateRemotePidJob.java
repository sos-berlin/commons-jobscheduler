package sos.scheduler.job;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import sos.net.ssh.SOSSSHJob2;
import sos.net.ssh.SOSSSHJobJSch;
import sos.net.ssh.exceptions.SSHConnectionError;
import sos.net.ssh.exceptions.SSHExecutionError;

import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "com_sos_net_messages", defaultLocale = "en")
public class SOSSSHTerminateRemotePidJob extends SOSSSHJobJSch{
  private final Logger logger = Logger.getLogger(this.getClass());
  private static final String PARAM_PIDS_TO_KILL = "PIDS_TO_KILL";
  private static final String PID_PLACEHOLDER = "${pid}";
  private static final String USER_PLACEHOLDER = "${user}";
  private static final String COMMAND_PLACEHOLDER = "${command}";
  private static final String DEFAULT_LINUX_TERMINATE_PID_COMMAND = "kill -15 " + PID_PLACEHOLDER;
  private static final String DEFAULT_WINDOWS_TERMINATE_PID_COMMAND = 
      "taskkill /pid " + PID_PLACEHOLDER + " /FI \"USERNAME eq " + 
      USER_PLACEHOLDER + "\" /FI \"IMAGENAME eq " + COMMAND_PLACEHOLDER + "\"";
  private String ssh_job_terminate_pid_command = "kill -15 " + PID_PLACEHOLDER;//default
  private List<Integer> allPids = new ArrayList<Integer>();
  
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
  public SOSSSHJob2 connect() {
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
    getTerminateCommandFromJobParameters();
    isConnected = true;
    return this;
  }

  @Override
  public SOSSSHJob2 execute() throws Exception {
    vfsHandler.setJSJobUtilites(objJSJobUtilities);
    boolean configuredRaiseExeptionOnError = objOptions.raise_exception_on_error.value();
    boolean configuredIgnoreError = objOptions.ignore_error.value();
	objOptions.raise_exception_on_error.value(false);
	objOptions.ignore_error.value(true);
    openSession();
    List<Integer> pidsToKillFromOrder = getPidsToKillFromOrder(); 
    try {
      if (isConnected == false) {
        this.connect();
      }
      logger.debug("try to kill remote PIDs");
      for(Integer pid : pidsToKillFromOrder){
    	  allPids.add(pid);
    	  executeGetAllChildProcesses(pid);
      }
      for(Integer pid : allPids){
          processTerminateCommand(pid);
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
    }finally{
		objOptions.raise_exception_on_error.value(configuredRaiseExeptionOnError);
		objOptions.ignore_error.value(configuredIgnoreError);
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

  private void processTerminateCommand(Integer pid){
	    boolean configuredRaiseExeptionOnError = objOptions.raise_exception_on_error.value();
	    boolean configuredIgnoreError = objOptions.ignore_error.value();
		objOptions.raise_exception_on_error.value(false);
		objOptions.ignore_error.value(true);
    logger.debug("Sending terminate command: " + ssh_job_terminate_pid_command + " with ${pid}=" + pid);
    String terminateCommand = null;
    if(ssh_job_terminate_pid_command.contains(PID_PLACEHOLDER)){
      terminateCommand = ssh_job_terminate_pid_command.replace(PID_PLACEHOLDER, pid.toString());
    }
    if(ssh_job_terminate_pid_command.contains(USER_PLACEHOLDER)){
      terminateCommand = terminateCommand.replace(USER_PLACEHOLDER, objOptions.UserName.Value());
    }
    if(ssh_job_terminate_pid_command.contains(COMMAND_PLACEHOLDER)){
      terminateCommand = terminateCommand.replace(COMMAND_PLACEHOLDER, objOptions.command.Value());
    }
    String stdErr = "";
    try {
      vfsHandler.ExecuteCommand(terminateCommand);
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
    }finally{
		objOptions.raise_exception_on_error.value(configuredRaiseExeptionOnError);
		objOptions.ignore_error.value(configuredIgnoreError);
	}
    
  }
  
   private void getTerminateCommandFromJobParameters(){
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
   
   private boolean executeGetAllChildProcesses(Integer pPid){
	    boolean configuredRaiseExeptionOnError = objOptions.raise_exception_on_error.value();
	    boolean configuredIgnoreError = objOptions.ignore_error.value();
		objOptions.raise_exception_on_error.value(false);
		objOptions.ignore_error.value(true);
		try {
			String command;
			if (objOptions.getssh_job_get_child_processes_command().Value().contains(PID_PLACEHOLDER)) {
				command = objOptions.getssh_job_get_child_processes_command().Value().replace(PID_PLACEHOLDER, pPid.toString());
			} else {
				command = objOptions.getssh_job_get_child_processes_command().Value();
			}
			logger.debug("***Execute read children of pid command!***");
			vfsHandler.ExecuteCommand(command);
			BufferedReader reader = new BufferedReader(new StringReader( new String(vfsHandler.getStdOut())));
			String line = null;
			while ((line = reader.readLine()) != null) {
				// get the first line via a regex matcher,
				// if first line is parseable to an Integer we have the pid for
				// the execute channel [SP]
				logger.debug(line);
				Matcher regExMatcher = Pattern.compile("^([^\r\n]*)\r*\n*").matcher(line);
				if (regExMatcher.find()) {
					String pid = regExMatcher.group(1).trim(); // key with leading and trailing whitespace removed
					try {
						logger.debug("PID: " + pid);
						allPids.add(Integer.parseInt(pid));
						executeGetAllChildProcesses(Integer.parseInt(pid));
						continue;
					} catch (Exception e) {
						logger.debug("no parseable pid received in line: \"" + pid + "\"");
					}
				}
			}
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}finally{
			objOptions.raise_exception_on_error.value(configuredRaiseExeptionOnError);
			objOptions.ignore_error.value(configuredIgnoreError);
		}
	  }

}
