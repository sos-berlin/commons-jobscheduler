package sos.net.ssh;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import sos.net.ssh.exceptions.SSHConnectionError;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSConnection;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.common.SOSVfsMessageCodes;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "com_sos_net_messages", defaultLocale = "en")
public class SOSSSHJobJcraft extends SOSSSHJob2 {

  // http://www.sos-berlin.com/jira/browse/JITL-112: Additional Handler for post commands
  private ISOSVFSHandler    prePostCommandVFSHandler        = null;
  private final Logger logger = Logger.getLogger(this.getClass());

  // http://www.sos-berlin.com/jira/browse/JITL-112
  private static final String SCHEDULER_RETURN_VALUES = "SCHEDULER_RETURN_VALUES";
  private String tempFileName;
  private ISOSVFSHandler vfsHandler;


  // http://www.sos-berlin.com/jira/browse/JITL-112
  @Override
  public void generateTemporaryFilename() {
    UUID uuid = UUID.randomUUID();
    tempFileName = "sos-ssh-return-values-" + uuid + ".txt";
  }

  @Override
  public String getPreCommand() {
    return String.format(objOptions.getPreCommand().Value()  + objOptions.command_delimiter.Value(), SCHEDULER_RETURN_VALUES, tempFileName);
  }

  @Override
  public void preparePostCommandHandler() {
    if (prePostCommandVFSHandler == null) {
      try {
        prePostCommandVFSHandler = VFSFactory.getHandler("SSH2");
      }
      catch (Exception e) {
        // TODO msg must be used in the VFSFactory because it is an VFS Msg
        throw new JobSchedulerException("SOS-VFS-E-0010: unable to initialize second VFS", e);
      }
    }
  }

  @Override
  public void processPostCommands(String tmpFileName){
    openPrePostCommandsSession();
    String postCommandRead = String.format(objOptions.getPostCommandRead().Value(), tmpFileName);
    String stdErr = "";


    try {
      prePostCommandVFSHandler.ExecuteCommand(postCommandRead);
      // check if command was processed correctly 
      if(prePostCommandVFSHandler.getExitCode() == 0){
        // read stdout of the read-temp-file statement per line
        if(prePostCommandVFSHandler.getStdOut().toString().length() > 0){
          BufferedReader reader = new BufferedReader(new StringReader(new String(prePostCommandVFSHandler.getStdOut())));
          String line = null;
          logger.debug(SOSVfsMessageCodes.SOSVfs_D_284.getFullMessage());
          while ((line = reader.readLine()) != null){
            Matcher regExMatcher = Pattern.compile("^([^=]+)=(.*)").matcher(line);
            if(regExMatcher.find()){
              String key = regExMatcher.group(1).trim(); // key with leading and trailing whitespace removed
              String value = regExMatcher.group(2).trim(); // value with leading and trailing whitespace removed
              objJSJobUtilities.setJSParam(key, value);
            }
          }
          // remove temp file after parsing return values from file
          String postCommandDelete = String.format(objOptions.getPostCommandDelete().Value(), tmpFileName);
          prePostCommandVFSHandler.ExecuteCommand(postCommandDelete);
          logger.debug(SOSVfsMessageCodes.SOSVfs_I_0113.params(tmpFileName));
        }else{
          logger.debug(SOSVfsMessageCodes.SOSVfs_D_280.getFullMessage());
        }
      }else{
        logger.debug(SOSVfsMessageCodes.SOSVfs_D_281.getFullMessage());
        stdErr = prePostCommandVFSHandler.getStdErr().toString();
        if(stdErr.length() > 0){
          logger.debug(stdErr);
        }
      }
    } catch (Exception e) {
      logger.debug(SOSVfsMessageCodes.SOSVfs_D_282.getFullMessage());
    }
    
  }

  @Override
  public ISOSVFSHandler getVFSSSH2Handler() {
    try {
      vfsHandler = VFSFactory.getHandler("SSH2.JCRAFT");
    }
    catch (Exception e) {
      throw new JobSchedulerException("SOS-VFS-E-0010: unable to initialize VFS", e);
    }
    return vfsHandler;
  }

  private void openPrePostCommandsSession(){
    try {
      if (!prePostCommandVFSHandler.isConnected()) {
        prePostCommandVFSHandler.Connect(objOptions);
      }
      ISOSAuthenticationOptions objAU = objOptions;
      ISOSConnection authenticate = prePostCommandVFSHandler.Authenticate(objAU);
      logger.debug("connection established");
    } catch (Exception e) {
      throw new SSHConnectionError("Error occured during connection/authentication: " + e.getLocalizedMessage(), e);
    }
    prePostCommandVFSHandler.setJSJobUtilites(objJSJobUtilities);
    try {
      prePostCommandVFSHandler.OpenSession(objOptions);
    } catch (Exception e1) {
      logger.error(SOSVfsMessageCodes.SOSVfs_E_283.params(e1));
    }
    finally {
      if (keepConnected == false) {
        DisConnect();
      }
    }
  }
  
  @Override
  public String getTempFileName() {
    return tempFileName;
  }

  @Override
  public StringBuffer getStdErr() throws Exception {
    return vfsHandler.getStdErr();
  }
  
  @Override
  public StringBuffer getStdOut() throws Exception {
    return vfsHandler.getStdOut();
  } 

}
