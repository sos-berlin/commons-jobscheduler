package com.sos.VirtualFileSystem.SSH;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Vector;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.interfaces.ISOSConnectionOptions;
import com.sos.JSHelper.interfaces.ISOSDataProviderOptions;
import com.sos.VirtualFileSystem.DataElements.SOSFileList;
import com.sos.VirtualFileSystem.DataElements.SOSFolderName;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSConnection;
import com.sos.VirtualFileSystem.Interfaces.ISOSSession;
import com.sos.VirtualFileSystem.Interfaces.ISOSShell;
import com.sos.VirtualFileSystem.Interfaces.ISOSShellOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFileSystem;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFolder;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSSSH2JcraftImpl extends SOSSSH2BaseImpl implements ISOSShell, ISOSVFSHandler, ISOSVirtualFileSystem,
    ISOSConnection, ISOSSession {

  private boolean flgIsRemoteOSWindows = false;
  private ISOSConnectionOptions sosConnectionOptions = null;
  private ISOSAuthenticationOptions sosAuthenticationOptions = null;
  private ISOSShellOptions sosShellOptions = null;
  private SOSConnection2OptionsAlternate connection2OptionsAlternate = null;
  private ISOSDataProviderOptions dataProviderOptions = null;
  private Integer exitCode = null;
  private String exitSignal = null;
  private Vector<String> vecFilesToDelete = new Vector<String>();
  /** ssh session object */
  private Session sshSession = null;
  private JSch secureChannel = null;
  private ChannelExec executeChannel;
  private String host;
  private int port;
  private String command;
  private String userName;
  private String password;
  private String authMethod;

  public SOSSSH2JcraftImpl() {
    secureChannel = new JSch();
  }

  @Override
  public ISOSSession OpenSession(ISOSShellOptions pobjShellOptions) throws Exception {
    this.sosShellOptions = pobjShellOptions;
    command = sosShellOptions.getCommand().toString();
    sshSession = secureChannel.getSession(userName, host, port);
    strbStdoutOutput = new StringBuffer();
    strbStderrOutput = new StringBuffer();
    return this;
  }

  @Override
  public void CloseSession() throws Exception {
    sshSession.disconnect();
  }

  @Override
  public ISOSConnection Connect() throws Exception {
    if (sosConnectionOptions == null) {
      throw new JobSchedulerException(SOSVfs_F_102.get());
    }
    try {
      host = sosConnectionOptions.getHost().Value();
      port = sosConnectionOptions.getPort().value();
      logger.debug(SOSVfs_D_0102.params(host, port));
    } catch (Exception e) {
      throw e;
    }
    return this;
  }

  @Override
  public ISOSConnection Connect(SOSConnection2OptionsAlternate pobjConnectionOptions) throws Exception {
    this.connection2OptionsAlternate = pobjConnectionOptions;
    userName = connection2OptionsAlternate.getUser().Value();
    host = connection2OptionsAlternate.getHost().Value();
    port = connection2OptionsAlternate.getport().value();
    authMethod = connection2OptionsAlternate.getAuth_method().Value();
    sshSession = secureChannel.getSession(userName, host, port);
    sshSession.connect();
    this.Connect();
    return this;
  }

  @Override
  public ISOSConnection Connect(ISOSDataProviderOptions pobjConnectionOptions) throws Exception {
    this.dataProviderOptions = pobjConnectionOptions;
    authMethod = dataProviderOptions.getAuth_method().Value();
    host = dataProviderOptions.getHost().Value();
    port = dataProviderOptions.getport().value();
    password = dataProviderOptions.getPassword().Value();
    userName = dataProviderOptions.getUser().Value();
    sshSession = secureChannel.getSession(userName, host, port);
    sshSession.setPassword(password);
    sshSession.connect();
    this.Connect();
    return this;
  }

  @Override
  public ISOSConnection Connect(ISOSConnectionOptions connectionOptions) throws Exception {
    this.sosConnectionOptions = connectionOptions;
    if (sosConnectionOptions != null) {
      this.host = connectionOptions.getHost().Value();
      this.port = connectionOptions.getPort().value();
      if (userName != null) {
        sshSession = secureChannel.getSession(userName, host, port);
        sshSession.connect();
      }
      this.Connect();
    }
    return this;
  }

  @Override
  public ISOSConnection Connect(String hostName, int portNumber) throws Exception {
    this.host = hostName;
    this.port = portNumber;
    if (userName != null) {
      sshSession = secureChannel.getSession(userName, host, port);
      sshSession.connect();
      this.Connect();
    }
    return this;
  }

  @Override
  public ISOSConnection Authenticate(ISOSAuthenticationOptions authenticationOptions) throws Exception {
    this.sosAuthenticationOptions = authenticationOptions;
    if (secureChannel == null) {
      secureChannel = new JSch();
    }
    sshSession = secureChannel.getSession(authenticationOptions.getUser().Value(), host, port);
    if (sosAuthenticationOptions.getAuth_method() != null) {
      this.authMethod = sosAuthenticationOptions.getAuth_method().Value();
    }
    if (sosAuthenticationOptions.getAuth_method().isPublicKey()) {
      File authenticationFile = new File(sosAuthenticationOptions.getAuth_file().Value());
      if (!authenticationFile.exists()) {
        throw new JobSchedulerException(SOSVfs_E_257.params(authenticationFile.getCanonicalPath()));
      }
      if (!authenticationFile.canRead()) {
        throw new JobSchedulerException(SOSVfs_E_258.params(authenticationFile.getCanonicalPath()));
      }
      secureChannel.addIdentity(authenticationFile.getAbsolutePath());
    } else if (sosAuthenticationOptions.getAuth_method().isPassword()) {
      sshSession.setPassword(sosAuthenticationOptions.getPassword().Value().toString());
    }
    sshSession.setConfig("StrictHostKeyChecking", "no");

    try {
      sshSession.connect();
      isAuthenticated = sshSession.isConnected();
    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }
    if (!isAuthenticated) {
      throw new JobSchedulerException(SOSVfs_E_235.params(sosAuthenticationOptions.toString()));
    }
    logger.info(SOSVfs_D_133.params(sosAuthenticationOptions.getUser().Value()));
    return this;
  }

  @Override
  public void CloseConnection() throws Exception {
    sshSession.disconnect();
  }

  @Override
  public ISOSConnection getConnection() {
    return this;
  }

  @Override
  public ISOSSession getSession() {
    return this;
  }

  @Override
  public boolean remoteIsWindowsShell() {
    flgIsRemoteOSWindows = false;
    strbStdoutOutput = new StringBuffer();
    strbStderrOutput = new StringBuffer();
    ChannelExec channel = null;
    try {
      String checkShellCommand = "echo %ComSpec%";
      logger.debug(SOSVfs_D_236.get());
      logger.debug(SOSVfs_D_0151.params(checkShellCommand));
      channel = (ChannelExec) sshSession.openChannel("exec");
      channel.setCommand(checkShellCommand);
      channel.connect(3000);
      logger.debug(SOSVfs_D_163.params("stdout", checkShellCommand));
      ipsStdOut = channel.getInputStream();
      ipsStdErr = channel.getErrStream();
      String stdOut = "";
      byte[] tmp = new byte[1024];
      while (true) {
        while (ipsStdOut.available() > 0) {
          int i = ipsStdOut.read(tmp, 0, 1024);
          if (i < 0) break;
          stdOut += new String(tmp, 0, i);
        }
        strbStdoutOutput.append(stdOut);
        if (channel.isClosed()) {
          if (ipsStdOut.available() > 0) continue;
          exitCode = channel.getExitStatus();
          break;
        }
      }
      logger.debug(SOSVfs_D_163.params("stderr", checkShellCommand));
      String stdErr = "";
      tmp = new byte[1024];
      while (true) {
        while (ipsStdErr.available() > 0) {
          int i = ipsStdErr.read(tmp, 0, 1024);
          if (i < 0) break;
          stdErr += new String(tmp, 0, i);
        }
        strbStderrOutput.append(stdErr);
        if (channel.isClosed()) {
          if (ipsStdErr.available() > 0) continue;
          exitCode = channel.getExitStatus();
          break;
        }
      }
      if (stdOut.indexOf("cmd.exe") > -1) {
        logger.debug(SOSVfs_D_237.get());
        flgIsRemoteOSWindows = true;
        return true;
      }
    } catch (Exception e) {
      logger.debug(SOSVfs_D_239.params(e));
    } finally {
      if (channel != null && !channel.isClosed()) try {
        channel.disconnect();
      } catch (Exception e) {
        logger.debug(SOSVfs_D_240.params(e));
      }
    }
    return false;
  }

  @Override
  public void ExecuteCommand(String strCmd) throws Exception {
    this.command = strCmd;
    strbStdoutOutput = new StringBuffer();
    strbStderrOutput = new StringBuffer();
    if (!sshSession.isConnected()) {
      sshSession = secureChannel.getSession(sosAuthenticationOptions.getUser().Value(), sosConnectionOptions.getHost()
          .Value(), sosConnectionOptions.getPort().value());
      sshSession.setPassword(sosAuthenticationOptions.getPassword().Value());
      sshSession.connect();
    }
    executeChannel = (ChannelExec) sshSession.openChannel("exec");
    if (authMethod.equals("password")) {
      sshSession.setConfig("StrictHostKeyChecking", "no");
    }
    executeChannel.setCommand(command);
    executeChannel.setInputStream(System.in, true);
    executeChannel.setOutputStream(System.out);
    executeChannel.setErrStream(System.err, true);
    ipsStdOut = executeChannel.getInputStream();
    ipsStdErr = executeChannel.getErrStream();
    executeChannel.connect(3000);
    String output = "";
    byte[] tmp = null;
    logger.debug(SOSVfs_D_163.params("stdout", command));
    tmp = new byte[1024];
    while (true) {
      while (ipsStdOut.available() > 0) {
        int i = ipsStdOut.read(tmp, 0, 1024);
        if (i < 0) 
          break;
        output += new String(tmp, 0, i);
      }
      strbStdoutOutput.append(output);
      if (executeChannel.isClosed()) {
        if (ipsStdOut.available() > 0) 
          continue;
        exitCode = executeChannel.getExitStatus();
        break;
      }
      try {
        Thread.sleep(1000);
      }
      catch (Exception ee) {
      }
    }
    logger.debug(SOSVfs_D_163.params("stderr", command));
    tmp = new byte[1024];  
    String errorOutput = "";  
    while (true) {
      while (ipsStdErr.available() > 0) {
        int i = ipsStdErr.read(tmp, 0, 1024);
        if (i < 0) 
          break;
        errorOutput = new String(tmp, 0, i);
      }
      strbStderrOutput.append(errorOutput);
      if (executeChannel.isClosed()) {
        if (ipsStdErr.available() > 0) 
          continue;
        exitCode = executeChannel.getExitStatus();
        break;
      }
      try {
        Thread.sleep(1000);
      }
      catch (Exception ee) {
      }
    }
    // JCraft might not write to stderr in case of error
    // if exitcode != 0 AND stderr is empty, use stdout instead [SP]
    if(exitCode != 0 && errorOutput.isEmpty()){
      strbStderrOutput.append(output);
    }
  }

  @Override
  public StringBuffer getStdErr() throws Exception {
    return strbStderrOutput;
  }

  @Override
  public StringBuffer getStdOut() throws Exception {
    return strbStdoutOutput;
  }

  @Override
  public Integer getExitCode() {
    return exitCode;
  }

  @Override
  public String getExitSignal() {
    return exitSignal;
  }

  @Override
  public String createScriptFile(String pstrContent) throws Exception {
    try {
      String commandScript = pstrContent;
      logger.info("pstrContent = " + pstrContent);
      if (flgIsRemoteOSWindows == false) {
        commandScript = commandScript.replaceAll("(?m)\r", "");
      }
      logger.info(SOSVfs_I_233.params(pstrContent));
      replaceSchedulerVars(flgIsRemoteOSWindows, commandScript);
      File fleTempScriptFile = File.createTempFile("sos-sshscript", getScriptFileNameSuffix());
      BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fleTempScriptFile)));
      out.write(commandScript);
      out.flush();
      out.close();
      fleTempScriptFile.deleteOnExit();
      putFile(fleTempScriptFile);
      String strFileName2Return = fleTempScriptFile.getName();
      if (flgIsRemoteOSWindows == false) {
        strFileName2Return = "./" + strFileName2Return;
      }
      add2Files2Delete(strFileName2Return);
      logger.info(SOSVfs_I_253.params(fleTempScriptFile.getAbsolutePath()));
      return strFileName2Return;
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  public void putFile(File pfleCommandFile) throws Exception {
    
    String strFileName = pfleCommandFile.getName();
    try {
      if(!sshSession.isConnected()){
        if(secureChannel == null){
          secureChannel = new JSch();
        }
        sshSession = secureChannel.getSession(sosAuthenticationOptions.getUser().Value(), 
            sosConnectionOptions.getHost().Value(), 
            sosConnectionOptions.getPort().value());        
        sshSession.setPassword(sosAuthenticationOptions.getPassword().Value());
        sshSession.connect();
      }
      ChannelSftp channel = (ChannelSftp)sshSession.openChannel("sftp");
      channel.connect();
      ChannelSftp sftp = (ChannelSftp) channel;
      sftp.put(pfleCommandFile.getCanonicalPath(), strFileName);
      sftp.chmod(new Integer(0700), strFileName);
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  private void add2Files2Delete(final String pstrFilenName2Delete) {
    this.getFilesToDelete().add(pstrFilenName2Delete);
    logger.debug(String.format(SOSVfs_D_254.params(pstrFilenName2Delete)));
  }

  private Vector<String> getFilesToDelete() {
    if (vecFilesToDelete == null) {
      vecFilesToDelete = new Vector<String>();
    }
    return vecFilesToDelete;
  }

  private String getScriptFileNameSuffix() {
    String strSuffix = flgIsRemoteOSWindows ? ".cmd" : ".sh";
    return strSuffix;
  }

  @Override
  public ISOSVirtualFolder mkdir(SOSFolderName pobjFolderName) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean rmdir(SOSFolderName pobjFolderName) throws IOException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public SOSFileList dir(SOSFolderName pobjFolderName) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SOSFileList dir(String pathname, int flag) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void doPostLoginOperations() {
    // TODO Auto-generated method stub
  }

  @Override
  public ISOSVFSHandler getHandler() {
    return this;
  }

  @Override
  public void setJSJobUtilites(JSJobUtilities pobjJSJobUtilities) {
    // TODO Auto-generated method stub
  }

}
