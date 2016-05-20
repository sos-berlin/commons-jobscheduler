package com.sos.VirtualFileSystem.DataElements;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.management.ManagementFactory;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.sos.JSHelper.Listener.JSListenerClass;
import com.sos.JSHelper.io.Files.JSFile;
import com.sos.VirtualFileSystem.DataElements.SOSFileListEntry.enuTransferStatus;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Options.SOSFTPOptions;

/** @author KB */
public class SOSFileListEntryTest extends JSListenerClass {

    private static final Logger LOGGER = Logger.getLogger(SOSFileListEntryTest.class);
    private static final String FILENAME = "text.txt";
    private static final String PATHNAME = "c:\\temp\\";
    private static final String APREFIX = "~~";
    private SOSFTPOptions objOptions = null;
    private SOSFileListEntry objE = null;
    private ISOSVFSHandler objVFS = null;
    private ISOSVfsFileTransfer objFileSystemHandler = null;

    @Before
    public void setUp() throws Exception {
        objOptions = new SOSFTPOptions();
        objVFS = VFSFactory.getHandler("local");
        objFileSystemHandler = (ISOSVfsFileTransfer) objVFS;
    }

    @Test
    public void testGetTargetFile() throws IOException {
        JSFile objFile = new JSFile(PATHNAME + FILENAME);
        objFile.deleteOnExit();
        objFile.writeLine("Das ist eine Testdatei. Weiter nichts");
        objFile.close();
        objOptions.atomicPrefix.setValue(APREFIX);
        objOptions.compressFiles.value(true);
        objOptions.compressedFileExtension.setValue(".zip");
        SOSFileListEntry objE = new SOSFileListEntry(PATHNAME + FILENAME);
        objE.setDataSourceClient(objFileSystemHandler);
        objE.setOptions(objOptions);
        objE.getTargetFile();
        LOGGER.info("SourceFileName         = " + objE.getSourceFileName());
        LOGGER.info("SourceTransferFileName = " + objE.getSourceTransferName());
        LOGGER.info("TargetTransferFileName = " + objE.getTargetTransferName());
        LOGGER.info("TargetFileName         = " + objE.getTargetFileName());
        assertEquals("Source-File Name", PATHNAME + FILENAME, objE.getSourceFileName());
        assertEquals("final TargetFileName", FILENAME + objOptions.compressedFileExtension.getValue(), objE.getTargetFileName());
    }

    private void createTestFile() {
        JSFile objFile = new JSFile(PATHNAME + FILENAME);
        objFile.deleteOnExit();
        try {
            objFile.writeLine("Das ist eine Testdatei. Weiter nichts");
            objFile.close();
            objE = new SOSFileListEntry(PATHNAME + FILENAME);
            objE.setDataSourceClient(objFileSystemHandler);
            objE.setOptions(objOptions);
            objE.getTargetFile();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void testGetTargetFile2() throws IOException {
        objOptions.atomicPrefix.setValue(APREFIX);
        objOptions.compressFiles.value(true);
        objOptions.compressedFileExtension.setValue(".zip");
        objOptions.ReplaceWhat.setValue("(t)ext\\.t(x)t");
        objOptions.ReplaceWith.setValue("u;u");
        createTestFile();
        objE.log4Debug();
        assertEquals("Source-File Name", PATHNAME + FILENAME, objE.getSourceFileName());
        assertEquals("final TargetFileName", "uext.tut" + objOptions.compressedFileExtension.getValue(), objE.getTargetFileName());
    }

    @Test
    public void testNormalized() {
        createTestFile();
        String strN = objE.normalized(PATHNAME);
        LOGGER.debug(strN);
        assertEquals("normalized path name", "c:/temp/", strN);
    }

    @Test
    public void testMakeAtomicFileName() {
        objOptions.atomicPrefix.setValue(APREFIX);
        SOSFileListEntry objE = new SOSFileListEntry(PATHNAME + FILENAME);
        objE.setDataSourceClient(objFileSystemHandler);
        objE.setOptions(objOptions);
        objE.getTargetFile();
        objE.log4Debug();
        assertEquals("Source-File Name", PATHNAME + FILENAME, objE.getSourceFileName());
        assertEquals("Source-Transfer-File Name", PATHNAME + FILENAME, objE.getSourceTransferName());
        assertEquals("intermediate Atomic-File TargetTransferName", objOptions.atomicPrefix.getValue() + FILENAME, objE.getTargetTransferName());
        assertEquals("final TargetFileName", FILENAME, objE.getTargetFileName());
    }

    @Test
    public void testMakeAtomicFileName2() {
        objOptions.atomicPrefix.setValue(APREFIX);
        objOptions.atomicSuffix.setValue(APREFIX + APREFIX);
        createTestFile();
        assertEquals("Source-File Name", PATHNAME + FILENAME, objE.getSourceFileName());
        assertEquals("Source-Transfer-File Name", PATHNAME + FILENAME, objE.getSourceTransferName());
        assertEquals("intermediate Atomic-File TargetTransferName", objOptions.atomicPrefix.getValue() + FILENAME + objOptions.atomicSuffix.getValue(),
                objE.getTargetTransferName());
        assertEquals("final TargetFileName", FILENAME, objE.getTargetFileName());
    }

    @Test
    public void testMakeAtomicFileName3() {
        SOSFileListEntry objE = new SOSFileListEntry(PATHNAME + FILENAME);
        objE.setDataSourceClient(objFileSystemHandler);
        objE.setOptions(objOptions);
        objE.getTargetFile();
        assertEquals("Source-File Name", PATHNAME + FILENAME, objE.getSourceFileName());
        assertEquals("Source-Transfer-File Name", PATHNAME + FILENAME, objE.getSourceTransferName());
        assertEquals("intermediate Atomic-File TargetTransferName", FILENAME, objE.getTargetTransferName());
        assertEquals("final TargetFileName", FILENAME, objE.getTargetFileName());
    }

    @Test
    public void testSetStatus() {
        createTestFile();
        objE.setStatus(enuTransferStatus.waiting4transfer);
        objE.setStatus(enuTransferStatus.transferring);
        objE.setDataTargetClient(objFileSystemHandler);
        for (int i = 1; i < 10; i++) {
            objE.setTransferProgress(i * 100);
        }
        objE.setStatus(enuTransferStatus.transferred);
    }

    @Test(expected = com.sos.JSHelper.Exceptions.JobSchedulerException.class)
    public void testSetNoOfBytesTransferred() {
        createTestFile();
        objE.setNoOfBytesTransferred(1234);
    }

    @Test
    public void testFileExists() {
        createTestFile();
    }

    @Test
    public void testPid() {
        /** this hack is tested for SUN-JVM only. No guarantee is made for other
         * JVMs */
        String pid = ManagementFactory.getRuntimeMXBean().getName();
        String strA[] = pid.split("@");
        LOGGER.info("name = " + pid + ", pid = " + strA[0]);
    }

}