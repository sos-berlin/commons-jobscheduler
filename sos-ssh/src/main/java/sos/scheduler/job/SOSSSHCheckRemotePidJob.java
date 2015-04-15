package sos.scheduler.job;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
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

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.Files.JSIniFile;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.common.SOSVfsMessageCodes;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "com_sos_net_messages", defaultLocale = "en")
public class SOSSSHCheckRemotePidJob extends SOSSSHJobJSch{
  private final Logger logger = Logger.getLogger(this.getClass());
  private List<Integer> availablePidsToKill = new ArrayList<Integer>();
  private static final String PARAM_PIDS_TO_KILL = "PIDS_TO_KILL";
  private static final String KEY_SSH_JOB_GET_ACTIVE_PROCESSES_COMMAND = "ssh_job_get_active_processes_command";
//  private static final String DEFAULT_LINUX_GET_ACTIVE_PROCESSES_COMMAND = "/bin/ps -ef | grep %s";
  private static final String DEFAULT_LINUX_GET_ACTIVE_PROCESSES_COMMAND = "kill -0 %s";
  private static final String DEFAULT_WINDOWS_GET_ACTIVE_PROCESSES_COMMAND = "echo Add command to get active processes to stdout here!";
//  private String ssh_job_get_active_processes_command = "/bin/ps -ef | grep %s";
  private String ssh_job_get_active_processes_command = "kill -0 %s";

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
    if(objOptions.osProfile.isDirty()){
      readActiveProcessesCommandFromPropertiesFile();
      logger.debug("Command to get active processes from OS Profile File used!");
    } else {
      if(flgIsWindowsShell){
        ssh_job_get_active_processes_command = DEFAULT_WINDOWS_GET_ACTIVE_PROCESSES_COMMAND;
        logger.debug("Default Windows command used to get active processes!");
      }else{
        ssh_job_get_active_processes_command = DEFAULT_LINUX_GET_ACTIVE_PROCESSES_COMMAND;
        logger.debug("Default Linux command used to get active processes!");
      }
    }
    isConnected = true;
    return this;
  } 

  @Override
  public SOSSSHJob2 Execute() {
    vfsHandler.setJSJobUtilites(objJSJobUtilities);
    openSession();
    Map<Integer, PsEfLine> remoteRunningPids = new HashMap<Integer, PsEfLine>();
    boolean configuredRaiseExeptionOnError = objOptions.raise_exception_on_error.value();
    boolean configuredIgnoreError = objOptions.ignore_error.value();
    List<Integer> pidsToKillFromOrder = getPidsToKill();
    List<Integer> pidsStillRunning = new ArrayList<Integer>();
    try {
      if (isConnected == false) {
        this.Connect();
      }
      objOptions.raise_exception_on_error.value(false);
      objOptions.ignore_error.value(true);
      for(Integer pid : pidsToKillFromOrder){
        vfsHandler.ExecuteCommand(String.format(ssh_job_get_active_processes_command, pid));
        if (vfsHandler.getExitCode() == 0) {
          // process found
          pidsStillRunning.add(pid);
          logger.debug("PID " + pid + " is still running");
        }else{
          logger.debug("PID " + pid + " is not running anymore");
        }
      }
      if(pidsStillRunning.size() > 0){
        // and override the order param with the resulting (still running) pids only to later kill them
        StringBuilder strb = new StringBuilder();
        logger.debug("Overriding param " + PARAM_PIDS_TO_KILL);
        boolean first = true;
        // create a String with the comma separated pids to put in one Param 
        for (Integer pid : pidsStillRunning){
          if (first){
            strb.append(pid.toString());
            first = false;
          }else{
            strb.append(",").append(pid.toString());
          }
        }
        logger.debug("still running PIDs to kill: " + strb.toString());
        objJSJobUtilities.setJSParam(PARAM_PIDS_TO_KILL, strb.toString());
      }else{
        objJSJobUtilities.setJSParam(PARAM_PIDS_TO_KILL, "");
      }
//      vfsHandler.ExecuteCommand(String.format(ssh_job_get_active_processes_command, objOptions.user.Value()));
      // check if command was processed correctly
//      if (vfsHandler.getExitCode() == 0) {
        // read stdout of the read-temp-file statement per line
//        if (vfsHandler.getStdOut().toString().length() > 0) {
//          BufferedReader reader = new BufferedReader(new StringReader(new String(vfsHandler.getStdOut())));
//          String line = null;
//          logger.debug(SOSVfsMessageCodes.SOSVfs_D_284.getFullMessage());
//          boolean firstLine = true;
//          while ((line = reader.readLine()) != null) {
//            if(firstLine){
//              // The first line is the Header so we skip that
//              firstLine = false;
//              continue;
//            }
//            if (line == null) break;
//            line = line.trim();
//            String[] fields = line.split(" +", 8);
//            PsEfLine psOutputLine = new PsEfLine();
//            // Field numbers correspond to linux format of ps command
//            // Windows Qprocess command differs in field sorting!
//            psOutputLine.user = fields[0];
//            psOutputLine.pid = Integer.parseInt(fields[1]);
//            psOutputLine.parentPid = Integer.parseInt(fields[2]);
//            psOutputLine.pidCommand = fields[7];
//            remoteRunningPids.put(new Integer(psOutputLine.pid), psOutputLine);
//          }
//          Iterator psOutputLineIterator = remoteRunningPids.values().iterator();
//          while (psOutputLineIterator.hasNext()) {
//            PsEfLine current = (PsEfLine) psOutputLineIterator.next();
//            PsEfLine parent = (PsEfLine) remoteRunningPids.get(new Integer(current.parentPid));
//            if (parent != null) {
////              logger.debug("Child of " + parent.pid + " is " + current.pid);
//              parent.children.add(new Integer(current.pid));
//            }
//          }
//        } else {
//          logger.debug("no stdout received from remote host");
//        }
//      }else{
//        logger.error("error occured executing command: " + String.format(ssh_job_get_active_processes_command, objOptions.user.Value()));
//      }
    } catch (JobSchedulerException ex){
      if(pidsStillRunning.size() == 0){
        logger.debug("Overriding PARAM_PIDS_TO_KILL with empty String");
        objJSJobUtilities.setJSParam(PARAM_PIDS_TO_KILL, "");
      }
    }catch (Exception e) {
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
    finally {
//      if (remoteRunningPids != null && remoteRunningPids.size() > 0){
//        // receive pids from Order params here
//        List<Integer> pidsToKillFromOrder = getPidsToKill();
//        availablePidsToKill = new ArrayList<Integer>();
//        for(Integer pidToKill : pidsToKillFromOrder){
//          // then check if the pids are still running on the remote host
//          PsEfLine psLine;
//          if((psLine = remoteRunningPids.get(pidToKill)) != null){
//            logger.debug("Added to available PIDs: " + psLine.pid);
//            availablePidsToKill.add(pidToKill);
//          } else{
//            logger.debug("PID " + pidToKill + " not found" );
//          }
//        }
//        // and override the order param with the resulting (still running) pids only to later kill them
//        StringBuilder strb = new StringBuilder();
//        if (availablePidsToKill.size() > 0){
//          logger.debug("Overriding param " + PARAM_PIDS_TO_KILL);
//          boolean first = true;
//          // create a String with the comma separated pids to put in one Param 
//          for (Integer pid : availablePidsToKill){
//            if (first){
//              strb.append(pid.toString());
//              first = false;
//            }else{
//              strb.append(",").append(pid.toString());
//            }
//          }
//          logger.debug("still running PIDs to kill: " + strb.toString());
//        }
//        objJSJobUtilities.setJSParam(PARAM_PIDS_TO_KILL, strb.toString());
//      }
      objOptions.raise_exception_on_error.value(configuredRaiseExeptionOnError);
      objOptions.ignore_error.value(configuredIgnoreError);
    }
    return this;
  }

  private List<Integer> getPidsToKill(){
    logger.debug("PIDs to kill From Order: " + objOptions.getItem(PARAM_PIDS_TO_KILL));
    String[] pidsFromOrder = null;
    if(objOptions.getItem(PARAM_PIDS_TO_KILL) != null && objOptions.getItem(PARAM_PIDS_TO_KILL).length() > 0){
      pidsFromOrder = objOptions.getItem(PARAM_PIDS_TO_KILL).split(",");
    }
    List<Integer> pidsToKill = new ArrayList<Integer>();
    if (pidsFromOrder != null) {
      for (String pid : pidsFromOrder) {
        if (pid != null && pid.length() > 0) {
          pidsToKill.add(Integer.parseInt(pid));
        } else {
          logger.debug("PID is empty!");
        }
      }
    }
    return pidsToKill;
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

  private void readActiveProcessesCommandFromPropertiesFile(){
    JSIniFile osProfile = new JSIniFile(objOptions.osProfile.Value());
    ssh_job_get_active_processes_command = osProfile.getPropertyString("ssh_commands", KEY_SSH_JOB_GET_ACTIVE_PROCESSES_COMMAND, "default");
  }
  
}
