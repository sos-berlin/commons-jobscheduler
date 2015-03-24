package sos.scheduler.job;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.common.SOSVfsMessageCodes;

import sos.net.ssh.SOSSSHJob2;
import sos.net.ssh.SOSSSHJobJSch;
import sos.net.ssh.exceptions.SSHConnectionError;

public class SOSSSHCheckRemotePidJob extends SOSSSHJobJSch{
  // https://change.sos-berlin.com/browse/JITL-147: Additional Handler for kill
  // commands
  private ISOSVFSHandler checkRemotePidCommandVFSHandler = null;
  private final Logger logger = Logger.getLogger(this.getClass());
  private Map<Integer, PsEfLine> pids = new HashMap<Integer, PsEfLine>();

  private void prepareCheckRemotePidCommandHandler() {
    if (checkRemotePidCommandVFSHandler == null) {
      try {
        checkRemotePidCommandVFSHandler = VFSFactory.getHandler("SSH2.JSCH");
      } catch (Exception e) {
        throw new JobSchedulerException("SOS-VFS-E-0010: unable to initialize VFS for checking remote pid command", e);
      }
    }
  }
  
  private void getPidsFromJob(){
    Vector<String> pidsFromJob = objOptions.getItems("PID_TO_KILL");
    
  }

  private void openCheckRemotePidCommandSession() {
    try {
      if (!checkRemotePidCommandVFSHandler.isConnected()) {
        SOSConnection2OptionsAlternate postAlternateOptions = getAlternateOptions(objOptions);
        postAlternateOptions.raise_exception_on_error.value(false);
        checkRemotePidCommandVFSHandler.Connect(postAlternateOptions);
      }
      checkRemotePidCommandVFSHandler.Authenticate(objOptions);
      logger.debug("connection for kill commands established");
    } catch (Exception e) {
      throw new SSHConnectionError("Error occured during connection/authentication: " + e.getLocalizedMessage(), e);
    }
    checkRemotePidCommandVFSHandler.setJSJobUtilites(objJSJobUtilities);
  }

  @Override
  public SOSSSHJob2 Connect() {
    getVFS();
    Options().CheckMandatory();
    try {
      SOSConnection2OptionsAlternate alternateOptions = getAlternateOptions(objOptions);
      checkRemotePidCommandVFSHandler.Connect(alternateOptions);
      checkRemotePidCommandVFSHandler.Authenticate(objOptions);
      logger.debug("connection established");
    } catch (Exception e) {
      throw new SSHConnectionError("Error occured during connection/authentication: " + e.getLocalizedMessage(), e);
    }
    flgIsWindowsShell = checkRemotePidCommandVFSHandler.remoteIsWindowsShell();
    isConnected = true;
    // https://change.sos-berlin.com/browse/JITL-147
    prepareCheckRemotePidCommandHandler();
    return this;
  } // private SOSSSHJob2 Connect

  public void executeListProcessesCommands() {
    openCheckRemotePidCommandSession();

    String stdOut = "";
    String stdErr = "";
    pids = new HashMap();
    try {
      checkRemotePidCommandVFSHandler.ExecuteCommand("/bin/ps -ef");
      // check if command was processed correctly
      if (checkRemotePidCommandVFSHandler.getExitCode() == 0) {
        // read stdout of the read-temp-file statement per line
        if (checkRemotePidCommandVFSHandler.getStdOut().toString().length() > 0) {
          BufferedReader reader = new BufferedReader(new StringReader(new String(checkRemotePidCommandVFSHandler.getStdOut())));
          String line = null;
          logger.debug(SOSVfsMessageCodes.SOSVfs_D_284.getFullMessage());
          while ((line = reader.readLine()) != null) {
            if (line == null) break;
            line = line.trim();
            String[] fields = line.split(" +", 8);
            PsEfLine psOutputLine = new PsEfLine();
            psOutputLine.user = fields[0];
            psOutputLine.pid = Integer.parseInt(fields[1]);
            psOutputLine.parentPid = Integer.parseInt(fields[2]);
            psOutputLine.pidCommand = fields[7];
            pids.put(new Integer(psOutputLine.pid), psOutputLine);
          }
          Iterator psOutputLineIterator = pids.values().iterator();
          while (psOutputLineIterator.hasNext()) {
            PsEfLine current = (PsEfLine) psOutputLineIterator.next();
            PsEfLine parent = (PsEfLine) pids.get(new Integer(current.parentPid));
            if (parent != null) {
              logger.debug("Child of " + parent.pid + " is " + current.pid);
              parent.children.add(new Integer(current.pid));
            }
          }
        } else {
          logger.debug("no stdout received from remote host");
        }
      }else{
        logger.error("error occured executing command /bin/ps -ef");
      }
    } catch (Exception e) {
      logger.error("error occured executing command");
    } finally {
      if (pids != null && pids.size() > 0){
        Vector<String> pidsToKillAsString = objOptions.getItems("PID_TO_KILL");
        Vector<Integer> pidsToKill = new Vector<Integer>();
        for (String pid : pidsToKillAsString){
          pidsToKill.add(Integer.parseInt(pid));
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
