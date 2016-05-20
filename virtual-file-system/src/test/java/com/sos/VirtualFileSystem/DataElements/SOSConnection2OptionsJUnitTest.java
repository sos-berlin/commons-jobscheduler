package com.sos.VirtualFileSystem.DataElements;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import com.sos.VirtualFileSystem.Options.SOSConnection2Options;

/** @author KB */
public class SOSConnection2OptionsJUnitTest extends JSToolBox {

    protected SOSConnection2Options objOptions = null;

    public SOSConnection2OptionsJUnitTest() {
        //
    }

    @Before
    public void setUp() throws Exception {
        objOptions = new SOSConnection2Options();
        JSListenerClass.bolLogDebugInformation = true;
        JSListenerClass.intMaxDebugLevel = 2;
    }

    @Test
    public void testPreCommand() {
        String strCmd = "chmod $TargetfileName 777";
        objOptions.getSource().preCommand.setValue(strCmd);
        assertEquals("chmod ", strCmd, objOptions.getSource().preCommand.getValue());
        objOptions.getTarget().preCommand.setValue(strCmd);
        assertEquals("chmod ", strCmd, objOptions.getTarget().preCommand.getValue());
    }

    @Test
    public void testhost() {
        objOptions.host.setValue("++----++");
        assertEquals("Host-Name This parameter specifies th", objOptions.host.getValue(), "++----++");
    }

    @Test
    public void testPassiveMode() {
        objOptions.passiveMode.setValue("true");
        assertTrue("passive_mode Passive mode for FTP is often used wit", objOptions.passiveMode.value());
        objOptions.passiveMode.setValue("false");
        assertFalse("passive_mode Passive mode for FTP is often used wit", objOptions.passiveMode.value());
    }

    @Test
    public void testPort() {
        objOptions.port.setValue("21");
        assertEquals("Port-Number to be used for Data-Transfer", objOptions.port.getValue(), "21");
    }

    @Test
    public void testProtocol() {
        objOptions.protocol.setValue("++ftp++");
        assertEquals("Type of requested Datatransfer The values ftp, sftp", objOptions.protocol.getValue(), "++ftp++");
    }

    @Test
    public void testTransferMode() {
        objOptions.transferMode.setValue("++binary++");
        assertEquals("Type of Character-Encoding Transfe", objOptions.transferMode.getValue(), "++binary++");
    }

    @Test
    public void testKeyWithPrefix() throws Exception {
        objOptions = new SOSConnection2Options(setJobSchedulerSSHJobOptions(new HashMap<String, String>()));
        assertEquals("host failed", "test1", objOptions.host.getValue());
        assertEquals("alternative_host failed", "test2", objOptions.getAlternatives().getHost().getValue());
        assertEquals("source_host failed", "test3", objOptions.getSource().host.getValue());
        assertEquals("target_host failed", "test5", objOptions.getTarget().host.getValue());
        assertEquals("source_alternative_host failed", "test4", objOptions.getSource().getAlternatives().host.getValue());
    }

    @Test
    public void testKeyWithAlias() throws Exception {
        objOptions = new SOSConnection2Options(setJobSchedulerSSHJobOptions(new HashMap<String, String>()));
        assertEquals("host failed", "test1", objOptions.host.getValue());
        assertEquals("alternative_host failed", "test2", objOptions.getAlternatives().host.getValue());
        assertEquals("source_host failed", "test3", objOptions.getSource().host.getValue());
        assertEquals("target_host failed", "test5", objOptions.getTarget().host.getValue());
        assertEquals("source_alternative_host failed", "test4", objOptions.getSource().getAlternatives().host.getValue());
    }

    @Test
    public void testAlternativeOptions() throws Exception {
        HashMap<String, String> objT = new HashMap<String, String>();
        objT.put("IsAnUnknownOption", "ValueOfUnknownOption");
        objT.put("host", "test1");
        objT.put("alternative_host", "test2");
        objT.put("alternative_port", "22");
        objT.put("alternative_protocol", "test2");
        objOptions = new SOSConnection2Options(objT);
        assertEquals("alternative_host failed", "test2", objOptions.getAlternatives().host.getValue());
    }

    @Test
    public void testStrictHostKeyChecking() {
        objOptions = new SOSConnection2Options();
        assertEquals("StrictHostKeyChecking", "no", objOptions.strictHostKeyChecking.getValue());
        objOptions.strictHostKeyChecking.setValue("yes");
        assertEquals("StrictHostKeyChecking", "yes", objOptions.strictHostKeyChecking.getValue());
    }

    @Test
    public void testForUnknownOptions() throws Exception {
        HashMap<String, String> objT = setJobSchedulerSSHJobOptionsAlias(new HashMap<String, String>());
        objT.put("IsAnUnknownOption", "ValueOfUnknownOption");
        objOptions = new SOSConnection2Options(objT);
        boolean flgAllOptionsProcessed = objOptions.checkNotProcessedOptions();
        assertFalse("Unknown Option found", flgAllOptionsProcessed);
    }

    private HashMap<String, String> setJobSchedulerSSHJobOptions(final HashMap<String, String> pobjHM) {
        pobjHM.put("host", "test1");
        pobjHM.put("port", "21");
        pobjHM.put("protocol", "test1");
        pobjHM.put("alternative_host", "test2");
        pobjHM.put("alternative_port", "22");
        pobjHM.put("alternative_protocol", "test2");
        pobjHM.put("source_host", "test3");
        pobjHM.put("source_port", "23");
        pobjHM.put("source_protocol", "test3");
        pobjHM.put("alternative_source_host", "test4");
        pobjHM.put("alternative_source_port", "24");
        pobjHM.put("alternative_source_protocol", "test4");
        pobjHM.put("target_host", "test5");
        pobjHM.put("target_port", "255");
        pobjHM.put("target_host", "test5");
        pobjHM.put("jump_port", "26");
        pobjHM.put("jump_protocol", "ftp");
        pobjHM.put("jump_host", "test6");
        return pobjHM;
    }

    private HashMap<String, String> setJobSchedulerSSHJobOptionsAlias(final HashMap<String, String> pobjHM) {
        pobjHM.put("ftp_host", "wilma.sos");
        pobjHM.put("ftp_port", "21");
        pobjHM.put("ftp_protocol", "test1");
        pobjHM.put("source_host", "wilma.sos");
        pobjHM.put("source_port", "23");
        pobjHM.put("source_protocol", "test3");
        pobjHM.put("alternative_source_host", "wilma.sos");
        pobjHM.put("alternative_source_port", "24");
        pobjHM.put("alternative_source_protocol", "test4");
        pobjHM.put("target_host", "wilma.sos");
        pobjHM.put("target_port", "25");
        pobjHM.put("target_host", "wilma.sos");
        pobjHM.put("jump_port", "26");
        pobjHM.put("jump_protocol", "26");
        pobjHM.put("jump_host", "wilma.sos");
        return pobjHM;
    }

}