package sos.scheduler.job;

import java.util.HashMap;
import java.util.UUID;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

import sos.net.ssh.SOSSSHJob2;
import sos.net.ssh.SOSSSHJobJSch;
import sos.net.ssh.SOSSSHJobTrilead;
import sos.net.ssh.SOSSSHJobOptions;
import sos.spooler.Job_chain;
import sos.spooler.Order;
import sos.spooler.Variable_set;

public class SOSSSHJob2JSAdapter extends SOSSSHJob2JSBaseAdapter {
  private final String conClassName = this.getClass().getSimpleName();
  private static final String PARAM_SSH_JOB_TASK_ID = "SSH_JOB_TASK_ID";
  private static final String PARAM_PIDS_TO_KILL = "PIDS_TO_KILL";
  private static final String PARAM_RUN_WITH_WATCHDOG = "RUN_WITH_WATCHDOG";
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
    finally {
    } // finally

    return signalSuccess();

  } // spooler_process
  
  private void doProcessing() throws Exception {
    SOSSSHJob2 objR;
    Variable_set allParams = getGlobalSchedulerParameters();
    allParams.merge(getParameters());
    if(allParams.value(PARAM_JITL_SSH_USE_JSCH_IMPL) == null ||
        allParams.value(PARAM_JITL_SSH_USE_JSCH_IMPL).equalsIgnoreCase("default") ||
        allParams.value(PARAM_JITL_SSH_USE_JSCH_IMPL).equalsIgnoreCase("false")){
      //this is the default value for 1.9, will change to JSch with v 1.10 [SP]
      useTrilead = true;
      objR = new SOSSSHJobTrilead();
      spooler_log.debug9("uses Trilead implementation of SSH");
    } else {
      useTrilead = false;
      objR = new SOSSSHJobJSch();
      // generate temporary file for remote pids for further usage
      pidFileName = generateTempPidFileName();
      ((SOSSSHJobJSch)objR).setPidFileName(pidFileName);
      if(allParams.value(PARAM_RUN_WITH_WATCHDOG) != null && 
          !allParams.value(PARAM_RUN_WITH_WATCHDOG).isEmpty() && 
          allParams.value(PARAM_RUN_WITH_WATCHDOG).equalsIgnoreCase("true")){
        createOrderForWatchdog();
        allParams.set_value(PARAM_PID_FILE_NAME_KEY, pidFileName);
      }
      spooler_log.debug9("uses JSch implementation of SSH");
    } 
    SOSSSHJobOptions objO = objR.Options();
    objO.CurrentNodeName(this.getCurrentNodeName());
    HashMap<String, String> hsmParameters1 = getSchedulerParameterAsProperties(allParams);
    objO.setAllOptions(objO.DeletePrefix(hsmParameters1, "ssh_"));
    objR.setJSJobUtilites(this);
    if (objO.commandSpecified() == false) {
      setJobScript(objO.command_script);
    }

    objO.CheckMandatory();
    objR.Execute();
  }
  
  // creates a new order for the cleanup jobchain with all the options, params, values and the TaskId of the task which created this
  private void createOrderForWatchdog(){
    Order order = spooler.create_order();
    order.params().merge(spooler_task.order().params());
    order.params().set_var(PARAM_SSH_JOB_TASK_ID, String.valueOf(spooler_task.id()));
    order.params().set_var(PARAM_PID_FILE_NAME_KEY, pidFileName);
    // delayed start after 5 seconds when the order is created
    order.set_at("now+15");
    Job_chain chain = spooler.job_chain("kill_jobs/remote_cleanup_test");
    chain.add_or_replace_order(order);
  }
  
  private String generateTempPidFileName(){
    UUID uuid = UUID.randomUUID();
    String pidFileName = "sos-ssh-pid-" + uuid + ".txt";
    return pidFileName;
  }

}
