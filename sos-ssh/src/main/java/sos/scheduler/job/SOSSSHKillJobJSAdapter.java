package sos.scheduler.job;

import java.util.HashMap;

import sos.net.ssh.SOSSSHJob2;
import sos.net.ssh.SOSSSHJobOptions;
import sos.net.ssh.exceptions.SSHExecutionError;
import sos.spooler.Variable_set;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class SOSSSHKillJobJSAdapter extends SOSSSHJob2JSBaseAdapter {
  private static final String PARAM_PIDS_TO_KILL = "PIDS_TO_KILL";
  private static final String PARAM_SSH_JOB_TASK_ID = "SSH_JOB_TASK_ID";
  private static final String PARAM_SSH_JOB_NAME = "SSH_JOB_NAME";
  private Variable_set allParams;
  
  @Override
  public boolean spooler_process() throws Exception {
    boolean successfull = true;
    try {
      super.spooler_process();
      successfull = doProcessing();
    }
    catch (Exception e) {
      logger.fatal(StackTrace2String(e));
      throw new JobSchedulerException(e);
    }
    if(successfull){
      return signalSuccess();
    }else{
      return signalFailure();
    }
  } 
  
  private boolean doProcessing() throws Exception {
    SOSSSHJob2 sshJob;
    allParams = getGlobalSchedulerParameters();
    allParams.merge(getParameters());
    boolean taskIsActive = true;
    if(allParams.value(PARAM_SSH_JOB_TASK_ID) != null && !allParams.value(PARAM_SSH_JOB_TASK_ID).isEmpty()){
      taskIsActive = isTaskActive(allParams.value(PARAM_SSH_JOB_TASK_ID));
    } else {
      taskIsActive = false;
    }
    logger.debug("Task is still active: " + taskIsActive);
    sshJob = executeCheckPids();
    String runningPids = getParameters().value(PARAM_PIDS_TO_KILL);
    if(taskIsActive && runningPids != null && !runningPids.isEmpty()){
      // if task is still running and remote pids are still available  --> do nothing
      // check again after some delay
      logger.debug("Task and remote processes are still active, do nothing!");
      return false;
    }else if (taskIsActive && (runningPids == null || runningPids.isEmpty())) {
      // if task is still running but remote pid is not available anymore (finished) --> kill task
      logger.debug("Task is still active, try to end task!");
      String killTaskXml = new String("<kill_task job=\"" + allParams.value(PARAM_SSH_JOB_NAME) + "\" id=\""+ allParams.value(PARAM_SSH_JOB_TASK_ID) + "\" immediately=\"yes\"/>");
      String killTaskXmlAnswer = spooler.execute_xml(killTaskXml);
      //log level info only for development change to debug afterwards
      logger.debug("killTaskXmlAnswer:\n" + killTaskXmlAnswer);
      return true;
    } else if (!taskIsActive && runningPids != null && !runningPids.isEmpty()) {
      logger.debug("Task is not active anymore, processing kill remote pids!");
      // if task is not running anymore but remote pid is still available --> kill remote pid
      sshJob = executeKillPids();
      return true;
    } else if(!taskIsActive && (runningPids == null || runningPids.isEmpty())){
      // if task is not running anymore AND remote pid is not available anymore --> do nothing
      logger.debug("Task is not active anymore, remote pids not available anymore. Nothing to do!");
      return true;
    }
    return true;
  }
  
  private boolean isTaskActive(String taskId){
    String showTaskXml = new String("<show_task id=\"" + taskId + "\"/>");
    String showTaskAnswerXml = spooler.execute_xml(showTaskXml);
    //log level info only for development change to debug afterwards
    logger.debug("showTaskAnswer:\n" + showTaskAnswerXml);
    if(showTaskAnswerXml.contains("state=\"running")){
      return true;
    } else {
      return false;
    }
  }
  
  private SOSSSHJob2 executeCheckPids() {
    SOSSSHJob2 sshJob = null;
    SOSSSHJobOptions options = null;
    try {
      sshJob = new SOSSSHCheckRemotePidJob();
      logger.debug("SOSSSHCheckRemotePidJob instantiated!");
      options = sshJob.Options();
      options.CurrentNodeName(this.getCurrentNodeName());
      HashMap<String, String> hsmParameters1 = getSchedulerParameterAsProperties(allParams);
      options.setAllOptions(options.DeletePrefix(hsmParameters1, "ssh_"));
      sshJob.setJSJobUtilites(this);
      options.CheckMandatory();
      sshJob.Execute();
    } catch (Exception e) {
      if(options.raise_exception_on_error.value()){
        if(options.ignore_error.value()){
          if(options.ignore_stderr.value()){
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
    return sshJob;
  }
  
  private SOSSSHJob2 executeKillPids(){
    SOSSSHJob2 sshJob = null;
    SOSSSHJobOptions options = null;
    try {
      sshJob = new SOSSSHKillRemotePidJob();
      logger.debug("SOSSSHKillRemotePidJob instantiated!");
      options = sshJob.Options();
      options.CurrentNodeName(this.getCurrentNodeName());
      HashMap<String, String> hsmParameters1 = getSchedulerParameterAsProperties(allParams);
      options.setAllOptions(options.DeletePrefix(hsmParameters1, "ssh_"));
      sshJob.setJSJobUtilites(this);
      options.CheckMandatory();
      sshJob.Execute();
    } catch (Exception e) {
      if(options.raise_exception_on_error.value()){
        if(options.ignore_error.value()){
          if(options.ignore_stderr.value()){
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
    return sshJob;
  }
  
}
