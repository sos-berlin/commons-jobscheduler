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
  private Variable_set allParams;
  
  @Override
  public boolean spooler_process() throws Exception {
    try {
      super.spooler_process();
      doProcessing();
    }
    catch (Exception e) {
      logger.fatal(StackTrace2String(e));
      throw new JobSchedulerException(e);
    }
    return signalSuccess();
  } 
  
  private void doProcessing() throws Exception {
    SOSSSHJob2 sshJob;
    allParams = getGlobalSchedulerParameters();
    allParams.merge(getParameters());
    if(allParams.value(PARAM_SSH_JOB_TASK_ID) != null && !allParams.value(PARAM_SSH_JOB_TASK_ID).isEmpty()){
      boolean taskIsActive = isTaskActive(allParams.value(PARAM_SSH_JOB_TASK_ID));
      logger.info("Task is still active: " + taskIsActive);
      sshJob = executeCheckPids();
      String runningPids = allParams.value(PARAM_PIDS_TO_KILL);
      if (taskIsActive) {
        // if task is still running but remote pid is not available anymore (finished) --> kill task
        if(runningPids == null || runningPids.isEmpty()){
          String killTaskXml = new String("<kill_task id=\""+ allParams.value(PARAM_SSH_JOB_TASK_ID) + "\"immediately=\"yes\"/>");
          String killTaskXmlAnswer = spooler.execute_xml(killTaskXml);
          //log level info only for development change to debug afterwards
          logger.info("killTaskXmlAnswer:\n" + killTaskXmlAnswer);
        }
      }else{
        logger.info("Task is not active anymore, processing kill remote pids!");
        // if task is not running anymore but remote pid is still available --> kill remote pid
        if(runningPids != null && !runningPids.isEmpty()){
          sshJob = executeKillPids();
        }
      }
      // if task is not running anymore AND remote pid is not available anymore --> do nothing
      if(!taskIsActive && (runningPids == null || runningPids.isEmpty())){
        logger.info("Task no active anymore, remote pids not available anymore. Nothing to do!");
      }
    }
  }
  
  private boolean isTaskActive(String taskId){
    String showTaskXml = new String("<show_task id=\"" + taskId + "\"/>");
    String showTaskAnswerXml = spooler.execute_xml(showTaskXml);
    //log level info only for development change to debug afterwards
    logger.info("showTaskAnswer:\n" + showTaskAnswerXml);
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
      logger.info("SOSSSHCheckRemotePidJob instantiated!");
      options = sshJob.Options();
      options.CurrentNodeName(this.getCurrentNodeName());
      HashMap<String, String> hsmParameters1 = getSchedulerParameterAsProperties(allParams);
      options.setAllOptions(options.DeletePrefix(hsmParameters1, "ssh_"));
      sshJob.setJSJobUtilites(this);
      options.CheckMandatory();
      sshJob.Execute();
    } catch (Exception e) {
      e.printStackTrace();
//      if(options.raise_exception_on_error.value()){
//        if(options.ignore_error.value()){
//          if(options.ignore_stderr.value()){
//            logger.debug(this.StackTrace2String(e));
//          }else{
//            logger.error(this.StackTrace2String(e));
//            throw new SSHExecutionError("Exception raised: " + e, e);
//          }
//        }else{
//          logger.error(this.StackTrace2String(e));
//          throw new SSHExecutionError("Exception raised: " + e, e);
//        }
//      }
    }
    return sshJob;
  }
  
  private SOSSSHJob2 executeKillPids(){
    SOSSSHJob2 sshJob = null;
    SOSSSHJobOptions options = null;
    try {
      sshJob = new SOSSSHKillRemotePidJob();
      logger.info("SOSSSHKillRemotePidJob instantiated!");
      options = sshJob.Options();
      options.CurrentNodeName(this.getCurrentNodeName());
      HashMap<String, String> hsmParameters1 = getSchedulerParameterAsProperties(allParams);
      options.setAllOptions(options.DeletePrefix(hsmParameters1, "ssh_"));
      sshJob.setJSJobUtilites(this);
      options.CheckMandatory();
      sshJob.Execute();
    } catch (Exception e) {
      logger.error(this.StackTrace2String(e));
//      if(options.raise_exception_on_error.value()){
//        if(options.ignore_error.value()){
//          if(options.ignore_stderr.value()){
//            logger.debug(this.StackTrace2String(e));
//          }else{
//            logger.error(this.StackTrace2String(e));
//            throw new SSHExecutionError("Exception raised: " + e, e);
//          }
//        }else{
//          logger.error(this.StackTrace2String(e));
//          throw new SSHExecutionError("Exception raised: " + e, e);
//        }
//      }
    }
    return sshJob;
  }
  
}
