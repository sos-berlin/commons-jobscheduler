package sos.scheduler.job;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import sos.net.ssh.SOSSSHJobJSch;
import sos.net.ssh.exceptions.SSHConnectionError;
import sos.net.ssh.exceptions.SSHExecutionError;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;

public class SOSSSHKillRemotePidJob extends SOSSSHJobJSch{
  // https://change.sos-berlin.com/browse/JITL-147: Additional Handler for kill
  // commands
  private ISOSVFSHandler killCommandsVFSHandler = null;
  private final Logger logger = Logger.getLogger(this.getClass());
  private Map<Integer, PsEfLine> pids = new HashMap<Integer, PsEfLine>();
  /** timestamp of the last text from stdin or stderr **/
  protected long lasttime = 0;
  /**
   * time to wait if anything more is coming from stdout or stderr when logging
   * in
   **/
  protected long loginTimeout = 0;
  /**
   * time to wait if anything more is coming from stdout or stderr when
   * executing commands
   **/
  protected long commandTimeout = 0;
  /** Line currently being displayed on the shell **/
  protected String currentLine = "";

  private void prepareKillCommandsHandler() {
    if (killCommandsVFSHandler == null) {
      try {
        killCommandsVFSHandler = VFSFactory.getHandler("SSH2.JSCH");
      } catch (Exception e) {
        throw new JobSchedulerException("SOS-VFS-E-0010: unable to initialize VFS for kill commands", e);
      }
    }
  }

  private void openKillCommandsSession() {
    try {
      if (!killCommandsVFSHandler.isConnected()) {
        SOSConnection2OptionsAlternate postAlternateOptions = getAlternateOptions(objOptions);
        postAlternateOptions.raise_exception_on_error.value(false);
        killCommandsVFSHandler.Connect(postAlternateOptions);
      }
      killCommandsVFSHandler.Authenticate(objOptions);
      logger.debug("connection for kill commands established");
    } catch (Exception e) {
      throw new SSHConnectionError("Error occured during connection/authentication: " + e.getLocalizedMessage(), e);
    }
    killCommandsVFSHandler.setJSJobUtilites(objJSJobUtilities);
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
    openKillCommandsSession();
    String killCommand = "kill -9 " + pid;
    String stdErr = "";
    try {
      killCommandsVFSHandler.ExecuteCommand(killCommand);
      // check if command was processed correctly
      if (killCommandsVFSHandler.getExitCode() != 0) {
        stdErr = killCommandsVFSHandler.getStdErr().toString();
        if (stdErr.length() > 0) {
          if (objOptions.ignore_stderr.value()) {
            logger.debug(stdErr);
          } else {
            logger.error(stdErr);
          }
        }
      }
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

  /**
   * This thread consumes output from the remote server puts it into fields of
   * the main class
   */
  class RemoteConsumer extends Thread {
    private StringBuffer sbuf;
    private boolean writeCurrentline = false;
    private InputStream stream;
    boolean end = false;

    private RemoteConsumer(StringBuffer buffer, boolean writeCurr, InputStream str) {
      this.sbuf = buffer;
      this.writeCurrentline = true;
      this.stream = str;
    }

    private void addText(byte[] data, int len) {
      lasttime = System.currentTimeMillis();
      String outstring = new String(data).substring(0, len);
      sbuf.append(outstring);
      if (writeCurrentline) {
        int newlineIndex = outstring.indexOf("\n");
        if (newlineIndex > -1) {
          String stringAfterNewline = outstring.substring(newlineIndex);
          currentLine = stringAfterNewline;
        } else
          currentLine += outstring;
      }
    }

    public void run() {
      byte[] buff = new byte[64];
      try {
        while (!end) {
          buff = new byte[8];
          int len = stream.read(buff);
          if (len == -1) return;
          addText(buff, len);
        }
      } catch (Exception e) {
        // TODO
      }
    }

    public synchronized void end() {
      end = true;
    }
  }

}
