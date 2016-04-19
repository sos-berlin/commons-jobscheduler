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

    public SOSFileListEntryTest() {
        //
    }

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
        objFile.WriteLine("Das ist eine Testdatei. Weiter nichts");
        objFile.close();
        objOptions.atomic_prefix.Value(APREFIX);
        objOptions.compress_files.value(true);
        objOptions.compressed_file_extension.Value(".zip");
        SOSFileListEntry objE = new SOSFileListEntry(PATHNAME + FILENAME);
        objE.setDataSourceClient(objFileSystemHandler);
        objE.Options(objOptions);
        objE.getTargetFile();
        LOGGER.info("SourceFileName         = " + objE.SourceFileName());
        LOGGER.info("SourceTransferFileName = " + objE.SourceTransferName());
        LOGGER.info("TargetTransferFileName = " + objE.TargetTransferName());
        LOGGER.info("TargetFileName         = " + objE.TargetFileName());
        assertEquals("Source-File Name", PATHNAME + FILENAME, objE.SourceFileName());
        assertEquals("final TargetFileName", FILENAME + objOptions.compressed_file_extension.Value(), objE.TargetFileName());
    }

    private void CreateTestFile() {
        JSFile objFile = new JSFile(PATHNAME + FILENAME);
        objFile.deleteOnExit();
        try {
            objFile.WriteLine("Das ist eine Testdatei. Weiter nichts");
            objFile.close();
            objE = new SOSFileListEntry(PATHNAME + FILENAME);
            objE.setDataSourceClient(objFileSystemHandler);
            objE.Options(objOptions);
            objE.getTargetFile();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void testGetTargetFile2() throws IOException {
        objOptions.atomic_prefix.Value(APREFIX);
        objOptions.compress_files.value(true);
        objOptions.compressed_file_extension.Value(".zip");
        objOptions.ReplaceWhat.Value("(t)ext\\.t(x)t");
        objOptions.ReplaceWith.Value("u;u");
        CreateTestFile();
        objE.Log4Debug();
        assertEquals("Source-File Name", PATHNAME + FILENAME, objE.SourceFileName());
        assertEquals("final TargetFileName", "uext.tut" + objOptions.compressed_file_extension.Value(), objE.TargetFileName());
    }

    @Test
    public void testNormalized() {
        CreateTestFile();
        String strN = objE.normalized(PATHNAME);
        LOGGER.debug(strN);
        assertEquals("normalized path name", "c:/temp/", strN);
    }

    @Test
    public void testMakeAtomicFileName() {
        objOptions.atomic_prefix.Value(APREFIX);
        SOSFileListEntry objE = new SOSFileListEntry(PATHNAME + FILENAME);
        objE.setDataSourceClient(objFileSystemHandler);
        objE.Options(objOptions);
        objE.getTargetFile();
        objE.Log4Debug();
        assertEquals("Source-File Name", PATHNAME + FILENAME, objE.SourceFileName());
        assertEquals("Source-Transfer-File Name", PATHNAME + FILENAME, objE.SourceTransferName());
        assertEquals("intermediate Atomic-File TargetTransferName", objOptions.atomic_prefix.Value() + FILENAME, objE.TargetTransferName());
        assertEquals("final TargetFileName", FILENAME, objE.TargetFileName());
    }

    @Test
    public void testMakeAtomicFileName2() {
        objOptions.atomic_prefix.Value(APREFIX);
        objOptions.atomic_suffix.Value(APREFIX + APREFIX);
        CreateTestFile();
        assertEquals("Source-File Name", PATHNAME + FILENAME, objE.SourceFileName());
        assertEquals("Source-Transfer-File Name", PATHNAME + FILENAME, objE.SourceTransferName());
        assertEquals("intermediate Atomic-File TargetTransferName", objOptions.atomic_prefix.Value() + FILENAME + objOptions.atomic_suffix.Value(),
                objE.TargetTransferName());
        assertEquals("final TargetFileName", FILENAME, objE.TargetFileName());
    }

    @Test
    public void testMakeAtomicFileName3() {
        SOSFileListEntry objE = new SOSFileListEntry(PATHNAME + FILENAME);
        objE.setDataSourceClient(objFileSystemHandler);
        objE.Options(objOptions);
        objE.getTargetFile();
        assertEquals("Source-File Name", PATHNAME + FILENAME, objE.SourceFileName());
        assertEquals("Source-Transfer-File Name", PATHNAME + FILENAME, objE.SourceTransferName());
        assertEquals("intermediate Atomic-File TargetTransferName", FILENAME, objE.TargetTransferName());
        assertEquals("final TargetFileName", FILENAME, objE.TargetFileName());
    }

    @Test
    public void testSetStatus() {
        CreateTestFile();
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
        CreateTestFile();
        objE.setNoOfBytesTransferred(1234);
    }

    @Test
    public void testFileExists() {
        CreateTestFile();
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
