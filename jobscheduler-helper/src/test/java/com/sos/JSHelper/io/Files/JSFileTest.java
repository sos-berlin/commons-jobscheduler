package com.sos.JSHelper.io.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSFileTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSFileTest.class);
    private static final String CLASSNAME = "JSFileTest";
    private static final String REC = "Eine Zeile zum Test ...";
    private static final String FOLDERNAME = "R:/nobackup/junittests/testdata/JSFileTest/";
    private String strTestFileName = FOLDERNAME + "test.txt";

    private void createTestFile() throws IOException {
        JSFile objTestFile = new JSFile(strTestFileName);
        for (int i = 0; i <= 40; i++) {
            objTestFile.writeLine(REC);
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

    @Test
    @Ignore
    public void testAppendFile() throws Exception {
        createTestFile();
        JSFile fleFile = new JSFile(strTestFileName);
        long lngFileSize = fleFile.length();
        String strTarget = FOLDERNAME + "target.txt";
        fleFile.copy(strTarget);
        fleFile.appendFile(strTarget);
        assertEquals("file size not as expected", lngFileSize * 2, fleFile.length());
    }

    @Test(expected = com.sos.JSHelper.Exceptions.JobSchedulerException.class)
    @Ignore
    public void testAppendFile2() throws Exception {
        createTestFile();
        JSFile fleFile = new JSFile(strTestFileName);
        long lngFileSize = fleFile.length();
        String strFile2Append = strTestFileName;
        fleFile.copy(strFile2Append);
        fleFile.appendFile(strFile2Append);
        assertEquals("file size not as expected", lngFileSize * 2, fleFile.length());
    }

    @Test
    @Ignore
    public void testLock() throws Exception {
        JSFile fleFile = new JSFile(strTestFileName);
        fleFile.doLock();
        assertTrue("Erwartet wird ein true, weil File gesperrt ... ", fleFile.isLocked());
        fleFile.writeLine(REC);
        fleFile.close();
        assertFalse("Erwartet wird ein false, weil File nicht mehr gesperrt ... ", fleFile.isLocked());
    }

    @Test
    @Ignore
    public void testExclusive() throws Exception {
        JSFile fleFile = new JSFile("c:/temp/test-exclusive.txt");
        fleFile.setExclusive(true);
        fleFile.writeLine("Die Basis ist das Fundament der Grundlage");
        assertTrue("Erwartet wird ein true, weil File exclusive ... ", fleFile.isExclusive());
        fleFile.close();
        assertFalse("Erwartet wird ein false, weil File nicht mehr exclusive ... ", fleFile.isExclusive());
        fleFile.delete();
    }

    @Test
    @Ignore
    public void doDelete() {
        JSFile fleFile = new JSFile(strTestFileName);
        fleFile.delete();
        assertFalse("Erwartet wird ein false, weil File nicht mehr da ... ", fleFile.exists());
    }

    @Test
    @Ignore
    public void doWrite() throws Exception {
        JSFile fleFile = new JSFile(strTestFileName);
        fleFile.doLock();
        for (int i = 0; i < 1; i++) {
            fleFile.writeLine(REC);
        }
        fleFile.close();
        assertEquals("Satz vergleich", REC + System.getProperty("line.separator"), fleFile.file2String());
        fleFile.close();
    }

    @Test
    @Ignore
    public void getLine() throws Exception {
        JSFile fleFile = new JSFile(strTestFileName);
        fleFile.doLock();
        assertEquals("Satz vergleich", REC, fleFile.getLine().toString());
        fleFile.close();
    }

    @Test
    @Ignore
    public void doRead() throws Exception {
        JSFile fleFile = new JSFile(strTestFileName);
        fleFile.doLock();
        assertEquals("Satz vergleich", REC + System.getProperty("line.separator"), fleFile.file2String());
        fleFile.close();
    }

    @Test
    @Ignore
    public void massRandom() throws Exception {
        JSFile fleFile = new JSFile(strTestFileName);
        fleFile.doLock();
        for (int i = 0; i < 10000; i++) {
            fleFile.writeLine(REC);
        }
        fleFile.close();
        fleFile.delete();
        assertEquals("Dummy", "a", "a");
    }

    @Test
    @Ignore
    public void massSequential() throws Exception {
        JSFile fleFile = new JSFile(strTestFileName);
        for (int i = 0; i < 10000; i++) {
            fleFile.writeLine(REC);
        }
        fleFile.close();
        assertEquals("Dummy", "a", "a");
    }

    @Test
    @Ignore
    public void createBackupTest() throws Exception {
        JSFile fleFile = new JSFile(strTestFileName);
        String strNewFileName = fleFile.createBackup();
        fleFile.close();
        JSFile fleBackUp = new JSFile(strNewFileName);
        assertTrue("Datei existiert", fleBackUp.exists());
        fleBackUp.delete();
    }

    @Test
    @Ignore
    public void createBackupTest2() throws Exception {
        JSFile fleFile = new JSFile(strTestFileName);
        String strNewFileName = fleFile.createBackup(".willi");
        fleFile.close();
        JSFile fleBackUp = new JSFile(strNewFileName);
        assertTrue("Datei existiert", fleBackUp.exists());
        fleBackUp.delete();
    }

    @Test
    @Ignore
    public void createBackupTest3() throws Exception {
        JSFile fleFile = new JSFile(strTestFileName);
        fleFile.BackupFolderName.setValue(System.getProperty("java.io.tmpdir"));
        String strNewFileName = fleFile.createBackup(".willi");
        fleFile.close();
        JSFile fleBackUp = new JSFile(strNewFileName);
        assertTrue("Datei existiert", fleBackUp.exists());
        fleBackUp.delete();
    }

    @Test
    @Ignore
    public void testToXml() {
        JSFile objFile = new JSFile(strTestFileName);
        LOGGER.info(objFile.toXml());
    }
    @Test
    @Ignore
    public void testGetContent() {
        JSFile objFile = new JSFile(strTestFileName);
        StringBuilder strB = new StringBuilder();
        String strT = REC + System.getProperty("line.separator");
        for (int i = 0; i < 10000; i++) {
            strB.append(strT);
        }
        assertEquals("Content read ...", strB.toString(), objFile.getContent());
        LOGGER.info(objFile.toXml());
    }

    @Test
    @Ignore
    public void testZipWrite() throws IOException {
        JSFile objFile = new JSFile(strTestFileName + ".gz");
        objFile.setZipFile(true);
        String strText = "Die Basis ist das Fundament der Grundlage";
        objFile.writeLine(strText);
        objFile.close();
        objFile = new JSFile(strTestFileName + ".gz");
        objFile.setZipFile(true);
        String strT = objFile.getContent();
        objFile.close();
        assertEquals("wrong content", strText + System.getProperty("line.separator"), strT);
        objFile = new JSFile(strTestFileName + ".gz");
        objFile.setZipFile(true);
        StringBuffer strB = objFile.getLine();
        objFile.close();
        assertEquals("wrong content", strText, strB.toString());
    }

    @Test
    @Ignore("Test set to Ignore for later examination, fails in Jenkins build")
    public void testCreateUniqueFileName() throws IOException {
        JSFile objFile = new JSFile(strTestFileName);
        String strT = objFile.getUniqueFileName();
        JSFile objF1 = new JSFile(strT);
        objF1.writeLine("test");
        strT = objFile.getUniqueFileName();
    }

    @Test
    @Ignore
    public void testCreateUniqueFileName2() throws IOException {
        strTestFileName = FOLDERNAME + "test";
        JSFile objFile = new JSFile(strTestFileName);
        objFile.writeLine("Test");
        String strT = objFile.getUniqueFileName();
        LOGGER.debug(strT);
        JSFile objF1 = new JSFile(strT);
        objF1.writeLine("test");
        strT = objFile.getUniqueFileName();
        LOGGER.debug(strT);
    }

    @Test
    @Ignore
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

    @Test
    @Ignore
    public void testBackupProperty() {
        System.setProperty(JSFile.conPropertySOS_JSFILE_EXTENSION_4_BACKUPFILE, ".sostmp");
        JSFile objFile = new JSFile(strTestFileName);
        objFile.createBackup();
    }

    @Test
    public void testExistsAndCanWrite() {
        JSTextFile objFile = new JSTextFile("src/test/resources/log4j.properties");
        if (!objFile.exists() && objFile.getParentFile().canWrite()) {
            assertTrue("geht nicht", false);
        }
    }

    @Test
    @Ignore
    public void testDumpHex() {
        JSFile objFile = new JSFile(strTestFileName);
        LOGGER.info(objFile.getContent());
        objFile.dumpHex(System.out);
    }

}