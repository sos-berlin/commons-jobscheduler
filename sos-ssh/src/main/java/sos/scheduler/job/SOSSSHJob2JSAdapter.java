package sos.scheduler.job;

import java.util.HashMap;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

import sos.net.ssh.SOSSSHJob2;
import sos.net.ssh.SOSSSHJobTrilead;
import sos.net.ssh.SOSSSHJobOptions;

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

    SOSSSHJob2 objR = new SOSSSHJobTrilead();
    SOSSSHJobOptions objO = objR.Options();
    objO.CurrentNodeName(this.getCurrentNodeName());
    HashMap<String, String> hsmParameters1 = getSchedulerParameterAsProperties(getJobOrOrderParameters());
    objO.setAllOptions(objO.DeletePrefix(hsmParameters1, "ssh_"));

    objR.setJSJobUtilites(this);
    if (objO.commandSpecified() == false) {
      setJobScript(objO.command_script);
    }

    objO.CheckMandatory();
    objR.Execute();
  }

}
