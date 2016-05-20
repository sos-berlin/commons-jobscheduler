package com.sos.VirtualFileSystem.Filter;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.VirtualFileSystem.Filter.Options.SOSFilterOptions;

public class SOSSearchAndReplaceFilterTest {

    @SuppressWarnings("unused")
    private final String conClassName = this.getClass().getSimpleName();
    @SuppressWarnings("unused")
    private static final String conSVNVersion = "$Id$";
    private final Logger logger = Logger.getLogger(this.getClass());

    private SOSSearchAndReplaceFilter objF = null;
    private SOSFilterOptions objFO = null;

    byte[] bteBuffer = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        objF = new SOSSearchAndReplaceFilter();
        objFO = objF.getOptions();
        BasicConfigurator.configure();
        logger.setLevel(Level.DEBUG);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testOptions() {
    }

    @Test
    public void testOptionsSOSFTPOptions() {
    }

    @Test
    public void testWriteByteArrayIntInt() {
    }

    @Test
    public void testWriteByteArray() {
        String strT = "Hallo, Welt!";
        objFO.replaceWhat.setValue("(Hallo)");
        objFO.replaceWith.setValue("<b>$1</b>");
        objF.write(strT.getBytes());

        bteBuffer = objF.read();
        String strX = new String(bteBuffer);
        logger.debug(strX);
        Assert.assertEquals("<b>Hallo</b>, Welt!", strX);
    }

    @Test
    public void testWriteByteArray2() {
        String strT = "Hallo, Welt!";
        objFO.replaceWhat.setValue("(Welt)");
        objFO.replaceWith.setValue("<b>$1</b>");
        objF.write(strT);

        String strX = objF.readString();
        logger.debug(strX);
        Assert.assertEquals("Hallo, <b>Welt</b>!", strX);
    }

    @Test
    public void testReadByteArray() {
    }

    @Test
    public void testReadByteArrayIntInt() {
    }

    @Test
    public void testReadBufferByteArray() {
    }

    @Test
    public void testReadBufferByteArrayIntInt() {
    }

    @Test
    public void testClose() {
    }

}
