package sos.net.ssh;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

import sos.net.ssh.exceptions.SSHExecutionError;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.JSHelper.Listener.JSListenerClass;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "com.sos.net.messages", defaultLocale = "en")
public class SOSSSHJob2TestWithJcraft extends JSJobUtilitiesClass<SOSSSHJobOptions> {
	private static final Logger	logger			= Logger.getLogger(SOSSSHJob2TestWithJcraft.class);

	private SOSSSHJob2 objSSH = null;
	private SOSSSHJobOptions objOptions = null;
	
	public SOSSSHJob2TestWithJcraft() {
		super(new SOSSSHJobOptions());
    initializeClazz();
	}

	private void initializeClazz () {
		objSSH = new SOSSSHJob2();
		objOptions = objSSH.Options();
		objSSH.setJSJobUtilites(this);
		JSListenerClass.bolLogDebugInformation = true;
		JSListenerClass.intMaxDebugLevel = 9;
		BasicConfigurator.configure();
		logger.setLevel(Level.DEBUG);
	}

  @Test
  public void testExecute() throws Exception {
    String strArgs[] = new String[] { 
        "-command", "ls", 
        "-auth_method", "password", 
        "-host", "homer.sos", 
        "-user", "test", 
        "-password", "12345" };
    objOptions.CommandLineArgs(strArgs);
    objSSH.Execute();
    assertTrue(objSSH.getStdErr().toString().isEmpty());
    assertFalse(objSSH.getStdOut().toString().isEmpty());
    objSSH.Clear();
  }

  @Test(expected=SSHExecutionError.class)
  public void testExecuteWithErrors() throws Exception {
    String strArgs[] = new String[] { 
        "-command", "ls unknownPath", 
        "-auth_method", "password", 
        "-host", "homer.sos", 
        "-user", "test", 
        "-password", "12345" };
    objOptions.CommandLineArgs(strArgs);
    objSSH.Execute();
    assertFalse(objSSH.getStdErr().toString().isEmpty());
    objSSH.Clear();
  }

  @Test
  public void testExecuteCmdScriptFile() throws Exception {
    String strArgs[] = new String[] { 
        "-command_script_file", "src/test/resources/testScriptSSH2Job.sh", 
        "-auth_method", "password", 
        "-host", "homer.sos", 
        "-user", "test", 
        "-password", "12345" };
    objOptions.CommandLineArgs(strArgs);
    objSSH.Execute();
    assertTrue(objSSH.getStdErr().toString().isEmpty());
    assertFalse(objSSH.getStdOut().toString().isEmpty());
    objSSH.Clear();
  }
  
  @Test
  public void testExecuteUsingKeyFile() throws Exception {
    String strArgs[] = new String[] { 
        "-command", "ls", 
        "-auth_method", "publickey", 
        "-host", "homer.sos", 
        "-auth_file", "src/test/resources/id_rsa", 
        "-user", "test"};
    objOptions.CommandLineArgs(strArgs);
    objSSH.Execute();
    assertTrue(objSSH.getStdErr().toString().isEmpty());
    assertFalse(objSSH.getStdOut().toString().isEmpty());
    objSSH.Clear();
  }
}
