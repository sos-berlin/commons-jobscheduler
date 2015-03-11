package sos.net.ssh;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.UUID;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import sos.net.ssh.exceptions.SSHConnectionError;
import sos.net.ssh.exceptions.SSHExecutionError;
import sos.net.ssh.exceptions.SSHMissingCommandError;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSConnection;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.SFTP.SOSVfsSFtpJCraft;
import com.sos.VirtualFileSystem.SSH.SOSSSH2TriLeadImpl;
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

    if(tempFilesToDelete != null && !tempFilesToDelete.isEmpty()){
      for(String tempFileName : tempFilesToDelete){
        ((SOSVfsSFtpJCraft)vfsHandler).delete(tempFileName);
        logger.debug(SOSVfsMessageCodes.SOSVfs_I_0113.params(tempFileName));
      }
    }
    tempFilesToDelete = null;

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
        SOSConnection2OptionsAlternate postAlternateOptions = getAlternateOptions(objOptions);
        postAlternateOptions.raise_exception_on_error.value(false);
        prePostCommandVFSHandler.Connect(postAlternateOptions);
      }
      prePostCommandVFSHandler.Authenticate(objOptions);
      logger.debug("connection established");
    } catch (Exception e) {
      throw new SSHConnectionError("Error occured during connection/authentication: " + e.getLocalizedMessage(), e);
    }
    prePostCommandVFSHandler.setJSJobUtilites(objJSJobUtilities);
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

  @Override
  public SOSSSHJob2 Execute() throws Exception {
    boolean flgScriptFileCreated = false; // http://www.sos-berlin.com/jira/browse/JITL-17
    vfsHandler.setJSJobUtilites(objJSJobUtilities);

    try {
      if (isConnected == false) {
        this.Connect();
      }

      if (objOptions.command.IsEmpty() == false) {
        strCommands2Execute = objOptions.command.values();
      } else {
        if (objOptions.isScript() == true) {
          strCommands2Execute = new String[1];
          String strTemp = objOptions.command_script.Value();
          if (objOptions.command_script.IsEmpty()) {
            strTemp = objOptions.command_script_file.JSFile().File2String();
          }
          strTemp = objJSJobUtilities.replaceSchedulerVars(flgIsWindowsShell, strTemp);
          strCommands2Execute[0] = vfsHandler.createScriptFile(strTemp);
          // http://www.sos-berlin.com/jira/browse/JITL-123
          if(!(vfsHandler instanceof SOSSSH2TriLeadImpl)){
            add2Files2Delete(strCommands2Execute[0]);
          }
          flgScriptFileCreated = true; // http://www.sos-berlin.com/jira/browse/JITL-17
          strCommands2Execute[0] += " " + objOptions.command_script_param.Value();
        } else {
          throw new SSHMissingCommandError(objMsg.getMsg(SOS_SSH_E_100)); // "SOS-SSH-E-100: neither Commands nor Script(file) specified. Abort.");
        }
      }
       
      for (String strCmd : strCommands2Execute) {
        try {
          // http://www.sos-berlin.com/jira/browse/JITL-112
          strCmd = getPreCommand() + strCmd;
          /**
           * \change Substitution of variables enabled
           *
           * see http://www.sos-berlin.com/jira/browse/JS-673
           *
           */
          logger.debug(String.format(objMsg.getMsg(SOS_SSH_D_110), strCmd));
          strCmd = objJSJobUtilities.replaceSchedulerVars(flgIsWindowsShell, strCmd);
          logger.debug(String.format(objMsg.getMsg(SOS_SSH_D_110), strCmd));
          vfsHandler.ExecuteCommand(strCmd);
          objJSJobUtilities.setJSParam(conExit_code, "0");
          CheckStdOut();
          CheckStdErr();
          CheckExitCode();
          ChangeExitSignal();
        } catch (Exception e) {
          logger.error(this.StackTrace2String(e));
          throw new SSHExecutionError("Exception raised: " + e, e);
        } finally {
          if (flgScriptFileCreated == true) { 
            // http://www.sos-berlin.com/jira/browse/JITL-17
            // file will be deleted by the Vfs Component.
          }
        }
      }
      // http://www.sos-berlin.com/jira/browse/JITL-112
      processPostCommands(getTempFileName());
    }
    catch (Exception e) {
      logger.error(this.StackTrace2String(e));
      String strErrMsg = "SOS-SSH-E-120: error occurred processing ssh command: ";
      logger.error(strErrMsg, e);
      throw new SSHExecutionError(strErrMsg, e);
    }
    finally {
      if (keepConnected == false) {
        DisConnect();
      }
    }
    return this;
  }

  @Override
  public void DisConnect() {
    if (isConnected == true) {
      try {
        vfsHandler.CloseConnection();
      } catch (Exception e) {
        throw new SSHConnectionError("problems closing connection", e);
      }
      isConnected = false;
    }
  }
  
  // http://www.sos-berlin.com/jira/browse/JITL-123
  private void add2Files2Delete(final String fileNameToDelete) {
    if (tempFilesToDelete == null){
      tempFilesToDelete = new Vector<String>();
    }
    tempFilesToDelete.add(fileNameToDelete);
    logger.debug(String.format(SOSVfsMessageCodes.SOSVfs_D_254.params(fileNameToDelete)));
  }

  @SuppressWarnings("deprecation")
  public SOSSSHJob2 Connect() {
    getVFS();
    Options().CheckMandatory();

    try {
      SOSConnection2OptionsAlternate alternateOptions = getAlternateOptions(objOptions);
      vfsHandler.Connect(alternateOptions);
      vfsHandler.Authenticate(objOptions);
      logger.debug("connection established");
    }
    catch (Exception e) {
      throw new SSHConnectionError("Error occured during connection/authentication: " + e.getLocalizedMessage(), e);
    }

    flgIsWindowsShell = vfsHandler.remoteIsWindowsShell();
    isConnected = true;
    
    // http://www.sos-berlin.com/jira/browse/JITL-112: 
    //   preparePostCommandHandler() has to be called once to generate a 
    //   second instance for post processing of stored return values
    preparePostCommandHandler();
    return this;
  } // private SOSSSHJob2 Connect
  
  private SOSConnection2OptionsAlternate getAlternateOptions(SOSSSHJobOptions options){
    SOSConnection2OptionsAlternate alternateOptions = new SOSConnection2OptionsAlternate();
    alternateOptions.setStrict_HostKey_Checking("no");
    alternateOptions.host.Value(options.getHost().Value());
    alternateOptions.port.value(options.getPort().value());
    alternateOptions.user.Value(options.getUser().Value());
    alternateOptions.proxy_host.Value(options.getProxy_host().Value());
    alternateOptions.proxy_port.value(options.getProxy_port().value());
    alternateOptions.proxy_user.Value(options.getProxy_user().Value());
    alternateOptions.proxy_password.Value(options.getProxy_password().Value());
    alternateOptions.raise_exception_on_error.value(options.getraise_exception_on_error().value());
    return alternateOptions;
  }
}
