package com.sos.VirtualFileSystem.zip;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.OutputStream;
import java.util.Vector;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Options.SOSOptionTransferType.enuTransferTypes;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;

/** @author KB */
public class SOSVfsZipTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVfsZipTest.class);
    private static final String TEST_PATH_NAME = "R:\\nobackup\\junittests\\testdata\\ZIP\\";
    private static final String TEST_ZIP = TEST_PATH_NAME + "test.zip";
    private static final String ZIP_FILE1 = TEST_PATH_NAME + "sos-net-src.zip";
    private ISOSVFSHandler objVFS = null;
    private ISOSVfsFileTransfer objFileSystemHandler = null;
    @Before
    public void setUp() throws Exception {
        objVFS = VFSFactory.getHandler("zip");
        objFileSystemHandler = (ISOSVfsFileTransfer) objVFS;
        objVFS.setSource();
    }

    @Test
    public final void testChangeWorkingDirectory() throws Exception {
        objFileSystemHandler.changeWorkingDirectory(ZIP_FILE1);
        objFileSystemHandler.close();
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public final void testListNames() throws Exception {
        objVFS.setSource();
        String[] strFileNames = objFileSystemHandler.listNames(ZIP_FILE1);
        for (String strFileName : strFileNames) {
            LOGGER.debug(String.format("File: %1$s", strFileName));
        }
        objFileSystemHandler.close();
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public final void testGetFiles() throws Exception {
        objVFS.setSource();
        Vector<ISOSVirtualFile> objFileList = objFileSystemHandler.getFiles(ZIP_FILE1);
        for (ISOSVirtualFile objVF : objFileList) {
            if (objVF.isDirectory() == true) {
                LOGGER.debug(String.format("%1$s is a directory", objVF.getName()));
            } else {
                LOGGER.debug(String.format("File %1$s, Size %2$d", objVF.getName(), objVF.getFileSize()));
            }
        }
        objFileSystemHandler.close();
    }

    @Test
    public final void testLogin() {
        objFileSystemHandler.login("test", "Test");
    }

    @Test
    public final void testLogout() throws Exception {
        objFileSystemHandler.logout();
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public final void testWriteByteArray() throws Exception {
        boolean objF = new File(TEST_ZIP).delete();
        objVFS.setTarget();
        objFileSystemHandler.changeWorkingDirectory(TEST_ZIP);
        SOSVfsZipFileEntry objVF = (SOSVfsZipFileEntry) objFileSystemHandler.getFileHandle("test.txt");
        OutputStream objOS = objVF.getFileOutputStream();
        objOS.write("abcdefghijkl".getBytes());
        objOS.close();
        objFileSystemHandler.close();
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public final void createZipWithMultipleFiles() throws Exception {
        boolean flgF = new File(TEST_ZIP).delete();
        objVFS.setTarget();
        objFileSystemHandler.changeWorkingDirectory(TEST_ZIP);
        ISOSVFSHandler objLocalVFS = VFSFactory.getHandler("local");
        ISOSVfsFileTransfer objLocalFiles = (ISOSVfsFileTransfer) objLocalVFS;
        String[] strA = objLocalFiles.getFilelist(TEST_PATH_NAME, ".*\\.txt", 1, false, null);
        for (String string : strA) {
            ISOSVirtualFile objVF = objLocalFiles.getFileHandle(string);
            objFileSystemHandler.putFile(objVF);
        }
        objFileSystemHandler.close();
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public final void directory4ZipWithMultipleFiles() throws Exception {
        objFileSystemHandler.changeWorkingDirectory(TEST_ZIP);
        String[] strA = objFileSystemHandler.getFilelist(TEST_PATH_NAME, "^.*\\.txt$", 1, false, null);
        for (String strFileName : strA) {
            LOGGER.info(strFileName);
        }
        objFileSystemHandler.close();
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public final void extractWithMultipleFiles() throws Exception {
        objFileSystemHandler.changeWorkingDirectory(TEST_ZIP);
        String[] strA = objFileSystemHandler.getFilelist("c:\\temp\\", ".*\\.txt$", 1, false, null);
        ISOSVFSHandler objLocalVFS = VFSFactory.getHandler(enuTransferTypes.local.getText());
        ISOSVfsFileTransfer objLocalFiles = (ISOSVfsFileTransfer) objLocalVFS;
        for (String strFileName : strA) {
            LOGGER.info(strFileName);
            ISOSVirtualFile objVF = objFileSystemHandler.getFileHandle(strFileName);
            ISOSVirtualFile objLocalFile = objLocalFiles.getFileHandle(strFileName);
            objLocalFile.putFile(objVF);
        }
        objFileSystemHandler.close();
        objLocalFiles.close();
    }

    @Test
    public final void testIsNegativeCommandCompletion() throws Exception {
        objFileSystemHandler.changeWorkingDirectory(ZIP_FILE1);
        assertFalse("Must be false", objFileSystemHandler.isNegativeCommandCompletion());
    }

    @Test
    public final void testClose() {
        objFileSystemHandler.close();
    }

}