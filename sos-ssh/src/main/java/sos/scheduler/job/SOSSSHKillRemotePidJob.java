package sos.scheduler.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
  private Map<Integer, PsEfLine> pids = new HashMap<Integer, PsEfLine>();
  private static final String PARAM_PIDS_TO_KILL = "PIDS_TO_KILL";
  
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

  private void killChildrenOfPid(PsEfLine psCommandLine, HashMap pidMap) throws Exception {
    if (psCommandLine.children.size() == 0) return;
    Iterator iter = psCommandLine.children.iterator();
    while (iter.hasNext()) {
      Integer childPid = (Integer) iter.next();
      PsEfLine child = (PsEfLine) pidMap.get(childPid);
      logger.debug("killing child pid: " + child.pid);
      processKillCommand(child.pid);
      killChildrenOfPid(child, pidMap);
    }
  }
  
  private void processKillCommand(Integer pid){
    logger.debug("Sending kill command: kill -9 " + pid);
    String killCommand = "kill -9 " + pid;
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

   private class PsEfLine implements Comparable {
    public String user;
    public Integer pid;
    public Integer parentPid;
    public String pidCommand;
    public List children = new ArrayList();

    /* (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object) */
    public int compareTo(Object o) {
      PsEfLine other = (PsEfLine) o;
      return pid - other.pid;
    }
  }

}
