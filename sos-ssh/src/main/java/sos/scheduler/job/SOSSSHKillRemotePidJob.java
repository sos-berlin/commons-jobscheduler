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

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "com_sos_net_messages", defaultLocale = "en")
public class SOSSSHKillRemotePidJob extends SOSSSHJobJSch{
  private ISOSVFSHandler vfsHandler = null;
  private final Logger logger = Logger.getLogger(this.getClass());
  private Map<Integer, PsEfLine> pids = new HashMap<Integer, PsEfLine>();
  private static final String PARAM_PIDS_TO_KILL = "PIDS_TO_KILL";
  
  @Override
  public ISOSVFSHandler getVFSSSH2Handler() {
    try {
      vfsHandler = VFSFactory.getHandler("SSH2.JSCH");
    }
    catch (Exception e) {
      throw new JobSchedulerException("SOS-VFS-E-0010: unable to initialize VFS", e);
    }
    return vfsHandler;
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
    // https://change.sos-berlin.com/browse/JITL-147
    return this;
  } // private SOSSSHJob2 Connect

  private List<Integer> getPidsToKillFromOrder(){
    String[] pidsFromOrder = objOptions.getItem(PARAM_PIDS_TO_KILL).split(",");
    List<Integer> pidsToKill = new ArrayList<Integer>();
    for(String pid : pidsFromOrder){
      pidsToKill.add(Integer.parseInt(pid));
    }
    return pidsToKill;
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
  
  private void prepareKillCommandsHandler() {
    if (vfsHandler == null) {
      try {
        vfsHandler = VFSFactory.getHandler("SSH2.JSCH");
      } catch (Exception e) {
        throw new JobSchedulerException("SOS-VFS-E-0010: unable to initialize VFS for kill commands", e);
      }
    }
  }

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
    String killCommand = "kill -9 " + pid;
    String stdErr = "";
    try {
      vfsHandler.ExecuteCommand(killCommand);
      // check if command was processed correctly
      if (vfsHandler.getExitCode() != 0) {
        stdErr = vfsHandler.getStdErr().toString();
        if (stdErr.length() > 0) {
          if (objOptions.ignore_stderr.value()) {
            logger.debug(stdErr);
          } else {
            logger.error(stdErr);
          }
        }
      }
//      CheckStdOut();
//      CheckStdErr();
//      CheckExitCode();
//      ChangeExitSignal();
    } catch (Exception e) {
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
  }

//  /**
//   * This thread consumes output from the remote server puts it into fields of
//   * the main class
//   */
//  class RemoteConsumer extends Thread {
//    private StringBuffer sbuf;
//    private boolean writeCurrentline = false;
//    private InputStream stream;
//    boolean end = false;
//
//    private RemoteConsumer(StringBuffer buffer, boolean writeCurr, InputStream str) {
//      this.sbuf = buffer;
//      this.writeCurrentline = true;
//      this.stream = str;
//    }
//
//    private void addText(byte[] data, int len) {
//      lasttime = System.currentTimeMillis();
//      String outstring = new String(data).substring(0, len);
//      sbuf.append(outstring);
//      if (writeCurrentline) {
//        int newlineIndex = outstring.indexOf("\n");
//        if (newlineIndex > -1) {
//          String stringAfterNewline = outstring.substring(newlineIndex);
//          currentLine = stringAfterNewline;
//        } else
//          currentLine += outstring;
//      }
//    }
//
//    public void run() {
//      byte[] buff = new byte[64];
//      try {
//        while (!end) {
//          buff = new byte[8];
//          int len = stream.read(buff);
//          if (len == -1) return;
//          addText(buff, len);
//        }
//      } catch (Exception e) {
//        // TODO
//      }
//    }
//
//    public synchronized void end() {
//      end = true;
//    }
//  }

}
