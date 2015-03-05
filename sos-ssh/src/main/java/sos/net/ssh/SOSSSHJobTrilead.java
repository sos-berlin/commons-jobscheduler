package sos.net.ssh;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "com_sos_net_messages", defaultLocale = "en")
public class SOSSSHJobTrilead extends SOSSSHJob2 {
  
  private ISOSVFSHandler vfsHandler;


  @Override
  public void generateTemporaryFilename() {
  }

  @Override
  public String getPreCommand() {
    return "";
  }

  @Override
  public void processPostCommands(String tmpReturnValueFileName) {
  }

  @Override
  public void preparePostCommandHandler() {
  }

  @Override
  public String getTempFileName() {
    return "";
  }

  @Override
  public ISOSVFSHandler getVFSSSH2Handler() {
    try {
      vfsHandler = VFSFactory.getHandler("SSH2.TRILEAD");
    }
    catch (Exception e) {
      throw new JobSchedulerException("SOS-VFS-E-0010: unable to initialize VFS", e);
    }
    return vfsHandler;
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
