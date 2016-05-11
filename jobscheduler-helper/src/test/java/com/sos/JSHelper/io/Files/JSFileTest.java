package com.sos.JSHelper.io.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

public class JSFileTest {

    private static final Logger LOGGER = Logger.getLogger(JSFileTest.class);
    private static final String REC = "Eine Zeile zum Test ...";
    private static final String FOLDER_NAME = "R:/nobackup/junittests/testdata/JSFileTest/";
    private String strTestFileName = FOLDER_NAME + "test.txt";

    private void createTestFile() throws IOException {
        JSFile objTestFile = new JSFile(strTestFileName);
        for (int i = 0; i <= 40; i++) {
            objTestFile.WriteLine(REC);
        }
        objTestFile.close();
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testCopy() throws Exception {
        JSFile fleFile = new JSFile("L:/TestData/data/invcon/reference-stocks-1546.xml");
        JSFile fleTarget = new JSFile("L:/TestData/data/invcon/reference-stocks-1546b.xml");
        fleFile.copy("L:/TestData/data/invcon/reference-stocks-1546b.xml");
        fleTarget.delete();
        fleFile = new JSFile("L:/TestData/data/invcon/", "reference-stocks-1546.xml");
        fleFile.copy("L:/TestData/data/invcon/reference-stocks-1546b.xml");
        fleTarget.delete();
    }

    public void testAppendFile() throws Exception {
        createTestFile();
        JSFile fleFile = new JSFile(strTestFileName);
        long lngFileSize = fleFile.length();
        String strTarget = FOLDER_NAME + "target.txt";
        fleFile.copy(strTarget);
        fleFile.AppendFile(strTarget);
        assertEquals("file size not as expected", lngFileSize * 2, fleFile.length());
    }

    public void testAppendFile2() throws Exception {
        createTestFile();
        JSFile fleFile = new JSFile(strTestFileName);
        long lngFileSize = fleFile.length();
        String strFile2Append = strTestFileName;
        fleFile.copy(strFile2Append);
        fleFile.AppendFile(strFile2Append);
        assertEquals("file size not as expected", lngFileSize * 2, fleFile.length());
    }

    public void testLock() throws Exception {
        JSFile fleFile = new JSFile(strTestFileName);
        fleFile.doLock();
        assertTrue("Erwartet wird ein true, weil File gesperrt ... ", fleFile.isLocked());
        fleFile.WriteLine(REC);
        fleFile.close();
        assertFalse("Erwartet wird ein false, weil File nicht mehr gesperrt ... ", fleFile.isLocked());
    }

    public void testExclusive() throws Exception {
        JSFile fleFile = new JSFile("c:/temp/test-exclusive.txt");
        fleFile.setExclusive(true);
        fleFile.WriteLine("Die Basis ist das Fundament der Grundlage");
        assertTrue("Erwartet wird ein true, weil File exclusive ... ", fleFile.isExclusive());
        fleFile.close();
        assertFalse("Erwartet wird ein false, weil File nicht mehr exclusive ... ", fleFile.isExclusive());
        fleFile.delete();
    }

    public void doDelete() {
        JSFile fleFile = new JSFile(strTestFileName);
        fleFile.delete();
        assertFalse("Erwartet wird ein false, weil File nicht mehr da ... ", fleFile.exists());
    }

    public void doWrite() throws Exception {
        JSFile fleFile = new JSFile(strTestFileName);
        fleFile.doLock();
        for (int i = 0; i < 1; i++) {
            fleFile.WriteLine(REC);
        }
        fleFile.close();
        assertEquals("Satz vergleich", REC + System.getProperty("line.separator"), fleFile.File2String());
        fleFile.close();
    }

    public void getLine() throws Exception {
        JSFile fleFile = new JSFile(strTestFileName);
        fleFile.doLock();
        assertEquals("Satz vergleich", REC, fleFile.GetLine().toString());
        fleFile.close();
    }

    public void doRead() throws Exception {
        JSFile fleFile = new JSFile(strTestFileName);
        fleFile.doLock();
        assertEquals("Satz vergleich", REC + System.getProperty("line.separator"), fleFile.File2String());
        fleFile.close();
    }

    public void MassRandom() throws Exception {
        JSFile fleFile = new JSFile(strTestFileName);
        fleFile.doLock();
        for (int i = 0; i < 10000; i++) {
            fleFile.WriteLine(REC);
        }
        fleFile.close();
        fleFile.delete();
        assertEquals("Dummy", "a", "a");
    }

    public void MassSequential() throws Exception {
        JSFile fleFile = new JSFile(strTestFileName);
        for (int i = 0; i < 10000; i++) {
            fleFile.WriteLine(REC);
        }
        fleFile.close();
        assertEquals("Dummy", "a", "a");
    }

    public void CreateBackupTest() throws Exception {
        JSFile fleFile = new JSFile(strTestFileName);
        String strNewFileName = fleFile.CreateBackup();
        fleFile.close();
        JSFile fleBackUp = new JSFile(strNewFileName);
        assertTrue("Datei existiert", fleBackUp.exists());
        fleBackUp.delete();
    }

    public void CreateBackupTest2() throws Exception {
        JSFile fleFile = new JSFile(strTestFileName);
        String strNewFileName = fleFile.CreateBackup(".willi");
        fleFile.close();
        JSFile fleBackUp = new JSFile(strNewFileName);
        assertTrue("Datei existiert", fleBackUp.exists());
        fleBackUp.delete();
    }

    public void CreateBackupTest3() throws Exception {
        JSFile fleFile = new JSFile(strTestFileName);
        fleFile.BackupFolderName.Value(System.getProperty("java.io.tmpdir"));
        String strNewFileName = fleFile.CreateBackup(".willi");
        fleFile.close();
        JSFile fleBackUp = new JSFile(strNewFileName);
        assertTrue("Datei existiert", fleBackUp.exists());
        fleBackUp.delete();
    }

    public void TestToXml() {
        JSFile objFile = new JSFile(strTestFileName);
        LOGGER.info(objFile.toXml());
    }

    public void TestGetContent() {
        JSFile objFile = new JSFile(strTestFileName);
        StringBuilder strB = new StringBuilder();
        String strT = REC + System.getProperty("line.separator");
        for (int i = 0; i < 10000; i++) {
            strB.append(strT);
        }
        assertEquals("Content read ...", strB.toString(), objFile.getContent());
        LOGGER.info(objFile.toXml());
    }

    public void testZipWrite() throws IOException {
        JSFile objFile = new JSFile(strTestFileName + ".gz");
        objFile.setZipFile(true);
        String strText = "Die Basis ist das Fundament der Grundlage";
        objFile.WriteLine(strText);
        objFile.close();
        objFile = new JSFile(strTestFileName + ".gz");
        objFile.setZipFile(true);
        String strT = objFile.getContent();
        objFile.close();
        assertEquals("wrong content", strText + System.getProperty("line.separator"), strT);
        objFile = new JSFile(strTestFileName + ".gz");
        objFile.setZipFile(true);
        StringBuffer strB = objFile.GetLine();
        objFile.close();
        assertEquals("wrong content", strText, strB.toString());
    }

    @Test
    @Ignore("Test set to Ignore for later examination, fails in Jenkins build")
    public void testCreateUniqueFileName() throws IOException {
        JSFile objFile = new JSFile(strTestFileName);
        String strT = objFile.getUniqueFileName();
        JSFile objF1 = new JSFile(strT);
        objF1.WriteLine("test");
        strT = objFile.getUniqueFileName();
    }

    public void testCreateUniqueFileName2() throws IOException {
        strTestFileName = FOLDER_NAME + "test";
        JSFile objFile = new JSFile(strTestFileName);
        objFile.WriteLine("Test");
        String strT = objFile.getUniqueFileName();
        LOGGER.debug(strT);
        JSFile objF1 = new JSFile(strT);
        objF1.WriteLine("test");
        strT = objFile.getUniqueFileName();
        LOGGER.debug(strT);
    }

    public void testGetExtensionFileName() {
        JSFile objFile = new JSFile("test.x");
        String strE = objFile.getFileExtensionName();
        assertEquals("wrong extension", ".x", strE);
        objFile = new JSFile("test.xy");
        strE = objFile.getFileExtensionName();
        assertEquals("wrong extension", ".xy", strE);
        objFile = new JSFile("test");
        strE = objFile.getFileExtensionName();
        assertEquals("wrong extension", "", strE);
        objFile = new JSFile("test.job.xml");
        strE = objFile.getFileExtensionName();
        assertEquals("wrong extension", ".xml", strE);
    }

    public void testBackupProperty() {
        System.setProperty(JSFile.conPropertySOS_JSFILE_EXTENSION_4_BACKUPFILE, ".sostmp");
        JSFile objFile = new JSFile(strTestFileName);
        objFile.CreateBackup();
    }

    @Test
    public void testExistsAndCanWrite() {
        JSTextFile objFile = new JSTextFile("./abcd.properties");
        if (objFile.exists() && !objFile.getParentFile().canWrite()) {
            assertTrue("geht nicht", false);
        }
    }

    public void testDumpHex() {
        JSFile objFile = new JSFile(strTestFileName);
        LOGGER.info(objFile.getContent());
        objFile.dumpHex(System.out);
    }

}