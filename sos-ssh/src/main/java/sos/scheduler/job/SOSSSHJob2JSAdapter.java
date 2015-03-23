package sos.scheduler.job;

import java.util.HashMap;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

import sos.net.ssh.SOSSSHJob2;
import sos.net.ssh.SOSSSHJobJSch;
import sos.net.ssh.SOSSSHJobTrilead;
import sos.net.ssh.SOSSSHJobOptions;
import sos.spooler.Variable_set;

public class SOSSSHJob2JSAdapter extends SOSSSHJob2JSBaseAdapter {
  private final String conClassName = this.getClass().getSimpleName();
  
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
    if(allParams.value("jitl.ssh.use_jsch_impl") == null ||
        allParams.value("jitl.ssh.use_jsch_impl").equalsIgnoreCase("default") ||
        allParams.value("jitl.ssh.use_jsch_impl").equalsIgnoreCase("false")){
      //this is the default value for 1.9, will change to JSch with v 1.10 [SP]
      objR = new SOSSSHJobTrilead();
      spooler_log.debug9("uses Trilead implementation of SSH");
    } else {
      objR = new SOSSSHJobJSch();
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

}
