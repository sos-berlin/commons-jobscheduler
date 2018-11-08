package sos.net.ssh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.CredentialStore.Options.SOSCredentialStoreOptions;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.io.Files.JSXMLFile;

/** @author KB */
public class SOSSSHJobOptionsTest {

    private static final Logger LOGGER = Logger.getLogger(SOSSSHJobOptionsTest.class);
    private static final String SOS_USER = "sos-user";
    private static final String USER = "user";
    private static final String AUTH_FILE = "auth_file";
    private static final String CLASSNAME = "SOSSSHJobOptionsTest";
    private static final String KEE_PASS_DB_FILE_NAME = "src/test/resources/keepassX-test.kdb";


    @Test
    public void TestToXml() throws Exception {
        String strParameterName = USER;
        String strParameterValue = "JunitTestUser";
        String strCmdLineArgs[] = { "-" + strParameterName, strParameterValue };
        SOSSSHJobOptions objOptions = new SOSSSHJobOptions();
        objOptions.commandLineArgs(strCmdLineArgs);
        assertEquals(strParameterName, strParameterValue, objOptions.user.getValue());
        String strTempFileName = "src/test/resources/" + CLASSNAME + ".xml";
        JSXMLFile objXF = objOptions.toXMLFile(strTempFileName);
        SOSSSHJobOptions objO2 = new SOSSSHJobOptions();
        objO2.loadXML(objXF);
        assertEquals(strParameterName, strParameterValue, objO2.user.getValue());
    }

    @Test
    public void testCommand_Script() {
        SOSSSHJobOptions objOptions = new SOSSSHJobOptions();
        objOptions.commandScript.getValue();
        objOptions.command.getValue();
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testSerialize() throws Exception {
        String strParameterName = USER;
        String strCmdLineArgs[] = { "-" + strParameterName, "JunitTestUser" };
        SOSSSHJobOptions objOptions = new SOSSSHJobOptions();
        objOptions.commandLineArgs(strCmdLineArgs);
        assertEquals(strParameterName, "JunitTestUser", objOptions.user.getValue());
        String strSerializedFileName = "c:/temp/test.object";
        objOptions.putObject(strSerializedFileName);
        System.setProperty(strParameterName, "sos-user2");
        objOptions.loadSystemProperties();
        assertEquals(strParameterName, "sos-user2", objOptions.user.getValue());
        SOSSSHJobOptions objO2 = new SOSSSHJobOptions();
        objO2 = (SOSSSHJobOptions) JSOptionsClass.getObject(strSerializedFileName);
        assertEquals(strParameterName, "JunitTestUser", objO2.user.getValue());
    }

    @Test
    public void testSystemProperties() throws Exception {
        String strCmdLineArgs[] = { "-user", "JunitTestUser" };
        SOSSSHJobOptions objOptions = new SOSSSHJobOptions();
        System.setProperty("SOSSSHJobOptions.user", SOS_USER);
        objOptions.loadSystemProperties();
        assertEquals(USER, System.getProperty("SOSSSHJobOptions.user"), objOptions.user.getValue());
        System.setProperty("SOSSSHJobOptions.user", "");
        objOptions.setAllOptions(new HashMap<String, String>());
        System.setProperty(USER, SOS_USER);
        objOptions.loadSystemProperties();
        assertEquals(USER, SOS_USER, objOptions.user.getValue());
        System.setProperty("SOSSSHJobOptions.user", "");
        objOptions.commandLineArgs(strCmdLineArgs);
        assertEquals(USER, "JunitTestUser", objOptions.user.getValue());
        System.setProperty(USER, "sos-user2");
        objOptions.loadSystemProperties();
        assertEquals(USER, "sos-user2", objOptions.user.getValue());
    }

    @Test
    public void setHashMap() throws Exception {
        SOSSSHJobOptions objOptions = new SOSSSHJobOptions();
        objOptions.setAllOptions(this.setJobSchedulerSSHJobOptions(new HashMap<String, String>()));
        assertEquals(AUTH_FILE, "test", objOptions.authFile.getValue());
        assertEquals(USER, "test", objOptions.user.getValue());
        objOptions.setCurrentNodeName("step1");
        objOptions.setAllOptions(this.setJobSchedulerSSHJobOptions(new HashMap<String, String>()));
        assertEquals(USER, "step1user", objOptions.user.getValue());
        objOptions.setCurrentNodeName("step2");
        objOptions.setAllOptions(this.setJobSchedulerSSHJobOptions(new HashMap<String, String>()));
        assertEquals(USER, "userofstep2", objOptions.user.getValue());
        LOGGER.info(objOptions.toString());
    }

    @Test
    public void setCmdArgs() throws Exception {
        SOSSSHJobOptions objOptions = new SOSSSHJobOptions();
        String strArgs[] = new String[] { "-command", "ls", "-auth_method", "password", "-host", "8of9.sos", "-auth_file", "test", "-user", "kb",
                "-password", "huhu" };
        objOptions.commandLineArgs(strArgs);
        objOptions.checkMandatory();
        assertEquals(AUTH_FILE, objOptions.authFile.getValue(), "test");
        assertEquals(USER, objOptions.user.getValue(), "kb");
        objOptions.commandLineArgs(new String[] { "-user", "testtest" });
        assertEquals(USER, "testtest", objOptions.user.getValue());
        LOGGER.info(objOptions.toString());
    }

    @Test
    public void setCmdArgs2() throws Exception {
        SOSSSHJobOptions objOptions = new SOSSSHJobOptions();
        String strArgs[] = new String[] { "-command=ls", "-auth_method=password", "-host=8of9.sos", "-AuthFile=test", "-user=kb", "-password=huhu" };
        objOptions.commandLineArgs(strArgs);
        objOptions.checkMandatory();
        assertEquals(AUTH_FILE, objOptions.authFile.getValue(), "test");
        assertEquals(USER, objOptions.user.getValue(), "kb");
        LOGGER.info(objOptions.toString());
    }

    @Test
    public void setCmdArgsString() throws Exception {
        SOSSSHJobOptions objOptions = new SOSSSHJobOptions();
        String strArgs = new String("-command=ls -auth_method=password -host=8of9.sos -AuthFile=test -user=kb -password=huhu");
        objOptions.commandLineArgs(strArgs);
        objOptions.checkMandatory();
        assertEquals(AUTH_FILE, objOptions.authFile.getValue(), "test");
        assertEquals(USER, objOptions.user.getValue(), "kb");
        LOGGER.info(objOptions.toString());
    }

    @Test(expected = com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing.class)
    public void setEmptyPassw() throws Exception {
        SOSSSHJobOptions objOptions = new SOSSSHJobOptions();
        String strArgs[] = new String[] { "-auth_method=password", "-host=ftphost", "-auth_file=test", "-user=kb", "-password=" };
        objOptions.commandLineArgs(strArgs);
        objOptions.checkMandatory();
        assertEquals(AUTH_FILE, objOptions.authFile.getValue(), "test");
        assertEquals(USER, objOptions.user.getValue(), "kb");
        LOGGER.info(objOptions.toString());
    }

    private HashMap<String, String> setJobSchedulerSSHJobOptions(final HashMap<String, String> pobjHM) {
        pobjHM.put("step1/user", "step1user");
        pobjHM.put("step2/user", "userofstep2");
        pobjHM.put("SOSSSHJobOptions.authfile", "test");
        pobjHM.put(AUTH_FILE, "test");
        pobjHM.put("SOSSSHJobOptions.auth_file", "test");
        pobjHM.put("SOSSSHJobOptions.auth_method", "password");
        pobjHM.put("SOSSSHJobOptions.command", "test");
        pobjHM.put("SOSSSHJobOptions.command_delimiter", "%%");
        pobjHM.put("SOSSSHJobOptions.command_script", "test");
        pobjHM.put("SOSSSHJobOptions.command_script_file", "test");
        pobjHM.put("SOSSSHJobOptions.command_script_param", "test");
        pobjHM.put("SOSSSHJobOptions.host", "wilma.sos");
        pobjHM.put("SOSSSHJobOptions.ignore_error", "false");
        pobjHM.put("SOSSSHJobOptions.ignore_exit_code", "12,33-47");
        pobjHM.put("SOSSSHJobOptions.ignore_signal", "false");
        pobjHM.put("SOSSSHJobOptions.ignore_stderr", "false");
        pobjHM.put("SOSSSHJobOptions.port", "22");
        pobjHM.put("SOSSSHJobOptions.proxy_host", "test");
        pobjHM.put("SOSSSHJobOptions.proxy_password", "test");
        pobjHM.put("SOSSSHJobOptions.proxy_port", "22");
        pobjHM.put("SOSSSHJobOptions.proxy_user", "test");
        pobjHM.put("SOSSSHJobOptions.simulate_shell", "false");
        pobjHM.put("SOSSSHJobOptions.simulate_shell_inactivity_timeout", "22");
        pobjHM.put("SOSSSHJobOptions.simulate_shell_login_timeout", "22");
        pobjHM.put("SOSSSHJobOptions.simulate_shell_prompt_trigger", "test");
        pobjHM.put("SOSSSHJobOptions.user", "test");
        pobjHM.put("SOSSSHJobOptions.user", "test");
        pobjHM.put(USER, "test");
        pobjHM.put("step1/SOSSSHJobOptions.user", "step1user");
        pobjHM.put("step2/SOSSSHJobOptions.user", "userofstep2");
        pobjHM.put("UseCredentialStore", "true"); 
        pobjHM.put("CredentialStoreFileName", KEE_PASS_DB_FILE_NAME);
        pobjHM.put("CredentialStorePassword", "testing");
        pobjHM.put("CredentialStoreKeyPath", "sos/server/wilma.sos");
        return pobjHM;
    }

    private HashMap<String, String> setSSHJobOptionsUsingCredentialStore(final HashMap<String, String> pobjHM) {
        pobjHM.put("SOSSSHJobOptions.command_delimiter", "%%");
        pobjHM.put("SOSSSHJobOptions.command_script", "test");
        pobjHM.put("SOSSSHJobOptions.command_script_file", "test");
        pobjHM.put("SOSSSHJobOptions.command_script_param", "test");
        pobjHM.put("SOSSSHJobOptions.ignore_error", "false");
        pobjHM.put("SOSSSHJobOptions.ignore_exit_code", "12,33-47");
        pobjHM.put("SOSSSHJobOptions.ignore_signal", "false");
        pobjHM.put("SOSSSHJobOptions.ignore_stderr", "false");
        pobjHM.put("UseCredentialStore", "true");
        pobjHM.put("CredentialStore_FileName", KEE_PASS_DB_FILE_NAME);
        pobjHM.put("CredentialStore_Password", "testing");
        pobjHM.put("CredentialStore_KeyPath", "/sos/server/wilma.sos");
        pobjHM.put("CredentialStore_ProcessNotesParams", "true");
        return pobjHM;
    }

    @Test(expected = java.lang.RuntimeException.class)
    public void testCredentialStore1() {
        SOSSSHJobOptions objOptions = new SOSSSHJobOptions();
        SOSCredentialStoreOptions objCSO = objOptions.getCredentialStore().getOptions();
        assertTrue("not null", objCSO != null);
        objCSO.useCredentialStore.setTrue();
        objCSO.credentialStoreFileName.setValue("c:/temp/t.1");
        objOptions.getCredentialStore().checkCredentialStoreOptions();
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testCredentialStore2() {
        SOSSSHJobOptions objOptions = new SOSSSHJobOptions();
        SOSCredentialStoreOptions objCSO = objOptions.getCredentialStore().getOptions();
        assertTrue("not null", objCSO != null);
        objCSO.useCredentialStore.setTrue();
        objCSO.credentialStoreFileName.setValue(KEE_PASS_DB_FILE_NAME);
        objCSO.credentialStorePassword.setValue("testing");
        objCSO.credentialStoreKeyPath.setValue("/sos/server/wilma.sos");
        objOptions.getCredentialStore().checkCredentialStoreOptions();
        assertEquals("userid", "test", objOptions.user.getValue());
        assertEquals("password", "12345", objOptions.password.getValue());
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testCredentialStore3() throws Exception {
        SOSSSHJobOptions objOptions = new SOSSSHJobOptions(setSSHJobOptionsUsingCredentialStore(new HashMap<String, String>()));
        assertTrue("not null", objOptions != null);
        assertEquals("userid", "test", objOptions.user.getValue());
        assertEquals("password", "12345", objOptions.password.getValue());
        assertEquals("command", "test.bsh", objOptions.command.getValue());
    }

}