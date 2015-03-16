package sos.net.ssh;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import sos.net.ssh.exceptions.SSHExecutionError;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.JSHelper.Listener.JSListenerClass;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "com.sos.net.messages", defaultLocale = "en")
public class TestSOSSSHJob2WithJSch extends JSJobUtilitiesClass<SOSSSHJobOptions> {
	private static final Logger	logger			= Logger.getLogger(TestSOSSSHJob2WithJSch.class);

	private static SOSSSHJob2 objSSH = null;
	private static SOSSSHJobOptions objOptions = null;
	
	public TestSOSSSHJob2WithJSch() {
		super(new SOSSSHJobOptions());
    initializeClazz();
	}

	public static void initializeClazz () {
    objSSH = new SOSSSHJobJSch();
    objOptions = objSSH.Options();
		JSListenerClass.bolLogDebugInformation = true;
		JSListenerClass.intMaxDebugLevel = 9;
    if( !Logger.getRootLogger().getAllAppenders().hasMoreElements() ) {
      BasicConfigurator.configure();
    }
		logger.setLevel(Level.DEBUG);
	}

	@Before
	public void beforeMethode(){
    objSSH.setJSJobUtilites(this);
	}
	
  @Test
  public void testExecuteLinux() throws Exception {
    logger.info("****testExecuteLinux started****");
    String strArgs[] = new String[] { 
        "-command", "echo ****testExecuteLinux successfully processed!****", 
        "-auth_method", "password", 
        "-host", "homer.sos", 
        "-user", "test", 
        "-password", "12345",
        "-strict_hostkey_checking", "no",
        "-ignore_stderr", "false",
        "-ignore_error", "true",
        "-raise_exception_on_error", "true",
        "-command_delimiter", ";" };
    objOptions.CommandLineArgs(strArgs);
    objSSH.Execute();
    assertTrue(objSSH.getStdErr().toString().isEmpty());
    objSSH.Clear();
  }

  @Test
  public void testExecuteOverProxyLinux() throws Exception {
    logger.info("****testExecuteLinux started****");
    String strArgs[] = new String[] { 
        "-command", "echo ****testExecuteLinux successfully processed!****", 
        "-auth_method", "password", 
        "-host", "wilma.sos", 
        "-user", "test", 
        "-password", "12345",
        "-proxy_host", "homer.sos", 
        "-proxy_port", "3128", 
        "-proxy_user", "proxy_user", 
        "-proxy_password", "12345",
        "-command_delimiter", ";" };
    objOptions.CommandLineArgs(strArgs);
    objSSH.Execute();
    assertTrue(objSSH.getStdErr().toString().isEmpty());
    objSSH.Clear();
  }

  @Test
  public void testExecuteGetShellPidLinux() throws Exception {
    logger.info("****testExecuteGetShellPidLinux started****");
    String strArgs[] = new String[] { 
        "-command", "echo $$; echo $$", 
        "-auth_method", "password", 
        "-host", "homer.sos", 
        "-user", "test", 
        "-password", "12345",
        "-command_delimiter", ";" };
    objOptions.CommandLineArgs(strArgs);
    objSSH.Execute();
    assertTrue(objSSH.getStdErr().toString().isEmpty());
    objSSH.Clear();
  }

  @Test(expected=SSHExecutionError.class)
  public void testExecuteWithErrors() throws Exception {
    logger.info("****testExecuteWithErrors started****");
    String strArgs[] = new String[] { 
        "-command", "ls unknownPath", 
        "-auth_method", "password", 
        "-host", "homer.sos", 
        "-user", "test", 
        "-password", "12345",
        "-command_delimiter", ";"  };
    objOptions.CommandLineArgs(strArgs);
    objSSH.Execute();
    assertFalse(objSSH.getStdErr().toString().isEmpty());
    objSSH.Clear();
  }

  @Test
  public void testExecuteCmdScriptFileOnLinux() throws Exception {
    logger.info("****testExecuteCmdScriptFileOnLinux started****");
    String strArgs[] = new String[] { 
        "-command_script_file", "src/test/resources/test.sh", 
        "-auth_method", "password", 
        "-host", "homer.sos", 
        "-user", "test", 
        "-password", "12345",
        "-command_delimiter", ";" };
    objOptions.CommandLineArgs(strArgs);
    objSSH.Execute();
    assertTrue(objSSH.getStdErr().toString().isEmpty());
    objSSH.Clear();
  }
  
  @Test
  public void testExecuteUsingKeyFile() throws Exception {
    logger.info("****testExecuteUsingKeyFile started****");
    String strArgs[] = new String[] { 
        "-command", "echo ****testExecuteUsingKeyFile successfully processed!****", 
        "-auth_method", "publickey", 
        "-host", "homer.sos", 
        "-auth_file", "src/test/resources/id_rsa", 
        "-user", "test",
        "-command_delimiter", ";" };
    objOptions.CommandLineArgs(strArgs);
    objSSH.Execute();
    assertTrue(objSSH.getStdErr().toString().isEmpty());
    objSSH.Clear();
  }

  @Test
  public void testExecuteWithMoreReturnValuesOnLinux() throws Exception {
    logger.info("****testExecuteWithMoreReturnValuesOnLinux started****");
    String strArgs[] = new String[] { 
        "-command", 
            "echo MY_PARAM=myParam >> $SCHEDULER_RETURN_VALUES; "
            + "echo MY_OTHER_PARAM=myOtherParam >> $SCHEDULER_RETURN_VALUES; "
            + "echo =myParamWithoutKey >> $SCHEDULER_RETURN_VALUES", 
        "-auth_method", "password", 
        "-host", "homer.sos", 
        "-user", "test", 
        "-password", "12345",
        "-command_delimiter", ";"};
    objOptions.CommandLineArgs(strArgs);
    objSSH.Execute();
    assertTrue(objSSH.getStdErr().toString().isEmpty());
    objSSH.Clear();
  }

  @Test
  public void testExecuteWithReturnValues() throws Exception {
    logger.info("****testExecuteWithReturnValues started****");
    String strArgs[] = new String[] { 
        "-command", "echo MY_PARAM=myParam >> $SCHEDULER_RETURN_VALUES", 
        "-auth_method", "password", 
        "-host", "homer.sos", 
        "-user", "test", 
        "-password", "12345",
        "-command_delimiter", ";" };
    objOptions.CommandLineArgs(strArgs);
    objSSH.Execute();
    assertTrue(objSSH.getStdErr().toString().isEmpty());
    objSSH.Clear();
  }

  @Test
  public void testExecuteRemoteScriptWithReturnValues() throws Exception {
    logger.info("****testExecuteRemoteScriptWithReturnValues started****");
    String strArgs[] = new String[] { 
        "-command", "./myCommandSp.sh", 
        "-auth_method", "password", 
        "-host", "homer.sos", 
        "-user", "test", 
        "-password", "12345",
        "-command_delimiter", ";" };
    objOptions.CommandLineArgs(strArgs);
    objSSH.Execute();
    assertTrue(objSSH.getStdErr().toString().isEmpty());
    objSSH.Clear();
  }

  @Test
  public void testExecuteWindows() throws Exception {
    logger.info("****testExecute on Windows started****");
    String strArgs[] = new String[] { 
        "-command", "echo ****testExecuteWindows successfully processed!****", 
        "-auth_method", "password", 
        "-host", "lutest.sos", 
        "-user", "test", 
        "-password", "12345",
        "-command_delimiter", ";" };
    objOptions.CommandLineArgs(strArgs);
    objSSH.Execute();
    assertTrue(objSSH.getStdErr().toString().isEmpty());
    objSSH.Clear();
  }

  @Test
  public void testExecuteWithMoreReturnValuesOnWindows() throws Exception {
    logger.info("****testExecuteWithMoreReturnValuesOnWindows started****");
    String strArgs[] = new String[] { 
        "-command", 
            "echo MY_PARAM=myParam >> $SCHEDULER_RETURN_VALUES;"
            + "echo MY_OTHER_PARAM=myOtherParam >> $SCHEDULER_RETURN_VALUES;"
            + "echo =myParamWithoutKey >> $SCHEDULER_RETURN_VALUES", 
        "-auth_method", "password", 
        "-host", "lutest.sos", 
        "-user", "test", 
        "-password", "12345",
        "-command_delimiter", ";"};
    objOptions.CommandLineArgs(strArgs);
    objSSH.Execute();
    assertTrue(objSSH.getStdErr().toString().isEmpty());
    objSSH.Clear();
  }

  @Test
  public void testExecuteRemoteScriptWithReturnValuesOnWindows() throws Exception {
    logger.info("****testExecuteRemoteScriptWithReturnValuesOnWindows started****");
    String strArgs[] = new String[] { 
        "-command", "./test.sh", 
        "-auth_method", "password", 
        "-host", "lutest.sos", 
        "-user", "test", 
        "-password", "12345",
        "-command_delimiter", ";"};
    objOptions.CommandLineArgs(strArgs);
    objSSH.Execute();
    assertTrue(objSSH.getStdErr().toString().isEmpty());
    objSSH.Clear();
  }

  @Test
  public void testExecuteCmdScriptFileOnWindowsCygwin() throws Exception {
    logger.info("****testExecuteCmdScriptFileOnWindows with OpenSSH via cygwin (CopSSH) started****");
    String strArgs[] = new String[] { 
        "-command_script_file", "src/test/resources/test.sh", 
        "-auth_method", "password", 
        "-host", "lutest.sos", 
        "-user", "test", 
        "-password", "12345",
        "-command_delimiter", ";" };
    objOptions.CommandLineArgs(strArgs);
    objSSH.Execute();
    assertTrue(objSSH.getStdErr().toString().isEmpty());
    objSSH.Clear();
  }
  
  @Test
  @Ignore("Tests works but remote machine isn´t always available as this test was meant for local testing only")
  public void testExecuteCmdScriptFileOnWindowsBitvise() throws Exception {
    logger.info("****testExecuteCmdScriptFileOnWindows with bitvise SSH Server started****");
    String strArgs[] = new String[] { 
        "-command_script_file", "src/test/resources/test.cmd", 
        "-auth_method", "password", 
        "-host", "sp.sos", 
        "-user", "test", 
        "-password", "12345",
        "-preCommand", "set %1s=%2s",
        "-postCommandRead", "type %s",
        "-postCommandDelete", "del %s",
        "-command_delimiter", "&" };
    objOptions.CommandLineArgs(strArgs);
    objSSH.Execute();
    assertTrue(objSSH.getStdErr().toString().isEmpty());
    objSSH.Clear();
  }
  
  @Test
  @Ignore("Tests works but remote machine isn´t always available as this test was meant for local testing only")
  public void testExecuteCommandOnWindowsBitvise() throws Exception {
    logger.info("****testExecuteCommandOnWindows with bitvise SSH Server started****");
    String strArgs[] = new String[] { 
        "-command", "echo ADD='Hallo Welt!' >> %SCHEDULER_RETURN_VALUES%", 
        "-auth_method", "password", 
        "-host", "sp.sos", 
        "-user", "test", 
        "-password", "12345",
        "-preCommand", "set %1s=%2s",
        "-postCommandRead", "type %s",
        "-postCommandDelete", "del %s",
        "-command_delimiter", "&" };
    objOptions.CommandLineArgs(strArgs);
    objSSH.Execute();
    assertTrue(objSSH.getStdErr().toString().isEmpty());
    objSSH.Clear();
  }

}
