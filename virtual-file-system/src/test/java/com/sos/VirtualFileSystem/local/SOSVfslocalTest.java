package com.sos.VirtualFileSystem.local;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Options.SOSOptionTransferType.enuTransferTypes;
import com.sos.JSHelper.io.Files.JSFile;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;

/** @author KB */
public class SOSVfslocalTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVfslocalTest.class);
    private ISOSVFSHandler objVFS = null;
    private ISOSVfsFileTransfer objFileSystemHandler = null;
    private final String strTestFileName = "text.txt";
    private final String strTestPathName = "R:\\nobackup\\junittests\\testdata\\LOCAL\\";
    private String strTestFilePath = "";

    public SOSVfslocalTest() {
        //
    }

    @Before
    public void setUp() throws Exception {
        objVFS = VFSFactory.getHandler(enuTransferTypes.local);
        objFileSystemHandler = (ISOSVfsFileTransfer) objVFS;
    }

    @Test
    public void testConnect() throws Exception {
        objVFS.connect();
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testAppendFile() {
        createTestFile();
        long lngFileSize = objFileSystemHandler.appendFile(strTestPathName + strTestFileName, strTestPathName + strTestFileName);
        assertEquals("FileSize of appened File", 88, lngFileSize);
    }

    @Test
    public void testExecuteCommand() throws Exception {
        objVFS.connect();
        String strR = objFileSystemHandler.getReplyString();
        assertEquals("Connect message", "ok", strR);
        objVFS.executeCommand("dir");
    }

    @Test
    public void testReplaceCommand4Windows() throws Exception {
        SOSVfsLocal objSOSVfsLocal = new SOSVfsLocal();
        String strR = objSOSVfsLocal.getCmdShell().replaceCommand4Windows("echo /Y //host/c/nobackup/text.txt c:/nobackup/text.txt /nobackup/text.txt");
        assertEquals("CommandStringReplace message", "echo /Y \\\\host\\c\\nobackup\\text.txt c:\\nobackup\\text.txt \\nobackup\\text.txt", strR);
    }

    @Test
    public void testAuthenticate() throws Exception {
        objVFS.connect();
        String strR = objFileSystemHandler.getReplyString();
        assertEquals("Connect message", "ok", strR);
        ISOSAuthenticationOptions objOptions = null;
        objVFS.authenticate(objOptions);
        strR = objFileSystemHandler.getReplyString();
        assertEquals("Login message", "230 Login successful.", strR);
        objVFS.closeSession();
        strR = objFileSystemHandler.getReplyString();
        assertEquals("Login message", "221 Goodbye.", strR);
        objVFS.closeConnection();
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testDeleteFile() {
        createTestFile();
        try {
            ISOSVirtualFile objF = objFileSystemHandler.getFileHandle(strTestFilePath);
            assertTrue("File created", objF.fileExists());
            objFileSystemHandler.delete(strTestFilePath);
            boolean flgResult = objF.fileExists();
            assertFalse("File deleted", flgResult);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void createTestFile() {
        strTestFilePath = strTestPathName + strTestFileName;
        JSFile objFile = new JSFile(strTestFilePath);
        objFile.deleteOnExit();
        try {
            objFile.writeLine("This is a line in a testfile. Nothing else");
            objFile.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void testPutFileString() throws Exception {
        createTestFile();
        objVFS.connect();
        String strR = objFileSystemHandler.getReplyString();
        assertEquals("Connect message", "ok", strR);
        ISOSAuthenticationOptions objOptions = null;
        objVFS.authenticate(objOptions);
        strR = objFileSystemHandler.getReplyString();
        assertEquals("Login message", "230 Login successful.", strR);
        objFileSystemHandler.putFile(strTestPathName + strTestFileName, strTestFileName);
        objVFS.closeSession();
        strR = objFileSystemHandler.getReplyString();
        assertEquals("Login message", "221 Goodbye.", strR);
        objVFS.closeConnection();
    }

}