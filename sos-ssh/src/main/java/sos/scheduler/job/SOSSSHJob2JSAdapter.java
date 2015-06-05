package sos.scheduler.job;

import java.util.HashMap;
import java.util.UUID;

import sos.net.ssh.SOSSSHJob2;
import sos.net.ssh.SOSSSHJobJSch;
import sos.net.ssh.SOSSSHJobOptions;
import sos.net.ssh.SOSSSHJobTrilead;
import sos.spooler.Job_chain;
import sos.spooler.Order;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class SOSSSHJob2JSAdapter extends SOSSSHJob2JSBaseAdapter {
  private final String conClassName = this.getClass().getSimpleName();
  private static final String PARAM_SSH_JOB_TASK_ID = "SSH_JOB_TASK_ID";
  private static final String PARAM_SSH_JOB_NAME = "SSH_JOB_NAME";
  private static final String PARAM_JITL_SSH_USE_JSCH_IMPL = "jitl.ssh.use_jsch_impl";
  private static final String PARAM_PID_FILE_NAME_KEY = "job_ssh_pid_file_name";
  private boolean useTrilead = true;
  private String pidFileName;
  
  @Override
  public boolean spooler_process() throws Exception {
    @SuppressWarnings("unused")
    final String conMethodName = conClassName + "::spooler_process";
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
    SOSSSHJob2 objR;
    String useJSch = spooler.var(PARAM_JITL_SSH_USE_JSCH_IMPL);
    SOSSSHJobOptions objO = null;
    if(!useJSch.equalsIgnoreCase("true")){
      //this is the default value for v1.9, will change to JSch with v1.10 [SP]
      useTrilead = true;
      objR = new SOSSSHJobTrilead();
      objO = objR.Options();
      spooler_log.debug9("uses Trilead implementation of SSH");
    } else {
      useTrilead = false;
      objR = new SOSSSHJobJSch();
      objO = objR.Options();
      spooler_log.debug9("uses JSch implementation of SSH");
    } 
    objO.CurrentNodeName(this.getCurrentNodeName());
    HashMap<String, String> hsmParameters1 = getSchedulerParameterAsProperties(getJobOrOrderParameters());
    if(!useTrilead){
      ((SOSSSHJobJSch)objR).setAllParams(hsmParameters1);
    }
    objO.setAllOptions(objO.DeletePrefix(hsmParameters1, "ssh_"));
    objR.setJSJobUtilites(this);
    if (objO.commandSpecified() == false) {
      setJobScript(objO.command_script);
    }
    objO.CheckMandatory();
    if(!useTrilead){
      // generate temporary file for remote pids for further usage
      spooler_log.debug9("Run with watchdog set to: " + objO.runWithWatchdog.Value());
      if(objO.runWithWatchdog.value()){
        pidFileName = generateTempPidFileName();
        ((SOSSSHJobJSch)objR).setPidFileName(pidFileName);
        createOrderForWatchdog();
      }
    }
    // if command_delimiter is not set by customer then we override the default value due to compatibility issues
    // the default command delimiter is used in the option class to split the commands with a delimiter not known by the os
    // but here a command delimiter (known by the os) is needed to chain commands together
    // TODO: a solution which fits for both cases [SP]
    if(!useTrilead && objO.command_delimiter.isNotDirty()){
      objO.command_delimiter.Value(";");
    }
    objR.Execute();
    if(!useTrilead){
      if(!((SOSSSHJobJSch)objR).getReturnValues().isEmpty()){
        for(String key : ((SOSSSHJobJSch)objR).getReturnValues().keySet()){
          spooler_task.order().params().set_var(key, ((SOSSSHJobJSch)objR).getReturnValues().get(key));
        }
      }
    }

  }
  
  // creates a new order for the cleanup jobchain with all the options, params, values and the TaskId of the task which created this
  private void createOrderForWatchdog(){
    spooler_log.debug9("createOrderForWatchdog started");
    Order order = spooler.create_order();
    order.params().merge(spooler_task.params());
    if(spooler_task.order() != null){
        order.params().merge(spooler_task.order().params());
    }
    order.params().set_var(PARAM_SSH_JOB_TASK_ID, String.valueOf(spooler_task.id()));
    order.params().set_var(PARAM_PID_FILE_NAME_KEY, pidFileName);
    order.params().set_var(PARAM_SSH_JOB_NAME, spooler_job.name());
    // delayed start after 15 seconds when the order is created
    order.set_at("now+15");
    Job_chain chain = null;
    if(spooler_task.params().value("cleanupJobchain") != null){
      chain = spooler.job_chain(spooler_task.params().value("cleanupJobchain"));      
      spooler_log.debug9("uses jobchainname from parameter");
      spooler_log.debug9("Jobchainname: " + spooler_task.params().value("cleanupJobchain"));
    }else{
      logger.error("No jobchain configured to received the order! Please configure the cleanupJobchain Parameter in your SSH Job Configuration.");
    }
    chain.add_or_replace_order(order);
    spooler_log.debug9("order send");
  }
  
  private String generateTempPidFileName(){
    UUID uuid = UUID.randomUUID();
    String pidFileName = "sos-ssh-pid-" + uuid + ".txt";
    return pidFileName;
  }

}
